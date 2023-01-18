package IHM;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import BDD.JDBC;
import Conversation.Conversation;
import Conversation.ConversationsManager;
import Conversation.Exceptions.ConversationNotFound;
import Network.NetworkManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

public class Home extends JFrame {
	
	private ArrayList<String> TabPseudo;
	private ArrayList<JButton> tabUsers;
	private String currentInterlocutor;
	private Conversation currentConversation;
	private InetAddress currentIpInterlocutor;
	private static Home instance;

	// j'étais en train d'essayer de mettre auto increment pour les messages 
	// + faut tester l'envoi messages

	public static Home getInstance() throws IOException
	{
		if (Home.instance == null)
		{
			Home.instance = new Home();
		}
		return Home.instance;
	}
	
	private Home() {
		//Set the look and feel.
        initLookAndFeel();
        setTitle("Clavardeur");
        //Make sure we have nice window decorations.
        setDefaultLookAndFeelDecorated(true);
        //Create and set up the window.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((int)size.getWidth()/2-1280/2, (int)size.getHeight()/2-720/2, 1280, 720);
		getContentPane().setBackground(Color.PINK);
		
		JScrollPane username = new JScrollPane();
		username.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		final JTextArea messagebox = new JTextArea();
		messagebox.setEditable(false);
		messagebox.setBackground(Color.LIGHT_GRAY);
		
		JScrollPane textfield = new JScrollPane();
		textfield.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		textfield.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		final JTextArea messagetosend = new JTextArea();
		textfield.setViewportView(messagetosend);
		
		JButton btnNewButton = new JButton("Send");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String tosend = messagetosend.getText();
				if (!tosend.isEmpty()) {
					try {
						//Creer le thread si conversation existe pas
						if(!ConversationsManager.getInstance().isConversationExist(currentIpInterlocutor)) {
							NetworkManager.getInstance().newDiscussion(currentIpInterlocutor);
						}
						ConversationsManager.getInstance().send(currentIpInterlocutor, tosend);
					} catch (IOException | ConversationNotFound e) {
						e.printStackTrace();
					}
					messagetosend.setText("");
				 }
				}
		});
		
		btnNewButton.setBackground(UIManager.getColor("CheckBoxMenuItem.acceleratorForeground"));
		
		JButton ChangeLogin = new JButton("Change Login");
		ChangeLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				/**/
			}
		});
		
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
						.addComponent(ChangeLogin, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(username, GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE))
					.addGap(18)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(textfield, GroupLayout.PREFERRED_SIZE, 988, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnNewButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addComponent(messagebox, GroupLayout.PREFERRED_SIZE, 1063, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(username, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 619, Short.MAX_VALUE)
						.addComponent(messagebox, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 619, Short.MAX_VALUE))
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(ChangeLogin, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED, 6, Short.MAX_VALUE)
							.addComponent(textfield, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(btnNewButton)))
					.addContainerGap())
		);

		
		
		JPanel Users = new JPanel();
		username.setViewportView(Users);
		Users.setLayout(new MigLayout("fillx"));
		tabUsers = new ArrayList<>();
		JDBC app = JDBC.getInstance();
		TabPseudo = app.selectPseudoA();
		for (int i=0; i<TabPseudo.size();i++) {
			JButton tmp = new JButton(TabPseudo.get(i));
			tmp.setLayout(null); 
			// marche pas
			tabUsers.add(tmp);
			tmp.addActionListener(new ActionListener() {
				
				// si on clique sur un user, on charge la conversation
				public void actionPerformed(ActionEvent e) {
					for (Iterator<JButton> iterator = tabUsers.iterator(); iterator.hasNext();) {
						JButton tmpButton = (JButton) iterator.next();
						if(tmpButton == e.getSource()) {
							currentInterlocutor = tmpButton.getText();
							try {
								currentIpInterlocutor =  InetAddress.getByName(JDBC.getInstance().selectIPfromPseudoA(currentInterlocutor));
							} catch (UnknownHostException e1) {
								e1.printStackTrace();
							}
							// potentielle erreur si select null !
							
							if(ConversationsManager.getInstance().isConversationExist(currentIpInterlocutor)) {
								try {
									this.loadChatHistory(JDBC.getInstance().selectmessagesM(NetworkManager.getInstance().getMyIpAddress().getHostAddress(), currentIpInterlocutor.getHostAddress()));
								} catch (IOException e2) {
									e2.printStackTrace();
								}
							}
							// met a jour currentConversation : 
							//	Si conv existe, la charge dedans et appelle la fonction qui va remplir les messages 
							//		- récupérer l'IP lié au pseudo depuis la bdd => currentIpInterlocutor
							//		- envoie ça au conversation manager isConvExist
							// 		- si oui, alors on get tous les messages de la conversation en question
							//		Il faut donc :
							//		- un selectMessageFromConversation dans JDBC
							//		- un getIpFromPseudo
							//	Sinon, laisse currentConversation à NULL et la conv sera créée depuis Network manager lors du 1er send
						}
						
					}
				}

				private void loadChatHistory(Hashtable<String, String> chatHistory) {
					String ipInterlocutorStr = currentIpInterlocutor.getHostAddress();

					Set<String> setOfKeys = chatHistory.keySet();
					for (String key : setOfKeys) {
						String ipDestTmp = key;
						String messageTmp = chatHistory.get(key);
						
						if(ipDestTmp.equals(ipInterlocutorStr)) {
							// cas d'un message envoyé par nous (le dest, c'est lui)
							
						}
						else {
							// cas d'un message reçu (le dest c'est nous)
						}
					}
				}
			});
			Users.add(tmp, "wrap, grow");
			
		};
		if(tabUsers.size()>0) {
			currentInterlocutor = tabUsers.get(0).getText();
		}
		/* A rajouter, l'espace change pseudo dans l'interface + fonction JDBC associé */
		getContentPane().setLayout(groupLayout);
	}
	
	
	
	//Specify the look and feel to use.  Valid values:
    //null (use the default), "Metal", "System", "Motif", "GTK+"
    final static String LOOKANDFEEL = "GTK+";
    
    private static void initLookAndFeel() {
        
        // Swing allows you to specify which look and feel your program uses--Java,
        // GTK+, Windows, and so on as shown below.
        String lookAndFeel = null;
        
        if (LOOKANDFEEL != null) {
            if (LOOKANDFEEL.equals("Metal")) {
                lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
            } else if (LOOKANDFEEL.equals("System")) {
                lookAndFeel = UIManager.getSystemLookAndFeelClassName();
            } else if (LOOKANDFEEL.equals("Motif")) {
                lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
            } else if (LOOKANDFEEL.equals("GTK+")) { //new in 1.4.2
                lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
            } else {
                System.err.println("Unexpected value of LOOKANDFEEL specified: "
                        + LOOKANDFEEL);
                lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
            }
            
            try {
                UIManager.setLookAndFeel(lookAndFeel);
            } catch (ClassNotFoundException e) {
                System.err.println("Couldn't find class for specified look and feel:"
                        + lookAndFeel);
                System.err.println("Did you include the L&F library in the class path?");
                System.err.println("Using the default look and feel.");
            } catch (UnsupportedLookAndFeelException e) {
                System.err.println("Can't use the specified look and feel ("
                        + lookAndFeel
                        + ") on this platform.");
                System.err.println("Using the default look and feel.");
            } catch (Exception e) {
                System.err.println("Couldn't get specified look and feel ("
                        + lookAndFeel
                        + "), for some reason.");
                System.err.println("Using the default look and feel.");
                e.printStackTrace();
            }
        }
    }
	
	/*public static void createAndShowGUI() {
        
    }*/
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 javax.swing.SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	        	Home frame = new Home();
	        	frame.setVisible(true);
	        }
		 });
	}
}