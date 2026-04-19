package jeu;

/**
 * La classe {@code Jeu} représente la logique principale du jeu.
 * <p>
 * Elle gère les zones, les déplacements, les commandes utilisateur et
 * l'interaction avec l'interface graphique {@link GUI}.
 * <p>
 * Les fonctionnalités principales incluent :
 * <ul>
 *     <li>Création et initialisation des zones du jeu</li>
 *     <li>Affichage de la localisation et des images correspondantes</li>
 *     <li>Traitement des commandes saisies par l'utilisateur</li>
 *     <li>Gestion de la fin du jeu</li>
 * </ul>
 *
 * Exemple d'utilisation :
 * <pre>
 *     Jeu jeu = new Jeu();
 *     GUI gui = new GUI(jeu);
 *     jeu.setGUI(gui);
 * </pre>
 */
public class Jeu {
	
	/** Interface graphique associée au jeu */
	private GUI gui; 
	private Zone zonePrecedente;
	/** Zone actuelle dans laquelle se trouve le joueur */
	private Zone zoneCourante;
    
	/**
     * Construit un nouveau jeu.
     * <p>
     * Les zones sont créées et reliées entre elles, mais l'interface graphique
     * n'est pas encore initialisée. Utiliser {@link #setGUI(GUI)} pour associer
     * une interface.
     */
	public Jeu() {
        creerCarte();
        gui = null;
    }

	/**
     * Associe une interface graphique au jeu et affiche le message de bienvenue.
     *
     * @param g l'instance de {@link GUI} à associer
     */
	public void setGUI( GUI g) { 
		gui = g; 
		afficherMessageDeBienvenue(); 
	}
    
	/**
     * Crée et initialise les zones du jeu et leurs sorties.
     */
	private void creerCarte() {
		// Création de 4 zones
        Zone [] zones = new Zone [4];
        zones[0] = new Zone("le couloir", "Couloir.jpg" );
        zones[1] = new Zone("l'escalier", "Escalier.jpg" );
        zones[2] = new Zone("la grande salle", "GrandeSalle.jpg" );
        zones[3] = new Zone("la salle à manger", "SalleAManger.jpg" );
        
        // Définition des sorties des zones
        zones[0].ajouteSortie(Direction.EST, zones[1]);
        zones[1].ajouteSortie(Direction.OUEST, zones[0]);
        zones[1].ajouteSortie(Direction.EST, zones[2]);
        zones[2].ajouteSortie(Direction.OUEST, zones[1]);
        zones[3].ajouteSortie(Direction.SUD, zones[0]);
        zones[0].ajouteSortie(Direction.NORD, zones[3]);
        
        // Zone de départ du joueur
        zoneCourante = zones[0]; 
    }
	
	/** 
	 * Vérifie l'initialisation de gui
	 */
	private void verifieGUI() {
	    if (gui == null) {
	        throw new IllegalStateException("GUI non initialisée !");
	    }
	}

	/**
     * Affiche la description complète de la zone actuelle via l'interface graphique.
     */
	private void afficherLocalisation() {
		verifieGUI();
        gui.afficher( zoneCourante.descriptionLongue());
        gui.afficher();
    }

	/**
     * Affiche le message de bienvenue et la localisation initiale.
     */
	private void afficherMessageDeBienvenue() {
		verifieGUI();
		gui.afficher("Bienvenue !");
    	gui.afficher();
        gui.afficher("Tapez '?' pour obtenir de l'aide.");
        gui.afficher();
        afficherLocalisation();
        gui.afficheImage(zoneCourante.nomImage());
    }
    
	/**
     * Traite une commande saisie par l'utilisateur.
     * <p>
     * Les commandes reconnues sont :
     * <ul>
     *     <li>?: affiche l'aide</li>
     *     <li>AIDE: affiche l'aide</li>
     *     <li>N ou NORD: se déplacer vers le nord</li>
     *     <li>S ou SUD: se déplacer vers le sud</li>
     *     <li>E ou EST: se déplacer vers l'est</li>
     *     <li>O ou OUEST: se déplacer vers l'ouest</li>
     *     <li>Q ou QUITTER: termine le jeu</li>
     * </ul>
     * Toute commande non reconnue génère un message d'erreur.
     *
     * @param commande la commande saisie par l'utilisateur
     */
	public void traiterCommande(String commande) {
		verifieGUI();
		gui.afficher( "> "+ commande + "\n");
        switch (commande.toUpperCase()) {
        case "?", "AIDE" -> afficherAide(); 
        case "N", "NORD" -> allerEn( Direction.NORD); 
        case "S", "SUD" -> allerEn( Direction.SUD); 
        case "E", "EST" -> allerEn( Direction.EST);
		case "R", "RETOUR" -> Retour();
        case "O", "OUEST" -> allerEn( Direction.OUEST); 
        case "Q", "QUITTER" -> terminer();
       	default -> gui.afficher("Commande inconnue");
        }
    }

	/**
     * Affiche l'aide à l'utilisateur, incluant toutes les commandes disponibles.
     */
	private void afficherAide() {
		verifieGUI();
        gui.afficher("Etes-vous perdu ?");
        gui.afficher();
        gui.afficher("Les commandes autorisées sont :");
        gui.afficher();
        gui.afficher(Commande.toutesLesDescriptions().toString());
        gui.afficher();
    }

	/**
     * Déplace le joueur vers une nouvelle zone dans la direction donnée.
     *
     * @param direction ("NORD", "SUD", "EST" ou "OUEST")
     */
	private void allerEn(Direction direction) {
		verifieGUI();
		zonePrecedente = zoneCourante;
		Zone nouvelle = zoneCourante.obtientSortie( direction);
    	if ( nouvelle == null ) {
        	gui.afficher( "Pas de sortie " + direction);
    		gui.afficher();
    	} else {
        	zoneCourante = nouvelle;
        	gui.afficher(zoneCourante.descriptionLongue());
        	gui.afficher();
        	gui.afficheImage(zoneCourante.nomImage());
        }
    }

	private void Retour() {
		verifieGUI();
		if (zonePrecedente == null) {
			gui.afficher("Aucun retour possible !");
			gui.afficher();
		} else {
			Zone temp = zoneCourante;
			zoneCourante = zonePrecedente;
			zonePrecedente = temp;
			gui.afficher(zoneCourante.descriptionLongue());
			gui.afficher();
			gui.afficheImage(zoneCourante.nomImage());
		}
	}
    
	/**
     * Termine le jeu, affiche un message d'au revoir et désactive l'interface.
     */
	private void terminer() {
		verifieGUI();
    	gui.afficher( "Au revoir...");
    	gui.enable( false);
    }
}
