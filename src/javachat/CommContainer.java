package javachat;

import java.util.LinkedList;

/**
 * used to store the message being sent and then the replies to that message
 */
public class CommContainer {
	public JavaChatMessage outMessage;
	public LinkedList<JavaChatMessage> replies;
	
	public CommContainer(JavaChatMessage outMessage) {
		this.outMessage = outMessage;
	}
}
