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
        // command
        // command = read | write 
        
        TFTPUDPClient client = new TFTPUDPClient();
        
        String command = args[0];
        switch (command){
            case "write":
                client.write(args[1]);
                break;
            case "read":
                client.read(args[1]);
                break;
        }
        
    }

    private void read(String filepath) throws IOException {
        //create read request
        System.out.println("reading");
        
        DatagramPacket p = createReadRequest(filepath);
        
        DatagramSocket socket;
        socket = new DatagramSocket(4000);
        
        socket.setSoTimeout(2000);
        boolean check = true;
        while (check){
            socket.send(p);
            try{
                socket.receive(p);
                check = false;
            }
            catch(SocketTimeoutException s){
                System.out.println("error");
                //no akt! try again
            }
        }
        //akt recieved
        
        
        
        
        
        
        
        
        
        
        
        
        FileOutputStream f = null;
        try {   
            System.out.println(filepath);

            f = new FileOutputStream("../"+filepath);
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
    
    public void write(String filepath) throws IOException{
        System.out.println("TESTING...");
        
        DatagramPacket p = createWriteRequest("heylol.png");
        
        DatagramSocket socket;
        socket = new DatagramSocket(4000);
        
        socket.setSoTimeout(2000);
        boolean check = true;
        while (check){
            socket.send(p);
            try{
                socket.receive(p);
                check = false;
            }
            catch(SocketTimeoutException s){
                System.out.println("error");
                //no akt! try again
            }
        }
        //akt recieved
            
            
            
            

        
        
        DatagramPacket[] packets;
        
        FileInputStream file = null; 
        try{
            file = new FileInputStream("../" + filepath);
        }
        catch (IOException e){
                System.err.println("file not found");
                System.exit(1);
        }
        
        
        
        int len = 512;
        
        int avaliableBytes = file.available();
        int plength = (avaliableBytes/512) + 1;
        packets = new DatagramPacket[plength];
        InetAddress address = InetAddress.getByName("127.0.0.1");

        int count = 0;
        int counter = 0;
        for(int i=0; i<avaliableBytes; i+=len){
            byte[] buf = new byte[len];
            int x = file.read(buf, 0, len);
            packets[counter] = new DatagramPacket(buf, len, address, 9000);
            counter++;
            count += x;
        }
        
        System.out.println(packets.length);
        
        

        for(int k=0; k<packets.length; k++){
            socket.setSoTimeout(2000);
            check = true;
            
            while (check){
                socket.send(packets[k]);
                try{
                    socket.receive(packets[k]);
                    check = false;
                }
                catch(SocketTimeoutException s){
                    System.out.println("error");
                }
            }
            
            String received = new String(packets[k].getData());
            System.out.println("" + received.substring(0, packets[k].getLength()));
            
        }



        socket.close();

        
        
    }
    
    
    
    public DatagramPacket createReadRequest(String fileName){
        byte[] buf = new byte[fileName.length()+1];
        buf[0] = (byte) 'r';
        for (int i = 1; i < fileName.length()+1; i++) {
	    buf[i] = (byte) fileName.charAt(i-1);
	}
        InetAddress address = null;
        try {
            address = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException ex) {
            System.out.println("Unknown host");
            System.exit(1);
        }

        DatagramPacket packet = new DatagramPacket(buf, fileName.length()+1, address, 9000);
        return packet;
    }
    
    public DatagramPacket createWriteRequest(String fileName){
        byte[] buf = new byte[fileName.length()+1];
        buf[0] = (byte) 'w';
        for (int i = 1; i < fileName.length()+1; i++) {
	    buf[i] = (byte) fileName.charAt(i-1);
	}
        InetAddress address = null;
        try {
            address = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException ex) {
            System.out.println("Unknown host");
            System.exit(1);
        }

        DatagramPacket packet = new DatagramPacket(buf, fileName.length()+1, address, 9000);
        return packet;
    }
}
