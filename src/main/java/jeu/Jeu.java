package jeu;

/**
 * La classe {@code Jeu} représente la logique principale du jeu.
 * <p>
 * Elle gère les dix zones du scénario, les déplacements, les commandes
 * utilisateur et l'interaction avec l'interface graphique {@link GUI}.
 * <p>
 * Les sorties conditionnelles (Couloir→Escaliers, Couloir→SalleBloquante)
 * sont ajoutées dynamiquement par la logique des énigmes (Couche 4).
 * Les zones verrouillées (BatimentMEGA, SortieUniversite) nécessitent
 * des conditions de jeu pour être accessibles.
 *
 * <p>Exemple d'utilisation :
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
   * Crée les dix zones du scénario avec leurs sorties, attributs, objets fixes
   * et textes lisibles.
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
    batimentMega.setVerrouillee(true);
    sortieUniversite.setVerrouillee(true);

    // ── Textes lisibles (commande LIRE) ──────────────────────────────────────
    salleDeCours.setTexte(
        "Le tableau noir est couvert d'équations de programmation linéaire.\n"
        + "Maximiser Z = ax + by sous contraintes cx + dy ≤ e, fx + gy ≤ h, x,y ≥ 0.\n"
        + "Une note griffonnée en bas : « Le code = valeur optimale de Z ».\n"
        + "[Tapez CHOISIR <code> pour entrer le code — Énigme disponible en Couche 4]");

    couloir.setTexte(
        "Des panneaux d'itinéraire indiquent les sorties de secours.\n"
        + "Un écriteau posé par terre : « ATTENTION — Labyrinthe en cours de maintenance ».");

    salleSecondaire.setTexte(
        "Un tableau d'affichage présente les plannings des professeurs.\n"
        + "Vous notez les noms, matières et numéros de bureau.");

    salleBloquante.setTexte(
        "Un panneau d'alarme rouge clignote : 'ZONE RESTREINTE — NE PAS ENTRER'.\n"
        + "La porte derrière vous s'est verrouillée. Chaque action vous coûte de l'énergie.");

    escaliers.setTexte(
        "Un panneau directionnel : '↓ RDC — Hall principal  /  ↑ 1er étage — Salles de cours'.");

    hall.setTexte(
        "Un écran d'accueil affiche :\n"
        + "  Université — Fermeture exceptionnelle ce soir.\n"
        + "  Bonne nuit à tous !\n"
        + "En dessous, une affiche : « Bâtiment MEGA — Accès sur présentation du code ».");

    restaurantU.setTexte(
        "Le tableau du menu affiche plusieurs plats avec leurs ingrédients\n"
        + "et leurs heures de préparation.\n"
        + "[Tapez CHOISIR <numéro> pour choisir un plat — Énigme disponible en Couche 4]");

    distributeur.setTexte(
        "L'écran affiche un extrait de code Java :\n"
        + "  int x = 0;\n"
        + "  for (int i = 0; i < 5; i++) { x += i; }\n"
        + "  // Quelle est la valeur de x ?\n"
        + "[Tapez CHOISIR <réponse> pour répondre — Énigme disponible en Couche 4]");

    batimentMega.setTexte(
        "Une pancarte dorée : 'BÂTIMENT MEGA — Accès restreint au personnel autorisé.\n"
        + "Présentez votre badge et le code d'accès au gardien.'");

    sortieUniversite.setTexte(
        "Un panneau lumineux : 'SORTIE — Bonne journée !\n"
        + "Vérifiez que vous n'oubliez rien. Fermez bien en partant.'");

    // ── Sorties statiques ────────────────────────────────────────────────────
    salleDeCours.ajouteSortie(Direction.NORD, couloir);
    couloir.ajouteSortie(Direction.SUD, salleDeCours);

    couloir.ajouteSortie(Direction.EST, salleSecondaire);
    salleSecondaire.ajouteSortie(Direction.OUEST, couloir);

    escaliers.ajouteSortie(Direction.SUD, couloir);
    escaliers.ajouteSortie(Direction.NORD, hall);
    hall.ajouteSortie(Direction.SUD, escaliers);

    hall.ajouteSortie(Direction.EST, restaurantU);
    restaurantU.ajouteSortie(Direction.OUEST, hall);

    hall.ajouteSortie(Direction.OUEST, distributeur);
    distributeur.ajouteSortie(Direction.EST, hall);

    hall.ajouteSortie(Direction.NORD, batimentMega);

    batimentMega.ajouteSortie(Direction.SUD, hall);
    batimentMega.ajouteSortie(Direction.NORD, sortieUniversite);

    // ── Objets fixes ─────────────────────────────────────────────────────────
    salleDeCours.ajouterObjet(
        new Item("Feuille de brouillon", 1,
            "Une feuille couverte de notes : nom du prof, matière, code d'accès MEGA."));

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
   * La commande est découpée en verbe + argument optionnel pour supporter les
   * commandes paramétrées (PRENDRE, DEPOSER). Le verbe est comparé en majuscules.
   * <p>
   * Commandes reconnues :
   * <ul>
   *   <li>?, AIDE : affiche l'aide</li>
   *   <li>N/NORD, S/SUD, E/EST, O/OUEST : déplacements</li>
   *   <li>R/RETOUR : revenir à la zone précédente (multi-niveaux)</li>
   *   <li>I/INVENTAIRE : affiche le sac et l'énergie</li>
   *   <li>ET/ETAT/ENERGIE : affiche uniquement l'énergie</li>
   *   <li>OBS/OBSERVER : description détaillée de la zone</li>
   *   <li>P/PRENDRE &lt;objet&gt; : prend un objet dans la zone</li>
   *   <li>DEP/DEPOSER &lt;objet&gt; : dépose un objet du sac</li>
   *   <li>L/LIRE : lit le texte présent dans la zone</li>
   *   <li>Q/QUITTER : termine le jeu</li>
   * </ul>
   *
   * @param commande la commande saisie par l'utilisateur
   */
  public void traiterCommande(String commande) {
    verifieGUI();
    gui.afficher("> " + commande + "\n");

    String[] parties = commande.trim().split("\\s+", 2);
    String verbe = parties[0].toUpperCase();
    String argument = parties.length > 1 ? parties[1].trim() : "";

    switch (verbe) {
      case "?", "AIDE"              -> afficherAide();
      case "N", "NORD"              -> allerEn(Direction.NORD);
      case "S", "SUD"               -> allerEn(Direction.SUD);
      case "E", "EST"               -> allerEn(Direction.EST);
      case "O", "OUEST"             -> allerEn(Direction.OUEST);
      case "R", "RETOUR"            -> retour();
      case "I", "INVENTAIRE"        -> afficherInventaire();
      case "ET", "ETAT", "ENERGIE"  -> afficherEtat();
      case "OBS", "OBSERVER"        -> observer();
      case "P", "PRENDRE"           -> prendreObjet(argument);
      case "DEP", "DEPOSER"         -> deposerObjet(argument);
      case "L", "LIRE"              -> lire();
      case "Q", "QUITTER"           -> terminer();
      default -> gui.afficher("Commande inconnue : \"" + verbe + "\". Tapez ? pour l'aide.");
    }
  }

  /** Affiche l'aide listant toutes les commandes disponibles. */
  private void afficherAide() {
    verifieGUI();
    gui.afficher("Êtes-vous perdu ?");
    gui.afficher();
    gui.afficher("Les commandes disponibles sont :");
    gui.afficher();
    for (String desc : Commande.toutesLesDescriptions()) {
      gui.afficher("  " + desc);
    }
    gui.afficher();
  }

  /**
   * Déplace le joueur vers une nouvelle zone dans la direction donnée.
   * <p>
   * Vérifie l'existence de la sortie puis l'état verrouillé de la destination.
   * La zone courante est empilée dans l'historique sauf si marquée {@code sansRetour}.
   *
   * @param direction la direction du déplacement
   */
  private void allerEn(Direction direction) {
    verifieGUI();
    Zone nouvelle = zoneCourante.obtientSortie(direction);
    if (nouvelle == null) {
      gui.afficher("Pas de sortie " + direction + ".");
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
      gui.afficher("Aucun retour possible — vous êtes déjà à votre point de départ.");
      gui.afficher();
    } else {
      zoneCourante = joueur.getHistorique().pop();
      gui.afficher(zoneCourante.descriptionLongue());
      gui.afficher();
      gui.afficheImage(zoneCourante.nomImage());
    }
  }

  /** Affiche l'inventaire complet et l'énergie du joueur. */
  private void afficherInventaire() {
    verifieGUI();
    gui.afficher(joueur.afficherInventaire());
    gui.afficher();
  }

  /** Affiche uniquement le niveau d'énergie du joueur. */
  private void afficherEtat() {
    verifieGUI();
    int e = joueur.getEnergie();
    String barre = barreEnergie(e);
    gui.afficher("Énergie : " + e + "/100  " + barre);
    gui.afficher();
  }

  /**
   * Affiche une description détaillée de la zone courante (sorties, objets, état).
   * Équivaut à la description affichée lors d'un déplacement.
   */
  private void observer() {
    verifieGUI();
    gui.afficher(zoneCourante.descriptionLongue());
    gui.afficher();
  }

  /**
   * Tente de prendre l'objet nommé dans la zone courante et de le mettre dans le sac.
   *
   * @param nomObjet le nom de l'objet à prendre
   */
  private void prendreObjet(String nomObjet) {
    verifieGUI();
    if (nomObjet.isEmpty()) {
      gui.afficher("Prendre quoi ? Précisez : P <nom de l'objet>");
      gui.afficher();
      return;
    }
    if (zoneCourante.isDansLeNoir() && !joueur.possedeItem("Lampe Torche")) {
      gui.afficher("Il fait trop noir pour chercher des objets. Il vous faudrait une lampe torche.");
      gui.afficher();
      return;
    }
    Item item = zoneCourante.retirerObjet(nomObjet);
    if (item == null) {
      gui.afficher("Il n'y a pas d'objet nommé \"" + nomObjet + "\" ici.");
      gui.afficher();
      return;
    }
    if (!joueur.ajouterItem(item)) {
      zoneCourante.ajouterObjet(item);
      gui.afficher("Votre sac est trop plein pour prendre \"" + item.getNom() + "\".");
      gui.afficher("Il occupe " + item.getSlots() + " slot(s), "
          + "mais il ne vous reste que " + joueur.slotsDisponibles() + " slot(s).");
      gui.afficher();
      return;
    }
    gui.afficher("Vous avez pris : " + item);
    gui.afficher();
  }

  /**
   * Tente de déposer l'objet nommé du sac dans la zone courante.
   *
   * @param nomObjet le nom de l'objet à déposer
   */
  private void deposerObjet(String nomObjet) {
    verifieGUI();
    if (nomObjet.isEmpty()) {
      gui.afficher("Déposer quoi ? Précisez : DEP <nom de l'objet>");
      gui.afficher();
      return;
    }
    Item item = joueur.retirerItem(nomObjet);
    if (item == null) {
      gui.afficher("Vous n'avez pas d'objet nommé \"" + nomObjet + "\" dans votre sac.");
      gui.afficher();
      return;
    }
    zoneCourante.ajouterObjet(item);
    gui.afficher("Vous avez déposé : " + item);
    gui.afficher();
  }

  /**
   * Lit le texte présent dans la zone courante (tableau, panneau, écran).
   * Affiche un message si la zone ne contient rien à lire.
   */
  private void lire() {
    verifieGUI();
    if (zoneCourante.isDansLeNoir() && !joueur.possedeItem("Lampe Torche")) {
      gui.afficher("Il fait trop noir pour lire quoi que ce soit.");
      gui.afficher();
      return;
    }
    String texte = zoneCourante.getTexte();
    if (texte == null || texte.isEmpty()) {
      gui.afficher("Il n'y a rien à lire ici.");
      gui.afficher();
    } else {
      gui.afficher("── Lecture ────────────────────────────────");
      gui.afficher(texte);
      gui.afficher("───────────────────────────────────────────");
      gui.afficher();
    }
  }

  /** Termine le jeu, affiche un message d'au revoir et désactive l'interface. */
  private void terminer() {
    verifieGUI();
    gui.afficher("Au revoir...");
    gui.enable(false);
  }

  /**
   * Génère une barre visuelle d'énergie sur 10 caractères.
   *
   * @param energie valeur entre 0 et 100
   * @return barre de type "[████░░░░░░]"
   */
  private String barreEnergie(int energie) {
    int pleins = energie / 10;
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < 10; i++) {
      sb.append(i < pleins ? "█" : "░");
    }
    sb.append("]");
    return sb.toString();
  }
}
