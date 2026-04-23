package jeu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Représente une zone du jeu.
 * <p>
 * Chaque zone possède une description, une image, des sorties vers d'autres zones,
 * une liste d'objets présents, et des drapeaux décrivant son état (verrouillée,
 * sans retour possible, plongée dans le noir).
 */
public class Zone implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Description textuelle de la zone. */
  private String description;

  /** Nom du fichier image associé à la zone. */
  private String nomImage;

  /** Map des sorties disponibles : direction -> zone voisine. */
  private final Map<Direction, Zone> sorties;

  /** Objets présents dans la zone. */
  private List<Item> objets;

  /** Si {@code true}, le joueur ne peut pas revenir en arrière depuis cette zone. */
  private boolean sansRetour;

  /** Si {@code true}, la zone est plongée dans le noir (coupure de courant). */
  private boolean dansLeNoir;

  /** Si {@code true}, la sortie vers cette zone est bloquée jusqu'à déverrouillage. */
  private boolean verrouillee;

  /** Texte lisible dans la zone (tableau, panneau, écran) affiché par la commande LIRE. */
  private String texte;

  /**
   * Construit une zone avec une description et un nom d'image.
   *
   * @param description description de la zone
   * @param image nom du fichier image associé
   */
  public Zone(String description, String image) {
    this.description = description;
    this.nomImage = image;
    this.sorties = new EnumMap<>(Direction.class);
    this.objets = new ArrayList<>();
    this.sansRetour = false;
    this.dansLeNoir = false;
    this.verrouillee = false;
    this.texte = null;
  }

  /**
   * Ajoute une sortie depuis cette zone vers une zone voisine.
   *
   * @param sortie la direction de la sortie
   * @param zoneVoisine la zone vers laquelle la sortie mène
   */
  public void ajouteSortie(Direction sortie, Zone zoneVoisine) {
    sorties.put(sortie, zoneVoisine);
  }

  /**
   * Obtient la zone voisine dans la direction donnée.
   *
   * @param direction direction de la sortie
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
   * @return la description
   */
  @Override
  public String toString() {
    return description;
  }

  /**
   * Retourne une description complète de la zone, incluant sorties et objets visibles.
   *
   * @return description complète
   */
  public String descriptionLongue() {
    StringBuilder sb = new StringBuilder();
    sb.append("Vous êtes dans ").append(description);
    if (dansLeNoir) {
      sb.append("\n[Il fait nuit noire — vous ne voyez presque rien.]");
    }
    sb.append("\nSorties : ").append(sorties());
    if (!objets.isEmpty() && !dansLeNoir) {
      sb.append("\nObjets visibles : ").append(listeObjets());
    }
    return sb.toString();
  }

  /**
   * Ajoute un objet dans la zone.
   *
   * @param item l'objet à déposer
   */
  public void ajouterObjet(Item item) {
    objets.add(item);
  }

  /**
   * Retire et retourne l'objet portant le nom donné (insensible à la casse).
   *
   * @param nom le nom de l'objet à récupérer
   * @return l'objet retiré, ou {@code null} si non trouvé
   */
  public Item retirerObjet(String nom) {
    for (int i = 0; i < objets.size(); i++) {
      if (objets.get(i).getNom().equalsIgnoreCase(nom)) {
        return objets.remove(i);
      }
    }
    return null;
  }

  /**
   * Retourne la liste des objets présents dans la zone.
   *
   * @return liste (non modifiable) des objets
   */
  public List<Item> getObjets() {
    return java.util.Collections.unmodifiableList(objets);
  }

  /**
   * Indique si le joueur ne peut pas revenir en arrière depuis cette zone.
   *
   * @return {@code true} si sans retour
   */
  public boolean isSansRetour() {
    return sansRetour;
  }

  /**
   * Définit si la zone est sans retour possible.
   *
   * @param sansRetour {@code true} pour bloquer le retour
   */
  public void setSansRetour(boolean sansRetour) {
    this.sansRetour = sansRetour;
  }

  /**
   * Indique si la zone est plongée dans le noir.
   *
   * @return {@code true} si coupure de courant active
   */
  public boolean isDansLeNoir() {
    return dansLeNoir;
  }

  /**
   * Définit l'état de coupure de courant de la zone.
   *
   * @param dansLeNoir {@code true} pour activer le noir
   */
  public void setDansLeNoir(boolean dansLeNoir) {
    this.dansLeNoir = dansLeNoir;
  }

  /**
   * Indique si la zone est verrouillée (accès bloqué).
   *
   * @return {@code true} si verrouillée
   */
  public boolean isVerrouillee() {
    return verrouillee;
  }

  /**
   * Verrouille ou déverrouille la zone.
   *
   * @param verrouillee {@code true} pour verrouiller
   */
  public void setVerrouillee(boolean verrouillee) {
    this.verrouillee = verrouillee;
  }

  /**
   * Retourne le texte lisible de la zone (tableau, panneau, écran).
   *
   * @return le texte, ou {@code null} si rien à lire
   */
  public String getTexte() {
    return texte;
  }

  /**
   * Définit le texte lisible de la zone.
   *
   * @param texte le contenu textuel affiché par la commande LIRE
   */
  public void setTexte(String texte) {
    this.texte = texte;
  }

  /** Retourne une chaîne listant les directions disponibles. */
  private String sorties() {
    return sorties.keySet().toString();
  }

  /** Retourne une chaîne listant les noms des objets présents. */
  private String listeObjets() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < objets.size(); i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(objets.get(i).getNom());
    }
    return sb.toString();
  }
}
