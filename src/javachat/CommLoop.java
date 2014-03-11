package javachat;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CommLoop implements Runnable {
	private ConcurrentLinkedQueue<JavaChatMessage> sendQueue;
	private ConcurrentLinkedQueue<JavaChatMessage> receivedQueue;
	private Object mutex;
	private ServerCommunication server;
	private boolean running = false;

	public CommLoop(ServerCommunication server) {
		this.server = server;
		this.sendQueue = new ConcurrentLinkedQueue<JavaChatMessage>();
		this.receivedQueue = new ConcurrentLinkedQueue<JavaChatMessage>();
		this.mutex = new Object();
	}

	@Override
	public void run() {
		this.running = true;
		
		while(this.server.isConnected() && this.running == true) {
			// wait for stuff to be in the queue
			synchronized (mutex) {
				if (this.sendQueue.isEmpty()) {
					try {
						mutex.wait();
					} catch (InterruptedException e) {
						// do nothing
					}
				}
			}
			// now send message to server
			JavaChatMessage outMessage = this.sendQueue.poll();
				
			LinkedList<JavaChatMessage> currentMessageReplies = sendAndReceive(outMessage);
			// now put all received replies into received queue
			for( JavaChatMessage inMessage : currentMessageReplies) {
				this.receivedQueue.add(inMessage);
			}
		}
	}
	
	public LinkedList<JavaChatMessage> sendAndReceive(JavaChatMessage outMessage) {
		LinkedList<JavaChatMessage> currentMessageReplies = null;
		
		try {
			System.out.println("sending message :\n" + outMessage);
			server.sendMessage(outMessage);
			
			/* 
	    	 * Loop until at least one message is received
	    	 * and no more are available or the socket times out.
	    	 * Store replies in the order they are received.
	    	 */
			currentMessageReplies = new LinkedList<JavaChatMessage>();
			while ( server.messageAvailable() || currentMessageReplies.isEmpty() ) {
				// read reply from server
				JavaChatMessage inMessage = server.readMessage();	
				System.out.println("got reply :\n" + inMessage);
				currentMessageReplies.add(inMessage);
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

	public JavaChatMessage getReply() {
		return receivedQueue.poll();
	}
	
	/**
	 * stops the thread
	 */
	public void stop() {
		this.running = false;
	}

}
