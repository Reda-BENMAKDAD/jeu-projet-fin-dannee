package jeu;

import jeu.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InventaireTest {

    @Test
    void testAjoutObjet() {
        Inventaire inv = new Inventaire(4);

        assertTrue(inv.ajouter(new Item("stylo", 1)));
        assertTrue(inv.ajouter(new Item("clé", 2)));
    }

    @Test
    void testSacPlein() {
        Inventaire inv = new Inventaire(3);

        inv.ajouter(new Item("clé", 2));
        boolean result = inv.ajouter(new Item("gros", 2));

        assertFalse(result);
    }
}