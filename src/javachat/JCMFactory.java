package javachat;

import javachat.*;

public class JCMFactory {
	
	
	/* this should be changed to an enum
	final static int CMD_EXIT = 0;
	final static int CMD_BADLY_FORMATTED_MSG = 1;
	final static int CMD_ECHO = 2;
	final static int CMD_LOGIN = 3;
	final static int CMD_LOGOFF = 4;
	final static int CMD_CREATE_USR = 5;
	final static int CMD_DELETE_USR = 6;
	final static int CMD_CREATE_STORE = 7;
	final static int CMD_SEND_MSG = 8;
	final static int CMD_QUERY_MSG = 9;*/
	
	/*public JCMFactory(String username, String password) {
		this.username = username;
		this.password = password;
	}*/
	
	/* 
	 * If we have username/pass in the constructor we'll need to instantiate a new JCMfactory
	 * for each user. Not very good practice IMO... 
	 */
	public JCMFactory() {}
	
	//TODO update function signatures to take 'User' objects instead of user/pass Strings
	
	public JavaChatMessage exit() {
		return new JavaChatMessage( Commands.EXIT.getId(), " " );
	}
	
	public JavaChatMessage echo(String messageData) {
		return new JavaChatMessage (Commands.ECHO.getId(), messageData );
	}
	
	public JavaChatMessage login(String user, String pass) {
		return new JavaChatMessage( Commands.LOGIN.getId(), formatArgs(user, pass) );
	}
	
	public JavaChatMessage logoff() {
		return new JavaChatMessage( Commands.LOGOFF.getId(), " ");
	}
	
	public JavaChatMessage createUser(String user, String pass) {
		return new JavaChatMessage( Commands.CREATE_USER.getId(), formatArgs(user, pass) );
	}
	
	public JavaChatMessage deleteUser() {
		return new JavaChatMessage( Commands.DELETE_USER.getId(), " " );
	}
	
	public JavaChatMessage createStore() {
		return new JavaChatMessage( Commands.CREATE_STORE.getId(), " " );
	}
	
	public JavaChatMessage sendMessageToUser(String destUser, String msg) {
		return new JavaChatMessage( Commands.SEND_MSG.getId(), formatArgs(destUser, msg) );
	}
	
	public JavaChatMessage queryMessages() {
		return new JavaChatMessage( Commands.QUERY_MSG.getId(), " " );
	}
	
	// Concatenates two strings and separates them with a comma
	public String formatArgs(String arg0, String arg1) {
		return arg0 + "," + arg1;
	}
	
}
