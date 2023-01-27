package Message;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class MessageTests {
	private Message mConn;
	private Message mComm;
	
	@Before
	public void setUp() throws Exception {
		mComm = new Message("Message de test COMMUNICATION",MessageType.COMMUNICATION);
		mConn = new Message("Message de test CONNECTIVITE",MessageType.CONNECTIVITE);
	}

	@Test
	public void testCreationMessageString() {
		assertEquals(this.mComm.getContenu(),"Message de test COMMUNICATION");
		assertEquals(this.mConn.getContenu(),"Message de test CONNECTIVITE");
	}


	@Test
	public void testCreationMessageBytes() {
		Message mBytes = new Message(this.mComm.getTrame());
		assertEquals(mBytes.getContenu(), mComm.getContenu());
	}
	
	

}
