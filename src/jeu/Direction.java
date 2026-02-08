package jeu;

/**
 * Enumération des directions possibles dans le jeu.
 * <p>
 * Chaque direction correspond à un déplacement du joueur dans la zone de jeu.
 * Les directions disponibles sont NORD, SUD, EST et OUEST.
 * Chaque direction possède une <b>abréviation</b> et une <b>description complète</b>
 * pour faciliter la saisie des commandes et l’affichage dans le jeu.
 * <p>
 * Exemples :
 * <ul>
 *   <li>{@link #NORD} : abréviation "N", description "N (aller au nord)"</li>
 *   <li>{@link #SUD} : abréviation "S", description "S (aller au sud)"</li>
 *   <li>{@link #EST} : abréviation "E", description "E (aller à l'est)"</li>
 *   <li>{@link #OUEST} : abréviation "O", description "O (aller à l'ouest)"</li>
 * </ul>
 */
public enum Direction implements Commande {
	/** Déplacement vers le nord */
    NORD("N", "N (aller au nord)"),
    
    /** Déplacement vers le sud */
    SUD("S", "S (aller au sud)"),
    
    /** Déplacement vers l'est */
    EST("E", "E (aller à l'est)"),
    
    /** Déplacement vers l'ouest */
    OUEST("O", "O (aller à l'ouest)");
	
	/** Abréviation de la direction */
	private final String abreviation;
	
	/** Description complète de la direction */
    private final String description;

    /**
     * Construit une direction avec son abréviation et sa description complète.
     *
     * @param a l’abréviation de la direction
     * @param d la description complète de la direction
     */
    private Direction(String a, String d) {
        this.abreviation = a;
        this.description = d;
    }

    /**
     * Retourne l’abréviation de la direction.
     *
     * @return l’abréviation (ex. "N" pour NORD)
     */
    @Override
    public String getAbreviation() {
        return abreviation;
    }

    /**
     * Retourne la description complète de la direction.
     *
     * @return la description complète (ex. "N (aller au nord)")
     */
    @Override
    public String getDescription() {
        return description;
    }
}
