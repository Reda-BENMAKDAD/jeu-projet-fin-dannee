package jeu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Interface d'authentification et d'inscription pour le jeu.
 * 
 * Permet aux joueurs de :
 * - S'inscrire avec un nouveau compte (nom dutilisateur + mot de passe)
 * - Se connecter avec un compte existant
 */
public class Authentification implements ActionListener {

    private final JFrame fenetre;
    private final JTextField champSaisie;
    private final JTextArea texte;
    private final GestionnaireComptes gestionnaire;
    
    // etats de l authentification
    private enum Etat {
        MENU, INSCRIRE_NOM, INSCRIRE_MDP, INSCRIRE_MDP_CONFIRMER,
        CONNEXION_NOM, CONNEXION_MDP
    }
    
    private Etat etat = Etat.MENU;
    private String nomUtilisateurTemp;
    private String mdpTemp1;
    private String nomUtilisateurConnecte;

    public Authentification() {
        this.gestionnaire = new GestionnaireComptes();
        this.nomUtilisateurConnecte = null;
        
        // Fenêtre principale
        fenetre = new JFrame("Nuit à la Fac - Authentification");
        fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Zone de texte scrollable
        texte = new JTextArea();
        texte.setEditable(false);
        texte.setLineWrap(true);
        texte.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(texte);
        scroll.setPreferredSize(new Dimension(400, 250));
        
        champSaisie = new JTextField();
        champSaisie.addActionListener(this);

        var panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(champSaisie, BorderLayout.SOUTH);
        
        fenetre.getContentPane().add(panel);
        fenetre.pack();
        fenetre.setLocationRelativeTo(null);
        fenetre.setVisible(true);
        

        afficherMenu();
        
        SwingUtilities.invokeLater(champSaisie::requestFocusInWindow);
    }

    /**
     * Affiche le menu principal.
     */
    private void afficherMenu() {
        texte.setText("");
        afficher("===== BIENVENUE =====");
        afficher("");
        afficher("Que voulez-vous faire ?");
        afficher("");
        afficher("Tapez 'I' pour vous inscrire");
        afficher("Tapez 'C' pour vous connecter");
        afficher("Tapez 'Q' pour quitter");
        afficher("");
        champSaisie.setText("");
        etat = Etat.MENU;
    }

    /**
     * Affiche un message dans la zone de texte.
     */
    private void afficher(String message) {
        SwingUtilities.invokeLater(() -> {
            texte.append(message + "\n");
            texte.setCaretPosition(texte.getDocument().getLength());
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String saisie = champSaisie.getText().trim().toUpperCase();
        champSaisie.setText("");
        
        if (saisie.isEmpty()) {
            return;
        }
        
        afficher("> " + champSaisie.getText());
        
        switch (etat) {
            case MENU:
                traiterMenu(saisie);
                break;
            case INSCRIRE_NOM:
                traiterInscribeNom(saisie);
                break;
            case INSCRIRE_MDP:
                traiterInscribeMdp(saisie);
                break;
            case INSCRIRE_MDP_CONFIRMER:
                traiterInscribeConfirmer(saisie);
                break;
            case CONNEXION_NOM:
                traiterConnexionNom(saisie);
                break;
            case CONNEXION_MDP:
                traiterConnexionMdp(saisie);
                break;
        }
    }

    /**
     * Traite les commandes du menu principal.
     */
    private void traiterMenu(String commande) {
        if ("I".equals(commande)) {
            afficher("");
            afficher("--- INSCRIPTION ---");
            afficher("");
            afficher("Entrez votre nom d'utilisateur :");
            etat = Etat.INSCRIRE_NOM;
        } else if ("C".equals(commande)) {
            afficher("");
            afficher("--- CONNEXION ---");
            afficher("");
            afficher("Entrez votre nom d'utilisateur :");
            etat = Etat.CONNEXION_NOM;
        } else if ("Q".equals(commande)) {
            System.exit(0);
        } else {
            afficher("Commande inconnue. Tapez 'I', 'C' ou 'Q'.");
        }
    }

    /**
     * Traite le nom d'utilisateur durant l'inscription.
     */
    private void traiterInscribeNom(String nom) {
        if (nom.isEmpty()) {
            afficher("Le nom d'utilisateur ne peut pas être vide.");
            return;
        }
        
        if (gestionnaire.utilisateurExiste(nom)) {
            afficher("Cet utilisateur existe déjà.");
            afficher("");
            afficherMenu();
            return;
        }
        
        nomUtilisateurTemp = nom;
        afficher("Entrez votre mot de passe :");
        etat = Etat.INSCRIRE_MDP;
    }

    /**
     * Traite le mot de passe durant l'inscription.
     */
    private void traiterInscribeMdp(String mdp) {
        if (mdp.isEmpty()) {
            afficher("Le mot de passe ne peut pas être vide.");
            return;
        }
        
        mdpTemp1 = mdp;
        afficher("Confirmez votre mot de passe :");
        etat = Etat.INSCRIRE_MDP_CONFIRMER;
    }

    /**
     * Traite la confirmation du mot de passe durant l'inscription.
     */
    private void traiterInscribeConfirmer(String mdp) {
        if (!mdp.equals(mdpTemp1)) {
            afficher("Les mots de passe ne correspondent pas.");
            afficher("");
            afficherMenu();
            return;
        }
        
        if (gestionnaire.inscrire(nomUtilisateurTemp, mdpTemp1)) {
            afficher("Inscription réussie !");
            afficher("");
            afficherMenu();
        } else {
            afficher("Erreur lors de l'inscription.");
            afficher("");
            afficherMenu();
        }
    }

    /**
     * Traite le nom d'utilisateur durant la connexion.
     */
    private void traiterConnexionNom(String nom) {
        if (nom.isEmpty()) {
            afficher("Le nom d'utilisateur ne peut pas être vide.");
            return;
        }
        
        nomUtilisateurTemp = nom;
        afficher("Entrez votre mot de passe :");
        etat = Etat.CONNEXION_MDP;
    }

    /**
     * Traite le mot de passe durant la connexion.
     */
    private void traiterConnexionMdp(String mdp) {
        if (gestionnaire.authentifier(nomUtilisateurTemp, mdp)) {
            afficher("Connexion réussie !");
            nomUtilisateurConnecte = nomUtilisateurTemp;
            afficher("");
            
            // Attendre avant de lancer le jeu
            SwingUtilities.invokeLater(() -> {
                try {
                    Thread.sleep(800);
                } catch (InterruptedException ignored) {
                }
                demarrerJeu();
            });
        } else {
            afficher("Nom d'utilisateur ou mot de passe incorrect.");
            afficher("");
            afficherMenu();
        }
    }

    /**
     * Vérifie si ll'utilisateur existe.
     */
    private boolean utilisateurExiste(String nom) {
        // Cette méthode est un peu surcharge, mais elle doit être accessible
        // On peut utiliser la méthode du gestionnaire indirectement
        try {
            gestionnaire.authentifier(nom, "test_pour_vérification");
            return true; // L'utilisateur existe si l'authentification est passée
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Démarre le jeu 
     */
    private void demarrerJeu() {
        fenetre.dispose();
        Jeu jeu = new Jeu(nomUtilisateurConnecte);
        GUI gui = new GUI(jeu);
        jeu.setGUI(gui);
    }

    /**
     * Retourne le nom d'utilisateur Actuellement connecté.
     */
    public String getNomUtilisateurConnecte() {
        return nomUtilisateurConnecte;
    }
}

