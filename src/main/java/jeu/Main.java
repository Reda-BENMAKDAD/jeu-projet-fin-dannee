package jeu;

import javax.swing.SwingUtilities;

/**
 * Point d'entrée du jeu.
 * <p>
 * Cette classe initialise l'interface d'authentification.
 * Une fois l'utilisateur authentifié, le jeu démarre.
 * <p>
 * L'initialisation est effectuée dans le thread de dispatching Swing
 * grâce à {@link javax.swing.SwingUtilities#invokeLater(Runnable)}, 
 * pour respecter les bonnes pratiques de Swing.
 * 
 * <pre>
 * Exemple d'exécution :
 *   java Main
 * </pre>
 */
public class Main {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
            new Authentification();
        });
	}
}
