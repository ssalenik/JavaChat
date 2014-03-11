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
	
	private ServerCommunication server;
	private JCMFactory jcmf;
	private CommLoop commLoop;
	private LinkedList<JavaChatMessage> serverReplies;
	
	String hostName = "dsp2014.ece.mcgill.ca";
    int portNumber = 5000;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
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
    	commLoop = new CommLoop(server);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testUserCreation() {
		serverReplies = commLoop.sendAndReceive( jcmf.createUser("123test", "test") );
		//
	}

}
