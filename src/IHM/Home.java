package IHM;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import BDD.JDBC;
import BDD.Tuple;
import Conversation.Conversation;
import Conversation.ConversationsManager;
import Conversation.Exceptions.ConversationNotFound;
import Network.NetworkManager;
import Network.Exceptions.InvalidPseudoException;
import net.miginfocom.swing.MigLayout;

import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

public class Home extends JFrame {


	private JPanel messagebox = new JPanel();
	private static JPanel destinataire = new JPanel();
	private static JPanel expediteur = new JPanel();
	private JScrollPane messbox = new JScrollPane();
	private static ArrayList<String> TabPseudo;
	private static ArrayList<JButton> tabUsers;
	private static String currentInterlocutor;
	private Conversation currentConversation;
	private static InetAddress currentIpInterlocutor;
	private static Home instance;
	private JLabel nom = new JLabel();
	private static JDBC app = JDBC.getInstance();
	private static JPanel Users = new JPanel();

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
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((int) size.getWidth() / 2 - 1280 / 2, (int) size.getHeight() / 2 - 720 / 2, 1280, 720);
		getContentPane().setBackground(new Color(108, 226, 247));
		
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent event) {
		        exitProcedure();
		    }
		});



		JScrollPane username = new JScrollPane();
		username.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		username.getVerticalScrollBar().setUnitIncrement(10);

		JScrollPane textfield = new JScrollPane();
		textfield.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		textfield.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		final JTextArea messagetosend = new JTextArea();
		textfield.setViewportView(messagetosend);

		JButton btnNewButton = new JButton("Send");
		btnNewButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) {
				String tosend = messagetosend.getText();
				if (!tosend.isEmpty()) {
					try {
						//Creer le thread si conversation existe pas
						if(!ConversationsManager.getInstance().isConversationExist(currentIpInterlocutor)) {
							System.out.println("[HOME] - SEND_BUTTONActionPerformed : Creation du thread car inexistant");
							NetworkManager.getInstance().newDiscussion(currentIpInterlocutor);
						}
						else {
							System.out.println("[HOME] - SEND_BUTTONActionPerformed : Thread déjà existant.");
						}
						
						System.out.println("[HOME] - SEND_BUTTONActionPerformed : Envoi du message...");
						ConversationsManager.getInstance().send(currentIpInterlocutor, tosend);
						
					} catch (IOException | ConversationNotFound e) {
						e.printStackTrace();
					}
					messagetosend.setText("");
				}
			}
		});

		btnNewButton.setBackground(UIManager.getColor("CheckBoxMenuItem.acceleratorForeground"));

		final JButton ChangeLogin = new JButton("Change Login");
		ChangeLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				/*Interface de changement de pseudo*/
				String Login = JOptionPane.showInputDialog(null, "Votre nouveau login :");
				/* Envoyer UDP pour tout le monde pour informer le changement */
				try {
					while (!NetworkManager.getInstance().changePseudo(Login)) {
						JOptionPane.showMessageDialog(null, "Le login est déjà utilisé !!");
						Login = JOptionPane.showInputDialog(null, "Votre nouveau login :");
					}
				} catch (HeadlessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				nom.setText(Login);


			}
		});



		messbox.getVerticalScrollBar().setUnitIncrement(10);

		JPanel Status = new JPanel();

		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
										.addGap(13)
										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
												.addComponent(ChangeLogin, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 179, GroupLayout.PREFERRED_SIZE)
												.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
														.addComponent(Status, GroupLayout.PREFERRED_SIZE, 175, GroupLayout.PREFERRED_SIZE)
														.addGap(6))))
								.addGroup(groupLayout.createSequentialGroup()
										.addContainerGap()
										.addComponent(username, GroupLayout.PREFERRED_SIZE, 176, GroupLayout.PREFERRED_SIZE)))
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
										.addGap(7)
										.addComponent(textfield, GroupLayout.DEFAULT_SIZE, 983, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(btnNewButton)
										.addGap(10))
								.addGroup(groupLayout.createSequentialGroup()
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(messbox, GroupLayout.DEFAULT_SIZE, 1064, Short.MAX_VALUE)
										.addGap(12))))
				);
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addComponent(messbox, GroupLayout.DEFAULT_SIZE, 612, Short.MAX_VALUE)
								.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
										.addComponent(Status, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(username, GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)))
						.addGap(1)
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
										.addGap(6)
										.addComponent(textfield, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE))
								.addGroup(groupLayout.createSequentialGroup()
										.addGap(6)
										.addComponent(ChangeLogin, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE))
								.addGroup(groupLayout.createSequentialGroup()
										.addGap(14)
										.addComponent(btnNewButton)))
						.addGap(11))
				);
		Status.setLayout(new MigLayout("", "[150.00px]", "[23.00px][21px]"));

		JLabel status1 = new JLabel("Connecté en tant que ");
		Status.add(status1, "cell 0 0,alignx center,aligny center");



		try {
			nom.setText(NetworkManager.getInstance().getPseudo());
		} catch (IOException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		nom.setForeground(Color.ORANGE);
		nom.setFont(new Font("New Times Roman", Font.PLAIN, 16));;
		Status.add(nom, "cell 0 1,alignx center,aligny top");

		messbox.setViewportView(messagebox);
		messagebox.setLayout(new GridLayout(0, 2, 0, 0));

		destinataire.setBackground(Color.LIGHT_GRAY);
		messagebox.add(destinataire);
		destinataire.setLayout(new MigLayout("", "[]", "[]"));

		expediteur.setBackground(Color.LIGHT_GRAY);
		messagebox.add(expediteur);
		expediteur.setLayout(new MigLayout("", "[]", "[]"));



		username.setViewportView(Users);
		Users.setLayout(new MigLayout("fillx"));
		this.tabUsers = new ArrayList<>();

		Home.updateUsersList();

		if(tabUsers.size()>0) {
			currentInterlocutor = tabUsers.get(0).getText();
		}
		/* A rajouter, l'espace change pseudo dans l'interface + fonction JDBC associé */
		getContentPane().setLayout(groupLayout);
	}

	public void exitProcedure() {
		try {
			NetworkManager.getInstance().finalize();
			System.out.println("Network Manager fermé !");
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	    this.dispose();
	    System.exit(0);
	}
	
	public static void updateUsersList() {

		Home.tabUsers.clear();
		Home.Users.removeAll();
		TabPseudo = app.selectPseudoA();
		for (int i=0; i<TabPseudo.size();i++) {
			String pstmp = TabPseudo.get(i);
			try {
				if (pstmp != NetworkManager.getInstance().getPseudo()) 
				{
					JButton tmp = new JButton(pstmp);
					tmp.setLayout(null);
					Home.tabUsers.add(tmp);
					tmp.addActionListener(new ActionListener() {

						// si on clique sur un user, on charge la conversation
						public void actionPerformed(ActionEvent e) {
							for (Iterator<JButton> iterator = tabUsers.iterator(); iterator.hasNext();) {
								JButton tmpButton = (JButton) iterator.next();
								if(tmpButton == e.getSource()) {
									Home.refresh();
									currentInterlocutor = tmpButton.getText();
									try {
										currentIpInterlocutor =  InetAddress.getByName(JDBC.getInstance().selectIPfromPseudoA(currentInterlocutor));
									} catch (UnknownHostException e1) {
										e1.printStackTrace();
									}
									// potentielle erreur si select null !
									//if(ConversationsManager.getInstance().isConversationExist(currentIpInterlocutor)) {
										try {
											this.loadChatHistory(JDBC.getInstance().selectmessagesM(NetworkManager.getInstance().getMyIpAddress().getHostAddress(), currentIpInterlocutor.getHostAddress()));
										} catch (IOException e2) {
											e2.printStackTrace();
										}
									//}
									// met a jour currentConversation :
									// Si conv existe, la charge dedans et appelle la fonction qui va remplir les messages
									// - récupérer l'IP lié au pseudo depuis la bdd => currentIpInterlocutor
									// - envoie ça au conversation manager isConvExist
									// - si oui, alors on get tous les messages de la conversation en question
									// Il faut donc :
									// - un selectMessageFromConversation dans JDBC
									// - un getIpFromPseudo
									// Sinon, laisse currentConversation à NULL et la conv sera créée depuis Network manager lors du 1er send
								}

							}
						}

						private void loadChatHistory( List <Tuple> chathistory) {
							String ipInterlocutorStr = currentIpInterlocutor.getHostAddress();

							for (int i = 0 ; i<chathistory.size(); i++) {
								String ipDestTmp = chathistory.get(i).getIp();
								String messageTmp = chathistory.get(i).getMessage();
								Timestamp temps = chathistory.get(i).getDate();
								Date date = temps;
								DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");  
								String dateTmp = dateFormat.format(date);
								if(ipDestTmp.equals(ipInterlocutorStr)) {
									// cas d'un message envoyé par nous (le dest, c'est lui)
									Home.addMessagesend(messageTmp, dateTmp,true);
									Home.addMessagereceive(messageTmp,dateTmp,true);
								}
								else {
									// cas d'un message reçu (le dest c'est nous)
									Home.addMessagesend(messageTmp,dateTmp,false);
									Home.addMessagereceive(messageTmp,dateTmp,false);
								}
							}
						}
					});
				Home.Users.add(tmp, "wrap, grow");
				Home.Users.revalidate();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected static void refresh() {
		expediteur.removeAll();
		destinataire.removeAll();
		expediteur.revalidate();
		destinataire.revalidate();
		expediteur.repaint();
		destinataire.repaint();
	}

	/* boolean to seperate if we add the message as the sender or receiver */
	public static void addMessagesend(String tosend, String date, boolean sender) {
		final Color col;
		if (sender) {
			col = Color.green;
		} else {
			col = new Color(98,141,244);
		}

		JPanel pmess = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Dimension arcs = new Dimension(15, 15);
				int width = getWidth();
				int height = getHeight();
				Graphics2D graphics = (Graphics2D) g;
				graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


				//Draws the rounded opaque panel with borders.
				graphics.setColor(col);
				graphics.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height); //paint background
				graphics.setColor(col);
				graphics.drawRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height); //paint border
			}
		};
		JTextArea mess = new JTextArea(tosend);
		mess.setEditable(false);
		mess.setBackground(col);
		mess.setLineWrap(true);
		mess.setWrapStyleWord(true);
		pmess.setOpaque(false);
		pmess.setLayout(new MigLayout());
		pmess.add(mess,"wrap, w 100%"); 
		JLabel Pseudo = new JLabel(date);
		Pseudo.setFont(new Font("New Times Roman", Font.ITALIC, 10));
		mess.setFont(new Font("New Times Roman", Font.PLAIN, 18));
		mess.setForeground(Color.black);
		mess.setBorder(new EmptyBorder(5, 5, 5, 5));
		if (!sender) {
			pmess.setVisible(false);
			Pseudo.setVisible(false);
		}
		Home.expediteur.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		Home.expediteur.add(pmess, "wrap, w 90%");
		Home.expediteur.add(Pseudo, "wrap");
		Home.expediteur.revalidate();
	}

	public static void addMessagereceive(String tosend, String date, boolean sender) {
		final Color col;
		if (sender) {
			col = Color.green;
		} else {
			col = new Color(98,141,244);
		}

		JPanel pmess = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Dimension arcs = new Dimension(15, 15);
				int width = getWidth();
				int height = getHeight();
				Graphics2D graphics = (Graphics2D) g;
				graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


				//Draws the rounded opaque panel with borders.
				graphics.setColor(col);
				graphics.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height); //paint background
				graphics.setColor(col);
				graphics.drawRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height); //paint border
			}
		};
		JTextArea mess = new JTextArea(tosend);
		mess.setBackground(col);
		mess.setEditable(false);
		mess.setLineWrap(true);
		mess.setWrapStyleWord(true);
		pmess.setOpaque(false);
		pmess.add(mess);
		pmess.setVisible(false);
		pmess.setLayout(new MigLayout());
		pmess.add(mess,"wrap, w 100%");
		JLabel Pseudo = new JLabel(date);
		Pseudo.setFont(new Font("New Times Roman", Font.ITALIC, 10));
		Pseudo.setVisible(false);
		mess.setFont(new Font("New Times Roman", Font.PLAIN, 18));
		mess.setForeground(Color.black);
		mess.setBorder(new EmptyBorder(5, 5, 5, 5));
		if (!sender) {
			pmess.setVisible(true);
			Pseudo.setVisible(true);
		}
		Home.destinataire.add(pmess, "wrap, w 90%");
		Home.destinataire.add(Pseudo,"wrap");
		Home.destinataire.revalidate();
	}



	//Specify the look and feel to use.  Valid values:
	//null (use the default), "Metal", "System", "Motif", "GTK+"
	final static String LOOKANDFEEL = "System";

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