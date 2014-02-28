package javachat;

import java.nio.ByteBuffer;

public class ServerCommunication {
	final static int MSG_INT_SIZE = 4; // size of integer in bytes for our messages
	final static int[] MSG_TYPE_RANGE = {0,9}; // value range of message types (0 - 9)
	
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
		if(messageType < MSG_TYPE_RANGE[0] || messageType > MSG_TYPE_RANGE[1]) {
			throw new IllegalArgumentException(
					"Invalid message type, must be between " + MSG_TYPE_RANGE[0] + " and " + MSG_TYPE_RANGE[1]);
		}
		if(subMessageType < MSG_TYPE_RANGE[0] || messageType > MSG_TYPE_RANGE[1]) {
			throw new IllegalArgumentException(
					"Invalid sub-message type, must be between " + MSG_TYPE_RANGE[0] + " and MSG_TYPE_RANGE[1]");
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

}
