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
        
        //create a new instance of a client
        TFTPUDPClient client = new TFTPUDPClient();
        //go to the correct function depending on the command
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
        
        //open a socket
        DatagramSocket socket;
        socket = new DatagramSocket(4000);
        
        //create a file output stream
        //this creates a file with the specified filename
        FileOutputStream f = null;
        try {   
            System.out.println(filepath);
            f = new FileOutputStream(filepath);
        } catch (FileNotFoundException ex) {
            //the file couldn't be made, exit the program
            System.out.println("Error creating file");
            System.exit(1);
 
        }
       
        //counter is used to keep track of the block number
        int counter=0;
        
        //send the read request
        socket.send(p);
        
        //the server should start sending data upon recieving the read request
        outerloop:
        while (true){
            //set the timeout
            //if nothing recieved in 2000ms then throw a timeout
            socket.setSoTimeout(2000);
            //used as a check if the data is the wrong packet
            boolean checktimeout = true;
            
            //this is the byte array the recieved data will be stored in
            byte[] recvBuf = new byte[516]; 
            //this is the packet for the packet that will be recieved
            DatagramPacket packet = new DatagramPacket(recvBuf, 516);
            while (checktimeout){
                try{
                    //recieve the packet from server
                    socket.receive(packet);
                    //get the data from the packet
                    recvBuf = packet.getData();
                    //if the data is 0, end of transmission has been reached
                    if (recvBuf.length == 0){
                        System.out.println("end");
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

                        //send packets data, address and port, then send
                        packet.setData(buf);
                        packet.setAddress(addr);
                        packet.setPort(srcPort);
                        socket.send(packet);
                        
                        //increment counter
                        //break loop as transmisson over
                        counter++;
                        break outerloop;
                    }
                    
                    //if the packet is a data packet
                    if (((int)recvBuf[1]) == 3){
                        //val is the block number
                        int val = ((recvBuf[2] & 0xff) << 8) | (recvBuf[3] & 0xff);
                        //if block number is the expected block
                        if (val == counter){
                            //packet was recieved correctly
                            checktimeout = false;
                        }
                    }
                } catch(SocketTimeoutException s){
                    //end of transmission
                    System.out.println("end");
                    break outerloop;
                }
            }
            for(int i=4; i<516; i++){
                //write the data to the file
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

            //send the akt
            InetAddress addr = packet.getAddress();
            int srcPort = packet.getPort();

            packet.setData(buf);

            packet.setAddress(addr);
            packet.setPort(srcPort);

            socket.send(packet);
            
            //increment counter as block recieved correctly
            counter++;
        }
        
        
        
        
    }
    
    public void write(String filepath, String addressed, int port) throws IOException{    
        //create a write request
        DatagramPacket p = createWriteRequest(filepath, addressed, port);
        
        //open a socket
        DatagramSocket socket;
        socket = new DatagramSocket(4000);
        
        //the byte array to store the akt
        byte[] buf2 = new byte[4];

        //set the socket timeout to 2000ms
        socket.setSoTimeout(2000);
        //check is a flag to check the akt is recieved
        boolean check = true;
        while (check){
            //send the write request
            socket.send(p);
            try{
                //recieve akt
                socket.receive(p);
                buf2 = p.getData();
                //if packet is not the akt packet
                if ((int) buf2[1] != 4){
                    System.out.println("error");
                }
                else{
                    //if packet is akt break from loop
                    check = false; 
                }
            }
            catch(SocketTimeoutException s){
                System.out.println("error");
                //no akt! try again
            }
        }
        //akt recieved
           
        //array of packets to be sent
        DatagramPacket[] packets;

        //create a file input stream
        //finds the file from the file system
        FileInputStream file = null; 
        try{
            file = new FileInputStream(filepath);
        }
        catch (IOException e){
                System.err.println("file not found");
                System.exit(1);
        }
        
        
        //length of data to be sent
        int len = 512;
        
        int avaliableBytes = file.available();
        //amount of packets required
        int plength = (avaliableBytes/512) + 1;
        packets = new DatagramPacket[plength];
        InetAddress address = InetAddress.getByName(addressed);

        //amount of bytes sent
        int count = 0;
        //block counter 
        int counter = 0;
        for(int i=0; i<avaliableBytes; i+=len){
            //create the packets
            //set opcode
            byte[] buf = new byte[516];
            buf[0] = (byte) 0;
            buf[1] = (byte) 3;
            //set block code
            buf[3] = (byte) (counter & 0xFF);
            buf[2] = (byte) ((counter >> 8) & 0xFF);
            
            //set data
            int x = file.read(buf, 4, len);
            packets[counter] = new DatagramPacket(buf, 516, address, port);
            
            //increment amount of data read and block number
            counter++;
            count += x;
            
        }        
        
        //for however many packets there are
        for(int k=0; k<packets.length; k++){
            socket.setSoTimeout(2000);
            check = true;
            //check is a flag to check correct packet recieved
            while (check){
                //send packet
                socket.send(packets[k]);
                try{
                    //recieve akt
                    socket.receive(packets[k]);
                    byte[] b = new byte[4];
                    b = packets[k].getData();
                    int val = ((b[2] & 0xff) << 8) | (b[3] & 0xff);
                    //check the akt recieved is the correct block number
                    if(val == k){
                        //break loop if true
                        check = false;
                    }
                }
                catch(SocketTimeoutException s){
                    System.out.println("error");
                }
            }
            
            
        }


        //Close socket when transmisson over
        socket.close();

        
        
    }
    
    
    
    public DatagramPacket createReadRequest(String fileName, String addressed, int port){
        
        //create a read request
        //set opcode, filename, and mode
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

        //return the packet
        DatagramPacket packet = new DatagramPacket(buf, fileName.length()+8, address, port);
        return packet;
    }
    
    public DatagramPacket createWriteRequest(String fileName, String addressed, int port){
        //create write request
        //set opcode, filename and mode
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

        //return the packet
        DatagramPacket packet = new DatagramPacket(buf, fileName.length()+8, address, port);
                  
        return packet;
    }
}
