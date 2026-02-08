package jeu;
import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

/**
 * La classe {@code GUI} représente l'interface graphique du jeu.
 * <p>
 * Elle gère l'affichage du texte, des images et la saisie des commandes
 * par l'utilisateur via un champ de texte. Cette classe est responsable
 * de l'interaction avec la logique du jeu représentée par la classe {@link Jeu}.
 * <p>
 * Les principales fonctionnalités incluent :
 * <ul>
 *     <li>Afficher des messages dans un JTextArea</li>
 *     <li>Afficher des images associées aux zones du jeu</li>
 *     <li>Recevoir et transmettre les commandes utilisateur à la classe {@link Jeu}</li>
 *     <li>Activer ou désactiver le champ de saisie</li>
 * </ul>
 * 
 * Exemple d'utilisation :
 * <pre>
 *     Jeu jeu = new Jeu();
 *     GUI gui = new GUI(jeu);
 * </pre>
 * 
 * Cette classe implémente {@link java.awt.event.ActionListener} pour écouter
 * les événements sur le champ de saisie.
 */
public class GUI implements ActionListener {
    private final Jeu jeu;
    private final JFrame fenetre;
    private final JTextField entree;
    private final JTextArea texte;
    private final JLabel image;
    
	/** Nom du repertoire des images. */
    private static final String NomRepertoireImages = "jeu/images/";

    public GUI(Jeu jeu) {
        // Jeu
    	this.jeu = jeu;

    	// Fenetre
        fenetre = new JFrame("Jeu");
        fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Zone de texte
        texte = new JTextArea();
        texte.setEditable(false);
        texte.setLineWrap(true);
        texte.setWrapStyleWord(true);
        //var scroll = new JScrollPane(texte);
        JScrollPane scroll = new JScrollPane(texte);
        scroll.setPreferredSize(new Dimension(400, 300));

        // Champ de saisie
        entree = new JTextField();
        entree.addActionListener(this);
        
        // Label pour l'image
        image = new JLabel();
        image.setHorizontalAlignment(SwingConstants.CENTER);

        // Panel principal
        var panel = new JPanel(new BorderLayout(5,5));
        panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        panel.add(image, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(entree, BorderLayout.SOUTH);

        fenetre.getContentPane().add(panel);
        fenetre.pack();
        fenetre.setLocationRelativeTo(null); 
        fenetre.setVisible(true);
        
        // focus sur le champ de saisie
        SwingUtilities.invokeLater(entree::requestFocusInWindow);
    }

    /** Affiche un message avec saut de ligne automatique */
    public void afficher(String s) {
    	SwingUtilities.invokeLater(() -> {
            texte.append(s + "\n");
            texte.setCaretPosition(texte.getDocument().getLength());
        });
    }
    
    /** Affiche un saut de ligne */
    public void afficher() {
        afficher("");
    }

    /** Affiche une image depuis le dossier resources */
    public void afficheImage(String nomImage) {
    	SwingUtilities.invokeLater(() -> {
            URL imageURL = getClass().getClassLoader().getResource(NomRepertoireImages + nomImage);
            if (imageURL != null) {
                image.setIcon(new ImageIcon(imageURL));
                fenetre.pack();
            } else {
                System.err.println("Image introuvable : " + nomImage);
                image.setIcon(null);
            }
        });
    }

    /** Active ou désactive le champ de saisie */
    public void enable(boolean ok) {
        entree.setEditable(ok);
        if (! ok) entree.getCaret().setBlinkRate(0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        executerCommande();
    }

    /** Exécute une commande */
    private void executerCommande() {
        var commande = entree.getText().trim();
        if (commande.isEmpty()) return;
        entree.setText("");
        jeu.traiterCommande( commande);
    }
}