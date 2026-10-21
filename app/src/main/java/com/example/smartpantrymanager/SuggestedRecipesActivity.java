package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** Shows only the recipes the user can make right now (strict matching). */
public class SuggestedRecipesActivity extends AppCompatActivity
        implements RecipeAdapter.OnRecipeClickListener {

    private DatabaseHelper db;
    private RecipeAdapter adapter;
    private TextView tvSubtitle, tvEmpty;
    private RecyclerView rvRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        NavigationHelper.setup(this, R.id.nav_recipes);

        db = new DatabaseHelper(this);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvRecipes = findViewById(R.id.rvRecipes);

        rvRecipes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this);
        rvRecipes.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions();
    }

    /** Runs the strict-matching logic against the current pantry and shows the result. */
    private void loadSuggestions() {
        List<Ingredient> pantry = db.getAllIngredients();
        List<Recipe> allRecipes = db.getAllRecipes();
        List<Recipe> suggested = RecipeMatcher.findSuggestedRecipes(allRecipes, pantry);

        adapter.setRecipes(suggested);

        if (suggested.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvRecipes.setVisibility(View.GONE);
            tvSubtitle.setText("0 of " + allRecipes.size() + " recipes match your pantry");
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvRecipes.setVisibility(View.VISIBLE);
            tvSubtitle.setText(suggested.size() + " of " + allRecipes.size()
                    + " recipes you can cook right now");
        }
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}