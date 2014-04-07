package javachat;

import java.io.*;
import java.net.*;
import java.util.Arrays;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import javachat.JavaChatMessage;;

public class ServerCommunication {
	private SSLSocket socket;
	private DataOutputStream outStream;
	private DataInputStream inStream;
//	private PushbackInputStream inStream;
	
	public ServerCommunication(String hostName, int portNumber) throws IOException {
		String trustStorePath = "./ssl/truststore.jks";
		String trustStorePassword = "javachat";
		
		System.setProperty("javax.net.ssl.trustStore", trustStorePath);
		System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
		
		SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
		SSLSocket newSocket = (SSLSocket) sf.createSocket(hostName, portNumber);
		newSocket.setSoTimeout(250);	// need timeout to ensure that we don't block infinitely on read()
		this.setSocket(newSocket);
	}
	
	public ServerCommunication(SSLSocket socket) throws IOException {
		socket.setSoTimeout(250);
		this.setSocket(socket);
	}
	
	public void setSocket(SSLSocket socket) throws IOException {
		// close previous socket if it exists
		this.closeSocket();
		
		this.socket = socket;
    	this.outStream = new DataOutputStream(socket.getOutputStream());
    	this.inStream = new DataInputStream(socket.getInputStream());
//    	this.inStream = new PushbackInputStream(socket.getInputStream());
	}
	
	public void closeSocket() throws IOException {
		if(this.outStream != null) {
			this.outStream.close();
		}
		if(this.inStream != null) {
			this.inStream.close();
		}
		if(this.socket != null) {
			this.socket.close();
		}
	}
	
	public void sendMessage(JavaChatMessage message) throws IOException {
		this.outStream.write(message.messageBytes);
	}
	
	public boolean isConnected() {
		return this.socket.isConnected();
	}
	
	/* 
	 * Tries to read the input stream. If the read operation times out, returns a 
	 * special JavaChatMessage of type -1 
	 */
	public JavaChatMessage readMessage() throws IOException {
		try {
			byte[] headers = new byte[12];
			int bytesRead = 0;
			while(bytesRead < 12) {
			  bytesRead += this.inStream.read(headers, bytesRead, 12 - bytesRead);
			}
			
			byte[] byteMessageType = Arrays.copyOfRange(headers,0,4);
			byte[] byteSubMessageType = Arrays.copyOfRange(headers,4,8);
			byte[] byteMessageSize = Arrays.copyOfRange(headers,8,12);
	    	    	
	    	int messageType = JavaChatMessage.bytesToInt(byteMessageType);	  	
//	    	System.out.println("Type: " + messageType);	    	
	    	int subMessageType = JavaChatMessage.bytesToInt(byteSubMessageType);	    	
//	    	System.out.println("Sub: " + subMessageType);	    	
	    	int messageSize = JavaChatMessage.bytesToInt(byteMessageSize);	    	
//	    	System.out.println("Size: " + messageSize);
	    	
	    	byte[] byteMessageData = new byte[messageSize];
	    	this.inStream.read(byteMessageData, 0, byteMessageData.length);
	    	String messageData = JavaChatMessage.bytesToString(byteMessageData);
	    	
	    	byte[] messageBytes = new byte[JavaChatMessage.MSG_INT_SIZE * 3 + messageSize]; 
	    	
	    	return new JavaChatMessage(messageType, subMessageType, messageSize, messageData, messageBytes);
	    	
		} catch (java.net.SocketTimeoutException e) {
			return new JavaChatMessage(-1, " ");	// socket timeout: return JCM of type -1
		} catch (IndexOutOfBoundsException e) {
			return new JavaChatMessage(-1, " ");	// socket timeout: return JCM of type -1
		}
	}
	
	public boolean messageAvailable() throws IOException {
//		try {
//			int data = this.inStream.read();
//			this.inStream.unread(data);
//			System.out.println("TRY");
//			return true;
//		} catch (IOException e) {
//			// no data on input stream
//			System.out.println("CATCH");
//			return false;
//		}		
		
		
		if(this.inStream.available() > 0 ) {
			return true;
		} else {
			return false;
		}
	}

}
