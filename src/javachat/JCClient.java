package javachat;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.*;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import utils.WrappingHTMLEditorKit;
import javachat.*;

public class JCClient extends JFrame {
	
	private ServerCommunication server;
	private JCMFactory jcmf;
	private Thread commThread;
	private CommLoop commLoop;
	private User currentUser;
	
	String hostName = "dsp2014.ece.mcgill.ca";
    int portNumber = 5000;

	private static final long serialVersionUID = 5935284425769244825L;
	private final int WIDTH = 600;
	private final int HEIGHT = 600;
	
	private static JTextPane msg;
	private static HTMLDocument msgDocument;
	private static WrappingHTMLEditorKit msgKit;
	private static JTextArea input;
	private static Timer queryTimer;
	private static Timer replyTimer;
	
	/* this is used to make sure there is some white space at the bottom
	 * of the output text pane for legibility
	 */
	public static final String WHITE_SPACE_PADDING = "\n\n\n\n\n\n\n";

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
        msg = new JTextPane(); 
        msg.setEditable(false);
        msg.setMargin(new Insets(10, 5, 0, 5));
        JScrollPane sp = new JScrollPane(msg);
        sp.setPreferredSize(new Dimension(WIDTH,570));
        chatPanel.add(sp);
        
		// text area HTML doc
		msgKit = new WrappingHTMLEditorKit();
        msgDocument = new HTMLDocument();
        msg.setEditorKit(msgKit);
        msg.setDocument(msgDocument);
        
        // User input text area
        input = new JTextArea();
        input.setFont(new Font("Arial", Font.BOLD, 16));
        input.getDocument().putProperty("filterNewlines", Boolean.TRUE); //enforce 1 single line of text
        input.setMargin(new Insets(5, 5, 5, 5));
        input.setLineWrap(true);
        
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
	    	
    	} catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        }     	
    	jcmf = new JCMFactory();
    	currentUser = new User(null, null);
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
        		while(commLoop.replyAvailable()) {
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
		writeLineToScreen("<br>");
//		writeLineToScreen(sanitize(cmd) + " " + sanitize(arg));
		
		// split the argument into tokens
		// at most 2, since there are at most 2 arguments
		String[] arg_tokens = arg.trim().split(" ", 2);
		
		// make the commands case-insensitive
		String command = cmd.toLowerCase();	
		
		if ( command.charAt(0) == '@' ) {
			// message command
			String username = command.substring(1); //strip the '@'
			if (arg_tokens.length < 1) {
				// write entered command to screen
				writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
				writeErrorToScreen("> Usage: @[username] [message] ");
				return;
			}
			writeOutMessageToScreen(command, arg_tokens[0]);
			commLoop.sendMessage( jcmf.sendMessageToUser(username, arg_tokens[0]) );
			
		}
		
		else if ( command.equals(Commands.EXIT.getText()) ) {
			// write entered command to screen
			writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
			if (arg != "") { 
				writeErrorToScreen("> Error: No arguments required for \'EXIT\' command");
				return;
			}
			commLoop.sendMessage( jcmf.exit() );
			exitClient();
		}
		
		else if ( command.equals(Commands.ECHO.getText()) ) {
			// write entered command to screen
			writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
			if (arg == "") { 
				writeErrorToScreen("> Usage: ECHO [message] ");
				return;
			}
			commLoop.sendMessage( jcmf.echo(arg_tokens[0]) );
		}

		else if ( command.equals(Commands.LOGIN.getText()) ) {
			// write entered command to screen
			writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
			if (arg_tokens.length != 2) { 
				writeErrorToScreen("> Usage: LOGIN [username] [password] ");
				return;
			}
			currentUser.setUser(arg_tokens[0], arg_tokens[1]); // set the current user's info
			commLoop.sendMessage( jcmf.login(arg_tokens[0], arg_tokens[1]) );
		}
		
		else if ( command.equals(Commands.LOGOFF.getText()) ) {
			// write entered command to screen
			writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
			if (arg != "") { 
				writeErrorToScreen("> Error: No arguments required for \'LOGOFF\' command");
				return;
			}
			commLoop.sendMessage( jcmf.logoff() );
		}
		
		else if ( command.equals(Commands.CREATE_USER.getText()) ) {
			// write entered command to screen
			writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
			if (arg_tokens.length != 2) { 
				writeErrorToScreen("> Usage: ADD [username] [password] ");
				return;
			}

			currentUser.setUser(arg_tokens[0], arg_tokens[1]); // set the current user's info
			commLoop.sendMessage( jcmf.createUser(arg_tokens[0], arg_tokens[1]) );
		}
		
		else if ( command.equals(Commands.DELETE_USER.getText()) ) {
			// write entered command to screen
			writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
			if (arg != "") { 
				writeErrorToScreen("> Error: No arguments required for \'DEL\' command");
				return;
			}
			commLoop.sendMessage( jcmf.deleteUser() );
		}
		
		else if ( command.equals(Commands.CREATE_STORE.getText()) ) {
			// write entered command to screen
			writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
			if (arg != "") { 
				writeErrorToScreen("> Error: No arguments required for \'STORE\' command");
				return;
			}
			commLoop.sendMessage( jcmf.createStore() );
		}
		
		else if ( command.equals(Commands.SEND_MSG.getText()) ) {
			// old command to send message
			if (arg_tokens.length < 2) {
				// write entered command to screen
				writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
				writeErrorToScreen("> Usage: MSG [username] [message] ");
				return;
			}
			writeOutMessageToScreen(command, arg_tokens[1]);
			commLoop.sendMessage( jcmf.sendMessageToUser(arg_tokens[0], arg_tokens[1]) );
		}
		
		else if ( command.equals(Commands.QUERY_MSG.getText()) ) {
			// write entered command to screen
			writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
			if (arg != "") { 
				writeErrorToScreen("> Error: No arguments required for \'QUERY\' command");
				return;
			}
			commLoop.sendMessage( jcmf.queryMessages() );
		}
		
		else if ( command.equals(Commands.HELP.getText()) ) {
			// write entered command to screen
			writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
			// write commands
			writeLineToScreen("<br>AVAILABLE COMMANDS");
			writeLineToScreen(makeBold(Commands.EXIT.getText() + " : ") + "disconnect from the server and exit the program" );
			writeLineToScreen(makeBold(Commands.ECHO.getText() + " : ") + "sends a message to the server, which echoes it back" );
			writeLineToScreen(makeBold(Commands.LOGIN.getText() + " [username] [password] : ") + "logs in to the server with the username and password provided" );
			writeLineToScreen(makeBold(Commands.LOGOFF.getText() + " : ") + "logs the current user out" );
			writeLineToScreen(makeBold(Commands.CREATE_USER.getText() + " [username] [password] : ") + "creates a new user with the username and password provided" );
			writeLineToScreen(makeBold(Commands.DELETE_USER.getText() + " : ") + "deletes the account of the current user" );
			writeLineToScreen(makeBold(Commands.CREATE_STORE.getText() + " : ") + "creates a message store for the current user (do this only once per account)" );
			writeLineToScreen(makeBold(Commands.SEND_MSG.getText() + " [recipient] [msg] : ") + "sends a message to the given recipient" );
			writeLineToScreen(makeBold(Commands.QUERY_MSG.getText() + " : ") + "query the server for messages" );
		}
		
		else {
			// write entered command to screen
			writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
			writeErrorToScreen("> Error: Unknown command \'" + cmd + "\'");
		}
	}
	
	/**
	 * Escape HTML chars in the string
	 * @param str
	 * @return
	 */
	public static String sanitize(String str) {
		str = str.replace("&", "&amp;");
		str = str.replace("<", "&lt;");
		str = str.replace(">", "&gt;");
		str = str.replace("\"", "&quot;");
		str = str.replace("'", "&#39;");
		
		return str;
	}
	
	/**
	 * Surrounds input string with <div align="right"> tag
	 * @param str
	 * @return
	 */
	public static String alignRight(String str) {
		return "<div align=\"right\">" + str + "</div>";
	}
	public static String makeBold(String str) {
		return "<b>" + str + "</b>";
	}
	public static String makeRed(String str) {
		return makeColor(str, "red");
	}
	public static String makeBlue(String str) {
		return makeColor(str, "blue");
	}
	public static String makeGreen(String str) {
		return makeColor(str, "green");
	}
	public static String makeColor(String str, String HTMLColor) {
		return "<font color=\"" + HTMLColor + "\">" + str + "</font>";
	}
	/**
	 * Formats received message and writes to output
	 * @param str
	 */
	public void writeInMessageToScreen(String from, String time, String message) {
		// write as received message
		writeLineToScreen(alignRight(makeBold(
				makeBlue(sanitize(from))
				+ ", " + sanitize(time)
				+ "<br>" + makeBlue(sanitize(message))
				)));
	}
	/**
	 * Formats sent message and writes to output
	 * @param str
	 */
	public void writeOutMessageToScreen(String user, String message) {
		// write in format of message to screen
		writeLineToScreen(makeBold(
				makeBlue(sanitize(user)) 
				+ "<br>" + makeGreen(sanitize(message))
				));
	}
	public void writeErrorToScreen(String str) {
		writeToScreen(makeRed(str));
	}
	/**
	 * Writes the input string plus a new line to the output TextPane
	 * 
	 * @param str
	 */
	public void writeLineToScreen(String str) {
		writeToScreen(str + "<br>");
	}

	/**
	 * Writes to the end of the output TextPane.
	 * 
	 * @param str
	 */
	public void writeToScreen(String str) {
		try {
			// get the location of the end of the text
			int offset = msgDocument.getEndPosition().getOffset();
			
			// make sure there is some new lines at the bottom for legibility
			int whiteSpaceLen = WHITE_SPACE_PADDING.length();
			if(offset >= whiteSpaceLen) {
				if (msgDocument.getText(
						offset - whiteSpaceLen,
						whiteSpaceLen).equals(WHITE_SPACE_PADDING))
				{
					offset = offset - whiteSpaceLen;
				} else {
					msgDocument.insertString(offset, WHITE_SPACE_PADDING, null);
				}
			} else {
				msgDocument.insertString(offset, WHITE_SPACE_PADDING, null);
			}
			
//			msgDocument.insertString(offset, "\n", null);
			try {
				msgKit.insertHTML(msgDocument, offset + 1, str, 0, 0, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// now scroll to the end
			int end = msgDocument.getLength();
			msg.scrollRectToVisible(msg.modelToView(end));
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	public void setUpUserAccount(User user) {
		commLoop.sendMessage( jcmf.login(user.getUsername(), user.getPassword()) );
		commLoop.sendMessage( jcmf.createStore() );
	}	
	
	public void handleReply(JavaChatMessage inMessage) {
		
		
		int type = inMessage.getMessageType();
		int subType = inMessage.getSubMessageType();
				

		/* 
		 * maybe this stuff below could be broken up into separate functions for a cleaner look?
		 *  i.e. handleLogin(), handleLogoff(), etc.? 
		 */
		
		// handle login
		if (type == Commands.LOGIN.getId()) {
			if (subType == 0) {
				queryTimer.start(); // successful login, start query timer
			} else {
				currentUser.setUser(null, null); // failed login, reset current user info
			}
		}
		
		// handle logoff
		else if (type == Commands.LOGOFF.getId()) {
			currentUser.setUser(null, null); // reset current user info on successful logoff
			queryTimer.stop();	// stop query timer on logoff
		}
		
		// handle account creation
		else if (type == Commands.CREATE_USER.getId()) {
			if (subType == 0) {
				setUpUserAccount(currentUser);
			} else {
				currentUser.setUser(null, null); // failed account creation, reset current user info
			}
		}
		
		else if (type == Commands.SEND_MSG.getId()) {
			if (subType == 0) {
				return;	// return: don't print reply from server
			}
		}
		
		// handle server queries
		else if (type == Commands.QUERY_MSG.getId()) {
			// stop query timer if we get a 'Must login first' message
			if(inMessage.getMessageData().equals("Must login first")
					&& queryTimer.isRunning()) 
			{
				queryTimer.stop();
				return;	// return: don't print reply from server
				
			// ignore if we get a 'No messages available" message
			} else if( inMessage.getMessageData().equals("No messages available") ){
				return;	// return: don't print reply from server
				
			// print if message from user
			} else if (subType == 1) {
				// split message on commas, into at most 3
				String [] messageData = inMessage.messageData.split(",", 3);
				// make sure the message makes sense
				if (messageData.length == 3) {
					writeInMessageToScreen(messageData[0], messageData[1], messageData[2]);
				} else {
					// not expected
					// simply write the message data
					writeLineToScreen("> " + sanitize(inMessage.getMessageData()));
				}
				return; // return: don't print reply from server
			}
		}
		
		if (subType != 0 || type == Commands.BADLY_FORMATTED_MSG.getId()) {
			// A message other than "Success" was returned by the server 
			// This case applies to all messages EXCEPT query, where subType=1 means "there are messages"
			writeErrorToScreen("> " + sanitize(inMessage.getMessageData()));
			return;
		}
		
		else if (subType == 0) {
			writeLineToScreen("> " + sanitize(inMessage.getMessageData()));
			return;
		}
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
