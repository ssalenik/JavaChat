package javachat;

import javachat.JavaChatMessage;

public class JCMFactory {
	String username;
	String password;
	
	// this should be changed to an enum
	final static int CMD_EXIT = 0;
	final static int CMD_BADLY_FORMATTED_MSG = 1;
	final static int CMD_ECHO = 2;
	final static int CMD_LOGIN = 3;
	final static int CMD_LOGOFF = 4;
	final static int CMD_CREATE_USR = 5;
	final static int CMD_DELETE_USR = 6;
	final static int CMD_CREATE_STORE = 7;
	final static int CMD_SEND_MSG = 8;
	final static int CMD_QUERRY_MSG = 9;
	
	public JCMFactory(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public JavaChatMessage exit() {
		return new JavaChatMessage(0, " ");
	}

}
