package jsr268gp.sampleclient;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.HashMap;

public class stat extends JFrame {
	private static Socket socket;
    private static BufferedReader reader;
    private static DataInputStream dataIn;
    private static DataOutputStream dataOut;
    
    public stat() throws IOException {
        super("PV GLOBAL DES ELECTIONS");
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
        mainPanel.setLayout(new BorderLayout());
        getContentPane().add(mainPanel);

        // Navbar Panel 
        JPanel navbarPanel = createNavbarPanel();
        mainPanel.add(navbarPanel, BorderLayout.NORTH);

        // Content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(null); 
        contentPanel.setBackground(Color.WHITE);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        JLabel titleLabel = new JLabel("PV GLOBAL DES ELECTIONS");
        titleLabel.setFont(new Font("Poppins", Font.BOLD, 36));
        titleLabel.setForeground(new Color(45, 51, 74));
        titleLabel.setBounds(285, 30, 700, 40);
        contentPanel.add(titleLabel);
        
        //Demmande des resultats du vote
        dataOut.writeUTF("SHOW_STAT");
        String nominees = dataIn.readUTF();
        dataOut.writeUTF("EXIT");
        
        //nomineeMap est une HashMap qui contient comme key le nom du nominee et comme value son score
        final HashMap<String, String> nomineeMap = convertToMap(nominees);

        //Calculer total score
        int totalScore = 0;
        for (String score : nomineeMap.values()) {
            totalScore += Integer.parseInt(score);
        }
        
        int y = 150; 
        //Pour chaque nominee afficher le score et le pourcentage 
        for (HashMap.Entry<String, String> entry : nomineeMap.entrySet()) {
            String nominee = entry.getKey();
            String score = entry.getValue();
            double percentage = (Double.parseDouble(score) / totalScore) * 100;
            
            JLabel nameLabel = new JLabel(nominee);
            nameLabel.setFont(new Font("Poppins", Font.BOLD, 24));
            nameLabel.setBounds(70, y, 300, 30);
            contentPanel.add(nameLabel);

            JLabel votesLabel = new JLabel("Total Votes: " + score);
            votesLabel.setFont(new Font("Poppins", Font.PLAIN, 18));
            votesLabel.setBounds(400, y, 200, 30);
            contentPanel.add(votesLabel);
            
            JLabel percentageLabel = new JLabel(percentage + "%");
            percentageLabel.setFont(new Font("Poppins", Font.PLAIN, 18));
            percentageLabel.setBounds(600, y, 50, 30);
            contentPanel.add(percentageLabel);

            // Barre de pourcentage colorée
            JPanel percentageBar = new JPanel();
            percentageBar.setBackground(getColorForPercentage(percentage));
            percentageBar.setBounds(670, y + 5, (int)(percentage * 2), 20);
            contentPanel.add(percentageBar);

            y += 50; // Augmenter la position verticale pour le prochain composant
        }

        // Bouton "Mise en fin des élections"
        JButton endElectionsButton = createStyledButton("Mise en fin des élections", new Color(140,203,227));
        endElectionsButton.setBounds(500, y + 30, 250, 50);
        contentPanel.add(endElectionsButton);
        
        endElectionsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	//Afficher le gagnant des elections 
            	String winner = null;
                int maxScore = 0;

                //Pour chaque nominee voir si son score est superieur au max score
                for (HashMap.Entry<String, String> entry : nomineeMap.entrySet()) {
                    String nominee = entry.getKey();
                    String scoreStr = entry.getValue();
                    int score = Integer.parseInt(scoreStr);

                    if (score > maxScore) {
                        maxScore = score;
                        winner = nominee;
                    }
                }
             
                //Afficher le gagnant
                WinnerInterface(winner);
            }
        });
            
        setVisible(true);
        }
    }
    public void WinnerInterface(String winnerName) {
        JFrame frame = new JFrame("Winner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 200);

        JLabel winnerLabel = new JLabel("Le gagnant est : " + winnerName);
        winnerLabel.setHorizontalAlignment(JLabel.CENTER);
        winnerLabel.setSize(500, 200);
        winnerLabel.setFont(new Font("Poppins", Font.PLAIN, 20));
        frame.add(winnerLabel);
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        int frameWidth = frame.getSize().width;
        int frameHeight = frame.getSize().height;
        int x = (screenWidth - frameWidth) / 2;
        int y = (screenHeight - frameHeight) / 2;
        frame.setLocation(x, y);

        frame.setVisible(true);
    }
    private JPanel createNavbarPanel() {
        JPanel navbarPanel = new JPanel();
        navbarPanel.setLayout(null);
        navbarPanel.setBackground(new Color(140,203,227));
        navbarPanel.setPreferredSize(new Dimension(1718, 90));

        JButton homeButton = createNavbarButton("Accueil");
        homeButton.setBounds(950, 25, 120, 40);
        homeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new inter1().setVisible(true); 
                dispose(); 
            }
        });
        navbarPanel.add(homeButton);

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


        // Ajoutez le logo à gauche de la barre de navigation
        URL imageUrl = getClass().getResource("votee.png");
        if (imageUrl != null) {
        ImageIcon logoIcon = new ImageIcon(imageUrl); 
        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setBounds(30, 10, 150, 60); // Position du logo
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

    // Méthode utilitaire pour définir la couleur en fonction du pourcentage
    private Color getColorForPercentage(double percentage) {
        if (percentage >= 70) {
            return Color.GREEN;
        } else if (percentage >= 40) {
            return Color.YELLOW;
        } else {
            return Color.RED;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
					new stat();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        });
    }
    
    //Methode qui a partir d'un string contenant nom et score cree la map associee
    public static HashMap<String, String> convertToMap(String nomineeInfoString) {
        HashMap<String, String> map = new HashMap<String, String>();
        String[] lines = nomineeInfoString.split("\n");

        for (String line : lines) {
            String[] parts = line.split(",Name: ");
            String name = parts[1].trim();
            String score = parts[0].split(": ")[1].trim();
            map.put(name, score);
        }

        return map;
    }
}
