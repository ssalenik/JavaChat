package test;

import javachat.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

public class ClientTest {
	public static void main(String[] args) throws IOException {
        
//        if (args.length != 2) {
//            System.err.println(
//                "Usage: java EchoClient <host name> <port number>");
//            System.exit(1);
//        }
 
        String hostName = "dsp2014.ece.mcgill.ca";
        int portNumber = 5000;
 
        try (
            Socket socket = new Socket(hostName, portNumber);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        	DataInputStream in = new DataInputStream(socket.getInputStream());
        ) {
        	int messageType = 2; // echo
        	int subMessageType = 0;
        	String messageData = "Test Echo Message";
        	
        	byte[] byteMessage = ServerCommunication.messageToByteArray(messageType, subMessageType, messageData);
        	
        	// write message
        	out.write(byteMessage);
        	
        	byte[] byteMessageTypeReply = new byte[4];
        	byte[] byteSubMessageTypeReply = new byte[4];
        	byte[] byteMessageSizeReply = new byte[4];
        	
        	// read message
        	in.read(byteMessageTypeReply, 0, byteMessageTypeReply.length);
        	int messageTypeReply = ByteBuffer.wrap(byteMessageTypeReply).getInt();
        	System.out.println("message reply received of type: " + messageTypeReply);
        	
        	in.read(byteSubMessageTypeReply, 0, byteSubMessageTypeReply.length);
        	int subMessageTypeReply = ByteBuffer.wrap(byteSubMessageTypeReply).getInt();
        	System.out.println("sub message type: " + subMessageTypeReply);
        	
        	in.read(byteMessageSizeReply, 0, byteMessageSizeReply.length);
        	int messageSizeReply = ByteBuffer.wrap(byteMessageSizeReply).getInt();
        	System.out.println("message length: " + messageSizeReply);
        	
        	byte[] byteMessageDataReply = new byte[messageSizeReply];
        	in.read(byteMessageDataReply, 0, byteMessageDataReply.length);
        	String messageDataReply = new String(byteMessageDataReply);
        	System.out.println("message data: " + messageDataReply);
        	
 
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
