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
        byte[] recvBuf = new byte[516];     // a byte array that will store the data received by the client

        try {
            // run forever
            while (true) {

                //packet recieved
                DatagramPacket packet = new DatagramPacket(recvBuf, 516);
                socket.receive(packet);
                
                String fileName = "";
                int mode = (int) recvBuf[1];
                System.out.println(mode);
                
                if(mode==2){
                    //get the file name
                    //write request
                    int i = 0;
                    for(i=2; i<512; i++){
                        if (recvBuf[i] == 0){
                            break;
                        }
                        else{
                            fileName += (char) recvBuf[i];
                        }
                    }
                    
                    //send akt
                    byte[] buf = new byte[4];
                    buf[1] = (byte) 4;

                    InetAddress addr = packet.getAddress();
                    int srcPort = packet.getPort();

                    packet.setData(buf);

                    packet.setAddress(addr);
                    packet.setPort(srcPort);
                    
                    socket.send(packet);
                    
                    //go to write function
                    write(fileName);
                    
                }
                else if(mode==1){
                    //get the file name
                    //read request
                    int i = 0;
                    for(i=2; i<512; i++){
                        if (recvBuf[i] == 0){
                            break;
                        }
                        else{
                            fileName += (char) recvBuf[i];
                            
                        }
                    }
                    
                    
                    InetAddress addr = packet.getAddress();
                    int srcPort = packet.getPort();

                    //go to read funtion
                    read(fileName, addr, srcPort);
                    
                    
                }
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        socket.close();
    }
    
    
    
    
    
    
    public void write(String fileName) throws IOException{
        //create a new file with specified filename 
        FileOutputStream f = null;
        try {   
            f = new FileOutputStream(fileName);
        } catch (FileNotFoundException ex) {
            System.out.println("Error creating file");
            System.exit(1);
        }
        
        //counter is used to keep track of the block number
        int counter=0;
        outerloop:
        while (true){
            //socket will timeout if nothing recieved within 2000ms
            socket.setSoTimeout(2000);
            //a flag to check right packet recieved
            boolean checktimeout = true;
            
            //the byte array and packet which will store the recieved data and packet
            byte[] recvBuf = new byte[516]; 
            DatagramPacket packet = new DatagramPacket(recvBuf, 516);
            while (checktimeout){
                try{
                    //recieve the packet
                    socket.receive(packet);
                    recvBuf = packet.getData();
                    //if the packet is data type
                    if (((int)recvBuf[1]) == 3){
                        int val = ((recvBuf[2] & 0xff) << 8) | (recvBuf[3] & 0xff);
                        if (val == counter){
                            //check whether correct block number and break from loop if correct
                            checktimeout = false;
                        }
                    }
                } catch(SocketTimeoutException s){
                    //end of transmission
                    System.out.println("end");
                    break outerloop;
                }
            }
            //write the data to the file
            for(int i=4; i<516; i++){
                f.write(recvBuf[i]);
            }
            
            //send akt
            String aktstring = String.valueOf(counter);
            int len = aktstring.length();                                           // length of the byte array
            byte[] buf = new byte[4];                                             // byte array that will store the data to be sent back to the client
            buf[1] = (byte) 4;
            //block code
            buf[3] = (byte) (counter & 0xFF);
            buf[2] = (byte) ((counter >> 8) & 0xFF);

            InetAddress addr = packet.getAddress();
            int srcPort = packet.getPort();

            packet.setData(buf);

            packet.setAddress(addr);
            packet.setPort(srcPort);

            //send the akt and increment the expected block number
            socket.send(packet);
            counter++;
        }
        
    }
    
    
    public void read(String fileName, InetAddress address, int port) throws IOException{
        
        //find the file at the specified filename
        FileInputStream file = null; 
        try{
            file = new FileInputStream(fileName);
        }
        catch (IOException e){
                System.err.println("file not found");
                System.exit(1);
        }


        int len = 512;
        
        int avaliableBytes = file.available();
        //the amount of packets expected to read
        int plength = (avaliableBytes/512) + 1;
        //create array of packets to send to client
        DatagramPacket[] packets = new DatagramPacket[plength];

        //count is amount of bytes
        //counter is block number tracket
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
            //create a packet and add to the array at the block number
            packets[counter] = new DatagramPacket(buf, 516, address, port);
            //increment variables
            counter++;
            count += x;
            
        }        
        
        //send the packets
        outerloop:
        for(int k=0; k<packets.length; k++){
            //set socket timeout to 2000ms
            socket.setSoTimeout(2000);
            //check is a flag to check correct packet recieved
            Boolean check = true;
            
            while (check){
                //if packet is empty
                if (packets[k] == null){
                    //send empty
                    byte[] b = new byte[0];
                    packets[k] = new DatagramPacket(b, 0, address, port);
                    //transmisson over
                    socket.close();
                    break;
                    
                }
                //send packet
                socket.send(packets[k]);
                try{
                    //recieve akt
                    socket.receive(packets[k]);
                    byte[] b = new byte[4];
                    b = packets[k].getData();
                    //check packet is akt type and block number is correct
                    int val = ((b[2] & 0xff) << 8) | (b[3] & 0xff);
                    if(val == k){
                        check = false;
                    }
                }
                catch(SocketTimeoutException s){
                    System.out.println("error");
                }
            }
        }
        
        
    }
    
    
}
