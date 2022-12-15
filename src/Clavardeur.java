import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Clavardeur {

	public static void main(String[] args) throws IOException {
		
		InetAddress ipToReach;
		int portToReach;
		
		String pseudo = "";
		if(args.length == 1)
		{
			pseudo = args[0];

			NetworkManager nm = new NetworkManager();
			boolean connexionOK = false;
			try {
				connexionOK = nm.connexion(pseudo);
			} catch (InvalidPseudoException e) {
				e.printStackTrace();
			}
			if(!connexionOK) {
				System.exit(0);
			}
			
			
			System.out.println("Ecoute :\n>>");
			while(connexionOK)
			{
				nm.update();
			}
		}
		if(args.length == 2)	// cas d'une machine voulant lancer une conversation
		{
			ipToReach = InetAddress.getByName(args[0]);
			portToReach = Integer.parseInt(args[1]);
			
			TCP_Sender tcp_send_thread = new TCP_Sender(ipToReach, portToReach);
			Thread t_udpsend = new Thread(tcp_send_thread);
			t_udpsend.start();
			
			//tcp_send_thread.setMessage(new Message("Coucou, je voudrais parler !", MessageType.COMMUNICATION));
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			tcp_send_thread.setMessage(new Message("Coucou, je voudrais parler !", MessageType.COMMUNICATION));
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			tcp_send_thread.stop();
		}
		else {
			System.out.println("argument PSEUDO manquant");
			System.exit(0);
		}
		
	}
}
