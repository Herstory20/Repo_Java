package IHM;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import BDD.JDBC;
/**
 *
 *
 */
import Network.NetworkManager;
import Network.Exceptions.InvalidPseudoException;
public class LoginScreen implements ActionListener  {
    
    private JTextField textField;
    private JLabel label =new JLabel("Attention !! Pas de caractères spéciaux !", JLabel.CENTER);
    private JButton button;
    private JPanel pane;
    private GraphicsConfiguration gc;
    private static JFrame frame ;
    private static NetworkManager nm;
    
    //Specify the look and feel to use.  Valid values:
    //null (use the default), "Metal", "System", "Motif", "GTK+"
    final static String LOOKANDFEEL = "System";
    
    public Component createComponents() {
        pane = new JPanel();
        pane.setLayout(new GridLayout(4,1));
        
        JLabel consigne = new JLabel("Entrez un pseudo pour cette connexion!",
                           JLabel.CENTER);
        consigne.setFont(new Font("Tahoma", Font.PLAIN, 20));
        pane.add(consigne);
        
        JPanel pane2= new JPanel();
        pane2.setLayout(new GridLayout(1,2));
        JLabel lblLogin= new JLabel("Login",JLabel.CENTER);
        lblLogin.setFont(new Font("Tahoma", Font.PLAIN,20));
        pane2.add(lblLogin);
        textField = new JTextField();
        textField.setFont(new Font("Tahoma", Font.PLAIN, 32));
        pane2.add(textField);
        pane.add(pane2);
        
        label.setFont(new Font("Tahoma", Font.PLAIN, 15));
        pane.add(label);
        
        button = new JButton("Se connecter");
        button.setFont(new Font("Tahoma", Font.PLAIN,20));
        button.addActionListener(this);
        pane.add(button);
        return pane;
    }
    
    private static void initNetwork() {
		try {
			nm = NetworkManager.getInstance();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private static boolean tryConnexion(String pseudo) throws InvalidPseudoException {
		boolean connexionOK = false;
		try {
			connexionOK = nm.connexion(pseudo);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return connexionOK;
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        String userName = textField.getText();
        Pattern p = Pattern.compile("[^A-Za-z0-9]");
        Matcher m = p.matcher(userName);
       // boolean b = m.matches();
        boolean b = m.find();
        if (b) {
        	label.setForeground(Color.RED);
        	label.setText("<html>Erreur !! Vous avez un caractère spécial, \n"
        			+ "veuillez rentrer un nouveau pseudo.</html>");
        }
        else if ((textField.getText().length()==0)) {
        	label.setForeground(Color.RED);
        	label.setText("<html>Erreur !! Vous ne pouvez pas rentrer un pseudo vide.</html>");
        }
        else {
        	JDBC app = new JDBC();
        	if (!app.IsLoginUsed(userName)) {
        		try {
					tryConnexion(userName);
	        		frame.dispose();
	                Home Inter = Home.getInstance();
	                Inter.setVisible(true);
	        		JOptionPane.showMessageDialog(button, "You have successfully logged ");
				} catch (InvalidPseudoException e1) {
		        	label.setText("<html>Erreur !! Pseudo invalidé par un autre utilisateur.</html>");
				} catch(IOException e2) {
					label.setText("<html>Erreur !! Impossible de créer la page 'Home'.</html>");
				}
        		
        	}
        	else {
        		label.setForeground(Color.GREEN);
        		label.setText("<html>Veuillez changer de pseudo, il est déjà utilisé.</html>");
        	}
        }	
   }
    
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
    
    public static void createAndShowGUI() {
        //Set the look and feel.
        initLookAndFeel();
        
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);
        //Create and set up the window.
        frame = new JFrame("Connexion");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds((int)size.getWidth()/2-800/2, (int)size.getHeight()/2-450/2, 800, 450);
        LoginScreen app = new LoginScreen();
        initNetwork();
        Component contents = app.createComponents();
        frame.getContentPane().add(contents, BorderLayout.CENTER);
        //Display the window.
        frame.setVisible(true);
    }
    
    public static void launch() {
    	//Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	LoginScreen.createAndShowGUI();
            }
        });
    }
    
}
