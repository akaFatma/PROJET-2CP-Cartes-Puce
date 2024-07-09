package jsr268gp.sampleclient;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.smartcardio.*;
import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

import jsr268gp.util.Util;


public class VoteInterface extends JFrame {
private static Socket socket;
    private static BufferedReader reader;
    private static DataInputStream dataIn;
    private static DataOutputStream dataOut;
    byte[] key;
    public static final byte[] N_array = { (byte) 0xF6, (byte) 0x6A,
(byte) 0x3C, (byte) 0x6F, (byte) 0x9A, (byte) 0x30, (byte) 0xA7,
(byte) 0xB9, (byte) 0x6D, (byte) 0xA8, (byte) 0x53, (byte) 0x1D,
(byte) 0xF8, (byte) 0xF0, (byte) 0xBC, (byte) 0x8F, (byte) 0xC4,
(byte) 0x61, (byte) 0xBD, (byte) 0x81, (byte) 0xC3, (byte) 0x3D,
(byte) 0xF1, (byte) 0x58, (byte) 0xEB, (byte) 0x70, (byte) 0xFB,
(byte) 0x07, (byte) 0x33, (byte) 0xB9, (byte) 0x05, (byte) 0xC1,
(byte) 0xE0, (byte) 0xF7, (byte) 0x6A, (byte) 0x15, (byte) 0x33,
(byte) 0x85, (byte) 0x37, (byte) 0xB0, (byte) 0x4E, (byte) 0x85,
(byte) 0xC4, (byte) 0x69, (byte) 0x92, (byte) 0x76, (byte) 0xF6,
(byte) 0x81, (byte) 0xF9, (byte) 0xED, (byte) 0xBE, (byte) 0x2E,
(byte) 0x21, (byte) 0xB1, (byte) 0x01, (byte) 0x55, (byte) 0xBC,
(byte) 0x5B, (byte) 0x6F, (byte) 0xC8, (byte) 0x69, (byte) 0x26,
(byte) 0x9C, (byte) 0x59, (byte) 0x8F, (byte) 0xF4, (byte) 0x20,
(byte) 0x94, (byte) 0xC3, (byte) 0xC3, (byte) 0xB6, (byte) 0x72,
(byte) 0x34, (byte) 0x98, (byte) 0xC5, (byte) 0xA3, (byte) 0x8A,
(byte) 0x14, (byte) 0x28, (byte) 0x31, (byte) 0x69, (byte) 0x90,
(byte) 0x9C, (byte) 0xE8, (byte) 0xE3, (byte) 0xF4, (byte) 0x62,
(byte) 0xA0, (byte) 0xE1, (byte) 0x24, (byte) 0x2C, (byte) 0x39,
(byte) 0x83, (byte) 0x4F, (byte) 0xF4, (byte) 0x91, (byte) 0xFE,
(byte) 0xD5, (byte) 0x12, (byte) 0x97, (byte) 0x56, (byte) 0xBB,
(byte) 0x66, (byte) 0x0F, (byte) 0xFF, (byte) 0xF4, (byte) 0xD5,
(byte) 0x7C, (byte) 0xCA, (byte) 0xE6, (byte) 0x2D, (byte) 0x71,
(byte) 0x09, (byte) 0x97, (byte) 0xD9, (byte) 0xDB, (byte) 0x07,
(byte) 0x1E, (byte) 0xEF, (byte) 0x61, (byte) 0x7C, (byte) 0xC2,
(byte) 0x8B, (byte) 0x5B, (byte) 0xD7, (byte) 0x7D, (byte) 0x73,
(byte) 0x21 };
public static final byte[] A_array = { (byte) 0xCF, (byte) 0x4A,
(byte) 0xED, (byte) 0x62, (byte) 0xC8, (byte) 0x2E, (byte) 0xA3,
(byte) 0xD7, (byte) 0x92, (byte) 0x9C, (byte) 0xA1, (byte) 0x22,
(byte) 0xC2, (byte) 0xD5, (byte) 0x44, (byte) 0xB6, (byte) 0x90,
(byte) 0x50, (byte) 0xAA, (byte) 0x54, (byte) 0xC2, (byte) 0x1E,
(byte) 0x3D, (byte) 0x64, (byte) 0xCE, (byte) 0x0A, (byte) 0x50,
(byte) 0x66, (byte) 0x7E, (byte) 0x3B, (byte) 0x4E, (byte) 0x0A,
(byte) 0xAB, (byte) 0x33, (byte) 0x7D, (byte) 0xAC, (byte) 0x74,
(byte) 0xAD, (byte) 0x12, (byte) 0xD6, (byte) 0xE5, (byte) 0x76,
(byte) 0x17, (byte) 0x61, (byte) 0xFC, (byte) 0x58, (byte) 0xCF,
(byte) 0x21, (byte) 0xF4, (byte) 0x20, (byte) 0xCC, (byte) 0x6F,
(byte) 0xFF, (byte) 0x4D, (byte) 0x95, (byte) 0xFF, (byte) 0xBB,
(byte) 0xEE, (byte) 0xEF, (byte) 0x36, (byte) 0x73, (byte) 0x15,
(byte) 0x1B, (byte) 0xF6, (byte) 0x1F, (byte) 0xB3, (byte) 0xB2,
(byte) 0x08, (byte) 0x88, (byte) 0x12, (byte) 0xC0, (byte) 0x7B,
(byte) 0x23, (byte) 0xC7, (byte) 0xFB, (byte) 0x49, (byte) 0x35,
(byte) 0x52, (byte) 0xED, (byte) 0x82, (byte) 0xC6, (byte) 0x15,
(byte) 0xD1, (byte) 0x96, (byte) 0x71, (byte) 0x16, (byte) 0x23,
(byte) 0x68, (byte) 0x9F, (byte) 0xA8, (byte) 0x25, (byte) 0x94,
(byte) 0x1D, (byte) 0x93, (byte) 0xFB, (byte) 0x49, (byte) 0x68,
(byte) 0x0E, (byte) 0xB1, (byte) 0xD7, (byte) 0x43, (byte) 0xE8,
(byte) 0x11, (byte) 0x6A, (byte) 0x47, (byte) 0x36, (byte) 0xEC,
(byte) 0xCB, (byte) 0x31, (byte) 0x9A, (byte) 0xD9, (byte) 0x05,
(byte) 0x75, (byte) 0x9F, (byte) 0xA5, (byte) 0x4A, (byte) 0x2C,
(byte) 0x3E, (byte) 0x74, (byte) 0x58, (byte) 0x6E, (byte) 0xA4,
(byte) 0xD8, (byte) 0x04, (byte) 0x5A, (byte) 0x4E, (byte) 0x90,
(byte) 0x01 };

    public VoteInterface() throws ClassNotFoundException, IOException {
        super("Votez ici !");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        setSize(screenWidth, screenHeight);
        getContentPane().setBackground(new Color(229, 229, 229)); // Couleur de fond du contenu

        try {
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
            // Création du panneau principal
            final JPanel mainPanel = new JPanel();
            mainPanel.setLayout(null); // Utilisation d'une disposition null pour positionner manuellement les composants
            mainPanel.setBackground(new Color(250, 249, 246)); // Couleur de fond du panneau principal
            add(mainPanel);

            // Message au-dessus des nominés
            JLabel selectLabel = new JLabel("Sélectionnez votre meilleur candidat :");
            selectLabel.setFont(new Font("Poppins", Font.BOLD, 20));
            selectLabel.setBounds(160, 100, 600, 30); // Position et taille du message
            mainPanel.add(selectLabel);

            // Barre de navigation (navbar)
            JPanel navbarPanel = createNavbarPanel();
            mainPanel.add(navbarPanel);

            // Logo à gauche de la barre de navigation
            JLabel logoLabel = new JLabel();
            navbarPanel.add(logoLabel);

            // Affichage initial du frame
            setVisible(true);

            // Exécution du code long dans un SwingWorker
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    // Exécutez votre code long ici
                  /********CREATION DE LA CLE DE SESSION ********/
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
         
          /********* Commande APDU de ID *********/
          commande = new CommandAPDU(0,2, 3, 4, 4);
         
          //Envoyer la commande APDU
          System.out.println("Commande ID : " + Util.byteArrayToHexString(commande.getBytes(), " "));
          reponse = canal.transmit(commande);
         
          //Afficher la réponse APDU
          System.out.println("Reponse ID : "+Util.byteArrayToHexString(reponse.getBytes()," "+'\n'));
         
          //Envoi de l'ID pour que le serveur recupere la cle publique de la carte
          dataOut.writeUTF("RECEIVE_ID");
          dataOut.flush();
          dataOut.write(reponse.getData());
          dataOut.flush();
         
          /********DEBUT DU PROTOCOLE DIFFIE HELLMAN - SIGNATURE NUMERIQUE********/
          dataOut.writeUTF("RECEIVE_N_A");
          dataOut.flush();
          dataOut.write(N_array);
          dataOut.flush();
          dataOut.write(A_array);
          dataOut.flush();
         
          /********Commande APDU de N ********/
          commande = new CommandAPDU(0,3, 2, 0, N_array,0);
         
          //Envoyer la commande APDU
          System.out.println("Commande N : " + Util.byteArrayToHexString(commande.getBytes(), " "));
          reponse = canal.transmit(commande);
         
          //Afficher la réponse APDU
          System.out.println("Reponse N : "+Util.byteArrayToHexString(reponse.getBytes()," "+'\n'));
         
         
          /********Commande APDU de N ********/
          commande = new CommandAPDU(0,4, 2, 0, A_array,0);
         
          //Envoyer la commande APDU
          System.out.println("Commande A : " + Util.byteArrayToHexString(commande.getBytes(), " "));
          reponse = canal.transmit(commande);
         
          //Afficher la réponse APDU
          System.out.println("Reponse A : "+Util.byteArrayToHexString(reponse.getBytes()," "+'\n'));
         
          //Demmande de a cle publique du serveur
          dataOut.writeUTF("GET_PUBLIC_KEY");
          dataOut.flush();
         
          //Convertir le String en RSAPublicKey
          KeyFactory keyFactory = KeyFactory.getInstance("RSA");
          X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Util.hexStringToByteArray(dataIn.readUTF()));
          RSAPublicKey clientPublicKey = (RSAPublicKey)keyFactory.generatePublic(publicKeySpec);
         
          byte[]modulusBytes =Util.toByteArray(clientPublicKey.getModulus());
          byte[]exponentBytes = Util.toByteArray(clientPublicKey.getPublicExponent());
         
          /********* Commande APDU de MODULUS *********/
          //Envoyer le modulo de la cle publique du serveur a la carte
          commande = new CommandAPDU(0, 1, 2, 0, modulusBytes, 0);
         
          // Envoyer la commande APDU
          System.out.println("Commande mod: " + Util.byteArrayToHexString(commande.getBytes(), " "));
          reponse = canal.transmit(commande);
         
          // Afficher la réponse APDU
          System.out.println("Reponse mod : "+ Util.byteArrayToHexString(reponse.getBytes(), " ")+'\n');
         
          /********* Commande APDU de PRIVATE EXPONENT *********/
          //Envoyer l'exposant de la cle publique du serveur a la carte
          commande = new CommandAPDU(0, 2, 2, 0, exponentBytes, 0);
         
          // Envoyer la commande APDU
          System.out.println("Commande exp: " + Util.byteArrayToHexString(commande.getBytes(), " "));
          reponse = canal.transmit(commande);
         
          // Afficher la réponse APDU
          System.out.println("Reponse exp : "+ Util.byteArrayToHexString(reponse.getBytes(), " ")+'\n');
         
          //Recuperer A^m mod N du serveur
          dataOut.writeUTF("GET_AmmodN");
          dataOut.flush();
          byte[] AmmodN_array = new byte[128];
          dataIn.read(AmmodN_array);
         
          /********* Commande APDU de A^m mod N *********/
          commande = new CommandAPDU(0,5, 2, 128, AmmodN_array, 128);
         
          // Envoyer la commande APDU
          System.out.println("Commande AmmodN: " + Util.byteArrayToHexString(commande.getBytes(), " "));
          reponse = canal.transmit(commande);
          byte[] AnmodN_array = reponse.getData();
         
          // Afficher la réponse APDU
          System.out.println("Reponse_AnmodN : "+Util.byteArrayToHexString(reponse.getBytes()," ")+'\n');
         
          //Envoie de AnmodN au serveur
          dataOut.writeUTF("RECEIVE_AnmodN");
          dataOut.flush();
          dataOut.write(AnmodN_array);
          dataOut.flush();
         
         
          /********* Commande APDU de SIGNATURE DE LA CARTE*********/
          commande = new CommandAPDU(0,6, 2,128, 128);
         
          // Envoyer la commande APDU
          System.out.println("Commande signature: " + Util.byteArrayToHexString(commande.getBytes(), " "));
          reponse = canal.transmit(commande);
          byte[] chiffree = reponse.getData();
         
          // Afficher la réponse APDU
          System.out.println("Reponse signature: "+Util.byteArrayToHexString(reponse.getBytes()," ")+'\n');
         
          //Envoie de la signature de la carte au serveur
          dataOut.writeUTF("RECEIVE_sign");
          dataOut.flush();
          dataOut.write(chiffree);
          dataOut.flush();
         
          String rep = dataIn.readUTF();
          if(rep.equals("SUCCESS")){
          //Recuperer la signature du serveur
          dataOut.writeUTF("GET_sign");
          dataOut.flush();
          chiffree = new byte[128];
          dataIn.read(chiffree);
         
          /********* Commande APDU de SIGNATURE DU SERVEUR *********/
          commande = new CommandAPDU(0,7, 2,1,chiffree, 1);
         
          //Envoyer la commande APDU
          System.out.println("Commande signature: " + Util.byteArrayToHexString(commande.getBytes(), " "));
          reponse = canal.transmit(commande);
         
          //Afficher la réponse APDU
          System.out.println("Reponse Verification : "+Util.byteArrayToHexString(reponse.getBytes(), " ")+'\n');
         
          if(reponse.getBytes()[0]== 0x02){ //Verification accordée
         
                 
                  /********* Commande APDU de SESSION KEY *********/
                  commande = new CommandAPDU(0,3, 3,16, 16);
                 
                  //Envoyer la commande APDU
          System.out.println("Commande cle de session: " + Util.byteArrayToHexString(commande.getBytes(), " "));
          reponse = canal.transmit(commande);
          key = reponse.getData();
         
          //Afficher la réponse APDU
          System.out.println("cle de session: "+Util.byteArrayToHexString(reponse.getBytes()," "));
          }
                   }
                  } catch (Exception ex) {
                      ex.printStackTrace();
                  }
                    return null;
                }

                @Override
                protected void done() {
                    // Panel central avec les nominés
                    try{
                    JPanel centerPanel = createNomineePanel();
                    centerPanel.setBounds(160, 150, 1060, 400);
                    centerPanel.setBackground(new Color(231, 231, 231));
                    mainPanel.add(centerPanel);
                }catch(Exception e){
                System.out.println(e.getMessage());
                }

                    // Actualiser le frame pour refléter les changements
                    revalidate();
                }
            };
           
           
            final JDialog pleaseWaitDialog = new JDialog(this, "Chargement en cours", true);
            JLabel waitLabel = new JLabel("    Veuillez patienter s'il vous plait !");
            waitLabel.setFont(new Font("Poppins", Font.BOLD, 20)); 
            pleaseWaitDialog.add(waitLabel);
            pleaseWaitDialog.setSize(500, 200);
            pleaseWaitDialog.setLocationRelativeTo(null); 
            pleaseWaitDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
           
            worker.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("state") && evt.getNewValue() == SwingWorker.StateValue.STARTED) {
                        pleaseWaitDialog.setVisible(true);
                    }
                }
            });
            worker.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("state") && evt.getNewValue() == SwingWorker.StateValue.DONE) {
                        pleaseWaitDialog.setVisible(false);
                    }
                }
            });
       
           
           
           
           
           
            worker.execute();
           
}
       
}

    // Méthode pour créer la barre de navigation (navbar) avec les changements demandés
      private JPanel createNavbarPanel() {
        JPanel navbarPanel = new JPanel();
        navbarPanel.setLayout(null);
        navbarPanel.setBackground(new Color(140,203,227));
        navbarPanel.setBounds(0, 0, 1460, 90);
        JLabel logoLabel = new JLabel();
        URL imageUrl = getClass().getResource("vote.png");
        if (imageUrl != null) {
        ImageIcon logoIcon = new ImageIcon(imageUrl);
        logoLabel.setIcon(logoIcon);
        logoLabel.setBounds(30, 10, 150, 60); // Position et taille du logo
        }
        navbarPanel.add(logoLabel);
     

        // Bouton "Accueil" à gauche de la barre de navigation
        JButton homeButton = createNavbarButton("Accueil");
        homeButton.setBounds(950, 25, 120, 40);
        homeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); // Fermer la fenêtre actuelle
                new inter1().setVisible(true); 
            }
        });
        navbarPanel.add(homeButton);

        JButton guideButton = createNavbarButton("Guide");
        guideButton.setBounds(1100, 25, 120, 40);
        guideButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); // Fermer la fenetre Personnalisation
                new inter1().setVisible(true); 
            }
        });
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
        return navbarPanel;
    }

    // Méthode utilitaire pour créer des boutons de la barre de navigation avec le texte donné
    private JButton createNavbarButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Poppins", Font.BOLD, 20));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(140,203,227));
        button.setFocusPainted(false);

        return button;
    }

    // Méthode pour créer le panel des nominés avec des boutons interactifs
    private JPanel createNomineePanel() throws IOException, ClassNotFoundException {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBackground(Color.WHITE);
        try{
        //Demmande de la liste des nominees
        dataOut.writeUTF("SHOW_ALL_NOMINEES");
        String encrypted = dataIn.readUTF();
       
        //Dechiffrement de la liste des nominees
        String nominees = Util. hexToASCII(Util.byteArrayToHexString(decrypt_aes(Util.hexStringToByteArray(encrypted), key), ""));
        System.out.println(nominees);
       
      //nomineeMap est une HashMap qui contient comme key le nom du nominee et comme value son ID
        final HashMap<String, String> nomineeMap = convertToMap(nominees);
       
        // Création des boutons pour chaque nominé
        for (HashMap.Entry<String, String> entry : nomineeMap.entrySet()) {
            JButton nomineeButton = new JButton(entry.getKey()); // Création d'un nouveau bouton pour le nominé
            nomineeButton.setLayout(new BorderLayout()); // Définition de la disposition du bouton
           
            // Style du bouton
            nomineeButton.setBackground(new Color(140,203,227));

           
            panel.add(nomineeButton);
            nomineeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                    int choice = JOptionPane.showConfirmDialog(null, "Vous avez choisis "+((JButton) e.getSource()).getText()+'\n'+"Continuer?", "Confirmation du vote", JOptionPane.YES_NO_OPTION);
                         if (choice == JOptionPane.YES_OPTION) {
                             // Recuperer le nom du nominee choisis et son ID
                             String nomineeName = ((JButton) e.getSource()).getText();
                             String nomineeID = getNomineeIdByName(nomineeMap, nomineeName);
                             System.out.println("name : "+nomineeName +"ID : "+ nomineeID);
                             
                            byte[] ID = Util.unsignedIntToByteArray(Integer.parseInt(nomineeID));
                            System.out.println("ID : "+ Util.byteArrayToHexString(ID, ""));
                //Chiffrer le choix du voteur avec la cle de session
                byte[] choix =  encrypt_aes(ID,key);
               
                //Envoie du choix chiffre
                dataOut.writeUTF("VOTE");
                dataOut.writeUTF(Util.byteArrayToHexString(choix, ""));
                if(dataIn.readUTF().compareTo("SUCCESS") == 0){
                //Afficher message de confirmation
                dataOut.writeUTF("EXIT");
                                JOptionPane.showMessageDialog(null, "SUCCESS", "Vote Confirmation", JOptionPane.INFORMATION_MESSAGE);
                                dispose(); 
                                new inter1().setVisible(true);
                }else{
                dataOut.writeUTF("EXIT");
                                JOptionPane.showMessageDialog(null, "ERREUR", "Vote Confirmation", JOptionPane.ERROR_MESSAGE);
                                dispose(); 
                                new inter1().setVisible(true);
                                }}
                } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return panel;
    }
   


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
new VoteInterface();
} catch (Exception e) {
e.printStackTrace();
}
            }
        });
    }
   
  //Methode qui a partir d'un string contenant nom et id cree la map associee
    public static HashMap<String, String> convertToMap(String nomineeInfoString) {
        HashMap<String, String> map = new HashMap<String, String>();
        String[] lines = nomineeInfoString.split("\n");

        for (String line : lines) {
            String[] parts = line.split(",Name: ");
            String name = parts[1].trim();
            String id = parts[0].split(": ")[1].trim();
            map.put(name, id);
        }

        return map;
    }
   
    //Methode qui de la map retourne le ID d'un nominee a partir de son nom
    public String getNomineeIdByName(HashMap<String, String> nomineeMap, String nomineeName) {
        for (HashMap.Entry<String, String> entry : nomineeMap.entrySet()) {
            if (entry.getKey().equals(nomineeName)) {
                return entry.getValue(); // retourner ID
            }
        }
        return null; //retourner null si le nom n'existe pas
    }
   
    //Methode de chiffrement AES CBC avec la cle de session
public static byte[] encrypt_aes(byte[] data, byte[] key) throws Exception {
SecretKey originalKey = new SecretKeySpec(key, "AES");
Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
cipher.init(Cipher.ENCRYPT_MODE, originalKey, new IvParameterSpec(key));
byte[] encryptedBytes = cipher.doFinal(data, 0, data.length);
return encryptedBytes;

}

    //Methode de dechiffrement AES CBC avec la cle de session
public static byte[] decrypt_aes(byte[] data, byte[] key) throws Exception {
SecretKey originalKey = new SecretKeySpec(key, "AES");
Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
cipher.init(Cipher.DECRYPT_MODE, originalKey, new IvParameterSpec(key));
byte[] encryptedBytes = cipher.doFinal(data, 0, data.length);
return encryptedBytes;

}

}
