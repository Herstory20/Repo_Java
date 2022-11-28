import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDP_Sender implements Runnable{

	private DatagramSocket socket;
	private InetAddress ip;
	private int port;
	private String message;	//l'objet message est partagé
	private MessageType type;
	private static UDP_Sender instance;
	private static boolean running;
	
	private UDP_Sender(InetAddress ip, int port) throws SocketException {
		this.socket = new DatagramSocket();
		this.ip = ip;
		this.port = port;
		this.message = "";
		this.type = MessageType.UNKNOWN;
		UDP_Sender.running = true;
	}

	public static UDP_Sender getInstance(InetAddress ip, int port) throws SocketException
	{
		if (UDP_Sender.instance == null)
		{
			return new UDP_Sender(ip, port);
		}
		else
		{
			return UDP_Sender.instance;
		}
	}
	
	public void setMessage(String message, MessageType type)
	{
		this.message = message ;
		this.type = type;
	}
	
	public void stop()
	{
		UDP_Sender.running = false;
	    this.socket.close();
	    try {
	    	this.socket.setSoTimeout(1);
	    } catch (SocketException e) {}
	    this.socket.disconnect();
	    this.socket = null;
	}
	
	private synchronized void send() throws IOException
	{
        ;
        byte buf[] = (this.type + this.message).getBytes();
		DatagramPacket DpSend = new DatagramPacket(buf, buf.length, this.ip, this.port);
		this.socket.send(DpSend);
		this.message = "";
	}

	@Override
	public void run() {
		System.out.println("[UDP_Sender] : running");
		while(UDP_Sender.running)
		{
			if(this.message != "")	//un message doit etre envoyé
			{
				try {
					System.out.println("Demande d'envoi de \"" + this.message + "\" en mode [" + this.type + "]...");
					this.send();
					System.out.println("Message envoyé !");
				} catch (IOException e) {
					System.out.println("Echec de l'envoi !");
					e.printStackTrace();
				}
				this.message = "";
			}
			
			//delay pour ne pas surcharger CPU
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("[UDP_Sender] : end of run");
	}

}
