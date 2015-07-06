package it.jaschke.alexandria;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by saj on 27/01/15.
 */
//Abhilash:Changed from PreferenceActivity to Preference Fragment to display the actionbar in settings menu
public class SettingsActivity extends ActionBarActivity {

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,new SettingsFragment()).commit();
    }
}
