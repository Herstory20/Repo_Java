package IHM;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import BDD.JDBC;
import net.miginfocom.swing.MigLayout;

import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

public class Home extends JFrame {
	
	public Home() {
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
					//Creer le thread si conversation existe pas
					//addMessagesend(tosend);
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
		
		JDBC app = JDBC.getInstance();
		ArrayList<String> TabPseudo = app.selectPseudoA();
		for (int i=0; i< 20 /*app.selectCountA()*/;i++) {
			JButton tmp = new JButton("Login" + i /*TabPseudo.get(i)*/);
			Users.add(tmp, "wrap");
		};
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