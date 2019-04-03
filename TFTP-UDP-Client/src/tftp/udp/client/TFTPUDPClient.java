/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tftp.udp.client;

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
                write();
                break;
            case "read":
                read();
                break;
        }
        
    }

    private static void read() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static void write() throws IOException{
        System.out.println("TESTING...");
        DatagramSocket socket;
        DatagramPacket packet;
        
        int len = 512;
        byte[] buf = new byte[len];
        
        socket = new DatagramSocket(4000);
        
        InetAddress address = InetAddress.getByName("127.0.0.1");
        
        packet = new DatagramPacket(buf, len);
        packet.setAddress(address);
        packet.setPort(9000);

        socket.send(packet);

        //ACK from server
        socket.receive(packet);

        // display response
        String received = new String(packet.getData());
        System.out.println("Today's date: " + received.substring(0, packet.getLength()));
        socket.close();
        
    }
    
}
