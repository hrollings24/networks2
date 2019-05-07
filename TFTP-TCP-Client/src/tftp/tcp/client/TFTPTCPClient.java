/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tftp.tcp.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author harryrollings
 */
public class TFTPTCPClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        
        // Check that both required input arguments are passed.
        if (args.length != 4) {
            System.err.println("<mode> <fileName> <address> <port>");
            System.exit(1);
        }
        
        TFTPTCPClient client = new TFTPTCPClient();
        String command = args[0];
        switch (command){
            case "write":
                client.write(args[1], args[2], Integer.parseInt(args[3]));
                break;
            case "read":
                client.read(args[1], args[2], Integer.parseInt(args[3]));
                break;
        }
        
    }
    
    public TFTPTCPClient(){
        
    }
    
    
    public void write(String fileName, String address, int port) throws IOException{
        byte[] buf = new byte[100];
        buf[0] = (byte) 0;
        buf[1] = (byte) 2;
        for (int i = 2; i < fileName.length()+2; i++) {
	    buf[i] = (byte) fileName.charAt(i-2);
	}
        buf[fileName.length()+2] = (byte) 0;
        String mode = "octet";
        for (int x=fileName.length()+3; x<fileName.length()+7; x++){
            buf[x] = (byte) mode.charAt(x-(fileName.length()+3));
        }
        buf[fileName.length()+7] = (byte) 0;
        
        Socket echoSocket = new Socket(address, port);
        
        OutputStream out = echoSocket.getOutputStream(); 
        DataOutputStream dos = new DataOutputStream(out);

        dos.write(buf);
        
        //sent write request
        
        //reciece akt
        DataInputStream dis = new DataInputStream(echoSocket.getInputStream());

        byte[] data = new byte[4];
        int x = dis.read(data);
        int op = (int) data[1];
        if (op != 4){
            System.out.println("Error recieving akt");
            System.exit(1);
        }
        
        FileInputStream file = null; 
        try{
            file = new FileInputStream(fileName);
        }
        catch (IOException e){
                System.err.println("file not found");
                System.exit(1);
        }
        int avaliableBytes = file.available();
        byte[] buf2 = new byte[avaliableBytes];
        int xi = file.read(buf2, 0, avaliableBytes);
        dos.write(buf2, 0, avaliableBytes);

        

    }
    
    public void read(String fileName, String address, int port) throws IOException{
        byte[] buf = new byte[100];
        buf[0] = (byte) 0;
        buf[1] = (byte) 1;
        for (int i = 2; i < fileName.length()+2; i++) {
	    buf[i] = (byte) fileName.charAt(i-2);
	}
        buf[fileName.length()+2] = (byte) 0;
        String mode = "octet";
        for (int x=fileName.length()+3; x<fileName.length()+7; x++){
            buf[x] = (byte) mode.charAt(x-(fileName.length()+3));
        }
        buf[fileName.length()+7] = (byte) 0;
        
        Socket echoSocket = new Socket(address, port);
        
        OutputStream out = echoSocket.getOutputStream(); 
        DataOutputStream dos = new DataOutputStream(out);

        dos.write(buf);
        
        
        DataInputStream dis = new DataInputStream(echoSocket.getInputStream());

        byte[] data = new byte[5000];
        int x = dis.read(data);
       
        FileOutputStream f = null;
        try {   
            System.out.println(fileName);

            f = new FileOutputStream(fileName);
            f.write(data);
        } catch (FileNotFoundException ex) {
            System.out.println("Error creating file");
            System.exit(1);
        }                
    }
    
}
