package com.theultimatelabs.secretsanta;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ConstraintsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.constraints);
    }
}