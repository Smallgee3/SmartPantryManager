package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

/**
 * Add/Edit Ingredient screen. If the Intent carries an ingredient id,
 * the screen opens in edit mode; otherwise it creates a new item.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    /** Key used to pass the id of the ingredient being edited. */
    public static final String EXTRA_INGREDIENT_ID = "ingredient_id";

    private static final String[] UNITS = {"g", "kg", "ml", "l", "pcs", "tsp", "tbsp", "cup"};

    private DatabaseHelper db;
    private TextInputLayout tilName, tilQuantity;
    private TextInputEditText etName, etQuantity, etExpiry;
    private Spinner spUnit;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        db = new DatabaseHelper(this);

        TextView tvHeading = findViewById(R.id.tvHeading);
        tilName = findViewById(R.id.tilName);
        tilQuantity = findViewById(R.id.tilQuantity);
        etName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        etExpiry = findViewById(R.id.etExpiry);
        spUnit = findViewById(R.id.spUnit);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, UNITS);
        spUnit.setAdapter(unitAdapter);
        // Pre-select the default unit chosen on the Settings screen
        String defaultUnit = SettingsActivity.getDefaultUnit(this);
        for (int i = 0; i < UNITS.length; i++) {
            if (UNITS[i].equals(defaultUnit)) {
                spUnit.setSelection(i);
                break;
            }
        }

        // Read the Intent: if an id was passed, we are editing an existing item.
        editId = getIntent().getLongExtra(EXTRA_INGREDIENT_ID, -1);
        if (editId != -1) {
            Ingredient existing = db.getIngredient(editId);
            if (existing != null) {
                tvHeading.setText("Edit Ingredient");
                etName.setText(existing.getName());
                etQuantity.setText(formatQuantity(existing.getQuantity()));
                etExpiry.setText(existing.getExpiryDate());
                for (int i = 0; i < UNITS.length; i++) {
                    if (UNITS[i].equals(existing.getUnit())) {
                        spUnit.setSelection(i);
                        break;
                    }
                }
            } else {
                editId = -1; // item no longer exists, fall back to add mode
            }
        }

        etExpiry.setOnClickListener(v -> showDatePicker());
        etExpiry.setOnLongClickListener(v -> {
            etExpiry.setText("");
            return true;
        });

        btnSave.setOnClickListener(v -> saveIngredient());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                etExpiry.setText(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    /** Validates the form; saves to the database only if every field is valid. */
    private void saveIngredient() {
        tilName.setError(null);
        tilQuantity.setError(null);

        String name = etName.getText() == null ? "" : etName.getText().toString().trim();
        String qtyText = etQuantity.getText() == null ? "" : etQuantity.getText().toString().trim();
        String expiry = etExpiry.getText() == null ? "" : etExpiry.getText().toString().trim();
        String unit = (String) spUnit.getSelectedItem();

        boolean valid = true;

        if (name.isEmpty()) {
            tilName.setError("Please enter an ingredient name");
            valid = false;
        } else if (!name.matches("[\\p{L} '\\-]+")) {
            tilName.setError("Use letters only (no numbers or symbols)");
            valid = false;
        }

        double quantity = 0;
        if (qtyText.isEmpty()) {
            tilQuantity.setError("Please enter a quantity");
            valid = false;
        } else {
            try {
                quantity = Double.parseDouble(qtyText);
                if (quantity <= 0) {
                    tilQuantity.setError("Quantity must be greater than 0");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                tilQuantity.setError("Enter a valid number");
                valid = false;
            }
        }

        if (!valid) {
            return; // stay on the form so the user can correct the errors
        }

        String expiryToSave = expiry.isEmpty() ? null : expiry;

        if (editId == -1) {
            db.addIngredient(new Ingredient(0, name, quantity, unit, expiryToSave));
            Toast.makeText(this, name + " added", Toast.LENGTH_SHORT).show();
        } else {
            db.updateIngredient(new Ingredient(editId, name, quantity, unit, expiryToSave));
            Toast.makeText(this, name + " updated", Toast.LENGTH_SHORT).show();
        }

        setResult(RESULT_OK);
        finish(); // returns to the pantry list, whose onResume() reloads the data
    }

    private static String formatQuantity(double q) {
        if (q == Math.floor(q)) {
            return String.valueOf((long) q);
        }
        return String.valueOf(q);
    }
}