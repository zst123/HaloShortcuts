package com.zst.app.haloshortcuts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class ShortcutReceiver extends Activity {
	@Override
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		Intent main = new Intent(this, MainActivity.class);
		main.putExtra(Common.KEY_SYSTEM_SHORTCUT, 0);
		// Place value so MainActivity will return result.
		startActivityForResult(main, Common.CODE_REQUEST_SHORTCUT);
	}
	
	@Override
	protected void onActivityResult(int args1, int args2, Intent intent) {
		if (intent != null) {
			setResult(RESULT_OK, intent); // send intent to system
		} else {
			Toast.makeText(this, getResources().getString(R.string.system_shortcut_fail),
					Toast.LENGTH_SHORT).show();
		}
		finish();
	}
}
