package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/** Shows the full ingredient list and method for one recipe. */
public class RecipeDetailActivity extends AppCompatActivity {

    /** Key used to pass the id of the recipe to display. */
    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = new DatabaseHelper(this).getRecipe(recipeId);

        if (recipe == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvTitle = findViewById(R.id.tvRecipeTitle);
        TextView tvIngredients = findViewById(R.id.tvIngredients);
        TextView tvSteps = findViewById(R.id.tvSteps);

        tvTitle.setText(recipe.getName());

        StringBuilder sb = new StringBuilder();
        for (RecipeIngredient ing : recipe.getIngredients()) {
            sb.append("\u2022 ")
                    .append(formatQuantity(ing.getQuantity()))
                    .append(" ")
                    .append(ing.getUnit())
                    .append(" ")
                    .append(ing.getName())
                    .append("\n");
        }
        tvIngredients.setText(sb.toString().trim());
        tvSteps.setText(recipe.getSteps());
    }

    private static String formatQuantity(double q) {
        if (q == Math.floor(q)) {
            return String.valueOf((long) q);
        }
        return String.valueOf(q);
    }
}