package javachat;

import javachat.JavaChatMessage;

public class JCMFactory {
	
	// is this really better than declaring constants? ehhh
	public enum cmd {
		EXIT(0),
		BADLY_FORMATTED_MSG(1),
		ECHO(2),
		LOGIN(3),
		LOGOFF(4),
		CREATE_USER(5),
		DELETE_USER(6),
		CREATE_STORE(7),
		SEND_MSG(8),
		QUERY_MSG(9);
		
		private int id;
		private cmd(int id) {
			this.id = id;
		}	
	}
	
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
	
	public JavaChatMessage exit() {
		return new JavaChatMessage( cmd.EXIT.id, " " );
	}
	
	public JavaChatMessage echo(String messageData) {
		return new JavaChatMessage (cmd.ECHO.id, messageData );
	}
	
	public JavaChatMessage login(String user, String pass) {
		return new JavaChatMessage( cmd.LOGIN.id, formatArgs(user, pass) );
	}
	
	public JavaChatMessage logoff() {
		return new JavaChatMessage( cmd.LOGOFF.id, " ");
	}
	
	public JavaChatMessage createUser(String user, String pass) {
		return new JavaChatMessage( cmd.CREATE_USER.id, formatArgs(user, pass) );
	}
	
	public JavaChatMessage deleteUser() {
		return new JavaChatMessage( cmd.DELETE_USER.id, " " );
	}
	
	public JavaChatMessage createStore() {
		return new JavaChatMessage( cmd.CREATE_STORE.id, " " );
	}
	
	public JavaChatMessage sendMessageToUser(String destUser, String msg) {
		return new JavaChatMessage( cmd.SEND_MSG.id, formatArgs(destUser, msg) );
	}
	
	public JavaChatMessage queryMessages() {
		return new JavaChatMessage( cmd.QUERY_MSG.id, " " );
	}
	
	// Concatenates two strings and separates them with a comma
	public String formatArgs(String arg0, String arg1) {
		return arg0 + "," + arg1;
	}
	
}
