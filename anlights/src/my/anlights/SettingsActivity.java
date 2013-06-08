package my.anlights;

import android.app.Activity;
import android.os.Bundle;
import my.anlights.gui.SettingsFragment;

/**
 * Created by Andreas on 07.06.13.
 */
public class SettingsActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Display the fragment as the main content.
		getFragmentManager().beginTransaction().replace(
				android.R.id.content, new SettingsFragment()
		).commit();
	}
}