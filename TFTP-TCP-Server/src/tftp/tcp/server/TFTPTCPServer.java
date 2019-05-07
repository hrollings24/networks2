/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tftp.tcp.server;

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
import java.net.DatagramPacket;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author harryrollings
 */
public class TFTPTCPServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        
        // the port number that the server socket will be bound to
        int portNumber = 10000;

        // The TCP ServerSocket object (master socket)
        ServerSocket masterSocket;
        Socket slaveSocket;

        masterSocket = new ServerSocket(portNumber);
        System.out.println("Server Started...");
        
        
        while (true) {
            
            slaveSocket = masterSocket.accept();
            
            System.out.println("Accepted TCP connection from: " + slaveSocket.getInetAddress() + ", " + slaveSocket.getPort() + "...");
            
            InputStream in = slaveSocket.getInputStream();
            DataInputStream dis = new DataInputStream(in);
            String filename = null;

            byte[] data = new byte[100];
            dis.readFully(data);
            int op = (int) data[1];
            for(int i=2; i<100; i++){
                if(data[i]==0){
                    break;
                } 
                else{
                    filename += (char) data[i];
                }
            }
            System.out.println(filename);

            TFTPTCPServer sv = new TFTPTCPServer();
            switch(op){
                case 1:
                    sv.read(filename, slaveSocket);
                    break;
                case 2:
                    sv.write(filename, slaveSocket);
                    break;
                case 3:
            }
            
            
            
            
            
            
        }
    }
    
    
    public void read(String filename, Socket slaveSocket) throws IOException{
        FileInputStream file = null; 
        try{
            file = new FileInputStream(filename);
        }
        catch (IOException e){
                System.err.println("file not found");
                System.exit(1);
        }
        DataOutputStream dos = new DataOutputStream(slaveSocket.getOutputStream());

        int avaliableBytes = file.available();

        byte[] buf = new byte[avaliableBytes];
        int x = file.read(buf, 0, avaliableBytes);

        dos.write(buf, 0, avaliableBytes);
    }
    
    
    public void write(String filename, Socket slaveSocket) throws IOException{
        //send akt
        byte[] buf = new byte[4];
        buf[1] = 4;
        DataOutputStream dos = new DataOutputStream(slaveSocket.getOutputStream());
        dos.write(buf, 0, 4);
        
        InputStream in = slaveSocket.getInputStream();
        DataInputStream dis = new DataInputStream(in);

        byte[] data = new byte[5000];
        int x = dis.read(data);
       
        FileOutputStream f = null;
        try {   
            System.out.println(filename);

            f = new FileOutputStream(filename);
            f.write(data);
        } catch (FileNotFoundException ex) {
            System.out.println("Error creating file");
            System.exit(1);
        }        
        
    }

}
