package jeu;

/**
 * Enumération des commandes non directionnelles du jeu.
 * <p>
 * Ces commandes ne correspondent pas à un déplacement dans une direction,
 * mais à des actions générales du joueur, comme afficher l'aide ou quitter le jeu.
 * Chaque commande possède une <b>abréviation</b> et une <b>description complète</b>.
 * <p>
 * Exemples :
 * <ul>
 *   <li>{@link #AIDE} : abréviation "?", description "? (aide)"</li>
 *   <li>{@link #QUITTER} : abréviation "Q", description "Q (quitter)"</li>
 * </ul>
 */
public enum CommandeNonDirectionnelle implements Commande {
	/** Commande affichant l’aide au joueur */
	AIDE("?", "? (aide)"), 
	
	/** Commande terminant le jeu */
	QUITTER("Q", "Q (quitter)"),

    RETOUR("R", "R (RETOUR)");

	/** Abréviation de la commande */
    private final String abreviation;
    
    /** Description complète de la commande */
    private final String description;
    
    /**
     * Construit une commande avec son abréviation et sa description.
     *
     * @param abreviation l’abréviation de la commande
     * @param description la description complète
     */
    private CommandeNonDirectionnelle(String a, String d ) { 
		abreviation = a;
		description = d; 
	}
    
    /**
     * Retourne l’abréviation de la commande.
     *
     * @return l’abréviation
     */
    @Override
    public String getAbreviation() {
        return abreviation;
    }

    /**
     * Retourne la description complète de la commande.
     *
     * @return la description complète
     */
    @Override
    public String getDescription() {
        return description;
    }
}
