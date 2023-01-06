import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.border.EmptyBorder;

public class Home extends JFrame {
	
	//Specify the look and feel to use.  Valid values:
    //null (use the default), "Metal", "System", "Motif", "GTK+"
    final static String LOOKANDFEEL = "GTK+";
    
    private JTextField textField;
    private JLabel label =new JLabel("!");
    private JPanel pane;
    private JButton button;
    private GraphicsConfiguration gc;
    private static JFrame frame ;
    
    
    public Component createComponents() {
    	pane = new JPanel();
    	pane.setBackground(Color.PINK);
        pane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        JPanel Chatview = new JPanel();
        
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5,5,5,5);
        
        c.gridx = 4;
        c.gridy = 0;
        c.gridwidth=1;
        c.gridheight=3;
        c.weightx = 1.0;
        c.weighty=1.0;
        pane.add(Chatview,c);
        
        JPanel paneUtilisateur = new JPanel();
        paneUtilisateur.setLayout(new GridLayout(13,1));
        for (int r=0 ; r<50 ;r++) {
        	button = new JButton("Nom");
        	button.setBackground(Color.CYAN);
        	button.setFont(new Font("normal",Font.PLAIN,12));
        	button.addActionListener(null);
        	paneUtilisateur.add(button);
        }
        JScrollPane scrlpane = new JScrollPane(paneUtilisateur);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth=3;
        c.gridheight=3;
        c.weightx = 0.1;
        c.weighty=0.0;
        pane.add(scrlpane,c);
        
        JButton cpseudo = new JButton("Change");
        cpseudo.setBackground(Color.GRAY);
        cpseudo.setFont(new Font("normal", Font.ITALIC,8));
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth=3;
        c.gridheight=1;
        c.weightx = 0.0;
        c.weighty=0.0;
        pane.add(cpseudo,c);
        
        button = new JButton("Envoyer");
        button.setBackground(Color.GRAY);
        button.setFont(new Font("normal", Font.ITALIC,8));
        button.addActionListener(null);
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth=3;
        c.gridheight=1;
        c.weightx = 0.0;
        c.weighty=0.0;
        pane.add(button,c);
        
        textField = new JTextField();
        textField.setFont(new Font("Tahoma", Font.PLAIN, 32));
        c.gridx = 3;
        c.gridy = 3;
        c.gridwidth=2;
        c.gridheight=2;
        c.weightx = 0.0;
        c.weighty=0.1;
        pane.add(textField,c);
    	return pane;
    }
    
    public void actionPerformed(ActionEvent ae) {
        String userName = textField.getText();
        Pattern p = Pattern.compile("[^A-Za-z0-9]");
        Matcher m = p.matcher(userName);
       // boolean b = m.matches();
        boolean b = m.find();
        if (b) {
        	label.setForeground(Color.RED);
        	label.setText("<html>Erreur !! Vous avez un caractère spéciale, \n"
        			+ "veuillez rentrer un nouveau pseudo.</html>");
        }
        else if ((textField.getText().length()==0)) {
        	label.setForeground(Color.RED);
        	label.setText("<html>Erreur !! Vous ne pouvez pas rentrer un pseudo vide.</html>");
        }
        else {
        	JDBC app = new JDBC();
        	if (!app.IsLoginUsed(userName)) {
        		frame.dispose();
                Home Inter = new Home();
                Inter.createAndShowGUI();
                Inter.setVisible(true);
        		JOptionPane.showMessageDialog(button, "You have successfully logged ");
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
        JFrame frame = new JFrame("Clavardeur");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds((int)size.getWidth()/2-1280/2, (int)size.getHeight()/2-720/2, 1280, 720);
        Home app = new Home();
        Component contents = app.createComponents();
        frame.getContentPane().add(contents);
        
        //Display the window.
        frame.setVisible(true);
    }
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 javax.swing.SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	        	createAndShowGUI();
	        }
		 });
	}
}