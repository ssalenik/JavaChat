package javachat;

public enum Commands {
	EXIT(0, "exit"),
	BADLY_FORMATTED_MSG(1, " "),
	ECHO(2, "echo"),
	LOGIN(3, "login"),
	LOGOFF(4, "logoff"),
	CREATE_USER(5, "add"),
	DELETE_USER(6, "del"),
	CREATE_STORE(7, "store"),
	SEND_MSG(8, "msg"),
	QUERY_MSG(9, "query"),
	REQUEST_SEND_FILE(10, "send_file"),
	SEND_FILE_CHUNK(11, "send"),
	CANCEL_SEND_FILE(12, "cancel_send"),
	REQUEST_RECEIVE_FILE(13, "receive_file"),
	RECEIVE_FILE_CHUNK(14, "receive"),
	CANCEL_RECEIVE_FILE(15, "cancel_receive"),
	REJECT_RECEIVE_FILE(16, "reject_file"),
	// helper commands, do not correspond to server commands
	CREATE_USER_AND_STORE(17, "new_user"),
	HELP(18, "help");
	
	public final int id;
	public final String text;
	private Commands(int id, String text) {
		this.id = id;
		this.text = text;
	}
	public int getId() {
		return id;
	}
	public String getText(){
		return text;
	}
}