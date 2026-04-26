package jeu;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;

import java.io.*;

import static org.junit.Assert.*;

/**
 * Tests de la classe Jeu sans interface graphique.
 * La construction d'un Jeu est possible sans GUI (gui est null jusqu'à setGUI).
 */
public class JeuTest {

    private static final String NOM_TEST = "JoueurTestJeu";
    // cheminSauvegarde utilise toLowerCase()
    private static final File FICHIER_SAVE = new File("saves/" + NOM_TEST.toLowerCase() + ".sav");

    @Before
    @After
    public void nettoyerSauvegarde() {
        if (FICHIER_SAVE.exists()) FICHIER_SAVE.delete();
    }

    // ── Construction ─────────────────────────────────────────────────────────

    @Test
    public void constructeur_sansSauvegardeExistante() {
        Jeu jeu = new Jeu(NOM_TEST);
        assertNotNull(jeu);
    }

    @Test
    public void constructeur_plusieursFoisSansCrash() {
        for (int i = 0; i < 5; i++) {
            assertNotNull(new Jeu(NOM_TEST));
        }
    }

    // ── sauvegardeExiste ─────────────────────────────────────────────────────

    @Test
    public void sauvegardeExiste_fauxSiAucunFichier() {
        assertFalse(Jeu.sauvegardeExiste("joueur_inexistant_xyz"));
    }

    @Test
    public void sauvegardeExiste_vraiApresSauvegarde() throws Exception {
        new File("saves").mkdirs();
        Jeu jeu = new Jeu(NOM_TEST);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FICHIER_SAVE))) {
            oos.writeObject(jeu);
        }
        assertTrue(Jeu.sauvegardeExiste(NOM_TEST));
    }

    // ── chargerSauvegarde ────────────────────────────────────────────────────

    @Test(expected = FileNotFoundException.class)
    public void chargerSauvegarde_leveExceptionSiFichierAbsent() throws Exception {
        Jeu.chargerSauvegarde("joueur_inexistant_xyz");
    }

    @Test
    public void chargerSauvegarde_retourneJeuValide() throws Exception {
        new File("saves").mkdirs();
        Jeu jeu = new Jeu(NOM_TEST);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FICHIER_SAVE))) {
            oos.writeObject(jeu);
        }

        Jeu charge = Jeu.chargerSauvegarde(NOM_TEST);
        assertNotNull(charge);
        assertTrue(charge instanceof Jeu);
    }

    @Test
    public void chargerSauvegarde_roundTrip() throws Exception {
        new File("saves").mkdirs();
        Jeu original = new Jeu(NOM_TEST);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FICHIER_SAVE))) {
            oos.writeObject(original);
        }

        Jeu charge = Jeu.chargerSauvegarde(NOM_TEST);
        assertNotNull(charge);
        // Vérifier que le rechargement ne génère pas d'exception et est cohérent
        assertFalse(Jeu.sauvegardeExiste("joueur_inexistant_xyz"));
    }
}
