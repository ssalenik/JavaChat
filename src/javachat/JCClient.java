package javachat;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import javachat.Commands;

public class JCClient extends JFrame {
	
	private ServerCommunication server;
	private JCMFactory jcmf;
	private Thread commThread;
	private CommLoop commLoop;
	String hostName = "dsp2014.ece.mcgill.ca";
    int portNumber = 5000;

	private static final long serialVersionUID = 5935284425769244825L;
	private final int WIDTH = 600;
	private final int HEIGHT = 600;
	
	private static JTextArea msg;
	private static JTextArea input;
	private static Timer queryTimer;
	private static Timer replyTimer;

	public JCClient() {
		
        initUI();
        initClient();
    }

    public final void initUI() {

        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        add(chatPanel);
        chatPanel.setBorder(new EmptyBorder(5, 5, 5, 5) );
        
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
                //input.setText(null);
            }
        	
        	@Override
        	public void focusLost(FocusEvent evt) {}        	
        });
        

    	// event handler for pressing the Enter key
        input.addKeyListener(new KeyListener() {        	
        	@Override
        	public void keyPressed(KeyEvent evt) {
        		int key = evt.getKeyCode();
        		if (key == KeyEvent.VK_ENTER) {
        			evt.consume(); // consume the '\n'
        			String userInput = input.getText().trim();
        			if(!userInput.equals("")) {
        				parseUserInput(userInput);
        				input.setText(null);	// clear input box
        			}
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
    	commLoop = new CommLoop(server);
    	commThread = new Thread(commLoop);
    	commThread.start();
    	
    	//Timer to query server
        ActionListener timerListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent evt) {
        		commLoop.sendMessage( jcmf.queryMessages() );
        	}
        };
        queryTimer = new Timer(1000, timerListener);
        
        // Timer to get replies from server
        ActionListener replyListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent evt) {
        		// get all available replies from the server
        		while(commLoop.replyAvailabe()) {
        			handleReply(commLoop.getReply());
        		}
        	}
        };
        replyTimer = new Timer(10, replyListener); // poll at 100Hz
        replyTimer.start();
    }
    
    public final void exitClient() {
    	// first stop server comm thread
    	commLoop.stop();
    	try {
			commThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	// now close the sockets
    	try {
			server.closeSocket();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	// finally exit
    	System.exit(0);
    }
    
	public void parseUserInput(String input) {
		String cmd = input;
		String arg = "";
		if(input.contains(" ")){
			cmd = input.substring(0, input.indexOf(" ")); 	//extract the command from the user input
			arg = input.substring(input.indexOf(" "), input.length());
		}		
		writeToScreen("\n" + cmd + " " + arg);
		
		String[] args = arg.trim().split(" "); // split the argument into tokens
		
		// make the commands case-insensitive
		String command = cmd.toLowerCase();	
		
		if ( command.equals(Commands.EXIT.getText()) ) {
			if (arg != "") { 
				writeToScreen("> Error: No arguments required for \'EXIT\' command");
				return;
			}
			commLoop.sendMessage( jcmf.exit() );
			exitClient();
		}
		
		else if ( command.equals(Commands.ECHO.getText()) ) {
			if (arg == "") { 
				writeToScreen("> Usage: ECHO [message] ");
				return;
			}
			commLoop.sendMessage( jcmf.echo(args[0]) );
		}

		else if ( command.equals(Commands.LOGIN.getText()) ) {
			if (args.length != 2) { 
				writeToScreen("> Usage: LOGIN [username] [password] ");
				return;
			}
			commLoop.sendMessage( jcmf.login(args[0], args[1]) );
		}
		
		else if ( command.equals(Commands.LOGOFF.getText()) ) {
			if (arg != "") { 
				writeToScreen("> Error: No arguments required for \'LOGOFF\' command");
				return;
			}
			commLoop.sendMessage( jcmf.logoff() );
		}
		
		else if ( command.equals(Commands.CREATE_USER.getText()) ) {
			if (args.length != 2) { 
				writeToScreen("> Usage: ADD [username] [password] ");
				return;
			}
			commLoop.sendMessage( jcmf.createUser(args[0], args[1]) );
		}
		
		else if ( command.equals(Commands.DELETE_USER.getText()) ) {
			if (arg != "") { 
				writeToScreen("> Error: No arguments required for \'DEL\' command");
				return;
			}
			commLoop.sendMessage( jcmf.deleteUser() );
		}
		
		else if ( command.equals(Commands.CREATE_STORE.getText()) ) {
			if (arg != "") { 
				writeToScreen("> Error: No arguments required for \'STORE\' command");
				return;
			}
			commLoop.sendMessage( jcmf.createStore() );
		}
		
		else if ( command.equals(Commands.SEND_MSG.getText()) ) {
			// concatenate all the tokens from index 1 onward to form the message string
			StringBuilder b = new StringBuilder();				
			for (int i=1; i<args.length; i++) {
			    if (b.length() > 0) {
			    	b.append(" ");
			    }
			    b.append(args[i]);
			}				
			String message = b.toString();
			System.out.println(message);
			
			commLoop.sendMessage( jcmf.sendMessageToUser(args[0], message) );
		}
		
		else if ( command.equals(Commands.QUERY_MSG.getText()) ) {
			if (arg != "") { 
				writeToScreen("> Error: No arguments required for \'QUERY\' command");
				return;
			}
			commLoop.sendMessage( jcmf.queryMessages() );
		}
		
		else if ( command.equals(Commands.HELP.getText()) ) {
			writeToScreen("\nAVAILABLE COMMANDS\n" +
					Commands.EXIT.getText() + " : disconnect from the server and exit the program\n" +
					Commands.ECHO.getText() + " [msg] : sends a message to the server, which echoes it back\n" +
					Commands.LOGIN.getText() + " [username] [password] : logs in to the server with the username and password provided\n" +
					Commands.LOGOFF.getText() + " : logs the current user out\n" +
					Commands.CREATE_USER.getText() + " [username] [password] : creates a new user with the username and password provided\n" +
					Commands.DELETE_USER.getText() + " : deletes the account of the current user\n" +
					Commands.CREATE_STORE.getText() + " : creates a message store for the current user (do this only once per account)\n" +
					Commands.SEND_MSG.getText() + " [recipient] [msg] : sends a message to the given recipient\n" +
					Commands.QUERY_MSG.getText() + " : manually query the server for messages\n" 
					);
		}
		
		else {
			writeToScreen("> Error: Unknown command \'" + cmd + "\'");
		}
	}	

	public void writeToScreen(String str) {
		msg.append(str + "\n");
	}
	
	// Sends a message to the server. Prints both the sent message and the reply(s) from the server.
//	public void sendMessage(JavaChatMessage outMessage) {		
//		try {      	
//	    	System.out.println("sending message :\n" + outMessage);
//	    	server.sendMessage(outMessage);	  //send message to server 
//	    		    	
//	    	/* 
//	    	 * Loop until at least one message is received and no more are available or the socket times out
//	    	 * Store replies in the order they are received.
//	    	 */
//	    	LinkedList<JavaChatMessage> replies = new LinkedList<JavaChatMessage>();
//	    	while ( server.messageAvailable() || replies.isEmpty() ) {
//	    		JavaChatMessage inMessage = server.readMessage();	// read reply from server
//	    		System.out.println("got reply :\n" + inMessage); // print reply message
//	    		replies.add(inMessage);
//	    		try {
//	    			Thread.sleep(100);
//	    		} catch (InterruptedException e) {
//	    			// sleep interrupted, this is OK
//	    		}
//	    	}
//	    	System.out.println("");
//	    	
//	    	// now handle the messages
//	    	for (JavaChatMessage inMessage : replies) {
//	    		handleReply(inMessage);
//	    	}
//	    	
//		} catch (IOException e) {
//            System.err.println("Couldn't get I/O for the connection");
//                System.exit(1);
//        }
//	}
	
	public void handleReply(JavaChatMessage inMessage) {
		int type = inMessage.getMessageType();
		int subType = inMessage.getSubMessageType();
		
		// login successful, start query timer
		if (type == Commands.LOGIN.getId() && subType == 0) {
			queryTimer.start();
		}
		
		else if (type == Commands.QUERY_MSG.getId()) {
			// stop query timer if we get a 'Must login first' message
			if(inMessage.getMessageData().equals("Must login first")
					&& queryTimer.isRunning()) 
			{
				queryTimer.stop();
				return;
			// ignore if we get a 'No messages available" message
			} else if( inMessage.getMessageData().equals("No messages available") ){
				return;
			}
		}
		
		// if query returns nothing, don't print
		else if (type == Commands.QUERY_MSG.getId() && subType == 0) {
			return;
		}
		
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
