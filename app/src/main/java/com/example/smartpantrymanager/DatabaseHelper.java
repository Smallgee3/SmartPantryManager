package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles creation of the SQLite database, seeding of recipes,
 * and CRUD operations for the pantry.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smartpantry.db";
    private static final int DB_VERSION = 1;

    // Pantry table
    public static final String T_PANTRY = "pantry";
    public static final String P_ID = "id";
    public static final String P_NAME = "name";
    public static final String P_QTY = "quantity";
    public static final String P_UNIT = "unit";
    public static final String P_EXPIRY = "expiry_date";

    // Recipes table
    public static final String T_RECIPES = "recipes";
    public static final String R_ID = "id";
    public static final String R_NAME = "name";
    public static final String R_STEPS = "steps";

    // Recipe ingredients table
    public static final String T_RI = "recipe_ingredients";
    public static final String RI_ID = "id";
    public static final String RI_RECIPE_ID = "recipe_id";
    public static final String RI_NAME = "name";
    public static final String RI_QTY = "quantity";
    public static final String RI_UNIT = "unit";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true); // needed for ON DELETE CASCADE
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_PANTRY + " ("
                + P_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + P_NAME + " TEXT NOT NULL, "
                + P_QTY + " REAL NOT NULL, "
                + P_UNIT + " TEXT NOT NULL, "
                + P_EXPIRY + " TEXT)");

        db.execSQL("CREATE TABLE " + T_RECIPES + " ("
                + R_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + R_NAME + " TEXT NOT NULL, "
                + R_STEPS + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + T_RI + " ("
                + RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + RI_RECIPE_ID + " INTEGER NOT NULL, "
                + RI_NAME + " TEXT NOT NULL, "
                + RI_QTY + " REAL NOT NULL, "
                + RI_UNIT + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + RI_RECIPE_ID + ") REFERENCES "
                + T_RECIPES + "(" + R_ID + ") ON DELETE CASCADE)");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_RI);
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + T_PANTRY);
        onCreate(db);
    }

    // ===================== PANTRY CRUD =====================

    /** CREATE: adds a pantry item, returns the new row id (or -1 on failure). */
    public long addIngredient(Ingredient item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(T_PANTRY, null, toValues(item));
    }

    /** READ: returns every pantry item, sorted by name. */
    public List<Ingredient> getAllIngredients() {
        List<Ingredient> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(T_PANTRY, null, null, null, null, null,
                P_NAME + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                list.add(fromCursor(c));
            }
        }
        return list;
    }

    /** READ: returns one pantry item by id, or null if not found. */
    public Ingredient getIngredient(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(T_PANTRY, null, P_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (c.moveToFirst()) {
                return fromCursor(c);
            }
        }
        return null;
    }

    /** UPDATE: saves changes to an existing pantry item, returns rows affected. */
    public int updateIngredient(Ingredient item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update(T_PANTRY, toValues(item), P_ID + "=?",
                new String[]{String.valueOf(item.getId())});
    }

    /** DELETE: removes a pantry item, returns rows affected. */
    public int deleteIngredient(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(T_PANTRY, P_ID + "=?", new String[]{String.valueOf(id)});
    }

    private ContentValues toValues(Ingredient item) {
        ContentValues v = new ContentValues();
        v.put(P_NAME, item.getName());
        v.put(P_QTY, item.getQuantity());
        v.put(P_UNIT, item.getUnit());
        v.put(P_EXPIRY, item.getExpiryDate());
        return v;
    }

    private Ingredient fromCursor(Cursor c) {
        return new Ingredient(
                c.getLong(c.getColumnIndexOrThrow(P_ID)),
                c.getString(c.getColumnIndexOrThrow(P_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(P_QTY)),
                c.getString(c.getColumnIndexOrThrow(P_UNIT)),
                c.getString(c.getColumnIndexOrThrow(P_EXPIRY)));
    }

    // ===================== RECIPE READS =====================

    /** Returns all recipes, each with its ingredient list loaded. */
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(T_RECIPES, null, null, null, null, null,
                R_NAME + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                long id = c.getLong(c.getColumnIndexOrThrow(R_ID));
                recipes.add(new Recipe(id,
                        c.getString(c.getColumnIndexOrThrow(R_NAME)),
                        c.getString(c.getColumnIndexOrThrow(R_STEPS)),
                        getRecipeIngredients(id)));
            }
        }
        return recipes;
    }

    /** Returns a single recipe by id, or null if not found. */
    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(T_RECIPES, null, R_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (c.moveToFirst()) {
                return new Recipe(id,
                        c.getString(c.getColumnIndexOrThrow(R_NAME)),
                        c.getString(c.getColumnIndexOrThrow(R_STEPS)),
                        getRecipeIngredients(id));
            }
        }
        return null;
    }

    private List<RecipeIngredient> getRecipeIngredients(long recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(T_RI, null, RI_RECIPE_ID + "=?",
                new String[]{String.valueOf(recipeId)}, null, null, null)) {
            while (c.moveToNext()) {
                list.add(new RecipeIngredient(
                        c.getString(c.getColumnIndexOrThrow(RI_NAME)),
                        c.getDouble(c.getColumnIndexOrThrow(RI_QTY)),
                        c.getString(c.getColumnIndexOrThrow(RI_UNIT))));
            }
        }
        return list;
    }

    // ===================== SEED DATA =====================
    // Ingredient format: {name (singular, lowercase), quantity, unit}

    private void seedRecipes(SQLiteDatabase db) {
        insertRecipe(db, "Scrambled Eggs",
                "1. Whisk the eggs with salt.\n2. Melt butter in a pan.\n3. Pour in eggs and stir gently until just set.",
                new String[][]{{"egg", "3", "pcs"}, {"butter", "10", "g"}, {"salt", "1", "tsp"}});

        insertRecipe(db, "Cheese Omelette",
                "1. Whisk the eggs.\n2. Melt butter in a pan and pour in eggs.\n3. Add grated cheese, fold and cook until golden.",
                new String[][]{{"egg", "3", "pcs"}, {"cheese", "50", "g"}, {"butter", "10", "g"}});

        insertRecipe(db, "Tomato Pasta",
                "1. Boil the pasta until tender.\n2. Fry chopped onion and garlic in oil.\n3. Add chopped tomatoes and simmer 10 minutes.\n4. Toss with the pasta.",
                new String[][]{{"pasta", "200", "g"}, {"tomato", "4", "pcs"}, {"onion", "1", "pcs"},
                        {"garlic", "2", "pcs"}, {"olive oil", "2", "tbsp"}});

        insertRecipe(db, "French Toast",
                "1. Whisk eggs, milk and sugar.\n2. Dip the bread slices.\n3. Fry in a pan until golden on both sides.",
                new String[][]{{"bread", "4", "pcs"}, {"egg", "2", "pcs"}, {"milk", "100", "ml"}, {"sugar", "1", "tbsp"}});

        insertRecipe(db, "Pancakes",
                "1. Mix flour, sugar, eggs and milk into a smooth batter.\n2. Pour small rounds into a hot pan.\n3. Flip when bubbles form and cook until golden.",
                new String[][]{{"flour", "200", "g"}, {"milk", "300", "ml"}, {"egg", "2", "pcs"}, {"sugar", "2", "tbsp"}});

        insertRecipe(db, "Cheese Toastie",
                "1. Butter the outside of the bread.\n2. Fill with cheese.\n3. Toast in a pan until golden and melted.",
                new String[][]{{"bread", "2", "pcs"}, {"cheese", "60", "g"}, {"butter", "10", "g"}});

        insertRecipe(db, "Egg Fried Rice",
                "1. Scramble the eggs and set aside.\n2. Fry onion in oil.\n3. Add cooked rice and soy sauce.\n4. Stir in the eggs and serve.",
                new String[][]{{"rice", "2", "cup"}, {"egg", "2", "pcs"}, {"onion", "1", "pcs"},
                        {"soy sauce", "2", "tbsp"}, {"oil", "1", "tbsp"}});

        insertRecipe(db, "Mashed Potatoes",
                "1. Boil peeled potatoes until soft.\n2. Drain and mash with butter and milk.\n3. Season with salt.",
                new String[][]{{"potato", "4", "pcs"}, {"butter", "30", "g"}, {"milk", "50", "ml"}, {"salt", "1", "tsp"}});

        insertRecipe(db, "Chicken Stir-Fry",
                "1. Slice chicken, onion and carrots.\n2. Fry chicken in oil until cooked.\n3. Add vegetables and soy sauce.\n4. Stir-fry 5 minutes.",
                new String[][]{{"chicken", "300", "g"}, {"onion", "1", "pcs"}, {"carrot", "2", "pcs"},
                        {"soy sauce", "2", "tbsp"}, {"oil", "1", "tbsp"}});

        insertRecipe(db, "Tuna Pasta",
                "1. Boil the pasta.\n2. Fry chopped onion in olive oil.\n3. Add the tuna and warm through.\n4. Toss with the pasta.",
                new String[][]{{"pasta", "200", "g"}, {"tuna", "1", "pcs"}, {"onion", "1", "pcs"}, {"olive oil", "1", "tbsp"}});

        insertRecipe(db, "Vegetable Soup",
                "1. Chop potatoes, carrots and onion.\n2. Add to a pot with the stock.\n3. Simmer for 25 minutes until soft.",
                new String[][]{{"potato", "2", "pcs"}, {"carrot", "2", "pcs"}, {"onion", "1", "pcs"},
                        {"vegetable stock", "500", "ml"}});

        insertRecipe(db, "Banana Pancakes",
                "1. Mash the bananas.\n2. Mix with eggs and flour.\n3. Fry spoonfuls in a pan until golden.",
                new String[][]{{"banana", "2", "pcs"}, {"egg", "2", "pcs"}, {"flour", "100", "g"}});

        insertRecipe(db, "Banana Oatmeal",
                "1. Heat oats and milk in a pot, stirring.\n2. Cook until thick.\n3. Top with sliced banana and honey.",
                new String[][]{{"oat", "1", "cup"}, {"milk", "250", "ml"}, {"banana", "1", "pcs"}, {"honey", "1", "tbsp"}});

        insertRecipe(db, "Egg Noodles",
                "1. Boil the noodles and drain.\n2. Scramble eggs in a hot pan with oil.\n3. Add noodles and soy sauce and toss.",
                new String[][]{{"noodle", "200", "g"}, {"egg", "2", "pcs"}, {"soy sauce", "2", "tbsp"}, {"oil", "1", "tbsp"}});

        insertRecipe(db, "Garlic Bread",
                "1. Mash butter with crushed garlic.\n2. Spread on the bread.\n3. Bake at 180°C for 10 minutes.",
                new String[][]{{"bread", "4", "pcs"}, {"butter", "40", "g"}, {"garlic", "3", "pcs"}});

        insertRecipe(db, "Tomato Soup",
                "1. Fry onion and garlic.\n2. Add chopped tomatoes and stock.\n3. Simmer 20 minutes and blend until smooth.",
                new String[][]{{"tomato", "6", "pcs"}, {"onion", "1", "pcs"}, {"garlic", "2", "pcs"},
                        {"vegetable stock", "500", "ml"}});
    }

    private void insertRecipe(SQLiteDatabase db, String name, String steps, String[][] ingredients) {
        ContentValues rv = new ContentValues();
        rv.put(R_NAME, name);
        rv.put(R_STEPS, steps);
        long recipeId = db.insert(T_RECIPES, null, rv);

        for (String[] ing : ingredients) {
            ContentValues iv = new ContentValues();
            iv.put(RI_RECIPE_ID, recipeId);
            iv.put(RI_NAME, ing[0]);
            iv.put(RI_QTY, Double.parseDouble(ing[1]));
            iv.put(RI_UNIT, ing[2]);
            db.insert(T_RI, null, iv);
        }
    }
}
