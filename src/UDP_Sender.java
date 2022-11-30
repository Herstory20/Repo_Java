import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class UDP_Sender implements Runnable{

	private DatagramSocket socket;
	private InetAddress ip;
	private InetAddress broadcastAddress;
	private int port;
	private String message;	//l'objet message est partagé
	private MessageType type;
	private static UDP_Sender instance;
	private static boolean running;
	private boolean broadcastMode;
	
	private UDP_Sender(InetAddress ip, int port) throws SocketException {
		this.socket = new DatagramSocket();
		this.ip = ip;
		this.port = port;
		this.message = "";
		this.type = MessageType.UNKNOWN;
		UDP_Sender.running = true;
		this.loadBroadcastAddress();
		this.broadcastMode = false;
	}
	
	private void loadBroadcastAddress() throws SocketException
	{
		Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
		NetworkInterface ni = en.nextElement();
		List<InterfaceAddress> list = ni.getInterfaceAddresses();
		Iterator<InterfaceAddress> it = list.iterator();
		while (it.hasNext()) {
			InterfaceAddress ia = it.next();
			if(ia.getBroadcast() != null)
			{
				this.broadcastAddress = ia.getBroadcast();
				//break; non nécessaire mais possible
				//s'il y a plus de résultats, le dernier serait sélectionné
			}
		}
		System.out.println("@ broadcast : " + this.broadcastAddress);
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
	
	public void setIp(InetAddress ip) {
		this.ip = ip;
	}
	
	public void setBroadcastEnabled()
	{
		this.broadcastMode = true;
	}

	public void setBroadcastDisabled()
	{
		this.broadcastMode = false;
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
	
	private synchronized String formaterMessage(){
		return this.type.ordinal() + this.message;
	}
	
	private synchronized void send() throws IOException
	{
        byte buf[] = this.formaterMessage().getBytes();
		DatagramPacket DpSend = new DatagramPacket(buf, buf.length, this.ip, this.port);
		this.socket.send(DpSend);
		this.message = "";
	}

    public void broadcast() throws IOException {
        this.socket.setBroadcast(true);

        byte[] buffer = this.formaterMessage().getBytes();

        DatagramPacket DpBroadCastSend = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), this.port);
        System.out.println("message : " + this.formaterMessage() + " - port : " + this.port);
        this.socket.send(DpBroadCastSend);
        socket.setBroadcast(false);
        
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
					if(this.broadcastMode)
					{
						this.broadcast();
					}
					else {
						this.send();
					}
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
