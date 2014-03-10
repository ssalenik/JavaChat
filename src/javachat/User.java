package javachat;

public class User {
	
	private String username;
	private String password;

	/**
	 * @param args
	 */
	public User(String username, String password) {
		this.username = username;
		this.password = password;		
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setUser(String newUsername, String newPassword) {
		username = newUsername;
		password = newPassword;
	}

}
