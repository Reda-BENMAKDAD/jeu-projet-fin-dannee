package jeu;

/**
 * Enumération des commandes non directionnelles du jeu.
 * <p>
 * Ces commandes couvrent les interactions avec l'environnement, la gestion de
 * l'inventaire, les énigmes et le contrôle général du jeu.
 * Chaque commande possède une <b>abréviation</b> et une <b>description complète</b>.
 */
public enum CommandeNonDirectionnelle implements Commande {

  /** Affiche l'aide listant toutes les commandes disponibles. */
  AIDE("?", "? — afficher la liste des commandes"),

  /** Affiche le contenu du sac et les slots restants. */
  INVENTAIRE("I", "I — voir votre sac et les emplacements restants"),

  /** Affiche l'énergie restante du joueur. */
  ETAT("ET", "ET — afficher votre niveau d'énergie"),

  /** Affiche une description détaillée de la zone courante. */
  OBSERVER("OBS", "OBS — observer la pièce (objets, sorties, état)"),

  /** Prend un objet présent dans la zone. Syntaxe : PRENDRE &lt;objet&gt; */
  PRENDRE("P", "P <nom> — ramasser un objet de la pièce"),

  /** Dépose un objet du sac dans la zone courante. Syntaxe : DEPOSER &lt;objet&gt; */
  DEPOSER("DEP", "DEP <nom> — poser un objet de votre sac"),

  /** Utilise un objet du sac dans le contexte courant. Syntaxe : UTILISER &lt;objet&gt; */
  UTILISER("U", "U <nom> — utiliser un objet de votre sac"),

  /** Lit le texte présent dans la zone (tableau, panneau, écran). */
  LIRE("L", "L — lire le tableau, panneau ou écran de la pièce"),

  /** Répond à une énigme active. Syntaxe : CHOISIR &lt;réponse&gt; */
  CHOISIR("CH", "CH <réponse> — soumettre une réponse à une énigme"),

  /** Parle au personnage non-joueur présent dans la zone. */
  PARLER("PA", "PA — parler à un personnage présent"),

  /** Revient à la zone précédente (multi-niveaux). */
  RETOUR("R", "R — retourner dans la pièce précédente"),

  /** Sauvegarde la partie en cours. */
  SAUVEGARDER("SAV", "SAV — sauvegarder la partie en cours"),

  /** Termine le jeu. */
  QUITTER("Q", "Q — quitter le jeu");

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
