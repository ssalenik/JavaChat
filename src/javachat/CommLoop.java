package javachat;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CommLoop implements Runnable {
	private ConcurrentLinkedQueue<JavaChatMessage> sendQueue;
	private ConcurrentLinkedQueue<CommContainer> receivedQueue;
	private Object mutex;
	private ServerCommunication server;
	private volatile boolean running = false;

	public CommLoop(ServerCommunication server) {
		this.server = server;
		this.sendQueue = new ConcurrentLinkedQueue<JavaChatMessage>();
		this.receivedQueue = new ConcurrentLinkedQueue<CommContainer>();
		this.mutex = new Object();
	}

	@Override
	public void run() {
		this.running = true;
		
		while(this.server.isConnected() && this.running == true) {
			// wait for stuff to be in the queue
			synchronized (mutex) {
				if (this.sendQueue.isEmpty() && this.running == true) {
					try {
						mutex.wait();
					} catch (InterruptedException e) {
						// do nothing
					}
				}
			}
			// now send message to server
			JavaChatMessage outMessage = this.sendQueue.poll();
			
			if( outMessage != null) {
				CommContainer msgContainer = new CommContainer(outMessage);
				msgContainer.replies = sendAndReceive(msgContainer.outMessage);
				// now put container into received queue
				this.receivedQueue.add(msgContainer);
			}
		}
	}
	
	public LinkedList<JavaChatMessage> sendAndReceive(JavaChatMessage outMessage) {
		LinkedList<JavaChatMessage> currentMessageReplies = null;

		int sentType = outMessage.getMessageType();
		boolean sameReplyTypeReceived = false;
		
		try {
			System.out.println("sending message :\n" + outMessage);
			server.sendMessage(outMessage);
			
			/* 
	    	 * Loop until at least one message of the same as the sent type is
	    	 * received or else a badly formatted message
	    	 * and no more are available or the socket times out.
	    	 * Store replies in the order they are received.
	    	 */
			currentMessageReplies = new LinkedList<JavaChatMessage>();
			
			/*
			 * server.messageAvailable never actually returns True, so what is its purpose?
			 */
			while ( sameReplyTypeReceived == false || server.messageAvailable() ) {
				if (sentType == Commands.EXIT.getId()) {
					break;	//Don't wait for a reply for "exit" messages
				}
				
				// read reply from server
				JavaChatMessage inMessage = server.readMessage();
				System.out.println("got reply :\n" + inMessage);
				currentMessageReplies.add(inMessage);
				
				// accept same message type or else badly formatted message type
				if (inMessage.getMessageType() == sentType 
						|| inMessage.getMessageType() == Commands.BADLY_FORMATTED_MSG.getId()) {
					sameReplyTypeReceived = true;
				}
				try {
					// sleep to wait for messages
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// sleep interrupted, this is OK
				}
			}
			
		} catch (IOException e) {			
		}

		return currentMessageReplies; 
		
	}

	public void sendMessage(JavaChatMessage outMessage) {
		sendQueue.add(outMessage);

		// notify thread
		synchronized (mutex) {
			mutex.notify();
		}
	}

	public boolean replyAvailable() {
		return !receivedQueue.isEmpty();
	}

	public CommContainer getReply() {
		return receivedQueue.poll();
	}
	
	/**
	 * stops the thread
	 */
	public void stop() {
		this.running = false;
		// notify the thread to stop
		synchronized (mutex) {
			mutex.notify();
		}
	}

}
