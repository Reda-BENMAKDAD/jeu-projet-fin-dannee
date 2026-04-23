package jeu;

/**
 * La classe {@code Jeu} représente la logique principale du jeu.
 * <p>
 * Elle gère les dix zones du scénario, les déplacements, les commandes
 * utilisateur et l'interaction avec l'interface graphique {@link GUI}.
 * <p>
 * Les sorties conditionnelles (Couloir→Escaliers, Couloir→SalleBloquante)
 * sont ajoutées dynamiquement par la logique des énigmes (Couche 4).
 * Les zones verrouillées (BatimentMEGA, SortieUniversite) nécessitent des
 * conditions de jeu pour être accessibles.
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

  // ── Références aux zones (utilisées par la logique des énigmes) ──────────

  /** Zone de départ — énigme du tableau pour déverrouiller le couloir. */
  private Zone salleDeCours;

  /** Couloir principal — labyrinthe BFS, hub vers salle secondaire et escaliers. */
  private Zone couloir;

  /** Salle secondaire — optionnelle, objets aléatoires. */
  private Zone salleSecondaire;

  /**
   * Salle bloquante — sans retour, game over progressif.
   * Pas de sortie dans la carte ; téléportation déclenchée par 3 erreurs BFS.
   */
  private Zone salleBloquante;

  /** Escaliers — accès conditionné par la réussite du BFS dans le couloir. */
  private Zone escaliers;

  /** Hall principal — hub central, jamais de coupure de courant. */
  private Zone hall;

  /** Restaurant universitaire — énigme plat. */
  private Zone restaurantU;

  /** Distributeur automatique — énigme code Java, donne code MEGA + cadeau. */
  private Zone distributeur;

  /** Bâtiment MEGA — agent de sécurité, verrouillé jusqu'à obtention du code. */
  private Zone batimentMega;

  /** Sortie de l'université — victoire, verrouillée jusqu'à obtention de la clé. */
  private Zone sortieUniversite;

  /**
   * Construit un nouveau jeu avec un joueur authentifié.
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
   * Crée les dix zones du scénario avec leurs sorties, attributs et objets fixes.
   *
   * <p>Sorties non ajoutées ici (gérées dynamiquement en Couche 4) :
   * <ul>
   *   <li>Couloir → Escaliers : ajoutée après réussite du labyrinthe BFS</li>
   *   <li>Couloir → SalleBloquante : déclenchée après 3 erreurs BFS</li>
   * </ul>
   */
  private void creerCarte() {
    // ── Création des zones ───────────────────────────────────────────────────
    salleDeCours = new Zone("la salle de cours", "SalleDeCours.jpg");
    couloir = new Zone("le couloir principal", "CouloirPrincipal.jpg");
    salleSecondaire = new Zone("la salle secondaire", "SalleSecondaire.jpg");
    salleBloquante = new Zone("la salle bloquante", "SalleBloquante.jpg");
    escaliers = new Zone("les escaliers", "Escaliers.jpg");
    hall = new Zone("le hall principal", "HallPrincipal.jpg");
    restaurantU = new Zone("le restaurant universitaire", "RestaurantU.jpg");
    distributeur = new Zone("le distributeur automatique", "Distributeur.jpg");
    batimentMega = new Zone("le bâtiment MEGA", "BatimentMEGA.jpg");
    sortieUniversite = new Zone("la sortie de l'université", "SortieUniversite.jpg");

    // ── Attributs spéciaux ───────────────────────────────────────────────────
    salleBloquante.setSansRetour(true);
    // BatimentMEGA : verrouillé jusqu'à présentation du code distributeur + carte
    batimentMega.setVerrouillee(true);
    // SortieUniversite : verrouillée jusqu'à obtention de la clé de sortie
    sortieUniversite.setVerrouillee(true);

    // ── Sorties statiques ────────────────────────────────────────────────────

    // SalleDeCours ↔ Couloir
    // (vérification énigme tableau ajoutée en Couche 4 dans allerEn())
    salleDeCours.ajouteSortie(Direction.NORD, couloir);
    couloir.ajouteSortie(Direction.SUD, salleDeCours);

    // Couloir ↔ SalleSecondaire (libre)
    couloir.ajouteSortie(Direction.EST, salleSecondaire);
    salleSecondaire.ajouteSortie(Direction.OUEST, couloir);

    // Escaliers ↔ Couloir et Hall (libres)
    escaliers.ajouteSortie(Direction.SUD, couloir);
    escaliers.ajouteSortie(Direction.NORD, hall);
    hall.ajouteSortie(Direction.SUD, escaliers);

    // Hall ↔ RestaurantU (libre)
    hall.ajouteSortie(Direction.EST, restaurantU);
    restaurantU.ajouteSortie(Direction.OUEST, hall);

    // Hall ↔ Distributeur (libre)
    hall.ajouteSortie(Direction.OUEST, distributeur);
    distributeur.ajouteSortie(Direction.EST, hall);

    // Hall → BatimentMEGA (verrouillé)
    hall.ajouteSortie(Direction.NORD, batimentMega);

    // BatimentMEGA ↔ Hall + BatimentMEGA → SortieUniversite (verrouillée)
    batimentMega.ajouteSortie(Direction.SUD, hall);
    batimentMega.ajouteSortie(Direction.NORD, sortieUniversite);

    // ── Objets fixes ─────────────────────────────────────────────────────────
    salleDeCours.ajouterObjet(
        new Item("Feuille de brouillon", 1,
            "Une feuille couverte de notes. On y devine : nom du prof, matière, code."));

    // ── Zone de départ ───────────────────────────────────────────────────────
    zoneCourante = salleDeCours;
  }

  /** Vérifie que la GUI est initialisée avant toute interaction. */
  private void verifieGUI() {
    if (gui == null) {
      throw new IllegalStateException("GUI non initialisée !");
    }
  }

  /** Affiche la description complète de la zone actuelle. */
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
   *   <li>R/RETOUR : revenir à la zone précédente (multi-niveaux)</li>
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
   * Vérifie l'existence de la sortie, puis l'état verrouillé de la destination
   * avant d'effectuer le déplacement. La zone courante est empilée dans
   * l'historique sauf si elle est marquée {@code sansRetour}.
   *
   * @param direction la direction du déplacement
   */
  private void allerEn(Direction direction) {
    verifieGUI();
    Zone nouvelle = zoneCourante.obtientSortie(direction);
    if (nouvelle == null) {
      gui.afficher("Pas de sortie " + direction);
      gui.afficher();
    } else if (nouvelle.isVerrouillee()) {
      gui.afficher(messagePourZoneVerrouillee(nouvelle));
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
   * Retourne le message d'accès refusé adapté à la zone verrouillée.
   *
   * @param zone la zone verrouillée
   * @return message à afficher au joueur
   */
  private String messagePourZoneVerrouillee(Zone zone) {
    if (zone == batimentMega) {
      return "L'entrée du bâtiment MEGA est verrouillée.\n"
          + "L'agent de sécurité vous barre la route. Il vous faut le code d'accès.";
    }
    if (zone == sortieUniversite) {
      return "Le portail de sortie est verrouillé.\n"
          + "Il vous faut la clé de sortie pour l'ouvrir.";
    }
    return "Cette zone est verrouillée. Vous devez remplir certaines conditions pour y accéder.";
  }

  /**
   * Revient à la zone précédente en dépilant l'historique du joueur.
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
