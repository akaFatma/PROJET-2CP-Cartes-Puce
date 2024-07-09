package jsr268gp.sampleclient;




import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
import javax.swing.*;

//import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

import jsr268gp.util.Util;

public class USERLOGIN extends JFrame {
    private static Socket socket;
    private static BufferedReader reader;
    private static DataInputStream dataIn;
    private static DataOutputStream dataOut;


	final static short SW_VERIFICATION_FAILED = 0x6300;

    public USERLOGIN() {
        super("Bienvenue sur eVote");
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
           
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        setSize(screenWidth, screenHeight);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null); // Utilisation d'une disposition null pour positionner manuellement les composants
        mainPanel.setBackground(new Color(229, 229, 229)); // Couleur de fond du panneau principal
        setContentPane(mainPanel);

        JLabel backgroundLabel = new JLabel();
        URL imageUrl = getClass().getResource("vote.png");
        if (imageUrl != null) {
        ImageIcon backgroundImage = new ImageIcon(imageUrl);
        backgroundLabel.setIcon(backgroundImage);
        backgroundLabel.setBounds(540, 50, 1000, 101);
        }
        mainPanel.add(backgroundLabel);

        JPanel loginPanel = createLoginPanel();
        loginPanel.setBounds(340, 160, 600, 400); // Position et taille du panneau de connexion
        mainPanel.add(loginPanel);

        setVisible(true);}
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(35, 48, 71)); // Couleur de fond du panneau de connexion
        panel.setLayout(null); // Utilisation d'une disposition null pour positionner manuellement les composants

        JLabel connectLabel = new JLabel("Se connecter");
        connectLabel.setFont(new Font("Poppins", Font.BOLD, 50));
        connectLabel.setForeground(new Color(255, 255, 255));
        connectLabel.setBounds(141, 30, 400, 60); // Position et taille du titre
        panel.add(connectLabel);

        JLabel pinLabel = new JLabel("PIN");
        pinLabel.setFont(new Font("Poppins", Font.BOLD, 24));
        pinLabel.setForeground(new Color(255, 255, 255));
        pinLabel.setBounds(50, 130, 100, 30); // Position et taille du label ID
        panel.add(pinLabel);
        final JPasswordField pinField = new JPasswordField();
        pinField.setFont(new Font("Poppins", Font.PLAIN, 20));
        pinField.setBounds(50, 170, 500,50);
        panel.add(pinField);
        
       

        JButton loginButton = new JButton("Se Connecter");
        loginButton.setFont(new Font("Poppins", Font.BOLD, 24));
        loginButton.setBackground(new Color(140,203,227));
        loginButton.setForeground(new Color(255, 255, 255));
        loginButton.setBounds(145, 285, 300, 50);
        panel.add(loginButton);

        
      
        final JCheckBox robotCheckbox = new JCheckBox();
        robotCheckbox.setBounds(175, 345, 20, 20); // Position et taille de la case a cocher
        panel.add(robotCheckbox);

        JLabel robotLabel = new JLabel("Je ne suis pas un robot !");
        robotLabel.setFont(new Font("Poppins", Font.PLAIN, 20));
        robotLabel.setForeground(new Color(255, 255, 255));
        robotLabel.setBounds(201, 340, 300, 30); // Position et taille du label Robot
        panel.add(robotLabel);
        loginButton.addActionListener((ActionListener) new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try{
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
        			System.out.println("Reponse SELECT : "+ Util.byteArrayToHexString(reponse.getBytes(), " ")+'\n');

        			//Convertir le pin de Sting vers byte[]
        			String pinString = new String(pinField.getPassword());
                    //Verifier que toutes les champs ont été remplis
                    if (!pinString.isEmpty()) {
                        if (robotCheckbox.isSelected()) {
                        	//Verifier que le PIN a une valeure numerique
        					if (pinString.matches("[0-9]+")){
//        						if((Integer.parseInt(pinString)>=1000)&&(Integer.parseInt(pinString)<=9999)){
        							if(pinString.length() == 4){	
        			byte[] pin =Util.hexStringToByteArray(Integer.toHexString(Integer.parseInt(pinString)));
        			if (pin.length == 1) {
    		            // Si la longueur du pin array est de 1 byte, prefixer par 0x00
    					byte ext = pin[0];
    		            pin = new byte[2];
    		            pin[0] = (byte)0x00;
    		            pin[1] = ext;
    				}
                    System.out.println("PIN : "+Util.byteArrayToHexString(pin, " "));
                        /********* Commande APDU de PIN *********/
                        commande = new CommandAPDU(0, 1, 3, 0, pin , 0);
                        
                        // Envoyer la commande APDU
                        System.out.println("Commande PIN: " + Util.byteArrayToHexString(commande.getBytes(), " "));
                        reponse = canal.transmit(commande);
                            
                        // Afficher la réponse APDU
                        System.out.print("Reponse PIN : " + Util.byteArrayToHexString(reponse.getBytes(), " ")+'\n');
                        
                        if (reponse.getSW() == SW_VERIFICATION_FAILED) {
                        //La carte est bloquee apres 3 essais 
                          JOptionPane.showMessageDialog(null, "Carte Bloquée", "PIN incorrect", JOptionPane.ERROR_MESSAGE);
                          dispose();
                          new inter1().setVisible(true);
                        } else if (reponse.getSW() != 0x9000) {
                        //PIN incorrect
                           JOptionPane.showMessageDialog(null, "PIN incorrect", "PIN incorrect", JOptionPane.ERROR_MESSAGE);
                        } else {
                           JOptionPane.showMessageDialog(null, "PIN correct", "PIN correct", JOptionPane.INFORMATION_MESSAGE);
                           
                           /********* Commande APDU de ID *********/
                      		commande = new CommandAPDU(0,2, 3, 4, 4);
                      			
                      		//Envoyer la commande APDU
                      		System.out.println("Commande PIN: " + Util.byteArrayToHexString(commande.getBytes(), " "));
                            reponse = canal.transmit(commande);
                      			
                      		//Afficher la réponse APDU
                      		System.out.println("Reponse ID : "+Util.byteArrayToHexString(reponse.getBytes()," ")+'\n');
                      		
                      		//Verifier si cette carte a deja vote ou pas
                      		dataOut.writeUTF("A_VOTE");
                      		dataOut.flush();
                      		dataOut.write(reponse.getData());
                      		dataOut.flush();
                      		
                      		//Recuperer la reponse
                      		String rep = dataIn.readUTF();
                            dataOut.writeUTF("EXIT");
               
                                if(rep.equals("PAS_VOTE")){
                                	//Ce voteur n'a pas encore voté donc lui accorder l'accces
                                	dispose();
                                	new VoteInterface().setVisible(true);
                                	}else{
                                		//Ce voteur a deja voté donc lui bloquer l'acces 
                                		JOptionPane.showMessageDialog(null, "Vous avez deja voté", "Erreur de connexion", JOptionPane.ERROR_MESSAGE);
                                		new inter1().setVisible(true);
                                	}
                            }
        						}else{
        							//Valeur du PIN saisi n'est pas sur 4 digits 
        	   						 JOptionPane.showMessageDialog(null, "Valeur invalide pour PIN", "Erreur de personnalisation", JOptionPane.ERROR_MESSAGE);
        	   					}
        					}else{
        						//Le PIN saisi n'est pas une valeur numerique
       						 JOptionPane.showMessageDialog(null, "Le PIN doit etre une valeure numerique ", "Erreur de personnalisation", JOptionPane.ERROR_MESSAGE);
       					}
                        } else {
                        	//Case "Je ne suis pas un robbot" n'est pas cochée
                            JOptionPane.showMessageDialog(null, "Veuillez cocher la case \"Je ne suis pas un Robot\" !", "Erreur de connexion", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        //Le champs PIN est vide
                        JOptionPane.showMessageDialog(null, "Veuillez saisir votre PIN", "Erreur de connexion", JOptionPane.ERROR_MESSAGE);
                    }
                } catch(Exception ex){
                    System.out.println(ex);
                }
            }
        });

        return panel;
        }
    

    public static void main(String[] args) {
    	SwingUtilities.invokeLater(new Runnable() {
    	    public void run() {
    	        new USERLOGIN();
    	    }
    	});

    }
}



