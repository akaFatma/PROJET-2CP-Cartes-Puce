package jsr268gp.sampleclient;

import javax.smartcardio.*;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import jsr268gp.util.Util;

public class ADMINLOGIN extends JFrame {
	private static Socket socket;
    private static BufferedReader reader;
    private static DataInputStream dataIn;
    private static DataOutputStream dataOut;

    public ADMINLOGIN() {
        super("Bienvenue sur eVote");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        setSize(screenWidth, screenHeight);

        try {
        	//Connexion au serveur
            socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1", 5001), 1000);
            System.out.println("Connection successful");

            reader = new BufferedReader(new InputStreamReader(System.in));
            dataIn = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur lors de la connexion au serveur", "Erreur de connexion", JOptionPane.ERROR_MESSAGE);
        }

        if (socket != null && socket.isConnected()) {

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(new Color(250, 249, 246));
        setContentPane(mainPanel);

        JLabel backgroundLabel = new JLabel();URL imageUrl = getClass().getResource("vote.png");
        if (imageUrl != null) {
        ImageIcon backgroundImage = new ImageIcon(imageUrl);
        backgroundLabel.setIcon(backgroundImage);
        backgroundLabel.setBounds(560, 50, 1000, 101);
        mainPanel.add(backgroundLabel);
        }
        JPanel loginPanel = createLoginPanel();
        loginPanel.setBounds(360, 160, 600, 400);
        mainPanel.add(loginPanel);

        setVisible(true);

        }
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(35, 48, 71));
        panel.setLayout(null);

        JLabel connectLabel = new JLabel("Se connecter");
        connectLabel.setFont(new Font("Poppins", Font.BOLD, 50));
        connectLabel.setForeground(new Color(255, 255, 255));
        connectLabel.setBounds(145, 30, 400, 60);
        panel.add(connectLabel);

        JLabel idLabel = new JLabel("ID");
        idLabel.setFont(new Font("Poppins", Font.BOLD, 24));
        idLabel.setForeground(new Color(255, 255, 255));
        idLabel.setBounds(50, 110, 100, 30);
        panel.add(idLabel);

        final JTextField idTextField = new JTextField();
        idTextField.setFont(new Font("Poppins", Font.PLAIN, 20));
        idTextField.setBounds(50, 150, 500, 40);
        panel.add(idTextField);

        JLabel passwordLabel = new JLabel("Mot de Passe");
        passwordLabel.setFont(new Font("Poppins", Font.BOLD, 24));
        passwordLabel.setForeground(new Color(255, 255, 255));
        passwordLabel.setBounds(50, 190, 200, 30);
        panel.add(passwordLabel);

        final JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(new Font("Poppins", Font.PLAIN, 20));
        passwordField.setBounds(50, 230, 500, 40);
        panel.add(passwordField);

        JButton loginButton = new JButton("Se Connecter");
        loginButton.setFont(new Font("Poppins", Font.BOLD, 24));
        loginButton.setBackground(new Color(140,203,227));
        loginButton.setForeground(new Color(255, 255, 255));
        loginButton.setBounds(170, 290, 300, 50);
        panel.add(loginButton);

        final JCheckBox robotCheckbox = new JCheckBox();
        robotCheckbox.setBounds(180, 355, 20, 20);
        panel.add(robotCheckbox);

        JLabel robotLabel = new JLabel("Je ne suis pas un robot !");
        robotLabel.setFont(new Font("Poppins", Font.PLAIN, 20));
        robotLabel.setForeground(new Color(255, 255, 255));
        robotLabel.setBounds(204, 350, 300, 30);
        panel.add(robotLabel);
        loginButton.addActionListener((ActionListener) new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String idtext = idTextField.getText();
                    String passwordText = new String(passwordField.getPassword());
                    String hashedPassword = hashText(passwordText);
        
                    //Verifier que tous les champs ont été remplis
                    if (!idtext.isEmpty() && !passwordText.isEmpty()) {
                        if (robotCheckbox.isSelected()) {
                        	
                        	//Envoie des informations de l'admin pour l'authentifier
                            dataOut.writeUTF("LOGIN_ADMIN");
                            dataOut.writeUTF(idtext);
                            dataOut.writeUTF(hashedPassword);
        
                            String response = dataIn.readUTF();
                            System.out.println("Authentification de l'administrateur : "+response);
        
                            if (response.equals("SUCCESS")) {
                            	
                                TerminalFactory tf = TerminalFactory.getDefault();
                                CardTerminals list = tf.terminals();
                                CardTerminal cad = list.getTerminal("ACS ACR1281 1S Dual Reader PICC 0");
                                System.out.println("Card: " + cad.isCardPresent());
                                Card c = cad.connect("T=0");
                    			ATR atr = c.getATR();
                    			System.out.println("ATR: "+ Util.byteArrayToHexString(atr.getBytes(), " ") + "\n");
                    			CardChannel canal = c.getBasicChannel();
                    			CommandAPDU commande = new CommandAPDU(new byte[] { (byte) 0x00,
                    					(byte) 0xA4, (byte) 0x04, (byte) 0x00, (byte) 0x07,
                    					(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    					(byte) 0x04, (byte) 0x00, (byte) 0x02 });
                    			ResponseAPDU reponse = canal.transmit(commande);
                    			System.out.println("Reponse SELECT : "+ Util.byteArrayToHexString(reponse.getBytes(), " "));
                                
                                /********* Commande APDU de GET ID *********/
                                commande = new CommandAPDU(0, 2, 3, 4, 4);
       
                                // Envoyer la commande APDU
                                System.out.println("Commande ID : " + Util.byteArrayToHexString(commande.getBytes(), " "));
                                reponse = canal.transmit(commande);
        
                                // Afficher la reponse APDU
                                System.out.println("Reponse ID : " + Util.byteArrayToHexString(reponse.getBytes(), " ")+'\n');
                                
                                dataOut.writeUTF("EXIT");
                                
                                if (new BigInteger(1, reponse.getData()).compareTo(BigInteger.ZERO) == 0) {
                                	//La carte est vide, permettre de la personnaliser
                                    dispose();
                                    new Personnalisation().setVisible(true);
                                } else {
                                    //Carte deja personnalisée
                                    JOptionPane.showMessageDialog(null, "Carte deja personnalisee", "Personnalisation impossible", JOptionPane.ERROR_MESSAGE);
                                    dispose();
                                    new inter1().setVisible(true);
                                }
                                socket.close();
                                reader.close();
                                dataIn.close();
                                dataOut.close();
                            } else {
                            	//ID ou mot de passe incorrect
                                JOptionPane.showMessageDialog(null, "Identifiant ou mot de passe incorrect", "Probleme d'authentification", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                        	//Case "Je ne suis pas un robbot" n'est pas cochée
                            JOptionPane.showMessageDialog(null, "Veuillez cocher la case \"Je ne suis pas un Robot !\"", "Probleme d'authentification", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                    	//L'un des champs est vide 
                        JOptionPane.showMessageDialog(null, "Veuillez remplir tous les champs", "Probleme d'authentification", JOptionPane.ERROR_MESSAGE);
                    }
               } catch (Exception ex) {
            	   //Erreur de connexion au serveur 
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Erreur lors de la connexion au serveur", "Erreur de connexion", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }

    public static void main(String[] args) {
    	SwingUtilities.invokeLater(new Runnable() {
    	    public void run() {
    	        new ADMINLOGIN();
    	    }
    	});
    }
    
    //Retourne le hash du text en entrée
    public static String hashText(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(text.getBytes());
            byte[] hashBytes = digest.digest();
            return Util.byteArrayToHexString(hashBytes, "");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
