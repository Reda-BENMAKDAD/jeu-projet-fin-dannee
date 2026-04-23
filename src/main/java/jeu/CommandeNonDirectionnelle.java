package jeu;

/**
 * Enumération des commandes non directionnelles du jeu.
 * <p>
 * Ces commandes ne correspondent pas à un déplacement dans une direction,
 * mais à des actions générales du joueur : interaction avec l'environnement,
 * gestion de l'inventaire, informations sur l'état du joueur, ou contrôle du jeu.
 * Chaque commande possède une <b>abréviation</b> et une <b>description complète</b>.
 */
public enum CommandeNonDirectionnelle implements Commande {

  /** Affiche l'aide listant toutes les commandes disponibles. */
  AIDE("?", "? (aide)"),

  /** Affiche le contenu du sac et les slots restants. */
  INVENTAIRE("I", "I (inventaire)"),

  /** Affiche l'énergie restante du joueur. */
  ETAT("ET", "ET (état / énergie)"),

  /** Affiche une description détaillée de la zone courante. */
  OBSERVER("OBS", "OBS (observer la zone)"),

  /** Prend un objet présent dans la zone. Syntaxe : PRENDRE <objet> */
  PRENDRE("P", "P <objet> (prendre un objet)"),

  /** Dépose un objet du sac dans la zone courante. Syntaxe : DEPOSER <objet> */
  DEPOSER("DEP", "DEP <objet> (déposer un objet)"),

  /** Lit le texte présent dans la zone (tableau, panneau, écran). */
  LIRE("L", "L (lire le tableau / panneau)"),

  /** Revient à la zone précédente (multi-niveaux). */
  RETOUR("R", "R (retour)"),

  /** Termine le jeu. */
  QUITTER("Q", "Q (quitter)");

  /** Abréviation de la commande. */
  private final String abreviation;

  /** Description complète de la commande. */
  private final String description;

  /**
   * Construit une commande avec son abréviation et sa description.
   *
   * @param abreviation l'abréviation de la commande
   * @param description la description complète
   */
  private CommandeNonDirectionnelle(String abreviation, String description) {
    this.abreviation = abreviation;
    this.description = description;
  }

  /**
   * Retourne l'abréviation de la commande.
   *
   * @return l'abréviation
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
