# Nuit à la Fac

Jeu d'aventure créé dans le cadre d'un projet annuel de groupe en Licence MIAGE. Le joueur incarne un étudiant accidentellement enfermé dans son université après s'être endormi en cours, la veille des vacances. Pour éviter d'y passer toutes les vacances, il devra réussir à s'échapper avant le lever du jour en résolvant une série de cinq énigmes interconnectées.

## Contexte

Le jeu se déroule en dix zones (salles de cours, couloir, restaurant universitaire, hall…). Chaque partie est **différente** : les énigmes, la position des objets et les coupures de courant sont générés aléatoirement. Le joueur dispose d'une barre d'énergie qui diminue à chaque déplacement et à chaque erreur — il peut la restaurer grâce aux objets ramassés en chemin.

**Les cinq énigmes :**
1. **Tableau** — résoudre un problème de programmation linéaire pour quitter la salle de cours
2. **Labyrinthe BFS** — traverser le couloir en déchiffrant des indices directionnels (3 erreurs max)
3. **Restaurant U** — choisir le bon plat selon une règle diététique aléatoire
4. **Distributeur** — résoudre un extrait de code Java pour obtenir le code d'accès MEGA
5. **Agent MEGA** — convaincre l'agent de sécurité (carte de professeur ou café selon son humeur)

## Prérequis

- Java 17+
- Maven 3.8+

## Installation et lancement

```bash
# se positionner dans le dossier du jeu
cd jeu-projet-fin-dannee

# Compilation
mvn clean compile

# Lancer le jeu
mvn exec:java
```

Si vous n'avez pas Maven (mvn) :

```bash
# sur Windows, suivez les étapes d'installation sur le site ci-dessous :
https://maven.apache.org/install.html

# sur Linux :
sudo apt install maven
```

Au premier lancement, une fenêtre d'authentification s'ouvre. Entrez `I` pour créer un compte ou `C` pour vous connecter.

## Commandes en jeu

| Commande | Action |
|---|---|
| `N` / `S` / `E` / `O` | Se déplacer |
| `OBS` | Observer la pièce (sorties, objets) |
| `P <nom>` | Ramasser un objet |
| `DEP <nom>` | Déposer un objet |
| `U <nom>` | Utiliser un objet |
| `L` | Lire le tableau / panneau de la pièce |
| `CH <réponse>` | Soumettre une réponse à une énigme |
| `PA` | Parler à un personnage |
| `R` | Revenir en arrière |
| `I` | Afficher l'inventaire |
| `SAV` | Sauvegarder la partie |
| `Q` | Quitter |

## Pour lancer les tests

```bash
mvn test
```

Les tests couvrent les classes `Item`, `Joueur`, `Zone`, `Direction`, `CommandeNonDirectionnelle`, `Commande` et `Jeu` (construction, sauvegarde/chargement).

```
Tests run: 96, Failures: 0, Errors: 0
```

## Structure du projet

```
src/
├── main/java/jeu/
│   ├── Main.java                  # Point d'entrée
│   ├── Jeu.java                   # Moteur de jeu (zones, énigmes, commandes)
│   ├── Joueur.java                # Énergie, inventaire, historique
│   ├── Zone.java                  # Salle avec sorties, objets, drapeaux
│   ├── Item.java                  # Objet ramassable
│   ├── Direction.java             # Enum NORD / SUD / EST / OUEST
│   ├── Commande.java              # Interface commande
│   ├── CommandeNonDirectionnelle.java
│   ├── GUI.java                   # Interface Swing
│   ├── Authentification.java      # Connexion / inscription
│   └── GestionnaireComptes.java   # Base de données SQLite
└── test/java/jeu/
    ├── ItemTest.java
    ├── JoueurTest.java
    ├── ZoneTest.java
    ├── DirectionTest.java
    ├── CommandeNonDirectionnelleTest.java
    ├── CommandeTest.java
    └── JeuTest.java
```

Les sauvegardes sont stockées dans `saves/<nom_joueur>.sav` et les comptes dans `comptes.db` à la racine du projet.
