package jeu;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class JoueurTest {

    private Joueur joueur;

    @Before
    public void setUp() {
        joueur = new Joueur("Alice");
    }

    // ── Constructeur ─────────────────────────────────────────────────────────

    @Test
    public void constructeur_nomCorrect() {
        assertEquals("Alice", joueur.getNom());
    }

    @Test
    public void constructeur_energieInitiale() {
        assertEquals(100, joueur.getEnergie());
    }

    @Test
    public void constructeur_sacVide() {
        assertTrue(joueur.getSac().isEmpty());
    }

    @Test
    public void constructeur_quatreSlotsDispo() {
        assertEquals(4, joueur.slotsDisponibles());
    }

    // ── ajouterItem ───────────────────────────────────────────────────────────

    @Test
    public void ajouterItem_reussitSiSlotSuffisant() {
        Item stylo = new Item("Stylo", 1, "Desc.");
        assertTrue(joueur.ajouterItem(stylo));
        assertTrue(joueur.possedeItem("Stylo"));
    }

    @Test
    public void ajouterItem_diminueSlotsDisponibles() {
        joueur.ajouterItem(new Item("Stylo", 1, "Desc."));
        assertEquals(3, joueur.slotsDisponibles());

        joueur.ajouterItem(new Item("Clé", 2, "Desc."));
        assertEquals(1, joueur.slotsDisponibles());
    }

    @Test
    public void ajouterItem_echoueSiSlotInsuffisant() {
        // Remplir avec 3 items (3 slots)
        joueur.ajouterItem(new Item("A", 1, ""));
        joueur.ajouterItem(new Item("B", 1, ""));
        joueur.ajouterItem(new Item("C", 1, ""));
        // 1 slot restant — clé 2 slots refusée
        Item cle = new Item("Clé", 2, "");
        assertFalse(joueur.ajouterItem(cle));
        assertFalse(joueur.possedeItem("Clé"));
    }

    @Test
    public void ajouterItem_sacPleinRefuse() {
        joueur.ajouterItem(new Item("A", 1, ""));
        joueur.ajouterItem(new Item("B", 1, ""));
        joueur.ajouterItem(new Item("C", 1, ""));
        joueur.ajouterItem(new Item("D", 1, ""));
        assertFalse(joueur.ajouterItem(new Item("E", 1, "")));
        assertEquals(0, joueur.slotsDisponibles());
    }

    // ── retirerItem ───────────────────────────────────────────────────────────

    @Test
    public void retirerItem_retireObjetExistant() {
        Item stylo = new Item("Stylo", 1, "Desc.");
        joueur.ajouterItem(stylo);
        Item retire = joueur.retirerItem("Stylo");
        assertNotNull(retire);
        assertEquals("Stylo", retire.getNom());
        assertFalse(joueur.possedeItem("Stylo"));
    }

    @Test
    public void retirerItem_insensibleCasse() {
        joueur.ajouterItem(new Item("Stylo", 1, "Desc."));
        Item retire = joueur.retirerItem("stylo");
        assertNotNull(retire);
    }

    @Test
    public void retirerItem_retourneNullSiAbsent() {
        assertNull(joueur.retirerItem("Inexistant"));
    }

    @Test
    public void retirerItem_libereSlotsApresRetrait() {
        joueur.ajouterItem(new Item("Clé", 2, ""));
        assertEquals(2, joueur.slotsDisponibles());
        joueur.retirerItem("Clé");
        assertEquals(4, joueur.slotsDisponibles());
    }

    // ── possedeItem ───────────────────────────────────────────────────────────

    @Test
    public void possedeItem_vraiSiPresent() {
        joueur.ajouterItem(new Item("Lampe Torche", 1, ""));
        assertTrue(joueur.possedeItem("Lampe Torche"));
    }

    @Test
    public void possedeItem_insensibleCasse() {
        joueur.ajouterItem(new Item("Lampe Torche", 1, ""));
        assertTrue(joueur.possedeItem("lampe torche"));
        assertTrue(joueur.possedeItem("LAMPE TORCHE"));
    }

    @Test
    public void possedeItem_fauxSiAbsent() {
        assertFalse(joueur.possedeItem("Stylo"));
    }

    // ── perdreEnergie / gagnerEnergie ─────────────────────────────────────────

    @Test
    public void perdreEnergie_diminueCorrectement() {
        joueur.perdreEnergie(20);
        assertEquals(80, joueur.getEnergie());
    }

    @Test
    public void perdreEnergie_neDescendPasEnDessousDeZero() {
        joueur.perdreEnergie(200);
        assertEquals(0, joueur.getEnergie());
    }

    @Test
    public void perdreEnergie_exactementZeroEstMort() {
        joueur.perdreEnergie(100);
        assertTrue(joueur.estMort());
    }

    @Test
    public void gagnerEnergie_augmenteCorrectement() {
        joueur.perdreEnergie(50);
        joueur.gagnerEnergie(25);
        assertEquals(75, joueur.getEnergie());
    }

    @Test
    public void gagnerEnergie_neDépassePas100() {
        joueur.gagnerEnergie(50);
        assertEquals(100, joueur.getEnergie());
    }

    // ── estMort ───────────────────────────────────────────────────────────────

    @Test
    public void estMort_fauxParDefaut() {
        assertFalse(joueur.estMort());
    }

    @Test
    public void estMort_vraiQuandEnergieZero() {
        joueur.perdreEnergie(100);
        assertTrue(joueur.estMort());
    }

    // ── afficherInventaire ────────────────────────────────────────────────────

    @Test
    public void afficherInventaire_sacVideMentionne() {
        String inv = joueur.afficherInventaire();
        assertTrue(inv.contains("Sac vide"));
        assertTrue(inv.contains("Énergie : 100/100"));
    }

    @Test
    public void afficherInventaire_listeItems() {
        joueur.ajouterItem(new Item("Stylo", 1, "Protège."));
        String inv = joueur.afficherInventaire();
        assertTrue(inv.contains("Stylo"));
        assertTrue(inv.contains("1/4"));
    }

    @Test
    public void afficherInventaire_compteSlots() {
        joueur.ajouterItem(new Item("Clé", 2, "La clé."));
        String inv = joueur.afficherInventaire();
        assertTrue(inv.contains("2/4"));
    }

    // ── historique ────────────────────────────────────────────────────────────

    @Test
    public void getHistorique_videAuDepart() {
        assertTrue(joueur.getHistorique().isEmpty());
    }

    @Test
    public void getHistorique_ajoutEtRetrait() {
        Zone zone = new Zone("couloir", "img.png");
        joueur.getHistorique().push(zone);
        assertEquals(1, joueur.getHistorique().size());
        assertEquals(zone, joueur.getHistorique().pop());
    }
}
