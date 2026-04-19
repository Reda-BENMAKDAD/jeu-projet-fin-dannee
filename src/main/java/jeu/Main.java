package jeu;

import javax.swing.SwingUtilities;

/**
 * Point d'entrée du jeu.
 * <p>
 * Cette classe initialise le jeu et son interface graphique Swing.
 * Elle crée une instance de {@link Jeu} et une instance de {@link GUI},
 * puis associe la GUI au jeu.
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
            Jeu jeu = new Jeu();
            GUI gui = new GUI(jeu);
            jeu.setGUI(gui);
        });
	}
}
