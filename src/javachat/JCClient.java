package javachat;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;

import utils.WrappingHTMLEditorKit;

public class JCClient extends JFrame {
	
	private ServerCommunication server;
	private JCMFactory jcmf;
	private Thread commThread;
	private CommLoop commLoop;
	private User currentUser;
	
	String hostName = "localhost";
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
	
	private JFileChooser filechooser;
	private JCFileSender filesender;
	
	/* this is used to make sure there is some white space at the bottom
	 * of the output text pane for legibility
	 */
	public static final String WHITE_SPACE_PADDING = "\n\n\n\n\n\n\n";

	public JCClient() {
		
        initUI();
        initClient();
        
        // shutdown hook to exit client cleanly on SIGTERM
        Runtime.getRuntime().addShutdownHook(new Thread(new JCClientShutdown(this)));
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
        
        // give input focus on start
        this.addWindowListener( new WindowAdapter() {
            public void windowOpened( WindowEvent e ){
                input.requestFocus();
            }
        }); 

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
    	filechooser = new JFileChooser();
    	filesender = new JCFileSender();
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
    
    public void exitServer() {
    	commLoop.sendMessage( jcmf.exit() );
    }
    
    public void cleanupCommThread() {
    	// first stop server comm thread
    	System.out.println("stopping comm loop");
    	commLoop.stop();
    	try {
			commThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	// now close the sockets
    	System.out.println("closing sockets");
    	try {
			server.closeSocket();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public final void exitClient() {
    	cleanupCommThread();
    	System.out.println("exiting JC");
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
			if (arg.equals("")) {
				writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
				writeErrorToScreen(sanitize("> Usage: @[username] [message] "));
				return;
			} else if (username.contains(",")) {
				writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
				writeErrorToScreen(sanitize("> Usernames may not contain commas! "));
				return;
			}
			if(currentUser.getUsername() != null && currentUser.getPassword() != null) {
				// logged in, send message
				writeOutMessageToScreen(command, arg);
				commLoop.sendMessage( jcmf.sendMessageToUser(username, arg) );
			} else {
				writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
				commLoop.sendMessage( jcmf.sendMessageToUser(username, arg) );
			}
			
			
			
		}
		
		else if ( command.equals(Commands.EXIT.getText()) ) {
			// write entered command to screen
			writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
			if (arg != "") { 
				writeErrorToScreen(sanitize("> Error: No arguments required for \'EXIT\' command"));
				return;
			}
			commLoop.sendMessage( jcmf.exit() );
			exitClient();
		}
		
		else if ( command.equals(Commands.ECHO.getText()) ) {
			// write entered command to screen
			writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
			if (arg == "") { 
				writeErrorToScreen(sanitize("> Usage: ECHO [message] "));
				return;
			}
			commLoop.sendMessage( jcmf.echo(arg_tokens[0]) );
		}

		else if ( command.equals(Commands.LOGIN.getText()) ) {
			// write entered command to screen
			writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
			if (arg_tokens.length != 2) { 
				writeErrorToScreen(sanitize("> Usage: LOGIN [username] [password] "));
				return;
			} else if (arg_tokens[0].contains(",")) {
				writeErrorToScreen(sanitize("> Usernames may not contain commas! "));
				return;
			}
			currentUser.setUser(arg_tokens[0], arg_tokens[1]); // set the current user's info
			commLoop.sendMessage( jcmf.login(arg_tokens[0], arg_tokens[1]) );
		}
		
		else if ( command.equals(Commands.LOGOFF.getText()) ) {
			// write entered command to screen
			writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
			if (arg != "") { 
				writeErrorToScreen(sanitize("> Error: No arguments required for \'LOGOFF\' command"));
				return;
			}
			commLoop.sendMessage( jcmf.logoff() );
		}
		
		else if ( command.equals(Commands.CREATE_USER.getText()) ) {
			// write entered command to screen
			writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
			if (arg_tokens.length != 2) { 
				writeErrorToScreen(sanitize("> Usage: ADD [username] [password] "));
				return;
			} else if (arg_tokens[0].contains(",")) {
				writeErrorToScreen(sanitize("> Usernames may not contain commas! "));
				return;
			}

//			currentUser.setUser(arg_tokens[0], arg_tokens[1]); // set the current user's info
			commLoop.sendMessage( jcmf.createUser(arg_tokens[0], arg_tokens[1]) );
		}
		
		else if ( command.equals(Commands.CREATE_USER_AND_STORE.getText()) ) {
			// write entered command to screen
			writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
			if (arg_tokens.length != 2) { 
				writeErrorToScreen(sanitize("> Usage: ADD [username] [password] "));
				return;
			} else if (arg_tokens[0].contains(",")) {
				writeErrorToScreen(sanitize("> Usernames may not contain commas! "));
				return;
			}

			currentUser.setUser(arg_tokens[0], arg_tokens[1]); // set the current user's info
			commLoop.sendMessage( jcmf.createUser(arg_tokens[0], arg_tokens[1]) );
		}
		
		else if ( command.equals(Commands.DELETE_USER.getText()) ) {
			// write entered command to screen
			writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
			if (arg != "") { 
				writeErrorToScreen(sanitize("> Error: No arguments required for \'DEL\' command"));
				return;
			}
			commLoop.sendMessage( jcmf.deleteUser() );
		}
		
		else if ( command.equals(Commands.CREATE_STORE.getText()) ) {
			// write entered command to screen
			writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
			if (arg != "") { 
				writeErrorToScreen(sanitize("> Error: No arguments required for \'STORE\' command"));
				return;
			}
			commLoop.sendMessage( jcmf.createStore() );
		}
		
		else if ( command.equals(Commands.SEND_MSG.getText()) ) {
			// old command to send message
			if (arg_tokens.length < 2) {
				// write entered command to screen
				writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
				writeErrorToScreen(sanitize("> Usage: MSG [username] [message] "));
				return;
			} else if (arg_tokens[0].contains(",")) {
				writeErrorToScreen(sanitize("> Usernames maynot contain commas! "));
				return;
			}
			writeOutMessageToScreen(command, arg_tokens[1]);
			commLoop.sendMessage( jcmf.sendMessageToUser(arg_tokens[0], arg_tokens[1]) );
		}
		
		else if ( command.equals(Commands.QUERY_MSG.getText()) ) {
			// write entered command to screen
			writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
			if (arg != "") { 
				writeErrorToScreen(sanitize("> Error: No arguments required for \'QUERY\' command"));
				return;
			}
			commLoop.sendMessage( jcmf.queryMessages() );
		} else if ( command.equals(Commands.REQUEST_SEND_FILE.getText()) ) {
			// write entered command to screen
			writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
			if (arg_tokens.length != 1) { 
				writeErrorToScreen(sanitize("> Usage: SEND_FILE [username] "));
				return;
			} else if (arg_tokens[0].equals("")) {
				writeErrorToScreen(sanitize("> Usage: SEND_FILE [username] "));
				return;
			} else if (arg_tokens[0].contains(",")) {
				writeErrorToScreen(sanitize("> Usernames maynot contain commas! "));
				return;
			}
			
			// check that another send isn't in progress
			if(filesender.isSendInProgress()) {
				writeErrorToScreen(sanitize("> Another send is in progress! "));
				return;
			}
			
			// open file chooser dialog
			int returnVal = filechooser.showOpenDialog(this);
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File file = filechooser.getSelectedFile();
	            String filename = file.getName();
	            long filesize = file.length();
	            String filesize_str = Long.toString(filesize);
	            writeLineToScreen(makeBold(sanitize("Attmepting to send \'" + filename + "\' of size " + filesize_str + " to " + arg_tokens[0])));
	            // make sure the file sender is OK with it
	            if(filesender.startFileSend(file)) {
	            	// do the actual send
	            	commLoop.sendMessage( jcmf.requestFileSend(arg_tokens[0], filesize_str, filename));
	            } else {
	            	writeErrorToScreen(sanitize("> Client-side error starting file send. "));
					return;
	            }
	        } else {
	        	// file selection canceled
	        	writeLineToScreen(makeBold(sanitize("File send canceled.")));
	        	return;
	        }
			
			
		}
		
		else if ( command.equals(Commands.HELP.getText()) ) {
			// write entered command to screen
			writeLineToScreen(makeBold(sanitize(cmd) + " " + sanitize(arg)));
			// write commands
			writeLineToScreen(" ");
			writeLineToScreen("USER COMMANDS");
			writeLineToScreen(makeBold(Commands.LOGIN.getText() + " [username] [password] : ") + "logs in to the server with the username and password provided" );
			writeLineToScreen(makeBold(Commands.CREATE_USER_AND_STORE.getText() + " [username] [password] : ") + "creates a new user with given credentials and logs in ready to chat" );
			writeLineToScreen(makeBold("@[recipient] [msg] : ") + "sends a message to the given recipient" );
			writeLineToScreen(makeBold(Commands.LOGOFF.getText() + " : ") + "logs the current user out" );
			writeLineToScreen(makeBold(Commands.EXIT.getText() + " : ") + "disconnect from the server and exit the program" );
			writeLineToScreen(" ");
			writeLineToScreen("DEBUG COMMANDS");
			writeLineToScreen(makeBold(Commands.ECHO.getText() + " : ") + "sends a message to the server, which echoes it back" );
			writeLineToScreen(makeBold(Commands.QUERY_MSG.getText() + " : ") + "query the server for messages" );
			writeLineToScreen(makeBold(Commands.CREATE_USER.getText() + " [username] [password] : ") + "creates a new user with the username and password provided" );
			writeLineToScreen(makeBold(Commands.CREATE_STORE.getText() + " : ") + "creates a message store for the current user (do this only once per account)" );
			writeLineToScreen(makeBold(Commands.DELETE_USER.getText() + " : ") + "deletes the account of the current user" );			
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
	public static String makePurple(String str) {
		return makeColor(str, "#CC00CC");
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
	
	public void sendMessage(JavaChatMessage outMessage) {
		commLoop.sendMessage(outMessage);
	}
	
	/**
	 * Formats received message and writes to output
	 * @param str
	 */
	public void writeInMessageToScreen(String from, String time, String message) {
		// write as received message
		writeToScreen("<div align=\"right\" style=\"background-color:#F0F0F0;\"><p>"
				+ makeBold(makePurple(sanitize(from)) + ", " + sanitize(time)) + "</p>"
				+ makeBold(makeBlue(sanitize(message))) + "</p></div>");
	}
	/**
	 * Formats sent message and writes to output
	 * @param str
	 */
	public void writeOutMessageToScreen(String user, String message) {
		// write in format of message to screen
		writeLineToScreen(makeBold(makePurple(sanitize(user))));
		writeLineToScreen(makeBold(makeGreen(sanitize(message))));
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
		writeToScreen("<p>" + str + "</p>");
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
	
	public void handleReply(CommContainer msgContainer) {
		
		// check if the messages succeeded based on replies
		boolean success = checkSuccessReply(msgContainer);
		
		// take action accordingly
		int sentType = msgContainer.outMessage.getMessageType();
		final Commands[] vals = Commands.values();
		switch (vals[sentType]) {
		case LOGIN:
			if(success){
				queryTimer.start(); // successful login, start query timer
			} else {
				currentUser.setUser(null, null); // failed login, reset current user info
			}
			break;
		case LOGOFF:
			if(success){
				currentUser.setUser(null, null); // reset current user info on successful logoff
				queryTimer.stop(); // stop querry timer
			}
			break;
		case CREATE_USER:
			if(success){
				if(currentUser.getUsername() != null && currentUser.getPassword() != null ) {
					// in this case the command was 'new_user'
					// do the rest of the account creation steps
					setUpUserAccount(currentUser);
				}
			} else {
				// failed account creation, reset current user info
				currentUser.setUser(null, null);
			}
			break;
		case CREATE_STORE:
			// nothing to do
			break;
		case SEND_MSG:
			// nothing to do
			break;
		case QUERY_MSG:
			// nothing to do
			break;
		case DELETE_USER:
			if(success) {
				currentUser.setUser(null, null);
				queryTimer.stop();
			}
			break;
		case REQUEST_SEND_FILE:
			
			if(success) {
				System.out.println("request send file success response");
				// check that file send is in progress
				if(filesender.isSendInProgress()) {
					// send the first chunk
					System.out.println("sending file chunk");
					commLoop.sendMessage(jcmf.sendFileChunk(filesender.getNextChunk()));
				} else {
					// send is not in progress, error?
					System.err.println("Server accepted file transfer but send is not in progress");
				}
			} else {
				// rejected file send, so cancel it client side
				System.out.println("request send file BAD response");
				filesender.cancelSend();
			}
			break;
		case SEND_FILE_CHUNK:
			if(success) {
				// check if the send is complete
				if(filesender.isSendComplete()) {
					writeLineToScreen(sanitize("> File transfer complete."));
				} else if(filesender.isSendInProgress()) {
					// send another chunk
					commLoop.sendMessage(jcmf.sendFileChunk(filesender.getNextChunk()));
				} else {
					// send is not in progress, error?
					System.err.println("Server accepted file transfer but send is not in progress");
				}
			} else {
				// rejected file send, so cancel it client side
				filesender.cancelSend();
				writeErrorToScreen(sanitize("> File transfer canceled."));
			}
			break;
		default:
			// nothing to do
			break;
		}
	}
	
	/**
	 * Checks if the sent message had a successful reply from the server
	 * indicating that the action was performed.
	 *  
	 * @return true if reply indicated success; false otherwise
	 */
	private boolean checkSuccessReply(CommContainer msgContainer) {
		boolean success = false;
		// get the sent message type
		int sentType = msgContainer.outMessage.getMessageType();
		
		try {
			// loop through replies for success
			for (JavaChatMessage inMessage : msgContainer.replies) {
				int type = inMessage.getMessageType();
				int subType = inMessage.getSubMessageType();		
				
				if (sentType == Commands.QUERY_MSG.getId()) {
					if(type == sentType) {
						// handle query messages differently because sub type of 1 means there is a message
						switch(subType) {
						case 0:// no message, do nothing
							success = true;
							break;
						case 1:// received message
							success = true;
							// split message on commas, into at most 3
							String [] messageData = inMessage.messageData.split(",", 3);
							// make sure the message makes sense
							if (messageData.length == 3) {
								writeInMessageToScreen(messageData[0], messageData[1], messageData[2]);
							} else {
								// not expected
								// simply write the message data
								writeLineToScreen(sanitize("> " + inMessage.getMessageData()));
							}
							break;
						case 2:// not logged in
							success = true;
							currentUser.setUser(null, null); // make sure no user is logged in
							queryTimer.stop(); // stop querying the server
							break;
						}
					}
				} else {
					if (type == sentType && subType == 0) {
						success = true;
						// print message as success
						writeLineToScreen(sanitize("> " + inMessage.getMessageData()));
					}
				}
			}
		 
		
			// print errors if not successful, ignore unrelated messages
			if(!success){
				// print all relevant received messages as error
				for (JavaChatMessage inMessage : msgContainer.replies) {
					int type = inMessage.getMessageType();
					// we want errors of the same type, or badly formated msg type
					// we assume all other messages are server errors
					if(type == sentType || type == Commands.BADLY_FORMATTED_MSG.getId()) {
						writeErrorToScreen(sanitize("> " + inMessage.getMessageData()));
					}
				}
			}
		}catch (NullPointerException e) {
		}
		return success;
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
