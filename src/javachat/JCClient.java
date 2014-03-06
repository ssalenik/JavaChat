package javachat;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.swing.*;

public class JCClient extends JFrame {
	
	private static ServerCommunication server;
	private static JCMFactory jcmf;
	String hostName = "dsp2014.ece.mcgill.ca";
    int portNumber = 5000;

	private static final long serialVersionUID = 5935284425769244825L;
	private final int WIDTH = 600;
	private final int HEIGHT = 600;
	
	private static JTextArea msg;
	private static JTextArea input;

	public JCClient() {
		
        initUI();
        initClient();
    }

    public final void initUI() {

        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        add(chatPanel);
        
        // Main text area (read-only)
        msg = new JTextArea(); 
        msg.setEditable(false);
        msg.setFont(new Font("Arial", Font.BOLD, 12));
        msg.setForeground(Color.BLACK);
        JScrollPane sp = new JScrollPane(msg);
        sp.setPreferredSize(new Dimension(WIDTH,570));        
        chatPanel.add(sp);     
        
        // User input text area
        input = new JTextArea();
        input.setFont(new Font("Arial", Font.BOLD, 16));
        input.getDocument().putProperty("filterNewlines", Boolean.TRUE); //enforce 1 single line of text
        
        input.addFocusListener(new FocusListener() {        	
        	@Override
        	public void focusGained(FocusEvent evt) {
                input.setText(null);
            }
        	
        	@Override
        	public void focusLost(FocusEvent evt) {}        	
        });
        
        input.addKeyListener(new KeyListener() {        	
        	@Override
        	// event handler for pressing the Enter key
        	public void keyPressed(KeyEvent evt) {
        		int key = evt.getKeyCode();
        		if (key == KeyEvent.VK_ENTER) {
        			parseUserInput(input.getText().trim());
        			input.setText(null);	// clear input box
        		}
        	}  
        	
        	@Override
        	public void keyTyped(KeyEvent evt) {}
        	
        	@Override
        	public void keyReleased(KeyEvent evt) {}
        });
        
        JScrollPane sp2 = new JScrollPane(input,
				        		JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        input.setPreferredSize(new Dimension(WIDTH,28));
        chatPanel.add(sp2); 

        setTitle("JavaChat");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
    
    public final void initClient() {
    	try {
	    	server = new ServerCommunication(hostName, portNumber);        	
	    	jcmf = new JCMFactory();
	    	
    	} catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        }
    }
    
	public static void parseUserInput(String input) {
		String cmd = input;
		String arg = "";
		if(input.contains(" ")){
			cmd = input.substring(0, input.indexOf(" ")); 	//extract the command from the user input
			arg = input.substring(input.indexOf(" "), input.length()); 
		}		
		writeToScreen("\n" + cmd + " " + arg);
		
		String[] args = arg.trim().split(" "); // split the argument into tokens
		
		// make the commands case-insensitive
		switch (cmd.toLowerCase()) {
			case "exit": 	if (arg != "") { 
								writeToScreen("> Error: No arguments required for \'EXIT\' command");
								break;
							}
							sendMessage( jcmf.exit() );
							System.exit(1);
							break;
							
			case "echo": 	if (arg == "") { 
								writeToScreen("> Usage: ECHO [message] ");
								break;
							}
							sendMessage( jcmf.echo(arg) );
							break;
							
			case "login":	if (args.length != 2) { 
								writeToScreen("> Usage: LOGIN [username] [password] ");
								break;
							}
							sendMessage( jcmf.login(args[0], args[1]) );
							break;
							
			case "logoff":	if (arg != "") { 
								writeToScreen("> Error: No arguments required for \'LOGOFF\' command");
								break;
							}
							sendMessage( jcmf.logoff() );
							break;
							
			case "add":		if (args.length != 2) { 
								writeToScreen("> Usage: ADD [username] [password] ");
								break;
							}
							sendMessage( jcmf.createUser(args[0], args[1]) );
							break;
			
			case "del":		if (arg != "") { 
								writeToScreen("> Error: No arguments required for \'DEL\' command");
								break;
							}
							sendMessage( jcmf.deleteUser() );
							break;
							
			case "store":	if (arg != "") { 
								writeToScreen("> Error: No arguments required for \'STORE\' command");
								break;
							}
							sendMessage( jcmf.createStore() );
							break;
							
			case "msg":		// concatenate all the tokens from index 1 onward to form the message string
							StringBuilder b = new StringBuilder();				
							for (int i=1; i<args.length; i++) {
							    if (b.length() > 0) {
							    	b.append(" ");
							    }
							    b.append(args[i]);
							}				
							String message = b.toString();
							System.out.println(message);
							
							sendMessage( jcmf.sendMessageToUser(args[0], message) );
							break;
							
			case "query":	if (arg != "") { 
								writeToScreen("> Error: No arguments required for \'QUERY\' command");
								break;
							}
							sendMessage( jcmf.queryMessages() );
							break;
			
			default: 		writeToScreen("> Error: Unknown command \'" + cmd + "\'");
							break;
		}
	}	

	public static void writeToScreen(String str) {
		msg.append(str + "\n");
	}
	
	// Sends a message to the server. Prints both the sent message and the reply(s) from the server.
	public static void sendMessage(JavaChatMessage outMessage) {
		
		try {      	
	    	System.out.println("sending message :\n" + outMessage);
	    	server.sendMessage(outMessage);	  //send message to server 
	    	
	    	JavaChatMessage inMessage = server.readMessage();	// read reply from server
	    	
	    	/* 
	    	 * Loop until the socket times out (returns a message with messageType -1). This loop 
	    	 * is necessary to account for commands which return more than one message (i.e. query_msg)
	    	 */
	    	while (inMessage.getMessageType() > 0) {
	    		System.out.println("got reply :\n" + inMessage); // print reply message
	    		handleReply(inMessage);
	    		inMessage = server.readMessage();	// read reply from server
	    	}
	    	System.out.println("");
	    	
		} catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection");
                System.exit(1);
        }
	}
	
	public static void handleReply(JavaChatMessage inMessage) {
		//maybe do some other stuff here later
		
		writeToScreen("> " + inMessage.getMessageData());
	}
	

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	JCClient ex = new JCClient();
                ex.setVisible(true);
            }
        });
    }
}
