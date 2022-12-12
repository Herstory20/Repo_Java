import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Clavardeur {

	public static void main(String[] args) throws IOException {
		
		InetAddress ipToReach;
		int portToReach;
		int portToWait;
		if(args.length == 2)	// cas d'une machine voulant lancer une conversation
		{
			ipToReach = InetAddress.getByName(args[0]);
			portToReach = Integer.parseInt(args[1]);
			
			TCP_Sender tcp_send_thread = new TCP_Sender(ipToReach, portToReach);
			Thread t_udpsend = new Thread(tcp_send_thread);
			t_udpsend.start();
			
			tcp_send_thread.setMessage(new Message("Coucou, je voudrais parler !", MessageType.COMMUNICATION));
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			tcp_send_thread.stop();
		}
		else if(args.length == 1) { // cas d'une machine attendant une conversation
			portToWait = Integer.parseInt(args[0]);

			TCP_Receiver tcp_receive_thread = new TCP_Receiver(portToWait);
			Thread t_udprcv = new Thread(tcp_receive_thread);
			t_udprcv.start();
			Message recu = null;
			while(recu == null) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				recu = tcp_receive_thread.getMessage();
			}
			System.out.println("Message recu : " + recu);
			tcp_receive_thread.stop();
			
		}
		else {
			System.out.println("arguments manquants");
			System.exit(0);
		}
		
		
		/*String pseudo = "";
		if(args.length == 1)
		{
			pseudo = args[0];
		}
		else {
			System.out.println("argument PSEUDO manquant");
			System.exit(0);
		}
		
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
		}*/
	}
}
