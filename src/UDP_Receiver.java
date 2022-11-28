import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDP_Receiver implements Runnable{


	private DatagramSocket socket;
	private int port;
	private byte[] message;
	// a l'avenir, faire liste de messages => dès qu'on reçoit un msg, on ajoute à la liste
	// cette liste serait dépilée au fur et a mesure et les messages transmis aux bons threads
	private static UDP_Receiver instance;
	private boolean stop;
	
	private UDP_Receiver(int port) throws SocketException {
		this.port = port;
		this.socket = new DatagramSocket(this.port);
		this.message = new byte[65535];
		this.stop = false;
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
	
	public byte[] getMessage()
	{
		byte[] tmp = this.message;
		this.message = new byte[65535];
		return tmp;
	}
	
	public void endThread()
	{
		this.stop = true;
	}
	
	private void receive()
	{
		DatagramPacket DpReceive = new DatagramPacket(this.message, this.message.length);
		try {
			this.socket.receive(DpReceive);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while(!this.stop)
		{
			// pb ici : this.socket.receive est bloquant ! comment faire jsp
			System.out.println("running");
			this.receive();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("UDP_RECEIVER : end run.");
	}
}
