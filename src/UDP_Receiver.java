import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class UDP_Receiver implements Runnable{


	private DatagramSocket socket;
	private int port;
	private Message message;
	// a l'avenir, faire liste de messages => dès qu'on reçoit un msg, on ajoute à la liste
	// cette liste serait dépilée au fur et a mesure et les messages transmis aux bons threads
	private static UDP_Receiver instance;
	private static boolean running;
	
	private UDP_Receiver(int port) throws SocketException {
		this.port = port;
		this.socket = new DatagramSocket(this.port);
    	this.socket.setSoTimeout(1000);
		this.message = null;
		UDP_Receiver.running = true;
	}

	public static UDP_Receiver getInstance(int port) throws SocketException
	{
		if (UDP_Receiver.instance == null)
		{
			return new UDP_Receiver(port);
		}
		else
		{
			return UDP_Receiver.instance;
		}
	}
	
	public synchronized String getMessageString()
	{
		String tmp;
		if(message==null) {
			tmp = "";
		}
		else {
			tmp  = this.message.getTrameString();
			this.message = null;
		}
		return tmp;
	}

    
	private void receive()
	{
		byte[] newMessage = new byte[65535];
		DatagramPacket DpReceive = new DatagramPacket(newMessage, newMessage.length);
		try {
			synchronized(this) {
				this.socket.receive(DpReceive);
				this.message = new Message(newMessage);
				System.out.println("[UDP_Receiver] : MESSAGE RECU ! message = [ " + this.message.getTrameString() + " ]");
			}
		} catch (IOException e) {
			//nothing to do
			// maybe later => if timeout 15 times, close connection
		}
	}

	
	public void stop()
	{
		UDP_Receiver.running = false;
	    this.socket.disconnect();
	    this.socket.close();
	    this.socket = null;
	}

	@Override
	public void run() {
		System.out.println("[UDP_Receiver] : running");
		while(UDP_Receiver.running)
		{
			this.receive();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("[UDP_Receiver] : end of run");
	}
}
