package xyz.z3ntu.minesweeper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

/**
 * Written by Luca Weiss (z3ntu)
 * https://github.com/z3ntu
 */
public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }


    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            EditTextPreference edit = (EditTextPreference) findPreference("rows");
            edit.setSummary(edit.getText());
            edit = (EditTextPreference) findPreference("columns");
            edit.setSummary(edit.getText());
            edit = (EditTextPreference) findPreference("bombs");
            edit.setSummary(edit.getText());
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            Preference pref = findPreference(s);
            System.out.println("ON SHARED PREFERENCE CHANGED");
            if (pref instanceof EditTextPreference) {
                EditTextPreference editTextPreference = (EditTextPreference) pref;
                editTextPreference.setSummary(editTextPreference.getText());
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }
    }
}
