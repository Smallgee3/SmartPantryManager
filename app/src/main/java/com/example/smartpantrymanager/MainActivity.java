package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;

/** Pantry List screen: shows every ingredient stored in the database. */
public class MainActivity extends AppCompatActivity implements IngredientAdapter.OnItemActionListener {

    private DatabaseHelper db;
    private IngredientAdapter adapter;
    private TextView tvEmpty, tvBanner, tvCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NavigationHelper.setup(this, R.id.nav_pantry);

        db = new DatabaseHelper(this);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvBanner = findViewById(R.id.tvExpiringBanner);
        tvCount = findViewById(R.id.tvCount);

        RecyclerView rvPantry = findViewById(R.id.rvPantry);
        rvPantry.setLayoutManager(new LinearLayoutManager(this));
        adapter = new IngredientAdapter(this);
        rvPantry.setAdapter(adapter);

        ExtendedFloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));
    }

    /** Reload from the database every time the screen becomes visible again. */
    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    private void loadPantry() {
        boolean alertsOn = SettingsActivity.isExpiringAlertsEnabled(this);
        adapter.setHighlightExpiring(alertsOn);

        List<Ingredient> list = db.getAllIngredients();
        adapter.setItems(list);
        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        tvCount.setText(list.isEmpty() ? "Cook with what you already have"
                : list.size() + (list.size() == 1 ? " ingredient" : " ingredients") + " in your pantry");

        // Count items that are expired or expiring soon, for the warning banner
        int urgent = 0;
        if (alertsOn) {
            for (Ingredient item : list) {
                String expiry = item.getExpiryDate();
                if (expiry != null && !expiry.trim().isEmpty()
                        && ExpiryUtil.daysUntil(expiry) <= ExpiryUtil.SOON_DAYS) {
                    urgent++;
                }
            }
        }
        if (urgent > 0) {
            tvBanner.setText(urgent + (urgent == 1 ? " item is" : " items are")
                    + " expired or expiring within " + ExpiryUtil.SOON_DAYS + " days");
            tvBanner.setVisibility(View.VISIBLE);
        } else {
            tvBanner.setVisibility(View.GONE);
        }
    }

    @Override
    public void onEdit(Ingredient item) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_INGREDIENT_ID, item.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(final Ingredient item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Remove " + item.getName() + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.deleteIngredient(item.getId());
                    loadPantry();
                    Toast.makeText(this, item.getName() + " deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}