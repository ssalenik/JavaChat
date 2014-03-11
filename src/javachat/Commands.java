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
	CREAT_USER_AND_STORE(10, "new_user"),
	HELP(11, "help");
	
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