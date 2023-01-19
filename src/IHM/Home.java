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
import Network.Exceptions.InvalidPseudoException;
import net.miginfocom.swing.MigLayout;

import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

public class Home extends JFrame {

	
	
	
	private JPanel messagebox = new JPanel();
	private JPanel destinataire = new JPanel();
	private JPanel expediteur = new JPanel();
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
        setBounds((int) size.getWidth() / 2 - 1280 / 2, (int) size.getHeight() / 2 - 720 / 2, 1280, 720);
        getContentPane().setBackground(Color.PINK);

        JScrollPane username = new JScrollPane();
        username.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        username.getVerticalScrollBar().setUnitIncrement(10);

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
					//try {
						//Creer le thread si conversation existe pas
						/*
						 * if(!ConversationsManager.getInstance().isConversationExist(currentIpInterlocutor)) {
							NetworkManager.getInstance().newDiscussion(currentIpInterlocutor);
						}
						ConversationsManager.getInstance().send(currentIpInterlocutor, tosend);
						*/
	                    addMessagesend(tosend);
	                    addMessagereceive(tosend);
					/*} catch (IOException | ConversationNotFound e) {
						e.printStackTrace();
					}*/
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


        JScrollPane messbox = new JScrollPane();
        messbox.getVerticalScrollBar().setUnitIncrement(10);

        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(
            groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                    .addGroup(groupLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(username, GroupLayout.PREFERRED_SIZE, 176, GroupLayout.PREFERRED_SIZE))
                    .addGroup(groupLayout.createSequentialGroup()
                        .addGap(11)
                        .addComponent(ChangeLogin, GroupLayout.PREFERRED_SIZE, 179, GroupLayout.PREFERRED_SIZE)))
                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                    .addGroup(groupLayout.createSequentialGroup()
                        .addGap(7)
                        .addComponent(textfield, GroupLayout.DEFAULT_SIZE, 992, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(btnNewButton)
                        .addGap(10))
                    .addGroup(groupLayout.createSequentialGroup()
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(messbox, GroupLayout.DEFAULT_SIZE, 1072, Short.MAX_VALUE)
                        .addGap(12))))
        );
        groupLayout.setVerticalGroup(
            groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                    .addGroup(groupLayout.createSequentialGroup()
                        .addComponent(messbox, GroupLayout.DEFAULT_SIZE, 611, Short.MAX_VALUE)
                        .addGap(1))
                    .addComponent(username, GroupLayout.DEFAULT_SIZE, 613, Short.MAX_VALUE))
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
                .addContainerGap())
        );

        messbox.setViewportView(messagebox);
        messagebox.setLayout(new GridLayout(0, 2, 0, 0));

        destinataire.setBackground(Color.LIGHT_GRAY);
        messagebox.add(destinataire);
        destinataire.setLayout(new MigLayout("", "[]", "[]"));

        expediteur.setBackground(Color.LIGHT_GRAY);
        messagebox.add(expediteur);
        expediteur.setLayout(new MigLayout("", "[]", "[]"));



		JPanel Users = new JPanel();
		username.setViewportView(Users);
		Users.setLayout(new MigLayout("fillx"));
		tabUsers = new ArrayList<>();
		JDBC app = JDBC.getInstance();
		TabPseudo = app.selectPseudoA();
		
		for (int i=0; i<TabPseudo.size();i++) {
			JButton tmp = new JButton(TabPseudo.get(i));
			tmp.setLayout(null); 
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

    public void addMessagesend(String tosend) {
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
                graphics.setColor(Color.green);
                graphics.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height); //paint background
                graphics.setColor(Color.green);
                graphics.drawRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height); //paint border
            }
        };
        JLabel mess = new JLabel(tosend);
        pmess.setOpaque(false);
        pmess.add(mess);
        JLabel Pseudo = new JLabel("<html> \n date \n </html>");
        Pseudo.setFont(new Font("New Times Roman", Font.ITALIC, 10));
        mess.setFont(new Font("New Times Roman", Font.PLAIN, 18));
        mess.setForeground(Color.black);
        mess.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.expediteur.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        this.expediteur.add(pmess, "wrap");
        this.expediteur.add(Pseudo, "wrap");
        this.expediteur.revalidate();
    }

    public void addMessagereceive(String tosend) {
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
                graphics.setColor(Color.green);
                graphics.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height); //paint background
                graphics.setColor(Color.green);
                graphics.drawRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height); //paint border
            }
        };
        JLabel mess = new JLabel(tosend);
        pmess.setOpaque(false);
        pmess.add(mess);
        JPanel videmess = pmess;
        videmess.setVisible(false);
        mess.setFont(new Font("New Times Roman", Font.PLAIN, 18));
        mess.setForeground(Color.black);
        mess.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.expediteur.add(videmess, "wrap");
        this.destinataire.add(pmess, "wrap");
        this.destinataire.revalidate();
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