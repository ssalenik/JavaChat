package test;

import javachat.*;
import java.io.*;
import java.net.*;

public class ClientTest {
	
	private static ServerCommunication server;
	private static JCMFactory jcmf;
	
	public static void main(String[] args) throws IOException {
        
//        if (args.length != 2) {
//            System.err.println(
//                "Usage: java EchoClient <host name> <port number>");
//            System.exit(1);
//        }
 
        String hostName = "dsp2014.ece.mcgill.ca";
        int portNumber = 5000;
        
    	String user1 = "qrrkjkk";
    	String pass = "pass1";
    	String user2 = "qrrkjk";
 
        try {
        	server = new ServerCommunication(hostName, portNumber);        	
        	jcmf = new JCMFactory();
        	
        	sendTestMessage( jcmf.login(user1, pass) ); 
        	sendTestMessage( jcmf.sendMessageToUser(user2, "TEST MSG 1"));
        	sendTestMessage( jcmf.sendMessageToUser(user2, "TEST MSG 2"));
        	sendTestMessage( jcmf.sendMessageToUser(user2, "TEST MSG 3"));
        	sendTestMessage( jcmf.logoff() );
        	
        	sendTestMessage( jcmf.login(user2, pass) ); 
        	sendTestMessage( jcmf.queryMessages() );
        	sendTestMessage( jcmf.logoff() );

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
	
	// Sends a message to the server. Prints both the sent message and the reply from the server.
	public static void sendTestMessage(JavaChatMessage outMessage) {
		
		try {      	
	    	System.out.println("sending message :\n" + outMessage);
	    	server.sendMessage(outMessage);	  //send message to server 
	    	
	    	JavaChatMessage inMessage = server.readMessage();	// read reply from server
	    	
	    	/* 
	    	 * Loop until the socket times out and returns a message of type -1. This loop 
	    	 * is necessary to account for commands which return more than one message (i.e. query_msg)
	    	 */
	    	while (inMessage.getMessageType() > 0) {
    			System.out.println("got message :\n" + inMessage); // print reply message
	    		inMessage = server.readMessage();	// read reply from server
	    	}
	    	System.out.println("");
	    	
		} catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection");
                System.exit(1);
        }
	}
}
