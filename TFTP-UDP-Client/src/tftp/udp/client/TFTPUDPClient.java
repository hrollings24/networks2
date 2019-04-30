/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tftp.udp.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

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
        
        String command = args[0];
        switch (command){
            case "write":
                write(args[1]);
                break;
            case "read":
                read();
                break;
        }
        
    }

    private static void read() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static void write(String filepath) throws IOException{
        System.out.println("TESTING...");
        DatagramSocket socket;
        DatagramPacket packet;
        
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
        
        
        socket = new DatagramSocket(4000);
        socket.setSoTimeout(2000);

        for(int k=0; k<packets.length; k++){
            socket.send(packets[k]);
            //ACK from server
            
            socket.receive(packets[k]);
            
            String received = new String(packets[k].getData());
            System.out.println("Today's date: " + received.substring(0, packets[k].getLength()));
            
        }

        socket.close();

        
        
    }
    
}
