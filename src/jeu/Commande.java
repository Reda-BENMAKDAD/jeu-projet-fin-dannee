package jeu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Représente une commande du jeu.
 * <p>
 * Une commande peut être directionnelle (NORD, SUD, EST, OUEST) ou
 * non-directionnelle (QUITTER, AIDE, RETOUR, etc.).  
 * Chaque commande possède une **abréviation** et une **description complète**.
 * <p>
 * Cette interface fournit également des méthodes utilitaires pour obtenir
 * toutes les commandes et leurs descriptions/abréviations.
 */
public interface Commande {
	/** Retourne l’abréviation de la commande */
    String getAbreviation();

    /** Retourne la description complète de la commande */
    String getDescription();
    
    /** 
     * Fusionne toutes les commandes directionnelles et non-directionnelles
     * pour simplifier la génération de listes.
     */
    private static List<Commande> toutes() {
        List<Commande> toutes = new ArrayList<>();
        toutes.addAll(Arrays.asList(Direction.values()));
        toutes.addAll(Arrays.asList(CommandeNonDirectionnelle.values()));
        return toutes;
    }
        
    /** Retourne la liste de toutes les descriptions */
    static List<String> toutesLesDescriptions() {
        List<String> resultat = new ArrayList<>();
        for (Commande c : toutes()) resultat.add(c.getDescription());
        return resultat;
    }
	
    /** Retourne la liste de tous les abréviations */
    static List<String> toutesLesAbreviations() {
        List<String> resultat = new ArrayList<>();
        for (Commande c : toutes()) resultat.add(c.getAbreviation());
        return resultat;
    }

    /** Retourne la liste de tous les noms */
    static List<String> tousLesNoms() {
        List<String> resultat = new ArrayList<>();
        for (Commande c : toutes()) resultat.add(c.toString());
        return resultat;
    }
}
