package jeu;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * Représente le joueur dans le jeu.
 * <p>
 * Gère l'énergie, l'inventaire (4 emplacements max) et l'historique des zones
 * visitées (pile utilisée pour la commande RETOUR multi-niveaux).
 */
public class Joueur implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Énergie de départ du joueur. */
  private static final int ENERGIE_INITIALE = 100;

  /** Nombre maximum d'emplacements dans le sac. */
  private static final int SLOTS_MAX = 4;

  /** Nom du joueur. */
  private String nom;

  /** Niveau d'énergie courant (0 = mort). */
  private int energie;

  /** Inventaire du joueur. */
  private List<Item> sac;

  /** Pile des zones visitées pour la commande RETOUR. */
  private Deque<Zone> historique;

  /**
   * Construit un joueur avec le nom donné et les valeurs initiales.
   *
   * @param nom le nom du joueur connecté
   */
  public Joueur(String nom) {
    this.nom = nom;
    this.energie = ENERGIE_INITIALE;
    this.sac = new ArrayList<>();
    this.historique = new ArrayDeque<>();
  }

  /**
   * Retourne le nom du joueur.
   *
   * @return le nom
   */
  public String getNom() {
    return nom;
  }

  /**
   * Retourne l'énergie courante du joueur.
   *
   * @return énergie (0–100)
   */
  public int getEnergie() {
    return energie;
  }

  /**
   * Tente d'ajouter un item dans le sac du joueur.
   * <p>
   * Échoue si les slots disponibles sont insuffisants.
   *
   * @param item l'objet à ajouter
   * @return {@code true} si l'ajout a réussi, {@code false} sinon
   */
  public boolean ajouterItem(Item item) {
    if (slotsDisponibles() < item.getSlots()) {
      return false;
    }
    sac.add(item);
    return true;
  }

  /**
   * Retire et retourne l'item portant le nom donné (insensible à la casse).
   *
   * @param nom le nom de l'objet à retirer
   * @return l'item retiré, ou {@code null} si non trouvé
   */
  public Item retirerItem(String nom) {
    Iterator<Item> it = sac.iterator();
    while (it.hasNext()) {
      Item item = it.next();
      if (item.getNom().equalsIgnoreCase(nom)) {
        it.remove();
        return item;
      }
    }
    return null;
  }

  /**
   * Retourne le nombre d'emplacements encore disponibles dans le sac.
   *
   * @return slots libres (0 à 4)
   */
  public int slotsDisponibles() {
    int utilises = 0;
    for (Item item : sac) {
      utilises += item.getSlots();
    }
    return SLOTS_MAX - utilises;
  }

  /**
   * Réduit l'énergie du joueur. L'énergie ne descend pas en dessous de 0.
   *
   * @param montant points d'énergie à retirer
   */
  public void perdreEnergie(int montant) {
    energie = Math.max(0, energie - montant);
  }

  /**
   * Augmente l'énergie du joueur. L'énergie ne dépasse pas 100.
   *
   * @param montant points d'énergie à gagner
   */
  public void gagnerEnergie(int montant) {
    energie = Math.min(100, energie + montant);
  }

  /**
   * Indique si le joueur est mort (énergie à 0).
   *
   * @return {@code true} si l'énergie est à 0
   */
  public boolean estMort() {
    return energie <= 0;
  }

  /**
   * Retourne une vue non modifiable du sac du joueur.
   *
   * @return liste des items en inventaire
   */
  public List<Item> getSac() {
    return Collections.unmodifiableList(sac);
  }

  /**
   * Vérifie si le joueur possède un item portant le nom donné (insensible à la casse).
   *
   * @param nom le nom de l'objet cherché
   * @return {@code true} si l'objet est dans le sac
   */
  public boolean possedeItem(String nom) {
    for (Item item : sac) {
      if (item.getNom().equalsIgnoreCase(nom)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Retourne la pile de l'historique des zones pour la commande RETOUR.
   *
   * @return la pile des zones visitées
   */
  public Deque<Zone> getHistorique() {
    return historique;
  }

  /**
   * Retourne un résumé de l'inventaire et de l'énergie pour affichage.
   *
   * @return chaîne formatée décrivant le sac et l'énergie
   */
  public String afficherInventaire() {
    int utilises = SLOTS_MAX - slotsDisponibles();
    StringBuilder sb = new StringBuilder();
    sb.append("=== Inventaire (").append(utilises).append("/").append(SLOTS_MAX).append(" slots) ===\n");
    if (sac.isEmpty()) {
      sb.append("Sac vide.\n");
    } else {
      for (Item item : sac) {
        sb.append("  - ").append(item).append("\n");
      }
    }
    sb.append("Énergie : ").append(energie).append("/100");
    return sb.toString();
  }
}
