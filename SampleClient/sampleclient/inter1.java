package jsr268gp.sampleclient;

import javax.swing.*;

import java.awt.*;
import java.net.URL;
import java.awt.event.*;


public class inter1 extends JFrame {

    public inter1() {
        super("Bienvenue sur eVote");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        setSize(screenWidth, screenHeight);
        getContentPane().setBackground(new Color(229, 229, 229)); 

        // Création du panneau principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null); 
        mainPanel.setBackground(new Color(250, 249, 246)); 
        add(mainPanel);

        // Barre de navigation (navbar)
        JPanel navbarPanel = createNavbarPanel();
        mainPanel.add(navbarPanel);

        // Image principale
        JLabel imageLabel = new JLabel();
        URL imageUrl = getClass().getResource("logo.png");
        if (imageUrl != null) {
            ImageIcon imageIcon = new ImageIcon(imageUrl);
            imageLabel.setIcon(imageIcon);
            imageLabel.setBounds(10, 150, 643, 580); // Position et taille de l'image
            mainPanel.add(imageLabel);
        }

        // Conteneur pour le contenu
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(new Color(250, 249, 246));
        contentPanel.setLayout(null); // Utilisation d'une disposition null pour positionner les elements a l'interieur
        contentPanel.setBounds(276, 30, 848, 700); // Position et taille du contenu
        mainPanel.add(contentPanel);

        // Titre "Bienvenue sur eVote"
        JLabel titleLabel = new JLabel("Bienvenue sur eVote");
        titleLabel.setFont(new Font("Poppins", Font.BOLD, 60));
        titleLabel.setForeground(new Color(45, 51, 74));
        titleLabel.setBounds(100, 150, 848, 90); // Position et taille du titre
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(titleLabel);

        // Description de eVote
        JLabel descriptionLabel = new JLabel("<html>eVote est une plateforme securisee de vote electronique utilisant des cartes a puces.</html>");
        descriptionLabel.setFont(new Font("Poppins", Font.BOLD, 22));
        descriptionLabel.setForeground(new Color(45, 51, 74));
        descriptionLabel.setBounds(240, 250, 660, 100); // Position et taille de la description
        contentPanel.add(descriptionLabel);
        // Description de eVote
        JLabel descriptionLabelll = new JLabel("<html>Veuillez selectionner votre role :</html>");
        descriptionLabelll.setFont(new Font("Poppins", Font.BOLD, 21));
        descriptionLabelll.setForeground(new Color(45, 51, 74));
        descriptionLabelll.setBounds(230, 380, 660, 100); // Position et taille de la description
        contentPanel.add(descriptionLabelll);


       
        JButton adminButton = createStyledButton("ADMINISTRATEUR", new Color(140,203,227));
        adminButton.setBounds(220, 450, 200, 60); // Position et taille du bouton Administrateur
        adminButton.setBorder(null);
        contentPanel.add(adminButton);


        
        JButton responsableButton = createStyledButton("RESPONSABLE", new Color(140,203,227));
        responsableButton.setBounds(435, 450, 200, 60); // Position et taille du bouton Responsable
        responsableButton.setBorder(null);
        contentPanel.add(responsableButton);

      
        JButton votantButton = createStyledButton("VOTANT", new Color(140,203,227));
        votantButton.setBounds(650, 450, 200, 60); // Position et taille du bouton Votant
        votantButton.setBorder(null);
        contentPanel.add(votantButton);

       
        adminButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                new ADMINLOGIN().setVisible(true);
            }
        });

        responsableButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); // Fermer la fenetre actuelle
                new RESPOLOGIN().setVisible(true);
            }
        });

        votantButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Ouvrir l'interface USERLOGIN
                dispose(); // Fermer la fenetre actuelle
                new USERLOGIN().setVisible(true);
            }
        });

        setVisible(true);
    }
    

    // Methode pour creer la barre de navigation (navbar)
    private JPanel createNavbarPanel() {
        JPanel navbarPanel = new JPanel();
        navbarPanel.setLayout(null);
        navbarPanel.setBackground(new Color(140,203,227));
        navbarPanel.setBounds(0, 0, 1460, 90); // Position et taille de la barre de navigation

        // Bouton "Guide" a gauche de la barre de navigation
        JButton guideButton = createNavbarButton("Guide");
        guideButton.setBounds(210, 25, 120, 40); // Position et taille du bouton "Guide"
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

        

        // Logo a gauche de la barre de navigation
        JLabel logoLabel = new JLabel();
        URL imageUrl = getClass().getResource("vote.png");
        if (imageUrl != null) {
        ImageIcon logoIcon = new ImageIcon(imageUrl); 
        logoLabel.setIcon(logoIcon);
        logoLabel.setBounds(30, 10, 150, 60); // Position et taille du logo
        navbarPanel.add(logoLabel);
        }

        return navbarPanel;
    }
    // Methode utilitaire pour creer des boutons de la barre de navigation avec le texte donnee
    private JButton createNavbarButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Poppins", Font.BOLD, 20));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(45, 51, 74));
        button.setFocusPainted(false); // Pour enlever le contour de focus
        return button;
    }

    // Methode utilitaire pour creer un bouton avec du texte et une couleur de fond specifique
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Poppins", Font.BOLD, 20));
        button.setForeground(new Color(39, 35, 67));
        button.setBackground(bgColor);
        
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        button.setFocusPainted(false);
        return button;
    }

}
