package jeu;

import java.io.Serializable;

/**
 * Représente un objet pouvant être ramassé et transporté par le joueur.
 * <p>
 * Chaque item occupe un certain nombre d'emplacements dans le sac du joueur
 * (entre 1 et 2). La clé de sortie occupe 2 emplacements, forçant un choix
 * stratégique lors de la collecte.
 */
public class Item implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Nom de l'objet (identifiant textuel utilisé dans les commandes). */
  private String nom;

  /** Nombre d'emplacements occupés dans le sac (1 ou 2). */
  private int slots;

  /** Description courte affichée dans l'inventaire. */
  private String description;

  /**
   * Construit un item avec son nom, son nombre de slots et sa description.
   *
   * @param nom le nom de l'objet
   * @param slots le nombre d'emplacements occupés (1 ou 2)
   * @param description description courte de l'objet
   */
  public Item(String nom, int slots, String description) {
    this.nom = nom;
    this.slots = slots;
    this.description = description;
  }

  /**
   * Retourne le nom de l'objet.
   *
   * @return le nom
   */
  public String getNom() {
    return nom;
  }

  /**
   * Retourne le nombre d'emplacements occupés dans le sac.
   *
   * @return nombre de slots (1 ou 2)
   */
  public int getSlots() {
    return slots;
  }

  /**
   * Retourne la description courte de l'objet.
   *
   * @return la description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Retourne une représentation textuelle de l'objet pour l'affichage inventaire.
   *
   * @return chaîne formatée avec nom, slots et description
   */
  @Override
  public String toString() {
    return nom + " (" + slots + " slot" + (slots > 1 ? "s" : "") + ") – " + description;
  }
}
