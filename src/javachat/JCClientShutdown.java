package javachat;

/**
 * Run to cleanup the client before exit from a shutdown hook
 *
 */
public class JCClientShutdown implements Runnable {
	private JCClient client;
	
	public JCClientShutdown(JCClient client) {
		this.client = client;
	}

	@Override
	public void run() {
		// send exit command
		this.client.exitServer();
		// stop thread(s)
		this.client.cleanupCommThread();
	}
}
