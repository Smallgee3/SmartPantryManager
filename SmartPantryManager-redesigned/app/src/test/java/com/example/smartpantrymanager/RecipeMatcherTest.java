package com.example.smartpantrymanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Tests for the strict-matching rule (assignment Section 2.3). */
public class RecipeMatcherTest {

    private static RecipeIngredient need(String name, double qty, String unit) {
        return new RecipeIngredient(name, qty, unit);
    }

    private static Ingredient have(String name, double qty, String unit) {
        return new Ingredient(0, name, qty, unit, null);
    }

    private static Recipe recipe(String name, RecipeIngredient... items) {
        return new Recipe(1, name, "steps", Arrays.asList(items));
    }

    private static List<Recipe> suggest(Recipe r, Ingredient... pantry) {
        List<Recipe> recipes = new ArrayList<>();
        recipes.add(r);
        return RecipeMatcher.findSuggestedRecipes(recipes, Arrays.asList(pantry));
    }

    @Test
    public void recipeWithAllIngredientsIsSuggested() {
        Recipe r = recipe("Omelette", need("egg", 3, "pcs"), need("cheese", 50, "g"));
        assertEquals(1, suggest(r, have("egg", 6, "pcs"), have("cheese", 100, "g")).size());
    }

    @Test
    public void recipeMissingOneOfFiveIngredientsIsNotSuggested() {
        Recipe r = recipe("Five", need("a", 1, "pcs"), need("b", 1, "pcs"),
                need("c", 1, "pcs"), need("d", 1, "pcs"), need("e", 1, "pcs"));
        assertTrue(suggest(r, have("a", 1, "pcs"), have("b", 1, "pcs"),
                have("c", 1, "pcs"), have("d", 1, "pcs")).isEmpty());
    }

    @Test
    public void insufficientQuantityIsNotSuggested() {
        Recipe r = recipe("Pancakes", need("flour", 200, "g"));
        assertTrue(suggest(r, have("flour", 100, "g")).isEmpty());
    }

    @Test
    public void exactQuantityIsSuggested() {
        Recipe r = recipe("Pancakes", need("flour", 200, "g"));
        assertEquals(1, suggest(r, have("flour", 200, "g")).size());
    }

    @Test
    public void pluralAndCaseDifferencesStillMatch() {
        Recipe r = recipe("Soup", need("tomato", 4, "pcs"));
        assertEquals(1, suggest(r, have("Tomatoes", 4, "pcs")).size());
    }

    @Test
    public void unitConversionKilogramsSatisfiesGrams() {
        Recipe r = recipe("Pancakes", need("flour", 200, "g"));
        assertEquals(1, suggest(r, have("flour", 1, "kg")).size());
    }

    @Test
    public void unitConversionTablespoonsAndMillilitres() {
        Recipe r = recipe("Dressing", need("olive oil", 30, "ml"));
        assertEquals(1, suggest(r, have("olive oil", 2, "tbsp")).size());
    }

    @Test
    public void incomparableUnitsAreNotMatched() {
        Recipe r = recipe("Pancakes", need("flour", 200, "g"));
        assertTrue(suggest(r, have("flour", 2, "pcs")).isEmpty());
    }

    @Test
    public void duplicatePantryEntriesAreAddedTogether() {
        Recipe r = recipe("Scrambled", need("egg", 3, "pcs"));
        assertEquals(1, suggest(r, have("egg", 2, "pcs"), have("Eggs", 2, "pcs")).size());
    }

    @Test
    public void emptyPantryMatchesNothing() {
        Recipe r = recipe("Scrambled", need("egg", 3, "pcs"));
        assertTrue(suggest(r).isEmpty());
    }

    @Test
    public void normalizeNameHandlesCommonPlurals() {
        assertEquals("tomato", RecipeMatcher.normalizeName("Tomatoes"));
        assertEquals("egg", RecipeMatcher.normalizeName(" EGGS "));
        assertEquals("berry", RecipeMatcher.normalizeName("berries"));
        assertEquals("olive oil", RecipeMatcher.normalizeName("olive oil"));
        assertFalse(RecipeMatcher.normalizeName("cheese").isEmpty());
    }
}