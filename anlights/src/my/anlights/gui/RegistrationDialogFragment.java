package my.anlights.gui;

import android.app.*;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import my.anlights.CallbackListener;
import my.anlights.HueRegistrationTask;
import my.anlights.R;
import my.anlights.data.HueBridge;
import my.anlights.util.MyLog;

/**
 * Created by Andreas on 15.06.13.
 */
public class RegistrationDialogFragment extends DialogFragment implements CallbackListener<Boolean> {


	private HueBridge bridge = null;
	private RegistrationDialogListener dialogListener;
	private ProgressBar progressBar;
	HueRegistrationTask task;

	private static final String CLASS_NAME = RegistrationDialogFragment.class.getCanonicalName();

	/**
	 * Create a new instance of MyDialogFragment, providing "num"
	 * as an argument.
	 */
	public static RegistrationDialogFragment newInstance() {
		RegistrationDialogFragment f = new RegistrationDialogFragment();

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the host
			dialogListener = (RegistrationDialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString() + " must implement RegistrationDialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		MyLog.entering(CLASS_NAME, "onCreateDialog");
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		View v = inflater.inflate(R.layout.fragment_registration_dialog, null);
		builder.setView(v);
		builder.setTitle(R.string.register_dialog_title);

		progressBar = (ProgressBar) v.findViewById(R.id.progressBar);

		task = new HueRegistrationTask(progressBar, bridge, this);

		task.execute();

		builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				MyLog.i("negative button pressed");
				task.cancel(false);
				dialogListener.onDialogRegistrationCancel(RegistrationDialogFragment.this);
			}
		});

		AlertDialog dialog = builder.create();
		MyLog.exiting(CLASS_NAME, "onCreateDialog", dialog);
		return dialog;
	}

	public void setBridge(HueBridge bridge) {
		this.bridge = bridge;
	}

	/**
	 * gets called once the Task finishes
	 *
	 * @param registrationSuccessful
	 */
	@Override
	public void callback(Boolean registrationSuccessful) {

		if (registrationSuccessful) {
			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			transaction.remove(this);
			transaction.commit();
			dialogListener.onDialogRegistrationSuccess();
		}


	}

	public interface RegistrationDialogListener {
		public void onDialogRegistrationCancel(DialogFragment dialog);

		public void onDialogRegistrationSuccess();
	}
}
