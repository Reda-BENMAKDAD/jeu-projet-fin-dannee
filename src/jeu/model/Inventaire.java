package jeu.model;


import java.util.ArrayList;
import java.util.List;

public class Inventaire {

    private List<Item> items;
    private int capaciteMax;

    public Inventaire(int capaciteMax) {
        this.capaciteMax = capaciteMax;
        this.items = new ArrayList<>();
    }

    public int getPoidsTotal() {
        int total = 0;
        for (Item item : items) {
            total += item.getPoids();
        }
        return total;
    }

    public boolean ajouter(Item item) {
        if (getPoidsTotal() + item.getPoids() > capaciteMax) {
            return false;
        }
        items.add(item);
        return true;
    }

    public boolean retirer(String nom) {
        for (Item item : items) {
            if (item.getNom().equalsIgnoreCase(nom)) {
                items.remove(item);
                return true;
            }
        }
        return false;
    }

    public void afficher() {
        if (items.isEmpty()) {
            System.out.println("Inventaire vide");
            return;
        }

        for (Item item : items) {
            System.out.println("- " + item);
        }
    }
}