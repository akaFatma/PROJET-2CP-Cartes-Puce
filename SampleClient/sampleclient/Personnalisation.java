package jsr268gp.sampleclient;



import javax.smartcardio.*;
import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import jsr268gp.util.Util;

public class Personnalisation extends JFrame {


    private static Socket socket;
    private static BufferedReader reader;
    private static DataInputStream dataIn;
    private static DataOutputStream dataOut;



    private JLabel messageLabel; // Champ de classe pour le JLabel

    public Personnalisation() {
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
            getContentPane().setBackground(new Color(229, 229, 229));

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(null);
            mainPanel.setBackground(new Color(250, 249, 246));
            add(mainPanel);

            JPanel navbarPanel = createNavbarPanel();
            mainPanel.add(navbarPanel);

            JPanel contentPanel = new JPanel();
            contentPanel.setBackground(new Color(250, 249, 246));
            contentPanel.setLayout(null);
            contentPanel.setBounds(265, 30, 848, 700);
            mainPanel.add(contentPanel);

            JPanel infoPanel = new JPanel();
            infoPanel.setBackground(Color.WHITE);
            infoPanel.setLayout(null);
            infoPanel.setBounds(70, 150, 700, 400);

            JLabel profileLabel = new JLabel("Votant");
            profileLabel.setFont(new Font("Poppins", Font.BOLD, 40));
            profileLabel.setForeground(new Color(45, 51, 74));
            profileLabel.setBounds(80, 20, 300, 30);
            infoPanel.add(profileLabel);

            JLabel nomLabel = new JLabel("Nom:");
            nomLabel.setFont(new Font("Poppins", Font.BOLD, 20));
            nomLabel.setBounds(90, 80, 80, 30);
            infoPanel.add(nomLabel);

            JLabel prenomLabel = new JLabel("Prenom:");
            prenomLabel.setBounds(90, 140, 80, 30);
            prenomLabel.setFont(new Font("Poppins", Font.BOLD, 20));
            infoPanel.add(prenomLabel);

            JLabel pinLabel1 = new JLabel("PIN :");
            pinLabel1.setBounds(90, 200, 80, 30);
            pinLabel1.setFont(new Font("Poppins", Font.BOLD, 20));
            infoPanel.add(pinLabel1);
            
            JLabel pinLabel2 = new JLabel("PIN : ");
            pinLabel2.setBounds(90, 260, 80, 30);
            pinLabel2.setFont(new Font("Poppins", Font.BOLD, 20));
            infoPanel.add(pinLabel2);

            final JTextField nomField = new JTextField();
            nomField.setBounds(240, 80, 290, 30);
            infoPanel.add(nomField);

            final JTextField prenomField = new JTextField();
            prenomField.setBounds(240, 140, 290, 30);
            infoPanel.add(prenomField);

            final JPasswordField pinField1 = new JPasswordField();
            pinField1.setFont(new Font("Poppins", Font.PLAIN, 20));
            pinField1.setBounds(240, 200, 290, 30);
            infoPanel.add(pinField1);

            final JPasswordField pinField2 = new JPasswordField();
            pinField2.setFont(new Font("Poppins", Font.PLAIN, 20));
            pinField2.setBounds(240, 260, 290, 30);
            infoPanel.add(pinField2);
          

           JButton customizeButton = createStyledButton("Personnaliser", new Color(140,203,227));
           
            customizeButton.setBounds(240, 320, 200, 50);
            customizeButton.setBorder(BorderFactory.createBevelBorder(70));
            customizeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
    				String pin1 = new String(pinField1.getPassword());
    				String pin2 = new String(pinField2.getPassword());
    				
    				//Verifier que toutes les champs ont été remplis
                if (!nomField.getText().isEmpty() && !prenomField.getText().isEmpty()&& !pin1.isEmpty()&& !pin2.isEmpty()) {
                	//Verifier que les deux champs PIN sont égaux
                	if(pin1.equals(pin2)){
                		//Verifier que le PIN a une valeure numerique
    					if (pin1.matches("[0-9]+")){
    						//Verifier que le PIN est composé de 4 digits 
//    						if((Integer.parseInt(pin1)>=1000)&&(Integer.parseInt(pin1)<=9999)){
    						if(pin1.length() == 4){	
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

            			//Retourne la valeur du ID consacrée à cette carte 
        				dataOut.writeUTF("GET_NEW_ID");
        				dataOut.flush();

        				byte[] ID = Util.toByteArray(new BigInteger(dataIn.readUTF()));
        				System.out.println("id" + Util.byteArrayToHexString(ID, " "));
        				
        				/********* Commande APDU de ID *********/
        				commande = new CommandAPDU(0, 4, 1, 0, ID, 0);

        				// Envoyer la commande APDU
        				System.out.println("Commande ID : " + Util.byteArrayToHexString(commande.getBytes(), " "));
                        reponse = canal.transmit(commande);

        				// Afficher la réponse APDU
        				System.out.println("Reponse ID : "+ Util.byteArrayToHexString(reponse.getBytes(), " ")+'\n');
        				
        				//Generation de la paire de cles
        				KeyPair keyPair;
        				keyPair = generateKey();
        				
        				RSAPublicKey cardPublicKey = (RSAPublicKey) keyPair.getPublic();
        				RSAPrivateKey cardPrivateKey = (RSAPrivateKey) keyPair.getPrivate();

        				byte[] exponentBytes = Util.toByteArray(cardPrivateKey.getPrivateExponent());
        				byte[] modulusBytes = Util.toByteArray(cardPrivateKey.getModulus());

        				//Envoie des informations du nouveau voteur : cle publique, nom, prenom
        				dataOut.writeUTF("CREATE_NEW_VOTER");
        				dataOut.writeUTF(Util.byteArrayToHexString(cardPublicKey.getEncoded(), ""));
                        dataOut.writeUTF(prenomField.getText());
                        dataOut.writeUTF(nomField.getText());

        				/********* Commande APDU de MODULUS *********/
                        //Envoyer le modulo de la cle privee a la carte
        				commande = new CommandAPDU(0, 1, 1, 0, modulusBytes, 0);

        				// Envoyer la commande APDU
        				System.out.println("Commande mod: " + Util.byteArrayToHexString(commande.getBytes(), " "));
        				reponse = canal.transmit(commande);

        				// Afficher la réponse APDU
        				System.out.println("Reponse mod : "+ Util.byteArrayToHexString(reponse.getBytes(), " ")+'\n');

        				/********* Commande APDU de PRIVATE EXPONENT *********/
        				//Envoyer l'exposant de la cle privee a la carte
        				commande = new CommandAPDU(0, 2, 1, 0, exponentBytes, 0);

        				// Envoyer la commande APDU
        				System.out.println("Commande exp: " + Util.byteArrayToHexString(commande.getBytes(), " "));
        				reponse = canal.transmit(commande);

        				// Afficher la réponse APDU
        				System.out.println("Reponse exp : "+ Util.byteArrayToHexString(reponse.getBytes(), " ")+'\n');

        				/********* Commande APDU de PIN *********/
        				//Envoyer le code PIN a la carte 
        				byte[] pin =Util.hexStringToByteArray(Integer.toHexString(Integer.parseInt(pin1)));
        				if (pin.length == 1) {
        		            // Si la longueur du pin array est de 1 byte, prefixer par 0x00
        					byte ext = pin[0];
        		            pin = new byte[2];
        		            pin[0] = (byte)0x00;
        		            pin[1] = ext;
        				}
        				commande = new CommandAPDU(0, 3, 1, 0,pin,0);

        				// Envoyer la commande APDU
        				System.out.println("Commande PIN: " + Util.byteArrayToHexString(commande.getBytes(), " "));
        				reponse = canal.transmit(commande);

        				// Afficher la réponse APDU
        				System.out.println("Reponse PIN : "+ Util.byteArrayToHexString(reponse.getBytes(), " ")+'\n');



                        // Recuperer la reponse du serveur
                        String response = dataIn.readUTF();

                        if (response.equals("SUCCESS")) {
                            dataOut.writeUTF("EXIT");
                            // Mettre a jour le texte du JLabel
                            messageLabel.setText("La carte a ete personnalisee avec succes !");
                            socket.close();
                            reader.close();
                            dataIn.close();
                            dataOut.close();
                        } else {
                        	//Probleme de connexion au serveur 
                            JOptionPane.showMessageDialog(null, "Erreur lors de la connexion au serveur", "Erreur de connexion", JOptionPane.ERROR_MESSAGE);
                        }


                    }catch(Exception ex){
                    	//Probleme de connexion au serveur 
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Erreur lors de la connexion au serveur", "Erreur de connexion", JOptionPane.ERROR_MESSAGE);
                    }
    						}else{
    							//Valeur du PIN saisi n'est pas sur 4 digits 
    	   						 JOptionPane.showMessageDialog(null, "Valeur invalide pour PIN", "Erreur de personnalisation", JOptionPane.ERROR_MESSAGE);
    	   					}
    					}else{
    						//Le PIN saisi n'est pas une valeur numerique
   						 JOptionPane.showMessageDialog(null, "Le PIN doit etre une valeure numerique ", "Erreur de personnalisation", JOptionPane.ERROR_MESSAGE);
   					}

   				}else{
   						//Les PINs saisis sont differents 
						 JOptionPane.showMessageDialog(null, "Les PIN saisis ne correspondent pas.", "Erreur de personnalisation", JOptionPane.ERROR_MESSAGE);
					}
                	}else{
                		//L'un des champs est vide 
						 JOptionPane.showMessageDialog(null, "Veuillez remplir tous les champs", "Erreur de personnalisation", JOptionPane.ERROR_MESSAGE);
					}
                }
            });
            infoPanel.add(customizeButton);

            // JLabel pour afficher le message aprï¿½s personnalisation
            messageLabel = new JLabel(""); // Initialisation du JLabel
            messageLabel.setFont(new Font("Poppins", Font.BOLD, 16));
            messageLabel.setForeground(Color.BLUE);
            messageLabel.setBounds(200, 360, 350, 30);
            infoPanel.add(messageLabel);

            contentPanel.add(infoPanel);

            setVisible(true);
        }   
    }

    private JPanel createNavbarPanel() {
        JPanel navbarPanel = new JPanel();
        navbarPanel.setLayout(null);
        navbarPanel.setBackground(new Color(140,203,227));
        navbarPanel.setBounds(0, 0, 1460, 90);

        JButton accueilButton = createNavbarButton("Accueil");
        accueilButton.setBounds(950, 25, 120, 40);
        accueilButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); // Fermer la fenetre Personnalisation
                new inter1().setVisible(true); // Ouvrir une nouvelle instance de Inter1
            }
        });
        navbarPanel.add(accueilButton);
        

        JButton guideButton = createNavbarButton("Guide");
        guideButton.setBounds(1100, 25, 120, 40);
        navbarPanel.add(guideButton);
        guideButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try{
                    Desktop.getDesktop().browse(new java.net.URI("https://docs.google.com/document/d/16gUwSP5yaYSIar1pP89zW2rBkDKu2YxC0uvfNPn3vg8/edit"));
                }
                catch(Exception ex){
                    System.out.println("Erreur lors de l'ouverture du lien");
                }
            }
        });

        JLabel logoLabel = new JLabel();
        URL imageUrl = getClass().getResource("vote.png");
        if (imageUrl != null) {
        ImageIcon logoIcon = new ImageIcon(imageUrl);
        logoLabel.setIcon(logoIcon);
        logoLabel.setBounds(30, 10, 150, 60);
        navbarPanel.add(logoLabel);
        }
        return navbarPanel;
    }

    private JButton createNavbarButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Poppins", Font.BOLD, 20));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(140,203,227));
        button.setFocusPainted(false);
        return button;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Poppins", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        return button;
    }

    public static void main(String[] args) {
    	SwingUtilities.invokeLater(new Runnable() {
    	    public void run() {
    	        new Personnalisation();
    	    }
    	});

    }
    
    //Methode qui genere une paire de cle RSA 1024 bits 
	public static KeyPair generateKey() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(1024);
			KeyPair keyPair = keyGen.generateKeyPair();
			return keyPair;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}


}
