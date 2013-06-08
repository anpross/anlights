package my.anlights.gui;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import my.anlights.R;

/**
 * Created by Andreas on 07.06.13.
 */
public class SettingsFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
	}
}