/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.regex.Pattern;
import javax.swing.border.EmptyBorder;
/**
 *
 *
 */
public class LoginScreen implements ActionListener  {
    
    private JTextField textField;
    private JLabel label =new JLabel("");
    private JButton button;
    private JPanel pane;
    
    //Specify the look and feel to use.  Valid values:
    //null (use the default), "Metal", "System", "Motif", "GTK+"
    final static String LOOKANDFEEL = "Motif";
    
    public Component createComponents() {
        pane = new JPanel();
        pane.setLayout(null);
        JLabel lblLogin= new JLabel("Login");
        lblLogin.setFont(new Font("Tahoma", Font.PLAIN,20));
        lblLogin.setBounds(100, 150, 100, 50);
        pane.add(lblLogin);
        
        JLabel consigne = new JLabel("Entrez un pseudo pour cette connexion!",
                           JLabel.CENTER);
        consigne.setFont(new Font("Tahoma", Font.PLAIN, 20));
        consigne.setBounds(100,50,500,50);
        pane.add(consigne);
        textField = new JTextField();
        textField.setFont(new Font("Tahoma", Font.PLAIN, 32));
        textField.setBounds(225, 150, 400, 50);
        pane.add(textField);
        textField.setColumns(10);
        
        button = new JButton("Se connecter");
        button.setFont(new Font("Tahoma", Font.PLAIN,20));
        button.setBounds(300, 300, 200, 75);
        button.addActionListener(this);
        pane.add(button);
        return pane;
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        String userName = textField.getText();
        JDBC app = new JDBC();
        if (app.IsLoginUsed(userName)) {
        	JOptionPane.showMessageDialog(button, "You have successfully logged ");
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
    
    private static void createAndShowGUI() {
        //Set the look and feel.
        initLookAndFeel();
        
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);
        //Create and set up the window.
        JFrame frame = new JFrame("Connexion");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(500, 250, 750, 450);
        LoginScreen app = new LoginScreen();
        Component contents = app.createComponents();
        frame.getContentPane().add(contents, BorderLayout.CENTER);
        
        //Display the window.
        frame.setVisible(true);
    }
    
    
    
    
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
        
        }

    
}
