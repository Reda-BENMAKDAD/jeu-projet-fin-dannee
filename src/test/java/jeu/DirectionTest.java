package jeu;

import org.junit.Test;
import static org.junit.Assert.*;

public class DirectionTest {

    @Test
    public void nord_abreviation() {
        assertEquals("N", Direction.NORD.getAbreviation());
    }

    @Test
    public void sud_abreviation() {
        assertEquals("S", Direction.SUD.getAbreviation());
    }

    @Test
    public void est_abreviation() {
        assertEquals("E", Direction.EST.getAbreviation());
    }

    @Test
    public void ouest_abreviation() {
        assertEquals("O", Direction.OUEST.getAbreviation());
    }

    @Test
    public void nord_description() {
        assertTrue(Direction.NORD.getDescription().contains("nord"));
    }

    @Test
    public void sud_description() {
        assertTrue(Direction.SUD.getDescription().contains("sud"));
    }

    @Test
    public void est_description() {
        assertTrue(Direction.EST.getDescription().contains("est"));
    }

    @Test
    public void ouest_description() {
        assertTrue(Direction.OUEST.getDescription().contains("ouest"));
    }

    @Test
    public void quatreDirectionsExistent() {
        assertEquals(4, Direction.values().length);
    }

    @Test
    public void implementeCommande() {
        for (Direction d : Direction.values()) {
            assertNotNull(d.getAbreviation());
            assertNotNull(d.getDescription());
        }
    }

    @Test
    public void valueOf_nord() {
        assertEquals(Direction.NORD, Direction.valueOf("NORD"));
    }
}
