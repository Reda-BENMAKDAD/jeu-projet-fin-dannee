package jeu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;

/**
 * Classe principale du jeu « Nuit à la Fac ».
 * <p>
 * Gère les dix zones du scénario, les déplacements, l'énergie du joueur,
 * les cinq énigmes et l'interaction avec l'interface graphique {@link GUI}.
 *
 * <h2>Flux d'une partie gagnante</h2>
 * <ol>
 *   <li>Résoudre l'énigme LP du tableau → déverrouille la sortie vers le Couloir</li>
 *   <li>Naviguer le labyrinthe BFS (3 étapes, max 3 erreurs) → déverrouille les Escaliers</li>
 *   <li>Récupérer des objets utiles (Salle secondaire, RU, Distributeur)</li>
 *   <li>Choisir le bon plat au RU → +énergie</li>
 *   <li>Résoudre l'énigme Java au Distributeur → obtient code MEGA + cadeau</li>
 *   <li>Parler à l'agent MEGA (en forme : Carte prof + code ; épuisé : Café)</li>
 *   <li>Entrer dans le Bâtiment MEGA, prendre la Clé de sortie (2 slots)</li>
 *   <li>Ouvrir la Sortie de l'université → victoire</li>
 * </ol>
 */
public class Jeu implements Serializable {

  private static final long serialVersionUID = 1L;

  // ══════════════════════════════════════════════════════════════════════════
  // DONNÉES STATIQUES — énigmes
  // ══════════════════════════════════════════════════════════════════════════

  /** Indices BFS : [direction_index][clue_index]. NORD=0, SUD=1, EST=2, OUEST=3. */
  private static final String[][] BFS_CLUES = {
    { // NORD
      "Sur le mur, une flèche griffonnée pointe vers le haut.",
      "La lumière de la sortie de secours brille droit devant.",
      "Vous sentez un courant d'air frais venant de face."
    },
    { // SUD
      "Un écho de la porte d'entrée résonne derrière vous.",
      "La lumière rouge ENTRÉE est visible en arrière dans le couloir.",
      "Un marquage au sol : '← Entrée principale 50 m'."
    },
    { // EST
      "Un post-it jaune : '→ Salle informatique' collé sur le mur.",
      "Le bourdonnement des serveurs vient de votre droite.",
      "La lumière de la fenêtre arrive de l'est, côté droit."
    },
    { // OUEST
      "Une plaque usée indique '← Réserve pédagogique' à gauche.",
      "Des traces de chariot récentes filent vers la gauche.",
      "L'odeur de la reprographie vient de l'ouest."
    }
  };

  /**
   * Règles de sécurité du RU. Index correspond à {@code PLATS_RU} et {@code BON_CHOIX_RU}.
   */
  private static final String[] REGLES_RU = {
    "Alerte allergènes : évitez tout produit laitier ce soir.",
    "Hygiène : écartez les plats préparés il y a plus de 3 heures.",
    "Diète médicale : choisissez le plat le moins calorique.",
    "Nutrition sportive : optez pour le plat sans glucides complexes.",
    "Contrôle qualité : sélectionnez le plat le plus riche en protéines.",
    "Fraîcheur absolue : le plat doit avoir été préparé après 19 h ce soir."
  };

  /** Plats proposés par scénario (3 plats par scénario). */
  private static final String[][] PLATS_RU = {
    { // Scénario 0 — sans lait, correct = 1
      "1. Riz cantonnais       — riz, légumes, sauce soja    (sans lait,  320 kcal)",
      "2. Gratin dauphinois    — pommes de terre, crème, fromage (LAIT, 480 kcal)",
      "3. Yaourt grec          — lait concentré, cultures    (LAIT,  100 kcal)"
    },
    { // Scénario 1 — < 3 h, correct = 2
      "1. Quiche Lorraine      — oeufs, lardons, crème       (4 h,  450 kcal)",
      "2. Salade César         — poulet, salade, parmesan    (1 h,  280 kcal)",
      "3. Pizza Margherita     — tomate, mozzarella          (5 h,  580 kcal)"
    },
    { // Scénario 2 — min calories, correct = 3
      "1. Burger maison        — steak, pain, cheddar, sauce (620 kcal)",
      "2. Sandwich jambon      — pain, jambon, beurre        (350 kcal)",
      "3. Soupe de légumes     — carottes, poireaux, céleri  (150 kcal)"
    },
    { // Scénario 3 — sans glucides, correct = 1
      "1. Omelette nature      — oeufs, sel, fines herbes   (sans glucides, 180 kcal)",
      "2. Pâtes Bolognaise     — pâtes, boeuf, tomate       (glucides élevés, 520 kcal)",
      "3. Pain perdu sucré     — pain, oeufs, sucre, beurre (glucides élevés, 380 kcal)"
    },
    { // Scénario 4 — max protéines, correct = 2
      "1. Soupe de légumes     — carottes, poireaux         (8 g protéines, 150 kcal)",
      "2. Blanc de poulet      — poulet grillé, citron      (32 g protéines, 200 kcal)",
      "3. Tarte aux pommes     — pommes, pâte brisée        (4 g protéines, 340 kcal)"
    },
    { // Scénario 5 — après 19 h, correct = 3
      "1. Ratatouille          — légumes du Midi            (prête à 14 h 00)",
      "2. Quiche provençale    — oeufs, légumes, fromage    (prête à 16 h 30)",
      "3. Omelette aux herbes  — oeufs, fines herbes        (prête à 19 h 30)"
    }
  };

  /** Index 1-based du bon plat pour chaque scénario RU. */
  private static final int[] BON_CHOIX_RU = {1, 2, 3, 1, 2, 3};

  /**
   * Banque d'énigmes Java : [0]=code, [1]=question, [2]=réponse attendue.
   * Les réponses sont comparées en minuscules sans espaces.
   */
  private static final String[][] ENIGMES_JAVA = {
    {
      "int s = 0;\nfor (int i = 1; i <= 4; i++) s += i * i;",
      "Quelle est la valeur de s ?",
      "30"
    },
    {
      "int a = 17, b = 5;\nint r = (a / b) * 10 + (a % b);",
      "Quelle est la valeur de r ?",
      "32"
    },
    {
      "String mot = \"MIAGE\";\nint n = 0;\nfor (char c : mot.toCharArray())\n    if (\"AEIOUY\".indexOf(c) >= 0) n++;",
      "Quelle est la valeur de n (voyelles dans \"MIAGE\") ?",
      "3"
    },
    {
      "int x = 0b1010; // 10 en décimal\nint y = 0b0110; //  6 en décimal\nint z = x ^ y;  // XOR",
      "Quelle est la valeur décimale de z ?",
      "12"
    },
    {
      "int x = 1, y = 2, z = 3;\nif (x < y && y < z)\n    x = y + z;\nelse\n    x = y - z;",
      "Quelle est la valeur de x après exécution ?",
      "5"
    }
  };

  // ══════════════════════════════════════════════════════════════════════════
  // CHAMPS D'INSTANCE
  // ══════════════════════════════════════════════════════════════════════════

  /** Générateur de nombres aléatoires pour la variabilité des énigmes. */
  private transient Random aleatoire;

  /** Interface graphique associée au jeu. */
  private transient GUI gui;

  /** Zone actuelle dans laquelle se trouve le joueur. */
  private Zone zoneCourante;

  /** Le joueur courant (énergie, inventaire, historique). */
  private Joueur joueur;

  // ── Zones ────────────────────────────────────────────────────────────────

  /** Zone de départ. */
  private Zone salleDeCours;
  /** Couloir principal. */
  private Zone couloir;
  /** Salle secondaire optionnelle. */
  private Zone salleSecondaire;
  /** Zone sans retour déclenchée par 3 erreurs BFS. */
  private Zone salleBloquante;
  /** Accès conditionné par la réussite du BFS. */
  private Zone escaliers;
  /** Hub central. */
  private Zone hall;
  /** Énigme plat. */
  private Zone restaurantU;
  /** Énigme Java + code MEGA. */
  private Zone distributeur;
  /** Gardé par l'agent de sécurité. */
  private Zone batimentMega;
  /** Zone de victoire. */
  private Zone sortieUniversite;

  // ── État général ─────────────────────────────────────────────────────────

  /** {@code true} une fois que la partie est terminée (victoire ou défaite). */
  private boolean partieTerminee = false;

  /** {@code true} si le Stylo a déjà absorbé une erreur. */
  private boolean styloEffetUtilise = false;

  // ── Énigme 1 — Tableau (Programmation Linéaire) ──────────────────────────

  /** Code correct (valeur optimale du PL). */
  private int codeTableau;
  /** {@code true} une fois le code LP validé. */
  private boolean enigmeTableauResolue = false;

  // ── Énigme 2 — Labyrinthe BFS ────────────────────────────────────────────

  /** Séquence de 3 directions correctes générée aléatoirement. */
  private Direction[] bfsSequence;
  /** Clue affiché à chaque étape du BFS. */
  private String[] bfsClues;
  /** Étape courante (0, 1, 2). */
  private int bfsEtape = 0;
  /** Nombre total d'erreurs BFS commises. */
  private int bfsErreurs = 0;
  /** {@code true} lorsque le BFS est en cours (en attente d'un CHOISIR). */
  private boolean bfsActif = false;

  // ── Énigme 3 — Restaurant Universitaire ──────────────────────────────────

  /** Index du scénario RU choisi aléatoirement (0-5). */
  private int scenarioRu;
  /** {@code true} une fois le bon plat choisi. */
  private boolean enigmeRuResolue = false;

  // ── Énigme 4 — Distributeur Java ─────────────────────────────────────────

  /** Index de l'énigme Java tirée au sort (0-4). */
  private int indexEnigmeJava;
  /** {@code true} une fois l'énigme Java résolue. */
  private boolean enigmeDistributeurResolue = false;
  /** Code d'accès MEGA révélé après résolution. */
  private String codeMega;
  /** {@code true} une fois le codeMega connu par le joueur. */
  private boolean codeMegaConnu = false;

  // ── Énigme 5 — Agent MEGA ────────────────────────────────────────────────

  /** Humeur de l'agent, tirée au sort. */
  private HumeurAgent humeurAgent;
  /** {@code true} une fois le bâtiment MEGA déverrouillé par l'agent. */
  private boolean megaDebloque = false;
  /** {@code true} quand l'agent épuisé attend un Café. */
  private boolean agentAttendCafe = false;

  /** Humeur possible de l'agent de sécurité du bâtiment MEGA. */
  private enum HumeurAgent {
    /** Exige Carte de professeur et code distributeur. */
    FORME,
    /** Accepte un Café à la place de la carte. */
    EPUISE
  }

  // ══════════════════════════════════════════════════════════════════════════
  // CONSTRUCTION
  // ══════════════════════════════════════════════════════════════════════════

  /**
   * Construit un nouveau jeu.
   * Crée la carte, génère toutes les énigmes aléatoires, puis attend {@link #setGUI}.
   *
   * @param nomJoueur le nom du joueur connecté
   */
  public Jeu(String nomJoueur) {
    this.aleatoire = new Random();
    this.joueur = new Joueur(nomJoueur);
    creerCarte();
    initialiserEnigmes();
    gui = null;
  }

  /**
   * Réinitialise les champs transitoires après désérialisation.
   * Appelée automatiquement par {@link ObjectInputStream} lors du chargement d'une sauvegarde.
   *
   * @param in le flux de désérialisation
   * @throws IOException en cas d'erreur d'entrée/sortie
   * @throws ClassNotFoundException si une classe est introuvable
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    aleatoire = new Random();
  }

  /**
   * Crée les dix zones du scénario, leurs connexions, leurs attributs et leurs objets fixes.
   * <p>
   * Sorties ajoutées dynamiquement (non présentes ici) :
   * <ul>
   *   <li>SalleDeCours → Couloir : ajoutée après résolution de l'énigme LP</li>
   *   <li>Couloir → Escaliers : ajoutée après réussite du BFS</li>
   *   <li>Couloir → SalleBloquante : déclenchée après 3 erreurs BFS</li>
   * </ul>
   */
  private void creerCarte() {
    salleDeCours   = new Zone("la salle de cours",            "SalleDeCours.jpg");
    couloir        = new Zone("le couloir principal",          "CouloirPrincipal.jpg");
    salleSecondaire = new Zone("la salle secondaire",         "SalleSecondaire.jpg");
    salleBloquante = new Zone("la salle bloquante",            "SalleBloquante.jpg");
    escaliers      = new Zone("les escaliers",                 "Escaliers.jpg");
    hall           = new Zone("le hall principal",             "HallPrincipal.jpg");
    restaurantU    = new Zone("le restaurant universitaire",   "RestaurantU.jpg");
    distributeur   = new Zone("le distributeur automatique",   "Distributeur.jpg");
    batimentMega   = new Zone("le bâtiment MEGA",              "BatimentMEGA.jpg");
    sortieUniversite = new Zone("la sortie de l'université",   "SortieUniversite.jpg");

    salleBloquante.setSansRetour(true);
    batimentMega.setVerrouillee(true);
    sortieUniversite.setVerrouillee(true);

    // ── Sorties statiques ────────────────────────────────────────────────────
    // SalleDeCours → Couloir : ajoutée après l'énigme du tableau
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
    salleDeCours.ajouterObjet(new Item("Feuille de brouillon", 1,
        "Notes griffonnées : nom du prof, matière, code MEGA. Lisez-la via UTILISER."));
    batimentMega.ajouterObjet(new Item("Clé de sortie", 2,
        "Une lourde clé qui ouvre le portail final. Occupe 2 emplacements."));

    // Items mobiles placés aléatoirement dans placerObjetsMobiles()

    // ── Textes lisibles (définis dynamiquement pour LP et RU/Distributeur) ───
    couloir.setTexte(
        "Des panneaux d'itinéraire couvrent les murs.\n"
        + "Un écriteau posé par terre : « ATTENTION — Labyrinthe en cours ».\n"
        + "Pour rejoindre les escaliers, tapez N pour démarrer le labyrinthe.");
    salleSecondaire.setTexte(
        "Un tableau d'affichage présente les plannings des professeurs.\n"
        + "Vous notez plusieurs noms, matières et numéros de bureau.");
    salleBloquante.setTexte(
        "Un panneau d'alarme rouge clignote : 'ZONE RESTREINTE — NE PAS ENTRER'.\n"
        + "La porte s'est verrouillée. Chaque action vous coûte 10 points d'énergie !");
    escaliers.setTexte(
        "Panneau : '↓ RDC — Hall principal  /  ↑ 1er — Salles de cours'.");
    hall.setTexte(
        "Écran d'accueil : 'Université — Fermeture exceptionnelle. Bonne nuit !'\n"
        + "Affiche collée : « Bâtiment MEGA — code distributeur requis pour l'accès ».");
    batimentMega.setTexte(
        "Pancarte dorée : 'BÂTIMENT MEGA — Accès restreint.\n"
        + "Présentez badge et code d'accès au gardien.'");
    sortieUniversite.setTexte(
        "Panneau lumineux : 'SORTIE — Bonne journée ! Fermez bien en partant.'");

    zoneCourante = salleDeCours;
  }

  /**
   * Génère toutes les énigmes aléatoirement une fois la carte créée.
   * Appelée une seule fois à la construction.
   */
  private void initialiserEnigmes() {
    genererEnigmeLP();
    genererEnigmeBFS();
    genererEnigmeRU();
    genererEnigmeDistributeur();
    humeurAgent = aleatoire.nextBoolean() ? HumeurAgent.FORME : HumeurAgent.EPUISE;
    codeMega = String.format("%04d", aleatoire.nextInt(9000) + 1000);
    placerObjetsMobiles();
    appliquerCoupures();
  }

  // ══════════════════════════════════════════════════════════════════════════
  // GÉNÉRATION DES ÉNIGMES
  // ══════════════════════════════════════════════════════════════════════════

  /**
   * Génère l'énigme de programmation linéaire.
   * LP : Maximiser Z = a·x + b·y  s.c.  x+y ≤ n, 2x+y ≤ m, x,y ≥ 0.
   * Les paramètres garantissent un optimum entier 4 chiffres (1 000–9 999).
   */
  private void genererEnigmeLP() {
    int[] nVals = {8, 10, 12};
    int[] mOff  = {2,  4,  6};
    int[] coefs = {200, 250, 300, 350, 400};

    int n = nVals[aleatoire.nextInt(nVals.length)];
    int m = n + mOff[aleatoire.nextInt(mOff.length)];
    int a = coefs[aleatoire.nextInt(coefs.length)];
    int b = coefs[aleatoire.nextInt(coefs.length)];

    // Sommets du polytope faisable
    int z1 = 0;                              // (0, 0)
    int z2 = a * (m / 2);                   // (m/2, 0)
    int z3 = b * n;                          // (0, n)
    int z4 = a * (m - n) + b * (2 * n - m); // intersection des deux contraintes
    codeTableau = Math.max(Math.max(z1, z2), Math.max(z3, z4));

    salleDeCours.setTexte(String.format(
        "╔══════════════════════════════════════════╗\n"
        + "║   ÉNIGME DU TABLEAU — Programmation LP  ║\n"
        + "╚══════════════════════════════════════════╝\n\n"
        + "Un étudiant prépare des groupes de TD.\n"
        + "  x = groupes en Maths   (bénéfice %d pts chacun)\n"
        + "  y = groupes en Info    (bénéfice %d pts chacun)\n\n"
        + "Maximiser  Z = %d·x + %d·y\n\n"
        + "Contraintes :\n"
        + "   x + y  ≤ %d   (créneaux disponibles)\n"
        + "  2x + y  ≤ %d   (ressources pédagogiques)\n"
        + "   x, y  ≥ 0  (entiers)\n\n"
        + "Le code du digicode = valeur optimale de Z.\n"
        + "Tapez : CHOISIR <valeur>",
        a, b, a, b, n, m));
  }

  /**
   * Génère la séquence secrète de 3 directions du labyrinthe BFS.
   * Deux directions consécutives ne sont jamais identiques.
   */
  private void genererEnigmeBFS() {
    Direction[] dirs = Direction.values();
    bfsSequence = new Direction[3];
    bfsClues    = new String[3];
    Direction prev = null;
    for (int i = 0; i < 3; i++) {
      Direction d;
      do {
        d = dirs[aleatoire.nextInt(dirs.length)];
      } while (d == prev);
      bfsSequence[i] = d;
      int idx = directionIndex(d);
      String[] cluesForDir = BFS_CLUES[idx];
      bfsClues[i] = cluesForDir[aleatoire.nextInt(cluesForDir.length)];
      prev = d;
    }
  }

  /** Choisit aléatoirement le scénario du restaurant et met à jour le texte de la zone. */
  private void genererEnigmeRU() {
    scenarioRu = aleatoire.nextInt(REGLES_RU.length);
    mettreAJourTexteRU();
  }

  /**
   * Répartit aléatoirement les quatre items mobiles dans quatre zones distinctes.
   * Chaque zone reçoit exactement un item (bijection via Fisher-Yates).
   * Items : Stylo, Lampe Torche, Boisson Énergisante, Carte de professeur.
   * Zones cibles : SalleDeCours, SalleSecondaire, Couloir, RestaurantU.
   */
  private void placerObjetsMobiles() {
    Item[] items = {
      new Item("Stylo", 1,
          "Absorbe automatiquement la perte d'énergie de votre première erreur d'énigme."),
      new Item("Lampe Torche", 1,
          "Éclaire les zones sombres. Sans elle, chaque action en zone noire coûte 5 énergie."),
      new Item("Boisson Énergisante", 1,
          "+25 énergie. Utilisez-la via : U Boisson Énergisante."),
      new Item("Carte de professeur", 1,
          "Badge officiel. Indispensable si l'agent MEGA est en forme.")
    };
    Zone[] zones = {salleDeCours, salleSecondaire, couloir, restaurantU};

    // Mélange Fisher-Yates sur les zones cibles
    for (int i = zones.length - 1; i > 0; i--) {
      int j = aleatoire.nextInt(i + 1);
      Zone tmp = zones[i];
      zones[i] = zones[j];
      zones[j] = tmp;
    }
    for (int i = 0; i < items.length; i++) {
      zones[i].ajouterObjet(items[i]);
    }
  }

  /**
   * Plonge 1 ou 2 zones dans le noir (coupure de courant aléatoire).
   * Zones candidates : Couloir, SalleSecondaire, RestaurantU.
   * Dans une zone sombre sans Lampe Torche, chaque action coûte 5 énergie.
   */
  private void appliquerCoupures() {
    Zone[] candidats = {couloir, salleSecondaire, restaurantU};
    int nb = aleatoire.nextInt(2) + 1; // 1 ou 2 coupures

    // Mélange Fisher-Yates sur les candidats
    for (int i = candidats.length - 1; i > 0; i--) {
      int j = aleatoire.nextInt(i + 1);
      Zone tmp = candidats[i];
      candidats[i] = candidats[j];
      candidats[j] = tmp;
    }
    for (int i = 0; i < nb; i++) {
      candidats[i].setDansLeNoir(true);
    }
  }

  /** Tire au sort l'énigme Java et met à jour le texte du distributeur. */
  private void genererEnigmeDistributeur() {
    indexEnigmeJava = aleatoire.nextInt(ENIGMES_JAVA.length);
    mettreAJourTexteDistributeur();
  }

  // ══════════════════════════════════════════════════════════════════════════
  // GUI
  // ══════════════════════════════════════════════════════════════════════════

  /**
   * Associe une interface graphique et affiche le message de bienvenue.
   *
   * @param g l'instance de {@link GUI} à associer
   */
  public void setGUI(GUI g) {
    gui = g;
    afficherMessageDeBienvenue();
  }

  // ══════════════════════════════════════════════════════════════════════════
  // SAUVEGARDE / CHARGEMENT
  // ══════════════════════════════════════════════════════════════════════════

  /** Répertoire de stockage des sauvegardes. */
  private static final String DOSSIER_SAVES = "saves";

  /**
   * Retourne le chemin du fichier de sauvegarde pour un joueur donné.
   *
   * @param nomJoueur le nom du joueur
   * @return chemin relatif du fichier {@code .sav}
   */
  private static String cheminSauvegarde(String nomJoueur) {
    return DOSSIER_SAVES + File.separator + nomJoueur.toLowerCase() + ".sav";
  }

  /**
   * Indique si une sauvegarde existe pour le joueur donné.
   *
   * @param nomJoueur le nom du joueur
   * @return {@code true} si le fichier de sauvegarde est présent
   */
  public static boolean sauvegardeExiste(String nomJoueur) {
    return new File(cheminSauvegarde(nomJoueur)).exists();
  }

  /**
   * Charge et retourne la sauvegarde du joueur donné.
   *
   * @param nomJoueur le nom du joueur
   * @return l'instance {@link Jeu} désérialisée
   * @throws IOException en cas d'erreur de lecture
   * @throws ClassNotFoundException si la classe {@link Jeu} est introuvable
   */
  public static Jeu chargerSauvegarde(String nomJoueur) throws IOException, ClassNotFoundException {
    try (ObjectInputStream ois =
        new ObjectInputStream(new FileInputStream(cheminSauvegarde(nomJoueur)))) {
      return (Jeu) ois.readObject();
    }
  }

  /**
   * Sauvegarde l'état courant du jeu dans {@code saves/<nomJoueur>.sav}.
   * Crée le répertoire {@code saves/} si nécessaire.
   */
  private void sauvegarder() {
    new File(DOSSIER_SAVES).mkdirs();
    try (ObjectOutputStream oos =
        new ObjectOutputStream(new FileOutputStream(cheminSauvegarde(joueur.getNom())))) {
      oos.writeObject(this);
      gui.afficher("Partie sauvegardée.");
    } catch (IOException e) {
      gui.afficher("Erreur lors de la sauvegarde : " + e.getMessage());
    }
    gui.afficher();
  }

  /**
   * Attache une interface graphique à une partie chargée et affiche l'état courant.
   * À appeler à la place de {@link #setGUI} lors d'une reprise de sauvegarde.
   *
   * @param g l'instance de {@link GUI} à associer
   */
  public void restaurerGUI(GUI g) {
    gui = g;
    gui.afficher("=== Partie reprise — " + joueur.getNom() + " ===");
    gui.afficher("Énergie : " + joueur.getEnergie() + "/100  " + barreEnergie(joueur.getEnergie()));
    gui.afficher();
    afficherLocalisation();
  }

  /** Vérifie que la GUI est initialisée. */
  private void verifieGUI() {
    if (gui == null) {
      throw new IllegalStateException("GUI non initialisée !");
    }
  }

  /** Affiche description longue + image de la zone courante. */
  private void afficherLocalisation() {
    gui.afficher(zoneCourante.descriptionLongue());
    gui.afficher();
    gui.afficheImage(zoneCourante.nomImage());
  }

  /** Affiche le message de bienvenue et l'état initial. */
  private void afficherMessageDeBienvenue() {
    verifieGUI();
    gui.afficher("Bienvenue " + joueur.getNom() + " !");
    gui.afficher();
    gui.afficher("Vous vous réveillez dans l'université déserte. Il faut s'échapper avant l'aube !");
    gui.afficher("Tapez ? pour l'aide.");
    gui.afficher();
    afficherLocalisation();
  }

  // ══════════════════════════════════════════════════════════════════════════
  // DISPATCH DES COMMANDES
  // ══════════════════════════════════════════════════════════════════════════

  /**
   * Traite une commande saisie par l'utilisateur.
   * <p>
   * La commande est découpée en verbe + argument optionnel (pour les commandes
   * paramétrées). Après traitement, applique le drain d'énergie passif :
   * salle bloquante (-10/action) ou zone sombre sans lampe (-5/action).
   *
   * @param commande la saisie brute du joueur
   */
  public void traiterCommande(String commande) {
    verifieGUI();
    if (partieTerminee) {
      return;
    }
    gui.afficher("> " + commande + "\n");

    String[] parts = commande.trim().split("\\s+", 2);
    String verbe   = parts[0].toUpperCase();
    String arg     = parts.length > 1 ? parts[1].trim() : "";

    switch (verbe) {
      case "?",    "AIDE"                   -> afficherAide();
      case "N",    "NORD"                   -> allerEn(Direction.NORD);
      case "S",    "SUD"                    -> allerEn(Direction.SUD);
      case "E",    "EST"                    -> allerEn(Direction.EST);
      case "O",    "OUEST"                  -> allerEn(Direction.OUEST);
      case "R",    "RETOUR"                 -> retour();
      case "I",    "INVENTAIRE"             -> afficherInventaire();
      case "ET",   "ETAT",    "ENERGIE"     -> afficherEtat();
      case "OBS",  "OBSERVER"               -> observer();
      case "P",    "PRENDRE"                -> prendreObjet(arg);
      case "DEP",  "DEPOSER"                -> deposerObjet(arg);
      case "U",    "UTILISER"               -> utiliserObjet(arg);
      case "L",    "LIRE"                   -> lire();
      case "CH",   "CHOISIR"                -> traiterChoix(arg);
      case "PA",   "PARLER"                 -> parler();
      case "SAV",  "SAUVEGARDER"            -> sauvegarder();
      case "Q",    "QUITTER"                -> terminer();
      default -> gui.afficher(
          "Commande inconnue : \"" + verbe + "\". Tapez ? pour l'aide.\n");
    }

    // Drain d'énergie passif après chaque commande
    if (!partieTerminee) {
      if (zoneCourante == salleBloquante) {
        // Salle bloquante : -10 par action
        joueur.perdreEnergie(10);
        gui.afficher("[Salle bloquante — Énergie : " + joueur.getEnergie() + "/100 "
            + barreEnergie(joueur.getEnergie()) + "]");
        gui.afficher();
        if (joueur.estMort()) {
          gameOver("Vos forces vous abandonnent dans cette salle sans issue...\nGame over.");
        }
      } else if (zoneCourante.isDansLeNoir() && !joueur.possedeItem("Lampe Torche")) {
        // Zone sombre sans lampe : -5 par action
        joueur.perdreEnergie(5);
        gui.afficher("[Zone sombre — Énergie : " + joueur.getEnergie() + "/100 "
            + barreEnergie(joueur.getEnergie()) + "]");
        gui.afficher();
        if (joueur.estMort()) {
          gameOver("Vous vous perdez dans le noir complet... Game over.");
        }
      }
    }
  }

  // ══════════════════════════════════════════════════════════════════════════
  // DÉPLACEMENTS
  // ══════════════════════════════════════════════════════════════════════════

  /**
   * Déplace le joueur dans la direction indiquée.
   * Gère : sortie nulle, départ conditionnel (SalleDeCours→Couloir),
   * déclenchement BFS (Couloir→NORD), vérification de la clé (Bâtiment MEGA→NORD),
   * zones verrouillées, coût en énergie et pile d'historique.
   *
   * @param direction la direction souhaitée
   */
  private void allerEn(Direction direction) {
    verifieGUI();

    // ── Cas spécial : sortir de SalleDeCours sans avoir résolu l'énigme ───────
    if (zoneCourante == salleDeCours && direction == Direction.NORD
        && !enigmeTableauResolue) {
      gui.afficher("La porte est verrouillée par un digicode.\n"
          + "Lisez le tableau (LIRE), résolvez l'énigme et entrez le code (CHOISIR <valeur>).");
      gui.afficher();
      return;
    }

    // ── Cas spécial : aller vers NORD dans le couloir → déclenche le BFS ─────
    if (zoneCourante == couloir && direction == Direction.NORD) {
      if (!bfsActif) {
        demarrerBFS();
      } else {
        gui.afficher("Labyrinthe en cours ! Utilisez CHOISIR <N/S/E/O> pour avancer.");
        gui.afficher();
      }
      return;
    }

    Zone nouvelle = zoneCourante.obtientSortie(direction);

    if (nouvelle == null) {
      gui.afficher("Pas de sortie " + direction + ".");
      gui.afficher();
      return;
    }

    // ── Sortie verrouillée : vérifier si la clé déverrouille la sortie ───────
    if (nouvelle.isVerrouillee()) {
      if (nouvelle == sortieUniversite && joueur.possedeItem("Clé de sortie")) {
        nouvelle.setVerrouillee(false); // la clé est utilisée
        gui.afficher("Vous insérez la clé dans le portail. Il s'ouvre lentement...");
        gui.afficher();
        // on laisse le déplacement s'effectuer ci-dessous
      } else {
        gui.afficher(messagePourZoneVerrouillee(nouvelle));
        gui.afficher();
        return;
      }
    }

    // ── Déplacement effectif ──────────────────────────────────────────────────
    if (!zoneCourante.isSansRetour()) {
      joueur.getHistorique().push(zoneCourante);
    }
    zoneCourante = nouvelle;

    // Coût en énergie par déplacement
    joueur.perdreEnergie(5);
    if (joueur.estMort()) {
      afficherLocalisation();
      gameOver("Épuisé(e), vous vous effondrez... Game over.");
      return;
    }

    afficherLocalisation();

    // Avertissement à l'entrée d'une zone sombre
    if (zoneCourante.isDansLeNoir() && !joueur.possedeItem("Lampe Torche")) {
      gui.afficher("[COUPURE DE COURANT ! Il fait nuit noire ici.]");
      gui.afficher("[Chaque action vous coûtera 5 énergie supplémentaires.]");
      gui.afficher("[Trouvez une Lampe Torche pour en être protégé(e).]");
      gui.afficher();
    }

    if (zoneCourante == sortieUniversite) {
      victoire();
    }
  }

  /**
   * Retourne à la zone précédente (dépile l'historique du joueur).
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
      gui.afficher("Aucun retour possible — vous êtes à votre point de départ.");
      gui.afficher();
    } else {
      joueur.perdreEnergie(5);
      zoneCourante = joueur.getHistorique().pop();
      afficherLocalisation();
      if (joueur.estMort()) {
        gameOver("Épuisé(e) en chemin... Game over.");
      }
    }
  }

  /**
   * Retourne le message d'accès refusé pour une zone verrouillée.
   *
   * @param zone la zone ciblée
   * @return message contextuel
   */
  private String messagePourZoneVerrouillee(Zone zone) {
    if (zone == batimentMega) {
      return "L'entrée du bâtiment MEGA est verrouillée.\n"
          + "L'agent de sécurité vous barre la route.\n"
          + "Tapez PA (PARLER) pour l'approcher.";
    }
    if (zone == sortieUniversite) {
      return "Le portail est verrouillé. Il vous faut la Clé de sortie.";
    }
    return "Cette zone est verrouillée.";
  }

  // ══════════════════════════════════════════════════════════════════════════
  // COMMANDES D'INFORMATION ET D'INTERACTION DE BASE
  // ══════════════════════════════════════════════════════════════════════════

  /** Affiche l'aide listant toutes les commandes. */
  private void afficherAide() {
    gui.afficher("Commandes disponibles :");
    gui.afficher();
    for (String desc : Commande.toutesLesDescriptions()) {
      gui.afficher("  " + desc);
    }
    gui.afficher();
  }

  /** Affiche l'inventaire complet et l'énergie. */
  private void afficherInventaire() {
    gui.afficher(joueur.afficherInventaire());
    gui.afficher();
  }

  /** Affiche l'énergie avec une barre visuelle. */
  private void afficherEtat() {
    int e = joueur.getEnergie();
    gui.afficher("Énergie : " + e + "/100  " + barreEnergie(e));
    gui.afficher();
  }

  /** Affiche la description longue de la zone courante. */
  private void observer() {
    gui.afficher(zoneCourante.descriptionLongue());
    gui.afficher();
  }

  /** Lit le texte présent dans la zone courante. Bloqué dans le noir sans lampe. */
  private void lire() {
    if (zoneCourante.isDansLeNoir() && !joueur.possedeItem("Lampe Torche")) {
      gui.afficher("Il fait trop noir pour lire. Il vous faudrait une Lampe Torche.");
      gui.afficher();
      return;
    }
    String texte = zoneCourante.getTexte();
    if (texte == null || texte.isEmpty()) {
      gui.afficher("Il n'y a rien à lire ici.");
    } else {
      gui.afficher("── Lecture ─────────────────────────────────");
      gui.afficher(texte);
      gui.afficher("────────────────────────────────────────────");
    }
    gui.afficher();
  }

  /**
   * Prend l'objet nommé dans la zone et le met dans le sac.
   * Bloqué dans le noir sans lampe. Vérifie les slots disponibles.
   *
   * @param nomObjet nom de l'objet
   */
  private void prendreObjet(String nomObjet) {
    if (nomObjet.isEmpty()) {
      gui.afficher("Prendre quoi ? Précisez : P <nom>");
      gui.afficher();
      return;
    }
    if (zoneCourante.isDansLeNoir() && !joueur.possedeItem("Lampe Torche")) {
      gui.afficher("Il fait trop noir pour chercher. Il vous faut une Lampe Torche.");
      gui.afficher();
      return;
    }
    Item item = zoneCourante.retirerObjet(nomObjet);
    if (item == null) {
      gui.afficher("Pas d'objet nommé \"" + nomObjet + "\" ici.");
      gui.afficher();
      return;
    }
    if (!joueur.ajouterItem(item)) {
      zoneCourante.ajouterObjet(item); // remettre en place
      gui.afficher("Sac trop plein pour \"" + item.getNom() + "\" ("
          + item.getSlots() + " slot(s) requis, "
          + joueur.slotsDisponibles() + " disponible(s)).\n"
          + "Déposez un objet avec DEP <objet>.");
      gui.afficher();
      return;
    }
    gui.afficher("Pris : " + item);
    gui.afficher();
  }

  /**
   * Dépose l'objet nommé du sac dans la zone courante.
   *
   * @param nomObjet nom de l'objet
   */
  private void deposerObjet(String nomObjet) {
    if (nomObjet.isEmpty()) {
      gui.afficher("Déposer quoi ? Précisez : DEP <nom>");
      gui.afficher();
      return;
    }
    Item item = joueur.retirerItem(nomObjet);
    if (item == null) {
      gui.afficher("\"" + nomObjet + "\" n'est pas dans votre sac.");
      gui.afficher();
      return;
    }
    zoneCourante.ajouterObjet(item);
    gui.afficher("Déposé : " + item);
    gui.afficher();
  }

  /**
   * Utilise un objet du sac de façon contextuelle.
   * Consommables : Boisson Énergisante (+25), Barre de céréales (+15), Café (agent).
   * Informatifs : Feuille de brouillon, Carte de professeur.
   *
   * @param nomObjet nom de l'objet
   */
  private void utiliserObjet(String nomObjet) {
    if (nomObjet.isEmpty()) {
      gui.afficher("Utiliser quoi ? Précisez : U <nom>");
      gui.afficher();
      return;
    }
    if (!joueur.possedeItem(nomObjet)) {
      gui.afficher("\"" + nomObjet + "\" n'est pas dans votre sac.");
      gui.afficher();
      return;
    }
    switch (nomObjet.toLowerCase().trim()) {
      case "boisson énergisante" -> {
        joueur.retirerItem(nomObjet);
        joueur.gagnerEnergie(25);
        gui.afficher("Vous buvez la Boisson Énergisante. +25 énergie.");
        gui.afficher("Énergie : " + joueur.getEnergie() + "/100  " + barreEnergie(joueur.getEnergie()));
      }
      case "barre de céréales" -> {
        joueur.retirerItem(nomObjet);
        joueur.gagnerEnergie(15);
        gui.afficher("Vous mangez la Barre de céréales. +15 énergie.");
        gui.afficher("Énergie : " + joueur.getEnergie() + "/100  " + barreEnergie(joueur.getEnergie()));
      }
      case "café" -> {
        if (agentAttendCafe) {
          joueur.retirerItem(nomObjet);
          gui.afficher("L'agent saisit le café avec un sourire de soulagement.\n"
              + "« Ah... merci ! Passez, passez... »");
          megaDebloque = true;
          batimentMega.setVerrouillee(false);
          agentAttendCafe = false;
          gui.afficher("[Bâtiment MEGA déverrouillé — tapez N depuis le Hall pour entrer.]");
        } else {
          gui.afficher("Vous n'avez personne à qui offrir ce café ici.\n"
              + "Gardez-le pour l'agent MEGA si celui-ci est épuisé (PARLER depuis le Hall).");
        }
      }
      case "feuille de brouillon" -> {
        gui.afficher("Vous relisez vos notes :\n"
            + "  « Pour accéder au bâtiment MEGA :\n"
            + "     – Obtenez le code au distributeur (résoudre énigme Java)\n"
            + "     – Ayez votre Carte de professeur (ou du Café si l'agent est fatigué)\n"
            + "     – Parlez à l'agent dans le Hall (PA) »");
      }
      case "carte de professeur" -> {
        gui.afficher("Badge officiel. L'agent en forme l'acceptera comme justificatif d'identité.");
      }
      case "lampe torche" -> {
        gui.afficher("Vous allumez la Lampe Torche.\n"
            + "Elle s'active automatiquement dans les zones sombres.");
      }
      case "stylo" -> {
        if (!styloEffetUtilise) {
          gui.afficher("Stylo prêt. Il absorbera automatiquement votre prochaine erreur d'énigme.");
        } else {
          gui.afficher("Votre Stylo a déjà servi. Il n'a plus d'effet.");
        }
      }
      default -> gui.afficher("Vous ne savez pas comment utiliser \"" + nomObjet + "\" ici.");
    }
    gui.afficher();
  }

  /** Termine la partie et désactive la saisie. */
  private void terminer() {
    gui.afficher("Au revoir...");
    gui.enable(false);
    partieTerminee = true;
  }

  // ══════════════════════════════════════════════════════════════════════════
  // DISPATCH CHOISIR
  // ══════════════════════════════════════════════════════════════════════════

  /**
   * Dispatch de la commande CHOISIR selon l'état de jeu courant.
   *
   * @param reponse l'argument saisi par le joueur
   */
  private void traiterChoix(String reponse) {
    if (reponse.isEmpty()) {
      gui.afficher("Choisir quoi ? Précisez : CH <réponse>");
      gui.afficher();
      return;
    }
    if (zoneCourante == salleDeCours && !enigmeTableauResolue) {
      validerCodeTableau(reponse);
    } else if (bfsActif) {
      validerBFS(reponse);
    } else if (zoneCourante == restaurantU && !enigmeRuResolue) {
      validerChoixRU(reponse);
    } else if (zoneCourante == distributeur && !enigmeDistributeurResolue) {
      validerDistributeur(reponse);
    } else {
      gui.afficher("Il n'y a pas d'énigme active ici.\n"
          + "Tapez LIRE pour trouver des indices.");
      gui.afficher();
    }
  }

  // ══════════════════════════════════════════════════════════════════════════
  // ÉNIGME 1 — TABLEAU (PROGRAMMATION LINÉAIRE)
  // ══════════════════════════════════════════════════════════════════════════

  /**
   * Valide la réponse du joueur pour l'énigme de programmation linéaire.
   * En cas de succès, déverrouille la sortie vers le Couloir.
   *
   * @param reponse la valeur saisie
   */
  private void validerCodeTableau(String reponse) {
    int valeur;
    try {
      valeur = Integer.parseInt(reponse.trim());
    } catch (NumberFormatException e) {
      gui.afficher("Entrez un nombre entier (ex. CHOISIR 1400).");
      gui.afficher();
      return;
    }

    if (valeur == codeTableau) {
      gui.afficher("✓ Code correct !");
      gui.afficher("Le digicode clignote en vert. La porte vers le couloir est déverrouillée.");
      enigmeTableauResolue = true;
      salleDeCours.ajouteSortie(Direction.NORD, couloir);
      salleDeCours.setTexte("Tableau résolu. Code : " + codeTableau
          + ". La sortie nord est maintenant ouverte.");
    } else {
      gui.afficher("✗ Code incorrect. Le digicode reste rouge.");
      if (!protegerErreurAvecStylo()) {
        joueur.perdreEnergie(10);
        gui.afficher("Énergie : " + joueur.getEnergie() + "/100  "
            + barreEnergie(joueur.getEnergie()));
        if (joueur.estMort()) {
          gameOver("Vous avez épuisé votre énergie à force d'essais... Game over.");
          return;
        }
      }
      gui.afficher("Relisez le tableau (LIRE) et réessayez.");
    }
    gui.afficher();
  }

  // ══════════════════════════════════════════════════════════════════════════
  // ÉNIGME 2 — LABYRINTHE BFS
  // ══════════════════════════════════════════════════════════════════════════

  /** Démarre le labyrinthe BFS et affiche la première étape. */
  private void demarrerBFS() {
    bfsActif = true;
    bfsEtape = 0;
    bfsErreurs = 0;
    gui.afficher("╔══════════════════════════════════════════╗");
    gui.afficher("║       LABYRINTHE DU COULOIR              ║");
    gui.afficher("╚══════════════════════════════════════════╝");
    gui.afficher("Le couloir se ramifie dans plusieurs directions.\n"
        + "Trouvez la bonne séquence de 3 directions pour atteindre les escaliers.\n"
        + "Attention : 3 erreurs au total et vous vous perdrez définitivement !");
    gui.afficher();
    afficherEtapeBFS();
  }

  /** Affiche l'indice de l'étape courante du BFS. */
  private void afficherEtapeBFS() {
    gui.afficher("── Étape " + (bfsEtape + 1) + "/3  |  Erreurs : "
        + bfsErreurs + "/3 ──────────────────────");
    gui.afficher("Indice : " + bfsClues[bfsEtape]);
    gui.afficher();
    gui.afficher("Quelle direction ? CHOISIR N  /  CHOISIR S  /  CHOISIR E  /  CHOISIR O");
    gui.afficher();
  }

  /**
   * Valide la direction saisie pour l'étape courante du BFS.
   *
   * @param reponse la direction saisie (N, S, E, O ou forme longue)
   */
  private void validerBFS(String reponse) {
    Direction choix = parseDirection(reponse);
    if (choix == null) {
      gui.afficher("Direction invalide. Tapez N, S, E ou O.");
      gui.afficher();
      return;
    }

    if (choix == bfsSequence[bfsEtape]) {
      gui.afficher("✓ Bonne direction !");
      bfsEtape++;
      if (bfsEtape == 3) {
        // BFS réussi
        bfsActif = false;
        gui.afficher("Vous avez navigué le labyrinthe avec succès !");
        gui.afficher("Les escaliers sont maintenant accessibles (tapez N).");
        couloir.ajouteSortie(Direction.NORD, escaliers);
        couloir.setTexte("Labyrinthe résolu. Sortie NORD vers les escaliers libre.");
      } else {
        gui.afficher();
        afficherEtapeBFS();
        return;
      }
    } else {
      bfsErreurs++;
      gui.afficher("✗ Mauvaise direction !");
      if (!protegerErreurAvecStylo()) {
        joueur.perdreEnergie(20);
        gui.afficher("Énergie : " + joueur.getEnergie() + "/100  "
            + barreEnergie(joueur.getEnergie()));
      }
      gui.afficher("Erreurs : " + bfsErreurs + "/3");
      if (bfsErreurs >= 3) {
        bfsActif = false;
        gui.afficher("\nVous avez trop erré dans le couloir. Une porte s'ouvre de force...");
        entrerSalleBloquante();
        return;
      }
      if (joueur.estMort()) {
        gameOver("Épuisé(e) à errer dans le couloir... Game over.");
        return;
      }
      gui.afficher();
      afficherEtapeBFS();
      return;
    }
    gui.afficher();
  }

  /** Force le joueur dans la salle bloquante (3 erreurs BFS). */
  private void entrerSalleBloquante() {
    // ajouter la connexion si inexistante (pour que l'historique fonctionne)
    if (couloir.obtientSortie(Direction.OUEST) == null) {
      couloir.ajouteSortie(Direction.OUEST, salleBloquante);
    }
    joueur.getHistorique().clear(); // plus de retour possible
    zoneCourante = salleBloquante;
    gui.afficher();
    afficherLocalisation();
    gui.afficher("[Vous êtes dans la salle bloquante. Chaque commande vous coûte 10 énergie !]");
    gui.afficher();
  }

  // ══════════════════════════════════════════════════════════════════════════
  // ÉNIGME 3 — RESTAURANT UNIVERSITAIRE
  // ══════════════════════════════════════════════════════════════════════════

  /** Construit et affecte le texte lisible du RU pour le scénario courant. */
  private void mettreAJourTexteRU() {
    StringBuilder sb = new StringBuilder();
    sb.append("╔══════════════════════════════════════════╗\n");
    sb.append("║          MENU DU SOIR — R.U.             ║\n");
    sb.append("╚══════════════════════════════════════════╝\n\n");
    sb.append("Règle ce soir : ").append(REGLES_RU[scenarioRu]).append("\n\n");
    for (String plat : PLATS_RU[scenarioRu]) {
      sb.append(plat).append("\n");
    }
    sb.append("\nQuel plat choisissez-vous ? Tapez : CHOISIR <1, 2 ou 3>");
    restaurantU.setTexte(sb.toString());
  }

  /**
   * Valide le choix de plat du joueur.
   * Bon choix : +30 énergie. Mauvais choix : game over immédiat.
   *
   * @param reponse "1", "2" ou "3"
   */
  private void validerChoixRU(String reponse) {
    int choix;
    try {
      choix = Integer.parseInt(reponse.trim());
    } catch (NumberFormatException e) {
      gui.afficher("Entrez 1, 2 ou 3.");
      gui.afficher();
      return;
    }
    if (choix < 1 || choix > 3) {
      gui.afficher("Choisissez entre 1, 2 et 3.");
      gui.afficher();
      return;
    }

    if (choix == BON_CHOIX_RU[scenarioRu]) {
      gui.afficher("✓ Excellent choix ! Ce plat respecte parfaitement la règle de ce soir.");
      joueur.gagnerEnergie(30);
      enigmeRuResolue = true;
      gui.afficher("Vous mangez avec appétit. +30 énergie.");
      gui.afficher("Énergie : " + joueur.getEnergie() + "/100  "
          + barreEnergie(joueur.getEnergie()));
      restaurantU.setTexte("Énigme résolue. Vous avez bien mangé ce soir.");
    } else {
      gui.afficher("✗ Mauvais choix... vous ressentez une violente indisposition.");
      gui.afficher("Intoxication alimentaire sévère. Game over.");
      gameOver("Vous vous effondrez. Fin de la partie.");
      return;
    }
    gui.afficher();
  }

  // ══════════════════════════════════════════════════════════════════════════
  // ÉNIGME 4 — DISTRIBUTEUR AUTOMATIQUE (CODE JAVA)
  // ══════════════════════════════════════════════════════════════════════════

  /** Construit et affecte le texte lisible du distributeur pour l'énigme courante. */
  private void mettreAJourTexteDistributeur() {
    String[] enigme = ENIGMES_JAVA[indexEnigmeJava];
    String texte = "╔══════════════════════════════════════════╗\n"
        + "║      DISTRIBUTEUR — ÉNIGME JAVA          ║\n"
        + "╚══════════════════════════════════════════╝\n\n"
        + "Analysez ce code Java :\n\n"
        + enigme[0] + "\n\n"
        + enigme[1] + "\n\n"
        + "Tapez : CHOISIR <réponse>";
    distributeur.setTexte(texte);
  }

  /**
   * Valide la réponse à l'énigme Java du distributeur.
   * En cas de succès, révèle le code MEGA et donne un objet cadeau.
   *
   * @param reponse la valeur saisie
   */
  private void validerDistributeur(String reponse) {
    String attendu = ENIGMES_JAVA[indexEnigmeJava][2].toLowerCase().trim();
    if (!reponse.toLowerCase().trim().equals(attendu)) {
      gui.afficher("✗ Réponse incorrecte. L'écran affiche : 'ACCÈS REFUSÉ'.");
      if (!protegerErreurAvecStylo()) {
        joueur.perdreEnergie(15);
        gui.afficher("Énergie : " + joueur.getEnergie() + "/100  "
            + barreEnergie(joueur.getEnergie()));
        if (joueur.estMort()) {
          gameOver("Épuisé(e)... Game over.");
          return;
        }
      }
      gui.afficher("Relisez le code (LIRE) et réessayez.");
      gui.afficher();
      return;
    }

    gui.afficher("✓ Réponse correcte ! L'écran affiche : 'CODE VALIDÉ'.");
    gui.afficher();
    gui.afficher("╔══════════════════════════════════════════╗");
    gui.afficher("║  CODE D'ACCÈS BÂTIMENT MEGA : " + codeMega + "     ║");
    gui.afficher("╚══════════════════════════════════════════╝");
    gui.afficher("Mémorisez ce code — l'agent MEGA vous le demandera !");
    gui.afficher();
    codeMegaConnu = true;
    enigmeDistributeurResolue = true;

    // Cadeau aléatoire
    Item cadeau = aleatoire.nextBoolean()
        ? new Item("Café", 1, "Un café chaud. Peut convaincre un agent MEGA épuisé.")
        : new Item("Barre de céréales", 1, "Snack énergétique. +15 énergie via UTILISER.");
    if (joueur.ajouterItem(cadeau)) {
      gui.afficher("Le distributeur crache un cadeau : " + cadeau.getNom() + " !");
    } else {
      zoneCourante.ajouterObjet(cadeau);
      gui.afficher("Le distributeur crache : " + cadeau.getNom()
          + " (sac plein — objet posé au sol).");
    }
    distributeur.setTexte("Énigme résolue. Code MEGA : " + codeMega + ".");
    gui.afficher();
  }

  // ══════════════════════════════════════════════════════════════════════════
  // ÉNIGME 5 — AGENT DE SÉCURITÉ MEGA
  // ══════════════════════════════════════════════════════════════════════════

  /**
   * Déclenche le dialogue avec l'agent de sécurité (depuis le Hall).
   * Accessible depuis le Hall uniquement.
   */
  private void parler() {
    if (zoneCourante != hall) {
      gui.afficher("Il n'y a personne à qui parler ici.");
      gui.afficher();
      return;
    }
    if (megaDebloque) {
      gui.afficher("L'agent vous fait un signe de tête bienveillant. La voie est libre.");
      gui.afficher();
      return;
    }
    dialogueAgent();
  }

  /** Gère le dialogue avec l'agent selon son humeur et l'état du joueur. */
  private void dialogueAgent() {
    if (humeurAgent == HumeurAgent.EPUISE) {
      gui.afficher("L'agent de sécurité a des cernes prononcés. Il vous regarde à peine.");
      gui.afficher("« Je suis épuisé... Un café me ferait tellement du bien... »");
      gui.afficher("Si vous avez un Café, utilisez-le (U Café).");
      agentAttendCafe = true;
    } else {
      // FORME : vérifier carte ET code
      boolean aLaCarte = joueur.possedeItem("Carte de professeur");
      boolean aLeCode  = codeMegaConnu;
      if (aLaCarte && aLeCode) {
        gui.afficher("L'agent examine votre Carte de professeur, puis vérifie le code.");
        gui.afficher("« " + codeMega + "... correct. Tout est en ordre. Vous pouvez passer. »");
        megaDebloque = true;
        batimentMega.setVerrouillee(false);
        gui.afficher("[Bâtiment MEGA déverrouillé — tapez N depuis le Hall.]");
      } else if (!aLaCarte && !aLeCode) {
        gui.afficher("L'agent vous toise sévèrement.");
        gui.afficher("« Il me faut votre Carte de professeur ET le code du distributeur. Circulez ! »");
      } else if (!aLaCarte) {
        gui.afficher("L'agent vérifie le code... puis tend la main.");
        gui.afficher("« Le code est bon, mais je dois voir votre Carte de professeur. »");
        gui.afficher("Cherchez la carte (salle secondaire ou autre zone).");
      } else {
        gui.afficher("L'agent saisit votre carte, puis attend.");
        gui.afficher("« La carte est valide, mais il me faut aussi le code du distributeur. »");
        gui.afficher("Résolvez l'énigme du distributeur (OUEST depuis le Hall).");
      }
    }
    gui.afficher();
  }

  // ══════════════════════════════════════════════════════════════════════════
  // UTILITAIRES
  // ══════════════════════════════════════════════════════════════════════════

  /**
   * Si le joueur possède un Stylo non utilisé, absorbe l'erreur et consomme l'effet.
   *
   * @return {@code true} si l'erreur a été absorbée
   */
  private boolean protegerErreurAvecStylo() {
    if (!styloEffetUtilise && joueur.possedeItem("Stylo")) {
      styloEffetUtilise = true;
      gui.afficher("[Votre Stylo vous protège : perte d'énergie annulée pour cette erreur !]");
      return true;
    }
    return false;
  }

  /**
   * Affiche le message de game over et désactive la saisie.
   *
   * @param message texte à afficher
   */
  private void gameOver(String message) {
    gui.afficher();
    gui.afficher("╔══════════════════════════════════════════╗");
    gui.afficher("║              GAME OVER                   ║");
    gui.afficher("╚══════════════════════════════════════════╝");
    gui.afficher(message);
    gui.afficher();
    gui.enable(false);
    partieTerminee = true;
  }

  /** Affiche le message de victoire et désactive la saisie. */
  private void victoire() {
    gui.afficher();
    gui.afficher("╔══════════════════════════════════════════╗");
    gui.afficher("║          FÉLICITATIONS !                 ║");
    gui.afficher("╚══════════════════════════════════════════╝");
    gui.afficher("Vous franchissez le portail et respirez l'air frais de la nuit.");
    gui.afficher("Vous avez réussi à vous échapper de l'université avant l'aube !");
    gui.afficher("Énergie restante : " + joueur.getEnergie() + "/100  "
        + barreEnergie(joueur.getEnergie()));
    gui.afficher();
    gui.enable(false);
    partieTerminee = true;
  }

  /**
   * Génère une barre visuelle d'énergie (10 blocs).
   *
   * @param energie valeur 0-100
   * @return ex. "[████████░░]"
   */
  private String barreEnergie(int energie) {
    int pleins = energie / 10;
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < 10; i++) {
      sb.append(i < pleins ? "█" : "░");
    }
    return sb.append("]").toString();
  }

  /**
   * Convertit une chaîne en {@link Direction} (insensible à la casse).
   *
   * @param s "N", "NORD", "S", "SUD", "E", "EST", "O", "OUEST"
   * @return la direction ou {@code null} si non reconnue
   */
  private Direction parseDirection(String s) {
    return switch (s.trim().toUpperCase()) {
      case "N", "NORD"  -> Direction.NORD;
      case "S", "SUD"   -> Direction.SUD;
      case "E", "EST"   -> Direction.EST;
      case "O", "OUEST" -> Direction.OUEST;
      default           -> null;
    };
  }

  /**
   * Retourne l'index de la direction dans l'ordre NORD=0, SUD=1, EST=2, OUEST=3.
   *
   * @param d la direction
   * @return index 0-3
   */
  private int directionIndex(Direction d) {
    return switch (d) {
      case NORD  -> 0;
      case SUD   -> 1;
      case EST   -> 2;
      case OUEST -> 3;
    };
  }
}
