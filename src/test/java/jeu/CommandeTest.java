package jeu;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class CommandeTest {

    @Test
    public void toutesLesDescriptions_contient17Elements() {
        // 4 directions + 13 non-directionnelles
        List<String> descriptions = Commande.toutesLesDescriptions();
        assertEquals(17, descriptions.size());
    }

    @Test
    public void toutesLesAbreviations_contient17Elements() {
        List<String> abrevs = Commande.toutesLesAbreviations();
        assertEquals(17, abrevs.size());
    }

    @Test
    public void tousLesNoms_contient17Elements() {
        List<String> noms = Commande.tousLesNoms();
        assertEquals(17, noms.size());
    }

    @Test
    public void toutesLesDescriptions_contientDirectionNord() {
        List<String> descriptions = Commande.toutesLesDescriptions();
        assertTrue(descriptions.stream().anyMatch(d -> d.contains("nord")));
    }

    @Test
    public void toutesLesAbreviations_contientQ() {
        List<String> abrevs = Commande.toutesLesAbreviations();
        assertTrue(abrevs.contains("Q"));
    }

    @Test
    public void toutesLesAbreviations_contientN() {
        List<String> abrevs = Commande.toutesLesAbreviations();
        assertTrue(abrevs.contains("N"));
    }

    @Test
    public void abreviationsToutes_sontUniques() {
        List<String> abrevs = Commande.toutesLesAbreviations();
        long distinctes = abrevs.stream().distinct().count();
        assertEquals(abrevs.size(), distinctes);
    }
}
