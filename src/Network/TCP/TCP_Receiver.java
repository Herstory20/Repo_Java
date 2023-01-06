package Network.TCP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import Message.Message;


public class TCP_Receiver implements Runnable{


	private int port;
	private Socket link;
	private InetAddress ipDest;
	ServerSocket servSocket;
	private BufferedReader in;
	private Message message;
	private boolean running;
	private boolean connected;
	
	public TCP_Receiver(int port) throws IOException {
        this.port = port;
        this.message = null;
        this.running = true;
        this.connected = false;

        System.out.println("[TCP_Receiver] : Attente de connexion sur " + this.port);
        this.servSocket = new ServerSocket(this.port);
	}
	
	private void attenteConnexion(){
        try {
			this.link = this.servSocket.accept();
	    	this.link.setSoTimeout(1000);
	        this.ipDest = this.link.getInetAddress();
	        System.out.println("[TCP_Receiver] : Connexion detectee de " + this.ipDest);
	        this.in = new BufferedReader(new InputStreamReader(this.link.getInputStream()));
	        this.connected = true;
		} catch (IOException e) {
			this.link = null;
			this.ipDest = null;
			this.in = null;
	        this.connected = false;
			System.out.println("[TCP_Receiver] : Connexion échouée.");
		}
    }
	
	public void stop() throws IOException
	{
		this.running = false;
        this.fermerConnexion();
	}
	
	public void fermerConnexion() {
        try {
			this.connected = false;
	        this.in.close();
			this.link.close();
			System.out.println("[TCP_Receiver] : Connexion Fermée !");
		} catch (IOException e) {
			System.out.println("[TCP_Receiver] : Connexion déjà fermée !");
		}
	}
	
	public InetAddress getIpDest() {
		return this.ipDest;
	}

	public synchronized void receive()  {
		String tmp = null;
		
		try {
			tmp = in.readLine();
			if(tmp != null) {
				// on fait ça pour créer un objet message a partir du message recu
				this.message = new Message(tmp.getBytes());
				System.out.println("[TCP_Receiver] : MESSAGE RECU ! message = [ " + this.message.getTrameString() + " ]");
			}
			else {
				System.out.println("[TCP_Receiver] : Client déconnecté !");
		        this.fermerConnexion();
			}
		} catch (IOException e) {
			if (!(e instanceof SocketTimeoutException))
			{
				System.out.println("[TCP_Receiver] : Erreur, client déconnecté !");
		        this.fermerConnexion();
				e.printStackTrace();
			}
			// else => timeout !
		}
	}
	
	public synchronized Message getMessage()
	{
		Message tmp = this.message;
		this.message = null;
		return tmp;
	}

	@Override
	public void run() {
		System.out.println("[TCP_Receiver] : running");
		
		while(this.running)
		{
			if(!connected) {
				System.out.println("[TCP_Receiver] : Aucune connexion détectée. Attente d'une connexion...");
				this.attenteConnexion();
			}
			else{

				this.receive();
			}
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		System.out.println("[TCP_Receiver] : end of run");
	}

}
