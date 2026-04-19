package jeu.model;

public class Item {
    private String nom;
    private int poids;

    public Item(String nom, int poids) {
        this.nom = nom.toLowerCase();
        this.poids = poids;
    }

    public String getNom() {
        return nom;
    }

    public int getPoids() {
        return poids;
    }

    @Override
    public String toString() {
        return nom + " (" + poids + ")";
    }
}
