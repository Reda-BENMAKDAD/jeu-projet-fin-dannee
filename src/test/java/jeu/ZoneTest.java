package jeu;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ZoneTest {

    private Zone zone;

    @Before
    public void setUp() {
        zone = new Zone("un couloir sombre", "couloir.png");
    }

    // ── Constructeur ─────────────────────────────────────────────────────────

    @Test
    public void constructeur_nomImageCorrect() {
        assertEquals("couloir.png", zone.nomImage());
    }

    @Test
    public void constructeur_toString() {
        assertEquals("un couloir sombre", zone.toString());
    }

    @Test
    public void constructeur_drapeauxParDefaut() {
        assertFalse(zone.isSansRetour());
        assertFalse(zone.isDansLeNoir());
        assertFalse(zone.isVerrouillee());
        assertNull(zone.getTexte());
    }

    @Test
    public void constructeur_aucunObjet() {
        assertTrue(zone.getObjets().isEmpty());
    }

    // ── Sorties ───────────────────────────────────────────────────────────────

    @Test
    public void ajouteSortie_sortieAccessible() {
        Zone voisine = new Zone("salle", "salle.png");
        zone.ajouteSortie(Direction.NORD, voisine);
        assertEquals(voisine, zone.obtientSortie(Direction.NORD));
    }

    @Test
    public void obtientSortie_nullSiAbsente() {
        assertNull(zone.obtientSortie(Direction.SUD));
    }

    @Test
    public void ajouteSortie_plusieursDirections() {
        Zone nord = new Zone("nord", "n.png");
        Zone sud  = new Zone("sud",  "s.png");
        zone.ajouteSortie(Direction.NORD, nord);
        zone.ajouteSortie(Direction.SUD,  sud);
        assertEquals(nord, zone.obtientSortie(Direction.NORD));
        assertEquals(sud,  zone.obtientSortie(Direction.SUD));
        assertNull(zone.obtientSortie(Direction.EST));
    }

    // ── Objets ────────────────────────────────────────────────────────────────

    @Test
    public void ajouterObjet_objetPresent() {
        Item stylo = new Item("Stylo", 1, "Desc.");
        zone.ajouterObjet(stylo);
        assertEquals(1, zone.getObjets().size());
        assertEquals("Stylo", zone.getObjets().get(0).getNom());
    }

    @Test
    public void retirerObjet_retireCorrectement() {
        zone.ajouterObjet(new Item("Stylo", 1, "Desc."));
        Item retire = zone.retirerObjet("Stylo");
        assertNotNull(retire);
        assertEquals("Stylo", retire.getNom());
        assertTrue(zone.getObjets().isEmpty());
    }

    @Test
    public void retirerObjet_insensibleCasse() {
        zone.ajouterObjet(new Item("Lampe Torche", 1, ""));
        assertNotNull(zone.retirerObjet("lampe torche"));
    }

    @Test
    public void retirerObjet_nullSiAbsent() {
        assertNull(zone.retirerObjet("Inexistant"));
    }

    @Test
    public void getObjets_listeNonModifiable() {
        zone.ajouterObjet(new Item("A", 1, ""));
        try {
            zone.getObjets().add(new Item("B", 1, ""));
            fail("La liste devrait être non modifiable");
        } catch (UnsupportedOperationException e) {
            // comportement attendu
        }
    }

    @Test
    public void ajouterPlusieursObjets() {
        zone.ajouterObjet(new Item("A", 1, ""));
        zone.ajouterObjet(new Item("B", 1, ""));
        zone.ajouterObjet(new Item("C", 1, ""));
        assertEquals(3, zone.getObjets().size());
    }

    // ── Drapeaux ──────────────────────────────────────────────────────────────

    @Test
    public void setSansRetour() {
        zone.setSansRetour(true);
        assertTrue(zone.isSansRetour());
        zone.setSansRetour(false);
        assertFalse(zone.isSansRetour());
    }

    @Test
    public void setDansLeNoir() {
        zone.setDansLeNoir(true);
        assertTrue(zone.isDansLeNoir());
        zone.setDansLeNoir(false);
        assertFalse(zone.isDansLeNoir());
    }

    @Test
    public void setVerrouillee() {
        zone.setVerrouillee(true);
        assertTrue(zone.isVerrouillee());
        zone.setVerrouillee(false);
        assertFalse(zone.isVerrouillee());
    }

    @Test
    public void setTexte_getTexte() {
        zone.setTexte("Tableau couvert d'équations.");
        assertEquals("Tableau couvert d'équations.", zone.getTexte());
    }

    // ── descriptionLongue ────────────────────────────────────────────────────

    @Test
    public void descriptionLongue_contientDescription() {
        String desc = zone.descriptionLongue();
        assertTrue(desc.contains("un couloir sombre"));
    }

    @Test
    public void descriptionLongue_sansObjets_neMentionneObjet() {
        String desc = zone.descriptionLongue();
        assertFalse(desc.contains("Objets visibles"));
    }

    @Test
    public void descriptionLongue_avecObjet_mentionneObjet() {
        zone.ajouterObjet(new Item("Stylo", 1, ""));
        String desc = zone.descriptionLongue();
        assertTrue(desc.contains("Stylo"));
    }

    @Test
    public void descriptionLongue_dansLeNoir_masqueObjets() {
        zone.ajouterObjet(new Item("Stylo", 1, ""));
        zone.setDansLeNoir(true);
        String desc = zone.descriptionLongue();
        assertFalse(desc.contains("Objets visibles"));
        assertTrue(desc.contains("nuit noire"));
    }

    @Test
    public void descriptionLongue_avecSorties_listeSorties() {
        zone.ajouteSortie(Direction.NORD, new Zone("voisin", "v.png"));
        String desc = zone.descriptionLongue();
        assertTrue(desc.contains("NORD"));
    }

    @Test
    public void descriptionLongue_sansSorties() {
        String desc = zone.descriptionLongue();
        assertTrue(desc.contains("Aucune sortie"));
    }
}
