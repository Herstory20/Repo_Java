import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class TCP_Receiver implements Runnable{


	private int port;
	private Socket link;
	private InetAddress ipDest;
	ServerSocket servSocket;
	private BufferedReader in;
	private Message message;
	private boolean running;
	
	public TCP_Receiver(int port) throws IOException {
        this.port = port;
        this.message = null;
        this.running = true;

        System.out.println("Attente de connexion sur " + this.port);
        this.servSocket = new ServerSocket(this.port);
	}
	
	private void attenteConnexion() throws IOException {
        this.link = this.servSocket.accept();
    	this.link.setSoTimeout(1000);
        this.ipDest = this.link.getInetAddress();
        System.out.println("connexion detectee de " + this.ipDest);
        
        this.in = new BufferedReader(new InputStreamReader(this.link.getInputStream()));
	}
	
	public void stop() throws IOException
	{
		this.running = false;
        this.link.close();
	}

	public synchronized void receive()  {
		String tmp;
		
		try {
			tmp = in.readLine();
			if(tmp != null) {
				// on fait ça pour créer un objet message a partir du message recu
				this.message = new Message(tmp.getBytes());
				System.out.println("[TCP_Receiver] : MESSAGE RECU ! message = [ " + this.message.getTrameString() + " ]");
			}
		} catch (IOException e) {
			//nothing to do
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
		try {
			this.attenteConnexion();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		while(this.running)
		{
			this.receive();
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("[TCP_Receiver] : end of run");
	}

}
