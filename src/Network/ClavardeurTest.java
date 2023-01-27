
package Network;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

import Conversation.ConversationsManager;
import Conversation.Exceptions.ConversationNotFound;
import Network.Exceptions.InvalidPseudoException;
public class ClavardeurTest {

	public static void main(String[] args) throws IOException, ConversationNotFound {
		
		InetAddress ipToReach = null;
		String pseudo = "";
		boolean messageSent = false;
		Scanner sc = new Scanner(System.in);
		NetworkManager nm;
		
		if(args.length == 1)
		{
			pseudo = args[0];

			nm = NetworkManager.getInstance();
			
			boolean connexionOK = false;
			try {
				nm.connexion(pseudo);
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

			nm = NetworkManager.getInstance();
			boolean connexionOK = false;
			try {
				nm.connexion(pseudo);
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
		sc.close();
	}
}
