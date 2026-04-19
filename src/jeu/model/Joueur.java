package jeu.model;

public class Joueur {

    private int energie;
    private Inventaire inventaire;

    public Joueur() {
        this.energie = 100;
        this.inventaire = new Inventaire(4);
    }

    // 🔋 Energie
    public void perdreEnergie(int valeur) {
        energie -= valeur;
        if (energie < 0) {
            energie = 0;
        }
    }

    public void gagnerEnergie(int valeur) {
        energie += valeur;
    }

    public int getEnergie() {
        return energie;
    }

    public boolean estVivant() {
        return energie > 0;
    }

    // 🎒 Inventaire
    public Inventaire getInventaire() {
        return inventaire;
    }
}
