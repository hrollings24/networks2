/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tftp.udp.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        
        
        int counter=0;
        byte[] recvBuf = new byte[512];     // a byte array that will store the data received by the client

        try {
            // run forever
            while (true) {

                DatagramPacket packet = new DatagramPacket(recvBuf, 512);
                socket.receive(packet);
                
                String fileName = null;
                char mode = (char) recvBuf[0];
                System.out.println(mode);
                
                if(mode=='w'){
                    //get the file name
                    //write request
                    int i = 0;
                    for(i=1; i<512; i++){
                        if (recvBuf[i] == 0){
                            break;
                        }
                        else{
                            fileName += (char) recvBuf[i];
                        }
                    }
                    System.out.println(fileName);
                    
                    //send akt
                    String aktstring = String.valueOf(0);
                    int len = aktstring.length();                                             // length of the byte array
                    byte[] buf = new byte[len];                                             // byte array that will store the data to be sent back to the client
                    System.arraycopy(aktstring.getBytes(), 0, buf, 0, len);

                    InetAddress addr = packet.getAddress();
                    int srcPort = packet.getPort();

                    packet.setData(buf);

                    packet.setAddress(addr);
                    packet.setPort(srcPort);

                    socket.send(packet);
                    
                    write(fileName);
                    
                }
                else if(mode=='r'){
                    //get the file name
                    //read request
                    int i = 0;
                    for(i=1; i<512; i++){
                        if (recvBuf[i] == 0){
                            break;
                        }
                        else{
                            fileName += (char) recvBuf[i];
                            
                        }
                    }
                    System.out.println(fileName);
                    
                    //send akt
                    String aktstring = String.valueOf(0);
                    int len = aktstring.length();                                             // length of the byte array
                    byte[] buf = new byte[len];                                             // byte array that will store the data to be sent back to the client
                    System.arraycopy(aktstring.getBytes(), 0, buf, 0, len);

                    InetAddress addr = packet.getAddress();
                    int srcPort = packet.getPort();

                    packet.setData(buf);

                    packet.setAddress(addr);
                    packet.setPort(srcPort);

                    socket.send(packet);

                    read(fileName, addr, srcPort);
                    
                    
                }
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        socket.close();
    }
    
    
    public void write(String fileName) throws IOException{
        FileOutputStream f = null;
        try {   
            f = new FileOutputStream("../"+fileName);
        } catch (FileNotFoundException ex) {
            System.out.println("Error creating file");
            System.exit(1);
        }
        
        
        int counter=1;
        outerloop:
        while (true){
            socket.setSoTimeout(2000);
            boolean checktimeout = true;
            
            byte[] recvBuf = new byte[512]; 
            DatagramPacket packet = new DatagramPacket(recvBuf, 512);
            while (checktimeout){
                try{
                    socket.receive(packet);
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

            socket.send(packet);
            counter++;
        }
        
    }
    
    
    public void read(String fileName, InetAddress addr, int src) throws IOException{
        FileInputStream file = null; 
        try{
            file = new FileInputStream("../" + fileName);
        }
        catch (IOException e){
                System.err.println("file not found");
                System.exit(1);
        }


        DatagramPacket packets[];
        int len = 512;

        int avaliableBytes = file.available();
        int plength = (avaliableBytes/512) + 1;
        packets = new DatagramPacket[plength];
        
        int counterq = 0;
        for(int e=0; e<avaliableBytes; e+=len){
            byte[] buf = new byte[len];
            int x = file.read(buf, 0, len);
            packets[counterq] = new DatagramPacket(buf, len, addr, src);
            counterq++;
        }
        
        Boolean check;
        for(int k=0; k<packets.length; k++){
            socket.setSoTimeout(2000);
            check = true;
            
            while (check){
                System.out.println("Sending " + k);
                socket.send(packets[k]);
                try{
                    socket.receive(packets[k]);
                    check = false;
                }
                catch(SocketTimeoutException s){
                    System.out.println("error");
                }
            }
            
        }
        
        
        
    }
    
}
