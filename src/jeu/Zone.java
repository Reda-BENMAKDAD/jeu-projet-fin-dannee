package jeu;
import java.util.Map;
import java.util.EnumMap;

/**
 * Représente une zone du jeu.
 * <p>
 * Chaque zone possède une description, une image et des sorties vers
 * d'autres zones identifiées par des directions.
 */
public class Zone {
	/** Description textuelle de la zone. */
	private String description;
	
	/** Nom du fichier image associé à la zone. */
	private String nomImage;
	
    /** Map des sorties disponibles : direction -> zone voisine. */
	private final Map<Direction,Zone> sorties;   

	/**
     * Construit une zone avec une description et un nom d'image.
     *
     * @param description description de la zone
     * @param image nom du fichier image associé
     */
	public Zone(String description, String image) {
        this.description = description;
        nomImage = image;
        sorties = new EnumMap<>(Direction.class);
    }

	/**
     * Ajoute une sortie depuis cette zone vers une zone voisine.
     *
     * @param sortie la direction de la sortie (enum {@link Direction})
     * @param zoneVoisine la zone vers laquelle la sortie mène
     */
	public void ajouteSortie(Direction sortie, Zone zoneVoisine) {
        sorties.put(sortie, zoneVoisine);
    }

	/**
     * Obtient la zone voisine dans la direction donnée.
     *
     * @param direction direction de la sortie ("NORD", "SUD", "EST", "OUEST", etc.)
     * @return la zone voisine si elle existe, sinon {@code null}
     */
	public Zone obtientSortie(Direction direction) {
    	return sorties.get(direction);
    }

	/**
     * Retourne le nom du fichier image associé à cette zone.
     *
     * @return le nom de l'image
     */
	public String nomImage() {
        return nomImage;
    }
     
	/**
     * Retourne la description courte de la zone.
     *
     * @return la description de la zone
     */
	public String toString() {
        return description;
    }

	/**
     * Retourne une description complète de la zone, incluant
     * la liste des sorties disponibles.
     *
     * @return description complète de la zone
     */
	public String descriptionLongue()  {
        return "Vous êtes dans " + description + "\nSorties : " + sorties();
    }

	/**
     * Retourne une chaîne listant les directions disponibles depuis cette zone.
     *
     * @return directions des sorties disponibles
     */
	private String sorties() {
        return sorties.keySet().toString();
    }

}

