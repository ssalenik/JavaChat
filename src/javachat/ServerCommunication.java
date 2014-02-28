package javachat;

import java.io.*;
import java.net.*;
import javachat.JavaChatMessage;;

public class ServerCommunication {
	private Socket socket;
	private DataOutputStream outStream;
	private DataInputStream inStream;
	
	public ServerCommunication(String hostName, int portNumber) throws IOException {
		Socket newSocket = new Socket(hostName, portNumber);
		this.setSocket(newSocket);
	}
	
	public ServerCommunication(Socket socket) throws IOException {
		this.setSocket(socket);
	}
	
	public void setSocket(Socket socket) throws IOException {
		// close previous socket if it exists
		this.closeSocket();
		
		this.socket = socket;
    	this.outStream = new DataOutputStream(socket.getOutputStream());
    	this.inStream = new DataInputStream(socket.getInputStream());
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
	
	public JavaChatMessage readMessage() throws IOException {
		byte[] byteMessageType = new byte[JavaChatMessage.MSG_INT_SIZE];
    	byte[] byteSubMessageType = new byte[JavaChatMessage.MSG_INT_SIZE];
    	byte[] byteMessageSize = new byte[JavaChatMessage.MSG_INT_SIZE];
    	
    	// read message
    	this.inStream.read(byteMessageType, 0, JavaChatMessage.MSG_INT_SIZE);
    	int messageType = JavaChatMessage.bytesToInt(byteMessageType);
    	
    	this.inStream.read(byteSubMessageType, 0, byteSubMessageType.length);
    	int subMessageType = JavaChatMessage.bytesToInt(byteSubMessageType);
    	
    	this.inStream.read(byteMessageSize, 0, byteMessageSize.length);
    	int messageSize = JavaChatMessage.bytesToInt(byteMessageSize);
    	
    	byte[] byteMessageData = new byte[messageSize];
    	this.inStream.read(byteMessageData, 0, byteMessageData.length);
    	String messageData = JavaChatMessage.bytesToString(byteMessageData);
    	
    	byte[] messageBytes = new byte[JavaChatMessage.MSG_INT_SIZE * 3 + messageSize];
    	
    	return new JavaChatMessage(messageType, subMessageType, messageSize, messageData, messageBytes);
	}

}
