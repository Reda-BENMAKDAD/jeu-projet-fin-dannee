package jeu;

import jeu.model.Joueur;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JoueurTest {

    @Test
    void testEnergie() {
        Joueur j = new Joueur();

        j.perdreEnergie(30);
        assertEquals(70, j.getEnergie());

        j.perdreEnergie(100);
        assertEquals(0, j.getEnergie());
    }

    @Test
    void testVie() {
        Joueur j = new Joueur();

        j.perdreEnergie(100);
        assertFalse(j.estVivant());
    }
}