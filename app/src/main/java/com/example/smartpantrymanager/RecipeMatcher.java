package com.example.smartpantrymanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Core business logic: decides which recipes can be cooked using ONLY
 * what is currently in the pantry (the "strict-matching rule").
 *
 * A recipe is suggested only if EVERY required ingredient is present in
 * the pantry in at least the required quantity. Partial matches are excluded.
 */
public class RecipeMatcher {

    private enum UnitType { MASS, VOLUME, COUNT, UNKNOWN }

    /** Small tolerance so floating-point rounding never rejects an exact match. */
    private static final double EPSILON = 1e-9;

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    /** Returns only the recipes the user can make right now. */
    public static List<Recipe> findSuggestedRecipes(List<Recipe> recipes, List<Ingredient> pantry) {
        Map<String, Double> stock = buildStock(pantry);
        List<Recipe> suggested = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (canMake(recipe, stock)) {
                suggested.add(recipe);
            }
        }
        return suggested;
    }

    /**
     * Totals up the pantry. Each ingredient is stored under a key made of its
     * normalised name and unit type, with the amount converted to a base unit
     * (grams, millilitres or pieces). Duplicate entries are added together.
     */
    public static Map<String, Double> buildStock(List<Ingredient> pantry) {
        Map<String, Double> stock = new HashMap<>();
        for (Ingredient item : pantry) {
            String key = keyFor(item.getName(), item.getUnit());
            double amount = toBaseUnit(item.getQuantity(), item.getUnit());
            Double existing = stock.get(key);
            stock.put(key, existing == null ? amount : existing + amount);
        }
        return stock;
    }

    /** True only if the recipe has ingredients and none of them is missing. */
    public static boolean canMake(Recipe recipe, Map<String, Double> stock) {
        List<RecipeIngredient> needed = recipe.getIngredients();
        if (needed == null || needed.isEmpty()) {
            return false; // an empty recipe must never "match" everything
        }
        return getMissingIngredients(recipe, stock).isEmpty();
    }

    /** Lists the ingredients the pantry cannot cover (also useful for an "Almost There" list). */
    public static List<RecipeIngredient> getMissingIngredients(Recipe recipe, Map<String, Double> stock) {
        List<RecipeIngredient> missing = new ArrayList<>();
        for (RecipeIngredient need : recipe.getIngredients()) {
            String key = keyFor(need.getName(), need.getUnit());
            double required = toBaseUnit(need.getQuantity(), need.getUnit());
            Double have = stock.get(key);
            if (have == null || have + EPSILON < required) {
                missing.add(need);
            }
        }
        return missing;
    }

    // ------------------------------------------------------------------
    // Name normalisation ("Tomatoes" -> "tomato")
    // ------------------------------------------------------------------

    public static String normalizeName(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        if (s.isEmpty()) {
            return s;
        }
        // Only the last word is made singular: "olive oils" -> "olive oil"
        int lastSpace = s.lastIndexOf(' ');
        String head = lastSpace == -1 ? "" : s.substring(0, lastSpace + 1);
        String last = lastSpace == -1 ? s : s.substring(lastSpace + 1);
        return head + singularize(last);
    }

    private static String singularize(String w) {
        if (w.length() <= 3) {
            return w;
        }
        if (w.endsWith("ies")) {                       // berries -> berry
            return w.substring(0, w.length() - 3) + "y";
        }
        if (w.endsWith("oes") || w.endsWith("ches")    // tomatoes -> tomato
                || w.endsWith("shes") || w.endsWith("xes")) {
            return w.substring(0, w.length() - 2);
        }
        if (w.endsWith("ss") || w.endsWith("us")) {    // hummus, glass: leave alone
            return w;
        }
        if (w.endsWith("s")) {                         // eggs -> egg
            return w.substring(0, w.length() - 1);
        }
        return w;
    }

    // ------------------------------------------------------------------
    // Unit handling
    // ------------------------------------------------------------------

    private static UnitType unitType(String unit) {
        if (unit == null) {
            return UnitType.UNKNOWN;
        }
        switch (unit.trim().toLowerCase(Locale.ROOT)) {
            case "g":
            case "kg":
                return UnitType.MASS;
            case "ml":
            case "l":
            case "tsp":
            case "tbsp":
            case "cup":
                return UnitType.VOLUME;
            case "pcs":
                return UnitType.COUNT;
            default:
                return UnitType.UNKNOWN;
        }
    }

    /** Converts a quantity to grams, millilitres or pieces. */
    private static double toBaseUnit(double quantity, String unit) {
        if (unit == null) {
            return quantity;
        }
        switch (unit.trim().toLowerCase(Locale.ROOT)) {
            case "kg":   return quantity * 1000;
            case "l":    return quantity * 1000;
            case "tsp":  return quantity * 5;
            case "tbsp": return quantity * 15;
            case "cup":  return quantity * 240;
            default:     return quantity; // g, ml, pcs
        }
    }

    /**
     * Name + unit type decide whether two entries are comparable.
     * 500 g of flour can satisfy 200 g, but "2 pcs" of flour cannot satisfy "200 g".
     */
    private static String keyFor(String name, String unit) {
        UnitType type = unitType(unit);
        String suffix = (type == UnitType.UNKNOWN)
                ? "unit:" + (unit == null ? "" : unit.trim().toLowerCase(Locale.ROOT))
                : type.name();
        return normalizeName(name) + "|" + suffix;
    }
}
