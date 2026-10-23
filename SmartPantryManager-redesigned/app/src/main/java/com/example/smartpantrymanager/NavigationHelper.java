package com.example.smartpantrymanager;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Wires up the bottom navigation bar so every main screen behaves the same way. */
public class NavigationHelper {

    public static void setup(final Activity activity, final int selectedItemId) {
        BottomNavigationView nav = activity.findViewById(R.id.bottomNav);
        nav.setSelectedItemId(selectedItemId);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedItemId) {
                return true; // already on this screen
            }

            Class<?> target;
            if (id == R.id.nav_pantry) {
                target = MainActivity.class;
            } else if (id == R.id.nav_recipes) {
                target = SuggestedRecipesActivity.class;
            } else if (id == R.id.nav_settings) {
                target = SettingsActivity.class;
            } else {
                return false;
            }

            Intent intent = new Intent(activity, target);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

            if (target == MainActivity.class) {
                // Pantry is the home screen: go back to it and clear the screens above it.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
            } else {
                activity.startActivity(intent);
                if (!(activity instanceof MainActivity)) {
                    activity.finish(); // swap one secondary screen for another
                }
            }
            return true;
        });
    }
}