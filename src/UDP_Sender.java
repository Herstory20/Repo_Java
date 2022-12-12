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
	private Message message;	//l'objet message est partagé
	private static UDP_Sender instance;
	private static boolean running;
	private boolean broadcastMode;
	private int tentativesEnvoi;
	private static final int MAX_TENTATIVES = 15;
	
	private UDP_Sender(InetAddress ip, int port) throws SocketException {
		this.socket = new DatagramSocket();
		this.ip = ip;
		this.port = port;
		this.message = null;
		UDP_Sender.running = true;
		this.loadBroadcastAddress();
		this.broadcastMode = false;
		this.tentativesEnvoi = 0;
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
	
	public void setMessage(Message message)
	{
		this.message = message;
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
	
	private synchronized void send() throws IOException
	{
        byte buf[] = this.message.getTrame();
		DatagramPacket DpSend = new DatagramPacket(buf, buf.length, this.ip, this.port);
		this.socket.send(DpSend);
		this.message = null;
	}

    public void broadcast() throws IOException {
        this.socket.setBroadcast(true);

        byte[] buffer = this.message.getTrame();

        DatagramPacket DpBroadCastSend = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), this.port);
        System.out.println("message : " + this.message.getTrameString() + " - port : " + this.port);
        this.socket.send(DpBroadCastSend);
        socket.setBroadcast(false);
		this.message = null;
    }
	
	

	@Override
	public void run() {
		System.out.println("[UDP_Sender] : running");
		while(UDP_Sender.running)
		{
			if(this.message != null)	//un message doit etre envoyé
			{
				try {
					System.out.println("Demande d'envoi de \"" + this.message.getContenu() + "\" en mode [" + this.message.getType() + "]...");
					this.tentativesEnvoi++; 
					if(this.broadcastMode)
					{
						this.broadcast();
					}
					else {
						this.send();
					}
					this.tentativesEnvoi = 0;
					System.out.println("Message envoyé !");
				} catch (IOException e) {
					System.out.println("Echec de l'envoi !");
					e.printStackTrace();
				}
				if(this.tentativesEnvoi > UDP_Sender.MAX_TENTATIVES)
				{
					this.message = null;
					this.tentativesEnvoi = 0;
				}
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
