package Network.UDP;

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

import Message.Message;

public class UDP_Sender implements Runnable{

	private static DatagramSocket socket;
	private static InetAddress ip;
	private static InetAddress broadcastAddress;
	private static int port;
	private static Message message;	//l'objet message est partagé
	private static UDP_Sender instance;
	private static boolean running;
	private static boolean broadcastMode;
	private static int tentativesEnvoi;
	private static final int MAX_TENTATIVES = 15;
	
	private UDP_Sender(InetAddress ip, int port) throws SocketException {
		UDP_Sender.socket = new DatagramSocket();
		UDP_Sender.ip = ip;
		UDP_Sender.port = port;
		UDP_Sender.message = null;
		UDP_Sender.running = true;
		this.loadBroadcastAddress();
		UDP_Sender.broadcastMode = false;
		UDP_Sender.tentativesEnvoi = 0;
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
				UDP_Sender.broadcastAddress = ia.getBroadcast();
				//break; non nécessaire mais possible
				//s'il y a plus de résultats, le dernier serait sélectionné
			}
		}
		System.out.println("@ broadcast : " + UDP_Sender.broadcastAddress);
	}

	public static UDP_Sender getInstance(InetAddress ip, int port) throws SocketException
	{
		if (UDP_Sender.instance == null)
		{
			UDP_Sender.instance = new UDP_Sender(ip, port);
		}
		return UDP_Sender.instance;
	}
	
	public void setMessage(Message message)
	{
		UDP_Sender.message = message;
	}
	
	public void setIp(InetAddress ip) {
		UDP_Sender.ip = ip;
	}
	
	public void setBroadcastEnabled()
	{
		UDP_Sender.broadcastMode = true;
	}

	private void setBroadcastDisabled()
	{
		UDP_Sender.broadcastMode = false;
	}
	
	public void stop()
	{
		UDP_Sender.running = false;
	    UDP_Sender.socket.close();
	    try {
	    	UDP_Sender.socket.setSoTimeout(1);
	    } catch (SocketException e) {}
	    UDP_Sender.socket.disconnect();
	    UDP_Sender.socket = null;
	}
	
	private synchronized void send() throws IOException
	{
        byte buf[] = UDP_Sender.message.getTrame();
		DatagramPacket DpSend = new DatagramPacket(buf, buf.length, UDP_Sender.ip, UDP_Sender.port);
		UDP_Sender.socket.send(DpSend);
		UDP_Sender.message = null;
	}

    private void broadcast() throws IOException {
        UDP_Sender.socket.setBroadcast(true);

        byte[] buffer = UDP_Sender.message.getTrame();

        DatagramPacket DpBroadCastSend = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), UDP_Sender.port);
        System.out.println("message : " + UDP_Sender.message.getTrameString() + " - port : " + UDP_Sender.port);
        UDP_Sender.socket.send(DpBroadCastSend);
        socket.setBroadcast(false);
		UDP_Sender.message = null;
		this.setBroadcastDisabled();	// on disable automatiquement après chaque broadcast
    }
	
	

	@Override
	public void run() {
		System.out.println("[UDP_Sender] : running");
		while(UDP_Sender.running)
		{
			if(UDP_Sender.message != null)	//un message doit etre envoyé
			{
				try {
					System.out.println("[UDP_Sender] : Demande d'envoi de \"" + UDP_Sender.message.getContenu() + "\" en mode [" + UDP_Sender.message.getType() + "]...");
					UDP_Sender.tentativesEnvoi++; 
					if(UDP_Sender.broadcastMode)
					{
						this.broadcast();
					}
					else {
						this.send();
					}
					UDP_Sender.tentativesEnvoi = 0;
					System.out.println("[UDP_Sender] : Message envoyé !");
				} catch (IOException e) {
					System.out.println("[UDP_Sender] : Echec de l'envoi !");
					e.printStackTrace();
				}
				if(UDP_Sender.tentativesEnvoi > UDP_Sender.MAX_TENTATIVES)
				{
					UDP_Sender.message = null;
					UDP_Sender.tentativesEnvoi = 0;
				}
			}
			
			//delay pour ne pas surcharger CPU
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("[UDP_Sender] : end of run");
	}

}
