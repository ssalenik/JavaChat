package javachat;

public class JCMFactory {
	
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
