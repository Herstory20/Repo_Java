import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
public class Clavardeur {

	public static void main(String[] args) throws IOException, ConversationNotFound {
		
		InetAddress ipToReach = null;
		String pseudo = "";
		boolean messageSent = false;
		Scanner sc = new Scanner(System.in);
		
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
				if(nm.getTcp_ipDistant() != null && ipToReach == null) {
					ipToReach = nm.getTcp_ipDistant();
				}
				if(ConversationsManager.getInstance().isConversationExist(ipToReach) && !messageSent) {

					if(ConversationsManager.getInstance().isConversationOpen(ipToReach))
					{
						System.out.print(">>Enter your message :");
						String input = sc.nextLine();
						System.out.print(">> The following message will be sent : " + input);
						ConversationsManager.getInstance().send(ipToReach, input);
					}
				}
			}
		}
		if(args.length == 2)	// cas d'une machine voulant lancer une conversation
		{
			pseudo = args[0];
			ipToReach = InetAddress.getByName(args[1]);

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
			
			nm.newDiscussion(ipToReach);
			System.out.println("Demande de discussion :\n>>");
			while(connexionOK)
			{
				nm.update();
				if(ConversationsManager.getInstance().isConversationExist(ipToReach) && !messageSent) {

					if(ConversationsManager.getInstance().isConversationOpen(ipToReach))
					{
						System.out.print(">>Enter your message :");
						String input = sc.nextLine();
						System.out.print(">> The following message will be sent : " + input);
						ConversationsManager.getInstance().send(ipToReach, input);
					}
				}
			}
		}
		else {
			System.out.println("argument PSEUDO manquant");
			System.exit(0);
		}
		
	}
}
