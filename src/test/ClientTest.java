package test;

import javachat.*;
import java.io.*;
import java.net.*;

public class ClientTest {
	public static void main(String[] args) throws IOException {
        
//        if (args.length != 2) {
//            System.err.println(
//                "Usage: java EchoClient <host name> <port number>");
//            System.exit(1);
//        }
 
        String hostName = "dsp2014.ece.mcgill.ca";
        int portNumber = 5000;
 
        try {
        	ServerCommunication server = new ServerCommunication(hostName, portNumber);
        	
        	int messageType = 2; // echo
        	String messageData = "Test Echo Message";
        	
        	JavaChatMessage outMessage = new JavaChatMessage(messageType, messageData);
        	
        	// write message
        	System.out.println("sending message :\n" + outMessage);
        	server.sendMessage(outMessage);
        	
        	// read message
        	JavaChatMessage inMessage = server.readMessage();
        	
        	// print message
        	System.out.println("got message :\n" + inMessage);
        	
        	server.closeSocket();
 
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        }
    }
}
