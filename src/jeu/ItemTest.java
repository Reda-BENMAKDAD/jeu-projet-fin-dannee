package jeu;

import jeu.model.Item;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ItemTest {

    @Test
    void testCreationItem() {
        Item item = new Item("Stylo", 1);

        assertEquals("stylo", item.getNom());
        assertEquals(1, item.getPoids());
    }
}