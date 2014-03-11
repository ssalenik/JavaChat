package test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javachat.*;

public class TestClient {	
	
	private static ServerCommunication server;
	private static JCMFactory jcmf;
	private static CommLoop commLoop;
	private LinkedList<JavaChatMessage> serverResponse;
	private JavaChatMessage reply;
	
	private static String hostName = "dsp2014.ece.mcgill.ca";
    private static int portNumber = 5000;
    private String testUser = "testuser12345";
    private String testPass = "test";
    private String testMsg = "this is a test message";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try {
	    	server = new ServerCommunication(hostName, portNumber); 	    	
    	} catch (UnknownHostException e) {
        } catch (IOException e) {
        }     	
    	jcmf = new JCMFactory();
    	commLoop = new CommLoop(server);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
		//cleanup
		commLoop.sendAndReceive( jcmf.login(testUser, testPass) );
		commLoop.sendAndReceive( jcmf.deleteUser() );
		Thread.sleep(250);
	}
	
	
	@Test
	public void UserCreation_NormalCase() {
		serverResponse = commLoop.sendAndReceive( jcmf.createUser(testUser, testPass) );
		reply = serverResponse.getFirst();
		assertTrue(reply.getMessageType() == 5 && reply.getSubMessageType() == 0);
		
	}
	
	@Test
	public void UserCreation_AlreadyExists() {
		commLoop.sendAndReceive( jcmf.createUser(testUser, testPass) );
		serverResponse = commLoop.sendAndReceive( jcmf.createUser(testUser, testPass) );		
		reply = serverResponse.getFirst();
		System.out.println(reply.getSubMessageType());
		assertTrue(reply.getMessageType() == 5 && reply.getSubMessageType() == 1);
	}
	
	@Test
	public void UserCreation_AlreadyLoggedIn() {
		commLoop.sendAndReceive( jcmf.createUser(testUser, testPass) );
		commLoop.sendAndReceive( jcmf.login(testUser, testPass) );		
		serverResponse = commLoop.sendAndReceive( jcmf.createUser(testUser, testPass) );		
		reply = serverResponse.getFirst();
		assertTrue(reply.getMessageType() == 5 && reply.getSubMessageType() == 2);
	}
	
	@Test
	public void UserCreation_IllegalChars() {
		//commLoop.sendAndReceive( jcmf.createUser(testUser + ",", testPass) );		
		assertTrue(1 == 1); //TODO
	}
	
	@Test
	public void Login_NormalCase() {
		commLoop.sendAndReceive( jcmf.createUser(testUser, testPass) );
		serverResponse = commLoop.sendAndReceive( jcmf.login(testUser, testPass) );		
		reply = serverResponse.getFirst();
		assertTrue(reply.getMessageType() == 3 && reply.getSubMessageType() == 0);
	}
	
	@Test
	public void Login_AlreadyLoggedIn() {
		commLoop.sendAndReceive( jcmf.createUser(testUser, testPass) );
		commLoop.sendAndReceive( jcmf.login(testUser, testPass) );
		serverResponse = commLoop.sendAndReceive( jcmf.login(testUser, testPass) );		
		reply = serverResponse.getFirst();
		assertTrue(reply.getMessageType() == 3 && reply.getSubMessageType() == 1);
	}
	
	@Test
	public void Login_UsernameIncorrect() {
		commLoop.sendAndReceive( jcmf.createUser(testUser, testPass) );
		serverResponse = commLoop.sendAndReceive( jcmf.login(testUser + "1", testPass) );		
		reply = serverResponse.getFirst();
		assertTrue(reply.getMessageType() == 3 && reply.getSubMessageType() == 2);
	}
	
	@Test
	public void Login_PasswordIncorrect() {
		commLoop.sendAndReceive( jcmf.createUser(testUser, testPass) );
		serverResponse = commLoop.sendAndReceive( jcmf.login(testUser, testPass + "1") );		
		reply = serverResponse.getFirst();
		assertTrue(reply.getMessageType() == 3 && reply.getSubMessageType() == 2);
	}
	
	@Test
	public void Logoff_NormalCase() {
		commLoop.sendAndReceive( jcmf.createUser(testUser, testPass) );	
		commLoop.sendAndReceive( jcmf.login(testUser, testPass) );
		serverResponse = commLoop.sendAndReceive( jcmf.logoff() );	
		reply = serverResponse.getFirst();
		assertTrue(reply.getMessageType() == 4 && reply.getSubMessageType() == 2);
	}
	
	
	@Test
	public void Logoff_NoLogin() {
		commLoop.sendAndReceive( jcmf.createUser(testUser, testPass) );		
		serverResponse = commLoop.sendAndReceive( jcmf.logoff() );	
		reply = serverResponse.getFirst();
		assertTrue(reply.getMessageType() == 4 && reply.getSubMessageType() == 1);
	}
	
	@Test
	public void QueryMsg_NoMessages() {
		commLoop.sendAndReceive( jcmf.createUser(testUser, testPass) );
		commLoop.sendAndReceive( jcmf.login(testUser, testPass) );
		serverResponse = commLoop.sendAndReceive( jcmf.queryMessages() );		
		reply = serverResponse.getFirst();
		assertTrue(reply.getMessageType() == 9 && reply.getSubMessageType() == 0);
	}
	
	@Test
	public void QueryMsg_MessagesPresent() {
		commLoop.sendAndReceive( jcmf.createUser(testUser, testPass) );
		commLoop.sendAndReceive( jcmf.login(testUser, testPass) );
		commLoop.sendAndReceive( jcmf.createStore() );
		commLoop.sendAndReceive( jcmf.sendMessageToUser(testUser, testMsg) );				
		serverResponse = commLoop.sendAndReceive( jcmf.queryMessages() );		
		reply = serverResponse.getFirst();
		assertTrue(reply.getMessageType() == 9 && reply.getSubMessageType() == 1);	// 1 = message received
	}
	
	@Test
	// the server returns 2 in this case instead of 1 for some reason
	public void SendMsg_NoUserStore() {
		commLoop.sendAndReceive( jcmf.createUser(testUser, testPass) );
		commLoop.sendAndReceive( jcmf.login(testUser, testPass) );
		serverResponse = commLoop.sendAndReceive( jcmf.sendMessageToUser(testUser, testMsg) );		
		reply = serverResponse.getFirst();
		assertTrue(reply.getMessageType() == 8 && reply.getSubMessageType() == 1);
	}
	
	@Test
	public void SendMsg_BadUsername() {
		commLoop.sendAndReceive( jcmf.createUser(testUser, testPass) );
		commLoop.sendAndReceive( jcmf.login(testUser, testPass) );
		serverResponse = commLoop.sendAndReceive( jcmf.sendMessageToUser(testUser + "1", testMsg) );		
		reply = serverResponse.getFirst();
		assertTrue(reply.getMessageType() == 8 && reply.getSubMessageType() == 2);
	}
	
	@Test
	public void SendMsg_NoLogin() {
		commLoop.sendAndReceive( jcmf.createUser(testUser, testPass) );
		serverResponse = commLoop.sendAndReceive( jcmf.sendMessageToUser(testUser, testMsg) );		
		reply = serverResponse.getFirst();
		assertTrue(reply.getMessageType() == 8 && reply.getSubMessageType() == 3);
	}
	
	@Test
	public void DeleteUser_NoLogin() {
		commLoop.sendAndReceive( jcmf.createUser(testUser, testPass) );
		serverResponse = commLoop.sendAndReceive( jcmf.deleteUser() );		
		reply = serverResponse.getFirst();
		assertTrue(reply.getMessageType() == 6 && reply.getSubMessageType() == 1);
	}

}
