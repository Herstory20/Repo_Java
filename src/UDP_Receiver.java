import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class UDP_Receiver implements Runnable{


	private DatagramSocket socket;
	private int port;
	private byte[] message;
	// a l'avenir, faire liste de messages => dès qu'on reçoit un msg, on ajoute à la liste
	// cette liste serait dépilée au fur et a mesure et les messages transmis aux bons threads
	private static UDP_Receiver instance;
	private static boolean running;
	
	private UDP_Receiver(int port) throws SocketException {
		this.port = port;
		this.socket = new DatagramSocket(this.port);
    	this.socket.setSoTimeout(1000);
		this.message = new byte[65535];
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
	/*
	public byte[] getMessage()
	{
		byte[] tmp = this.message;
		this.message = new byte[65535];
		return tmp;
	}*/
	
	public synchronized String getMessageString()
	{
		String tmp  = this.convertMessageByteToString();
		this.message = new byte[65535];
		return tmp;
	}
	private String convertMessageByteToString()
	{
		String tmp = new String(this.message);	//trim pour éliminer les espaces générés dus à la taille du tableau de bytes
		tmp = tmp.trim();
		return tmp;
	}

    
	private void receive()
	{
		byte[] newMessage = new byte[65535];
		DatagramPacket DpReceive = new DatagramPacket(newMessage, newMessage.length);
		try {
			synchronized(this) {
				this.socket.receive(DpReceive);
				this.message = newMessage;
				System.out.println("[UDP_Receiver] : MESSAGE RECU ! message = [ " + this.convertMessageByteToString() + " ]");
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
