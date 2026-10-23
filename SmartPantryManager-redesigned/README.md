# Smart Pantry Manager

A Java Android app that helps reduce food waste. The user records the ingredients they have at home (their "pantry"), and the app suggests only the recipes they can cook right now, using **strictly** those ingredients and no shopping trip required.

Built for Mobile App Development 700 (Richfield), Practical Assignment.

- **Student:** [Elton Gwanyanya], [402308417]
- **Language / IDE:** Java, Android Studio

## Features

- Pantry management: add, edit and delete ingredients (name, quantity, unit, optional expiry date) with input validation
- Pantry list screen (RecyclerView with a custom adapter) bound to the database
- 16 pre-loaded recipes, seeded into the database on first run
- Suggested Recipes screen using strict matching: a recipe appears only if **every** ingredient is in the pantry in at least the required quantity
- Matching copes with plurals and case ("Tomatoes" = "tomato") and unit differences (1 kg satisfies 200 g)
- Recipe Detail screen with the full ingredient list and method
- Friendly message when no recipes match
- Settings screen: toggle for highlighting ingredients that are expired or expiring within 3 days, and a default-unit preference
- Bottom navigation bar (Pantry, Recipes, Settings)
- Unit tests for the matching logic (RecipeMatcherTest)

## Database choice: SQLite

I used **SQLite** through `SQLiteOpenHelper`. The app is entirely personal and offline, so on-device storage means no accounts, no network and no backend to host, and the data persists after the app is closed. Relational tables fit the data well: a pantry table, a recipes table, and a recipe_ingredients table linked to recipes by a foreign key with cascade delete. Small user options (the Settings screen) are stored with SharedPreferences.

## Project structure

- `DatabaseHelper`: creates the tables, seeds the recipes, and provides CRUD for the pantry
- `RecipeMatcher`: the strict-matching logic, including name normalisation and unit conversion
- `MainActivity`, `AddEditIngredientActivity`, `SuggestedRecipesActivity`, `RecipeDetailActivity`, `SettingsActivity`: the screens
- `IngredientAdapter`, `RecipeAdapter`: RecyclerView adapters
- `NavigationHelper`, `ExpiryUtil`: shared helpers

## Setup and run

1. Install [Android Studio](https://developer.android.com/studio).
2. Clone the repository: `git clone https://github.com/Smallgee3/SmartPantryManager.git`
3. In Android Studio choose **File > Open** and select the cloned folder. Wait for the Gradle sync to finish.
4. Create or start an emulator (**Tools > Device Manager**), or connect a phone with USB debugging enabled.
5. Select the **app** run configuration and click **Run**.
6. To run the unit tests, right-click `RecipeMatcherTest` and choose **Run**.

## Scope

As required by the brief, the app uses no maps, GPS or location services, and has no payment features.

## Screenshots

![Pantry](screenshots/pantry.png)
![Add ingredient](screenshots/add.png)
![Recipes](screenshots/recipes.png)
![Recipe detail](screenshots/detail.png)
![Settings](screenshots/settings.png)