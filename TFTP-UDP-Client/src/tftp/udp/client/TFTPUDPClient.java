/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tftp.udp.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author harryrollings
 */
public class TFTPUDPClient {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        // parse arguments
        // command = read | write 
        
        // Check that both required input arguments are passed.
        if (args.length != 4) {
            System.err.println("<mode> <file name> <address> <port>");
            System.err.println("read | write");
            System.exit(1);
        }
        
        TFTPUDPClient client = new TFTPUDPClient();
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

    private void read(String filepath, String address, int port) throws IOException {
        //create read request 
        
        DatagramPacket p = createReadRequest(filepath, address, port);
        
        DatagramSocket socket;
        socket = new DatagramSocket(4000);
        
        byte[] buf2 = new byte[4];

        socket.setSoTimeout(2000);
        boolean check = true;
        while (check){
            socket.send(p);
            try{
                socket.receive(p);
                buf2 = p.getData();
                if ((int) buf2[1] != 4){
                    System.out.println("error");
                }
                else{
                    check = false; 
                }
            }
            catch(SocketTimeoutException s){
                System.out.println("error");
                //no akt! try again
            }
        }
        
        
        
        FileOutputStream f = null;
        try {   
            System.out.println(filepath);

            f = new FileOutputStream(filepath);
        } catch (FileNotFoundException ex) {
            System.out.println("Error creating file");
            System.exit(1);
        }
        
        
        int counter=1;
        outerloop:
        while (true){
            System.out.println("entered loop");
            socket.setSoTimeout(2000);
            boolean checktimeout = true;
            
            byte[] recvBuf = new byte[512]; 
            DatagramPacket packet = new DatagramPacket(recvBuf, 512);
            while (checktimeout){
                try{
                    socket.receive(packet);
                    System.out.println("recieved " + counter);
                    checktimeout = false;
                } catch(SocketTimeoutException s){
                    //end of transmission
                    System.out.println("end");
                    break outerloop;
                }
            }
            

            
            f.write(recvBuf);

            //send akt
            String aktstring = String.valueOf(counter);
            int len = aktstring.length();                                             // length of the byte array
            byte[] buf = new byte[len];                                             // byte array that will store the data to be sent back to the client
            System.arraycopy(aktstring.getBytes(), 0, buf, 0, len);

            InetAddress addr = packet.getAddress();
            int srcPort = packet.getPort();

            packet.setData(buf);

            packet.setAddress(addr);
            packet.setPort(srcPort);

            System.out.println("akt");
            socket.send(packet);
            counter++;
        }
        
        
        
        
    }
    
    public void write(String filepath, String addressed, int port) throws IOException{        
        DatagramPacket p = createWriteRequest(filepath, addressed, port);
        
        DatagramSocket socket;
        socket = new DatagramSocket(4000);
        
        byte[] buf2 = new byte[4];

        socket.setSoTimeout(2000);
        boolean check = true;
        while (check){
            socket.send(p);
            try{
                socket.receive(p);
                buf2 = p.getData();
                if ((int) buf2[1] != 4){
                    System.out.println("error");
                }
                else{
                    check = false; 
                }
            }
            catch(SocketTimeoutException s){
                System.out.println("error");
                //no akt! try again
            }
        }
        //akt recieved
            
            
            
            

        
        
        DatagramPacket[] packets;
        
        System.out.println("fp: " + filepath);
        FileInputStream file = null; 
        try{
            file = new FileInputStream(filepath);
        }
        catch (IOException e){
                System.err.println("file not found");
                System.exit(1);
        }
        
        
        
        int len = 512;
        
        int avaliableBytes = file.available();
        System.out.println("avaliable: " + avaliableBytes);
        int plength = (avaliableBytes/512) + 1;
        packets = new DatagramPacket[plength];
        InetAddress address = InetAddress.getByName(addressed);

        int count = 0;
        int counter = 0;
        for(int i=0; i<avaliableBytes; i+=len){
            byte[] buf = new byte[516];
            buf[0] = (byte) 0;
            buf[1] = (byte) 3;
            //block code
            buf[3] = (byte) (counter & 0xFF);
            buf[2] = (byte) ((counter >> 8) & 0xFF);
            
            int x = file.read(buf, 4, len);
            packets[counter] = new DatagramPacket(buf, 516, address, port);
            counter++;
            count += x;
            
        }        
        

        for(int k=0; k<packets.length; k++){
            socket.setSoTimeout(2000);
            check = true;
            
            while (check){
                socket.send(packets[k]);
                try{
                    socket.receive(packets[k]);
                    byte[] b = new byte[4];
                    b = packets[k].getData();
                    int val = ((b[2] & 0xff) << 8) | (b[3] & 0xff);
                    if(val == k){
                        check = false;
                    }
                }
                catch(SocketTimeoutException s){
                    System.out.println("error");
                }
            }
            
            String received = new String(packets[k].getData());
            
        }



        socket.close();

        
        
    }
    
    
    
    public DatagramPacket createReadRequest(String fileName, String addressed, int port){
        byte[] buf = new byte[fileName.length()+8];
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
        
        InetAddress address = null;
        try {
            address = InetAddress.getByName(addressed);
        } catch (UnknownHostException ex) {
            System.out.println("Unknown host");
            System.exit(1);
        }

        DatagramPacket packet = new DatagramPacket(buf, fileName.length()+8, address, port);
        return packet;
    }
    
    public DatagramPacket createWriteRequest(String fileName, String addressed, int port){
        byte[] buf = new byte[fileName.length()+8];
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
        
        InetAddress address = null;
        try {
            address = InetAddress.getByName(addressed);
        } catch (UnknownHostException ex) {
            System.out.println("Unknown host");
            System.exit(1);
        }

        DatagramPacket packet = new DatagramPacket(buf, fileName.length()+8, address, port);
                  
        return packet;
    }
}
