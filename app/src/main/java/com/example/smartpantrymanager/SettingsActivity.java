package com.example.smartpantrymanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

/** Settings screen. Options are stored with SharedPreferences. */
public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS = "pantry_settings";
    private static final String KEY_EXPIRING = "expiring_alerts";
    private static final String KEY_DEFAULT_UNIT = "default_unit";

    // Same units as the Add/Edit screen
    private static final String[] UNITS = {"g", "kg", "ml", "l", "pcs", "tsp", "tbsp", "cup"};

    /** True if expiring-soon highlighting is switched on (default: on). */
    public static boolean isExpiringAlertsEnabled(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_EXPIRING, true);
    }

    /** The unit pre-selected when adding a new ingredient (default: pcs). */
    public static String getDefaultUnit(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_DEFAULT_UNIT, "pcs");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        NavigationHelper.setup(this, R.id.nav_settings);

        final SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        // Toggle: highlight ingredients that are expiring soon
        SwitchCompat swExpiring = findViewById(R.id.swExpiring);
        swExpiring.setChecked(isExpiringAlertsEnabled(this));
        swExpiring.setOnCheckedChangeListener((button, checked) ->
                prefs.edit().putBoolean(KEY_EXPIRING, checked).apply());

        // Dropdown: default unit for new ingredients
        Spinner spDefaultUnit = findViewById(R.id.spDefaultUnit);
        spDefaultUnit.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, UNITS));

        String current = getDefaultUnit(this);
        for (int i = 0; i < UNITS.length; i++) {
            if (UNITS[i].equals(current)) {
                spDefaultUnit.setSelection(i);
                break;
            }
        }

        spDefaultUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.edit().putString(KEY_DEFAULT_UNIT, UNITS[position]).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}