package ba.ima.hepek;

import ba.ima.hepek.R;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

/**
 * This is activity used for sharing Hepek status and image on social networks.
 * 
 * @author amer Change 1: 15-12-2012 - Initial development
 */

public class ShareHepekActivity extends Activity {

	/* List of Facebook friends names used for Auto Complete */
	private String[] facebookFriends;

	/* Returns Facebook sharing switch state */
	private boolean facebookSharingEnabled = false;

	/* Returns Twitter sharing switch state */
	private boolean twitterSharingEnabled = false;

	private ImageView hepekShareImage;

	private AutoCompleteTextView textView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);

		// AutoComplete initialize
		textView = (AutoCompleteTextView) findViewById(R.id.share_description);

		addFacebookAutoComplete();

		// ImageView initialize
		hepekShareImage = (ImageView) findViewById(R.id.hepekShareImage);
		
		View layout = (View) findViewById(R.id.share_activity_layout);
		layout.setBackgroundResource(R.drawable.sharebackground);
		
	}

	/**
	 * Initializes autocomplete feature if Facebook sharing is enabled
	 */
	private void addFacebookAutoComplete() {
		if (facebookSharingEnabled) {

			// Get the string array
			getFacebookContactListArray();

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, facebookFriends);
			textView.setAdapter(adapter);
		}
	}

	/**
	 * Used to get list of Facebook contacts from Facebook Graph API.
	 * 
	 */
	private void getFacebookContactListArray() {

		// TODO: Reimplement
		facebookFriends = new String[] { "Samir", "Amer", "Vedran" };
	}

	/**
	 * Use to change hepek image to be shared on this activity image view;
	 */
	public void changeHepekShareImage(Bitmap bitmap) {
		hepekShareImage.setImageBitmap(bitmap);
	}

	// Get and Set methods

	public boolean isFacebookSharingEnabled() {
		return facebookSharingEnabled;
	}

	public void setFacebookSharingEnabled(boolean facebookSharingEnabled) {
		this.facebookSharingEnabled = facebookSharingEnabled;
	}

	public boolean isTwitterSharingEnabled() {
		return twitterSharingEnabled;
	}

	public void setTwitterSharingEnabled(boolean twitterSharingEnabled) {
		this.twitterSharingEnabled = twitterSharingEnabled;
	}

}