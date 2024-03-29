package javachat;

import java.nio.ByteBuffer;

public class JavaChatMessage {
	final static int MSG_INT_SIZE = 4; // size of integer in bytes for our messages
	final static int[] MSG_TYPE_RANGE = {0,9}; // value range of message types (0 - 9)
	final static int MSG_MAX_SIZE = 262144; // maximum data payload size
	
	int messageType;
	int subMessageType;
	int messageSize;
	String messageData;
	byte[] messageDataBin;
	byte[] messageBytes = null;
	
	public JavaChatMessage(int messageType, String messageData) throws IllegalArgumentException {
		this.messageType = messageType;
		this.subMessageType = 0;
		this.messageData = messageData;
		
		getMessageByteArray();
	}
	
	public JavaChatMessage(int messageType, byte[] messageData) throws IllegalArgumentException{
		this.messageType = messageType;
		this.subMessageType = 0;
		this.messageData = "<binary data>";
		// copy the array
		this.messageDataBin = new byte[messageData.length];
		System.arraycopy( messageData, 0, this.messageDataBin, 0, messageData.length );
		
		getBinMessageByteArray();
	}
	
	public JavaChatMessage(int messageType, int subMessageType, int messageSize, String messageData, byte[] messageBytes) {
		this.messageType = messageType;
		this.subMessageType = subMessageType;
		this.messageSize= messageSize ;
		this.messageData= messageData ;
		this.messageBytes = messageBytes;
	}
	
	public String toString() {
		return "[ " + messageType 
			+ " | " + subMessageType
			+ " | " + messageSize
			+ " | \"" + messageData + "\" ]";
	}
	
	private void getMessageByteArray() throws IllegalArgumentException {
		byte[] byteMessageType = ByteBuffer.allocate(MSG_INT_SIZE).putInt(this.messageType).array();
    	byte[] byteSubMessageType = ByteBuffer.allocate(MSG_INT_SIZE).putInt(this.subMessageType).array();
    	byte[] byteMessageData = this.messageData.getBytes();
    	this.messageSize = byteMessageData.length;
    	
    	// check max msg size
    	if(this.messageSize > MSG_MAX_SIZE) {
    		throw new IllegalArgumentException("Message data exceeds maximum size: " + MSG_MAX_SIZE + "bytes");
    	}
    	byte[] byteMessageSize = ByteBuffer.allocate(MSG_INT_SIZE).putInt(this.messageSize).array();
		
    	this.messageBytes = new byte[MSG_INT_SIZE * 3 + byteMessageData.length];
    	System.arraycopy(byteMessageType, 0, this.messageBytes, 0, MSG_INT_SIZE);
    	System.arraycopy(byteSubMessageType, 0, this.messageBytes, MSG_INT_SIZE, MSG_INT_SIZE);
    	System.arraycopy(byteMessageSize, 0, this.messageBytes, MSG_INT_SIZE * 2, MSG_INT_SIZE);
    	System.arraycopy(byteMessageData, 0, this.messageBytes, MSG_INT_SIZE * 3, byteMessageData.length);
   	}
	
	private void getBinMessageByteArray() throws IllegalArgumentException {
		byte[] byteMessageType = ByteBuffer.allocate(MSG_INT_SIZE).putInt(this.messageType).array();
    	byte[] byteSubMessageType = ByteBuffer.allocate(MSG_INT_SIZE).putInt(this.subMessageType).array();
    	this.messageSize = this.messageDataBin.length;
    	
    	// check max msg size
    	if(this.messageSize > MSG_MAX_SIZE) {
    		throw new IllegalArgumentException("Message data exceeds maximum size: " + MSG_MAX_SIZE + "bytes");
    	}
    	byte[] byteMessageSize = ByteBuffer.allocate(MSG_INT_SIZE).putInt(this.messageSize).array();
		
    	this.messageBytes = new byte[MSG_INT_SIZE * 3 + messageDataBin.length];
    	System.arraycopy(byteMessageType, 0, this.messageBytes, 0, MSG_INT_SIZE);
    	System.arraycopy(byteSubMessageType, 0, this.messageBytes, MSG_INT_SIZE, MSG_INT_SIZE);
    	System.arraycopy(byteMessageSize, 0, this.messageBytes, MSG_INT_SIZE * 2, MSG_INT_SIZE);
    	System.arraycopy(messageDataBin, 0, this.messageBytes, MSG_INT_SIZE * 3, messageDataBin.length);
	}
	
	/**
	 * Converts message parameters into byte array ready to be sent to the chat server.
	 * 
	 * @param messageType
	 * @param subMessageType
	 * @param messageData
	 * @return Returns byte array containing the message.
	 */
	public static byte[] messageToByteArray(
			int messageType, int subMessageType, String messageData) throws IllegalArgumentException {
		// check input
		if(messageType < JavaChatMessage.MSG_TYPE_RANGE[0] 
				|| messageType > JavaChatMessage.MSG_TYPE_RANGE[1]) {
			throw new IllegalArgumentException(
					"Invalid message type, must be between " + JavaChatMessage.MSG_TYPE_RANGE[0] + " and " + JavaChatMessage.MSG_TYPE_RANGE[1]);
		}
		if(subMessageType < JavaChatMessage.MSG_TYPE_RANGE[0] 
				|| messageType > JavaChatMessage.MSG_TYPE_RANGE[1]) {
			throw new IllegalArgumentException(
					"Invalid sub-message type, must be between " + JavaChatMessage.MSG_TYPE_RANGE[0] + " and MSG_TYPE_RANGE[1]");
		}
		
		byte[] byteMessage = null;
		
		byte[] byteMessageType = ByteBuffer.allocate(MSG_INT_SIZE).putInt(messageType).array();
    	byte[] byteSubMessageType = ByteBuffer.allocate(MSG_INT_SIZE).putInt(subMessageType).array();
    	byte[] byteMessageData = messageData.getBytes();
    	byte[] byteMessageSize = ByteBuffer.allocate(MSG_INT_SIZE).putInt(byteMessageData.length).array();
		
    	byteMessage = new byte[MSG_INT_SIZE * 3 + byteMessageData.length];
    	System.arraycopy(byteMessageType, 0, byteMessage, 0, MSG_INT_SIZE);
    	System.arraycopy(byteSubMessageType, 0, byteMessage, MSG_INT_SIZE, MSG_INT_SIZE);
    	System.arraycopy(byteMessageSize, 0, byteMessage, MSG_INT_SIZE * 2, MSG_INT_SIZE);
    	System.arraycopy(byteMessageData, 0, byteMessage, MSG_INT_SIZE * 3, byteMessageData.length);
    	
		return byteMessage;
	}
	
	public static int bytesToInt(byte[] byteArray) throws IllegalArgumentException {
		// first check size of byteArray
		if(byteArray.length != MSG_INT_SIZE) {
			throw new IllegalArgumentException("Array length must be " + MSG_INT_SIZE);
		}
		int messageInt = ByteBuffer.wrap(byteArray).getInt();
		return messageInt;
	}
	
	public static String bytesToString(byte[] byteArray) {
		return new String(byteArray);
	}
	
	// Accessor method for the type identifier of a JavaChatMessage
	public int getMessageType() {
		return messageType;
	}
	
	// Accessor method for the subtype identifier of a JavaChatMessage
	public int getSubMessageType() {
		return subMessageType;
	}
	
	public String getMessageData() {
		return messageData;
	}

}
