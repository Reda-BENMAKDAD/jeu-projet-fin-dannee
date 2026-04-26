package jeu;

import org.junit.Test;
import static org.junit.Assert.*;

public class CommandeNonDirectionnelleTest {

    @Test
    public void aide_abreviation() {
        assertEquals("?", CommandeNonDirectionnelle.AIDE.getAbreviation());
    }

    @Test
    public void inventaire_abreviation() {
        assertEquals("I", CommandeNonDirectionnelle.INVENTAIRE.getAbreviation());
    }

    @Test
    public void etat_abreviation() {
        assertEquals("ET", CommandeNonDirectionnelle.ETAT.getAbreviation());
    }

    @Test
    public void observer_abreviation() {
        assertEquals("OBS", CommandeNonDirectionnelle.OBSERVER.getAbreviation());
    }

    @Test
    public void prendre_abreviation() {
        assertEquals("P", CommandeNonDirectionnelle.PRENDRE.getAbreviation());
    }

    @Test
    public void deposer_abreviation() {
        assertEquals("DEP", CommandeNonDirectionnelle.DEPOSER.getAbreviation());
    }

    @Test
    public void utiliser_abreviation() {
        assertEquals("U", CommandeNonDirectionnelle.UTILISER.getAbreviation());
    }

    @Test
    public void lire_abreviation() {
        assertEquals("L", CommandeNonDirectionnelle.LIRE.getAbreviation());
    }

    @Test
    public void choisir_abreviation() {
        assertEquals("CH", CommandeNonDirectionnelle.CHOISIR.getAbreviation());
    }

    @Test
    public void parler_abreviation() {
        assertEquals("PA", CommandeNonDirectionnelle.PARLER.getAbreviation());
    }

    @Test
    public void retour_abreviation() {
        assertEquals("R", CommandeNonDirectionnelle.RETOUR.getAbreviation());
    }

    @Test
    public void sauvegarder_abreviation() {
        assertEquals("SAV", CommandeNonDirectionnelle.SAUVEGARDER.getAbreviation());
    }

    @Test
    public void quitter_abreviation() {
        assertEquals("Q", CommandeNonDirectionnelle.QUITTER.getAbreviation());
    }

    @Test
    public void treizeCommandesExistent() {
        assertEquals(13, CommandeNonDirectionnelle.values().length);
    }

    @Test
    public void toutesLesDescriptionsNonNulles() {
        for (CommandeNonDirectionnelle c : CommandeNonDirectionnelle.values()) {
            assertNotNull(c.getDescription());
            assertFalse(c.getDescription().isEmpty());
        }
    }

    @Test
    public void abreviationsUniques() {
        java.util.Set<String> abrevs = new java.util.HashSet<>();
        for (CommandeNonDirectionnelle c : CommandeNonDirectionnelle.values()) {
            assertTrue(
                "Abréviation dupliquée : " + c.getAbreviation(),
                abrevs.add(c.getAbreviation())
            );
        }
    }
}
