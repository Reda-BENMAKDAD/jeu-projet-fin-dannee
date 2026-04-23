package jeu;

import java.sql.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Classe gérant la persistance des comptes joueurs dans une base de données
 * SQLite.
 * 
 * Cette classe fournit des méthodes pour l'inscription et l'authentification
 * des joueurs. Les mots de passe sont hachés avant d'être stockés en base de
 * données.
 */
public class GestionnaireComptes {

    private static final String URL_BD = "jdbc:sqlite:comptes.db";
    private static final String NOM_TABLE = "comptes";

    /**
     * Initialise la base de données  créé la table si elle n'existe pas.
     */
    public GestionnaireComptes() {
        // Charger le driver SQLite
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver SQLite non trouvé: " + e.getMessage());
        }
        initialiserBD();
    }

    /**
     * Crée la table des comptes si elle n'existe pas.
     */
    private void initialiserBD() {
        String sql = "CREATE TABLE IF NOT EXISTS " + NOM_TABLE + " ("
                + "nom_utilisateur TEXT PRIMARY KEY,"
                + "mot_de_passe_hache TEXT NOT NULL"
                + ")";
        
        try (Connection conn = DriverManager.getConnection(URL_BD);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Erreur initialisation BD: " + e.getMessage());
        }
    }

    /**
     * Hache un mot de passe avec un salt pour le stocker en base de données.
     * 
     * @param motDePasse le mot de passe de lutilisteur
     * @return le mot de passe aprs haché avec salt
     */
    private String hacherMotDePasse(String motDePasse) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] hash = digest.digest(motDePasse.getBytes());
            
            // Combiner salt et hash puis encoder en Base64
            byte[] saltPlusHash = new byte[salt.length + hash.length];
            System.arraycopy(salt, 0, saltPlusHash, 0, salt.length);
            System.arraycopy(hash, 0, saltPlusHash, salt.length, hash.length);
            
            return Base64.getEncoder().encodeToString(saltPlusHash);
        } catch (Exception e) {
            throw new RuntimeException("Erreur hachage mot de passe", e);
        }
    }

    /**
     * Vérifie qu'un mot de passe en clair correspond au mot de passe haché stocké
     * en base de données.
     * 
     * @param motDePasseEnClair le mot de passe saisi par l'utilisateur
     * @param motDePasseHache   le mot de passe (haché) stocké sur le fichier sqlite
     * @return true si les mots de passe correspondent, false sinon
     */
    private boolean verifierMotDePasse(String motDePasseEnClair, String motDePasseHache) {
        try {
            byte[] decoded = Base64.getDecoder().decode(motDePasseHache);
            byte[] salt = new byte[16];
            System.arraycopy(decoded, 0, salt, 0, 16);
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] hash = digest.digest(motDePasseEnClair.getBytes());
            

            if (hash.length != decoded.length - 16) {
                return false;
            }
            for (int i = 0; i < hash.length; i++) {
                if (hash[i] != decoded[i + 16]) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Inscrit un nouveau joueur dans la base de données.
     * 
     * @param nomUtilisateur
     * @param motDePasse
     * @return true si l'inscription a réussi, false si l'utilisateur existe déjà
     */
    public boolean inscrire(String nomUtilisateur, String motDePasse) {
        if (utilisateurExiste(nomUtilisateur)) {
            return false;
        }
        
        String motDePasseHache = hacherMotDePasse(motDePasse);
        String sql = "INSERT INTO " + NOM_TABLE
                + " (nom_utilisateur, mot_de_passe_hache) VALUES (?, ?)";
        
        try (Connection conn = DriverManager.getConnection(URL_BD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomUtilisateur);
            pstmt.setString(2, motDePasseHache);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur inscription: " + e.getMessage());
            return false;
        }
    }

    /**
     * Authentifi un joueur en vérifiant son nom d'utilisateur et son mot de
     * passe.
     * 
     * @param nomUtilisateur
     * @param motDePasse
     * @return true si l'authentification a réussi, false sinon
     */
    public boolean authentifier(String nomUtilisateur, String motDePasse) {
        String sql = "SELECT mot_de_passe_hache FROM " + NOM_TABLE
                + " WHERE nom_utilisateur = ?";
        
        try (Connection conn = DriverManager.getConnection(URL_BD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomUtilisateur);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String motDePasseHache = rs.getString("mot_de_passe_hache");
                return verifierMotDePasse(motDePasse, motDePasseHache);
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erreur authentification: " + e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si un utilisateur existe en bdd.
     * 
     * @param nomUtilisateur le nom d'utilisateur
     * @return true si l'utilisateur existe, false sinon
     */
    public boolean utilisateurExiste(String nomUtilisateur) {
        String sql = "SELECT 1 FROM " + NOM_TABLE + " WHERE nom_utilisateur = ?";
        
        try (Connection conn = DriverManager.getConnection(URL_BD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomUtilisateur);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Erreur verif utilisateur: " + e.getMessage());
            return false;
        }
    }
}
