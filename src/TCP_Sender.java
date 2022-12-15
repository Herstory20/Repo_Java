import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCP_Sender implements Runnable{
	private InetAddress ip;
	private int port;
	private Socket link;
	private PrintWriter out;
	private Message message;
	private boolean running;
	
	public TCP_Sender(InetAddress ip, int port) throws UnknownHostException, IOException {
        this.ip = ip;
        this.port = port;
        this.message = null;
        this.running = true;

        System.out.println("Tentative de connexion sur " + this.ip + " ; "+ this.port);
        this.link = new Socket(this.ip, this.port);
        this.out = new PrintWriter(this.link.getOutputStream(),true);
        if(this.link.isConnected())
        	System.out.println("Connexion reussie !");
	}
	
	public void setMessage(Message message)
	{
		this.message = message;
	}
	
	private synchronized void send() {
		if(this.message != null) {
	        this.out.println(this.message.getTrameString());
			this.message = null;
		}
	}

	public void stop() throws IOException
	{
		this.running = false;
        this.link.close();
        this.out.close();
	}


	@Override
	public void run() {
		System.out.println("[TCP_Sender] : running");
		while(this.running)
		{
			if(this.message != null)	//un message doit etre envoyé
			{
				System.out.println("[TCP_Sender] : Demande d'envoi de \"" + this.message.getContenu() + "\" en mode [" + this.message.getType() + "]...");
				this.send();
				System.out.println("[TCP_Sender] : Message envoyé !");
			}
			
			//delay pour ne pas surcharger CPU
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("[TCP_Sender] : end of run");
	}
	

}
