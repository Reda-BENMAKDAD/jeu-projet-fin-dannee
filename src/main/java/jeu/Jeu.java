package jeu;

/**
 * La classe {@code Jeu} représente la logique principale du jeu.
 * <p>
 * Elle gère les zones, les déplacements, les commandes utilisateur et
 * l'interaction avec l'interface graphique {@link GUI}.
 * <p>
 * Les fonctionnalités principales incluent :
 * <ul>
 *   <li>Création et initialisation des zones du jeu</li>
 *   <li>Affichage de la localisation et des images correspondantes</li>
 *   <li>Traitement des commandes saisies par l'utilisateur</li>
 *   <li>Gestion de la fin du jeu</li>
 * </ul>
 *
 * Exemple d'utilisation :
 * <pre>
 *   Jeu jeu = new Jeu("nomJoueur");
 *   GUI gui = new GUI(jeu);
 *   jeu.setGUI(gui);
 * </pre>
 */
public class Jeu {

  /** Interface graphique associée au jeu. */
  private GUI gui;

  /** Zone actuelle dans laquelle se trouve le joueur. */
  private Zone zoneCourante;

  /** Le joueur courant (énergie, inventaire, historique de navigation). */
  private Joueur joueur;

  /**
   * Construit un nouveau jeu avec un joueur authentifié.
   * <p>
   * Les zones sont créées et reliées entre elles, mais l'interface graphique
   * n'est pas encore initialisée. Utiliser {@link #setGUI(GUI)} pour associer
   * une interface.
   *
   * @param nomJoueur le nom du joueur connecté
   */
  public Jeu(String nomJoueur) {
    this.joueur = new Joueur(nomJoueur);
    creerCarte();
    gui = null;
  }

  /**
   * Associe une interface graphique au jeu et affiche le message de bienvenue.
   *
   * @param g l'instance de {@link GUI} à associer
   */
  public void setGUI(GUI g) {
    gui = g;
    afficherMessageDeBienvenue();
  }

  /**
   * Crée et initialise les zones du jeu et leurs sorties.
   */
  private void creerCarte() {
    Zone[] zones = new Zone[4];
    zones[0] = new Zone("le couloir", "Couloir.jpg");
    zones[1] = new Zone("l'escalier", "Escalier.jpg");
    zones[2] = new Zone("la grande salle", "GrandeSalle.jpg");
    zones[3] = new Zone("la salle à manger", "SalleAManger.jpg");

    zones[0].ajouteSortie(Direction.EST, zones[1]);
    zones[1].ajouteSortie(Direction.OUEST, zones[0]);
    zones[1].ajouteSortie(Direction.EST, zones[2]);
    zones[2].ajouteSortie(Direction.OUEST, zones[1]);
    zones[3].ajouteSortie(Direction.SUD, zones[0]);
    zones[0].ajouteSortie(Direction.NORD, zones[3]);

    zoneCourante = zones[0];
  }

  /** Vérifie que la GUI est initialisée avant toute interaction. */
  private void verifieGUI() {
    if (gui == null) {
      throw new IllegalStateException("GUI non initialisée !");
    }
  }

  /** Affiche la description complète de la zone actuelle via l'interface graphique. */
  private void afficherLocalisation() {
    verifieGUI();
    gui.afficher(zoneCourante.descriptionLongue());
    gui.afficher();
  }

  /** Affiche le message de bienvenue et la localisation initiale. */
  private void afficherMessageDeBienvenue() {
    verifieGUI();
    gui.afficher("Bienvenue " + joueur.getNom() + " !");
    gui.afficher();
    gui.afficher("Tapez '?' pour obtenir de l'aide.");
    gui.afficher();
    afficherLocalisation();
    gui.afficheImage(zoneCourante.nomImage());
  }

  /**
   * Traite une commande saisie par l'utilisateur.
   * <p>
   * Commandes reconnues :
   * <ul>
   *   <li>?, AIDE : affiche l'aide</li>
   *   <li>N/NORD, S/SUD, E/EST, O/OUEST : déplacements</li>
   *   <li>R/RETOUR : revenir à la zone précédente</li>
   *   <li>I/INVENTAIRE : affiche le sac et l'énergie</li>
   *   <li>Q/QUITTER : termine le jeu</li>
   * </ul>
   *
   * @param commande la commande saisie par l'utilisateur
   */
  public void traiterCommande(String commande) {
    verifieGUI();
    gui.afficher("> " + commande + "\n");
    switch (commande.toUpperCase()) {
      case "?", "AIDE" -> afficherAide();
      case "N", "NORD" -> allerEn(Direction.NORD);
      case "S", "SUD" -> allerEn(Direction.SUD);
      case "E", "EST" -> allerEn(Direction.EST);
      case "O", "OUEST" -> allerEn(Direction.OUEST);
      case "R", "RETOUR" -> retour();
      case "I", "INVENTAIRE" -> afficherInventaire();
      case "Q", "QUITTER" -> terminer();
      default -> gui.afficher("Commande inconnue");
    }
  }

  /** Affiche l'aide à l'utilisateur. */
  private void afficherAide() {
    verifieGUI();
    gui.afficher("Êtes-vous perdu ?");
    gui.afficher();
    gui.afficher("Les commandes autorisées sont :");
    gui.afficher();
    gui.afficher(Commande.toutesLesDescriptions().toString());
    gui.afficher();
  }

  /**
   * Déplace le joueur vers une nouvelle zone dans la direction donnée.
   * <p>
   * La zone courante est empilée dans l'historique sauf si elle est marquée
   * {@code sansRetour}.
   *
   * @param direction la direction du déplacement
   */
  private void allerEn(Direction direction) {
    verifieGUI();
    Zone nouvelle = zoneCourante.obtientSortie(direction);
    if (nouvelle == null) {
      gui.afficher("Pas de sortie " + direction);
      gui.afficher();
    } else {
      if (!zoneCourante.isSansRetour()) {
        joueur.getHistorique().push(zoneCourante);
      }
      zoneCourante = nouvelle;
      gui.afficher(zoneCourante.descriptionLongue());
      gui.afficher();
      gui.afficheImage(zoneCourante.nomImage());
    }
  }

  /**
   * Revient à la zone précédente en dépilant l'historique du joueur.
   * <p>
   * Impossible si l'historique est vide ou si la zone courante est sans retour.
   */
  private void retour() {
    verifieGUI();
    if (zoneCourante.isSansRetour()) {
      gui.afficher("Vous ne pouvez pas faire demi-tour ici !");
      gui.afficher();
      return;
    }
    if (joueur.getHistorique().isEmpty()) {
      gui.afficher("Aucun retour possible !");
      gui.afficher();
    } else {
      zoneCourante = joueur.getHistorique().pop();
      gui.afficher(zoneCourante.descriptionLongue());
      gui.afficher();
      gui.afficheImage(zoneCourante.nomImage());
    }
  }

  /** Affiche l'inventaire et l'énergie du joueur. */
  private void afficherInventaire() {
    verifieGUI();
    gui.afficher(joueur.afficherInventaire());
    gui.afficher();
  }

  /** Termine le jeu, affiche un message d'au revoir et désactive l'interface. */
  private void terminer() {
    verifieGUI();
    gui.afficher("Au revoir...");
    gui.enable(false);
  }
}
