package jeu;

import org.junit.Test;
import static org.junit.Assert.*;

public class ItemTest {

    @Test
    public void getNom_retourneNomCorrect() {
        Item item = new Item("Stylo", 1, "Un stylo bleu.");
        assertEquals("Stylo", item.getNom());
    }

    @Test
    public void getSlots_retourneNombreSlots() {
        Item item1 = new Item("Stylo", 1, "Un stylo.");
        Item item2 = new Item("Clé de sortie", 2, "La clé finale.");
        assertEquals(1, item1.getSlots());
        assertEquals(2, item2.getSlots());
    }

    @Test
    public void getDescription_retourneDescription() {
        Item item = new Item("Lampe Torche", 1, "Éclaire les zones sombres.");
        assertEquals("Éclaire les zones sombres.", item.getDescription());
    }

    @Test
    public void toString_formatUnSlot() {
        Item item = new Item("Stylo", 1, "Protège contre une erreur.");
        String result = item.toString();
        assertTrue(result.contains("Stylo"));
        assertTrue(result.contains("1 slot"));
        assertTrue(result.contains("Protège contre une erreur."));
    }

    @Test
    public void toString_formatDeuxSlots() {
        Item item = new Item("Clé de sortie", 2, "Ouvre la sortie.");
        String result = item.toString();
        assertTrue(result.contains("Clé de sortie"));
        assertTrue(result.contains("2 slots"));
        assertFalse(result.contains("2 slot "));
    }
}
