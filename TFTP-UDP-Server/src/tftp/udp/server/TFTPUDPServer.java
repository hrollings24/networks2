/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tftp.udp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;

/**
 *
 * @author harryrollings
 */
public class TFTPUDPServer extends Thread {

    protected DatagramSocket socket = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SocketException {
        new TFTPUDPServer().start();
        System.out.println("Time Server Started");

    }
    
    public TFTPUDPServer() throws SocketException {
        this("UDPSocketServer");
    }
    
    public TFTPUDPServer(String name) throws SocketException {
        super(name);

        socket = new DatagramSocket(9000);
        
    }
    
    @Override
    public void run() {
        
        System.out.println("IN SERVER");
        
        int counter = 0;                    // just a counter - used below
        byte[] recvBuf = new byte[512];     // a byte array that will store the data received by the client

        try {
            // run forever
            while (true) {

                DatagramPacket packet = new DatagramPacket(recvBuf, 512);
                socket.receive(packet);

                // Get the current date/time and copy it in the byte array
                String dString = new Date().toString() + " - Counter: " + (counter);
                int len = dString.length();                                             // length of the byte array
                byte[] buf = new byte[len];                                             // byte array that will store the data to be sent back to the client
                System.arraycopy(dString.getBytes(), 0, buf, 0, len);

                InetAddress addr = packet.getAddress();
                int srcPort = packet.getPort();

                packet.setData(buf);
                
                packet.setAddress(addr);
                packet.setPort(srcPort);

                socket.send(packet);

                counter++;
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        socket.close();
    }
    
}
