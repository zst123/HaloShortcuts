package com.zst.app.haloshortcuts;

import com.zst.app.haloshortcuts.adapter.AppAdapter;
import com.zst.app.haloshortcuts.adapter.AppAdapter.AppItem;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	static boolean isSystemShortcutSelector = false;
	static AppAdapter mAppAdapter;
	static ListView mListView;
	static View mProgressBar;
	static ImageView dialogIconView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (getIntent().getExtras() != null) {
			isSystemShortcutSelector = true;
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mListView = (ListView) findViewById(R.id.listView1);
		mProgressBar = findViewById(R.id.progressBar1);
		mAppAdapter = new AppAdapter(this, mProgressBar);
		mListView.setAdapter(mAppAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				AppItem info = (AppItem) parent.getItemAtPosition(pos);
				showAppInfo(info.packageName, info.title.toString(), info.icon);
			}
		});
	}
	
	private void showAppInfo(final String packageName, final String title, final Drawable icon) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle(R.string.dialog_title);
		
		LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = li.inflate(R.layout.dialog_app_info, null);
		builder.setView(view);
		
		dialogIconView = (ImageView) view.findViewById(R.id.icon_selector);
		final EditText titleView = (EditText) view.findViewById(R.id.label_info);
		final TextView pkgView = (TextView) view.findViewById(R.id.pkg_info);
		
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface di, int which) {
				if (isLoading()) return;
				
				try {
					addShortcut(getFloatingIntent(packageName), titleView.getText().toString(),
							dialogIconView.getDrawable());
				} catch (Exception e) {
					Toast.makeText(getBaseContext(),
							getResources().getString(R.string.get_intent_unable) + e.toString(),
							Toast.LENGTH_LONG).show();
				}
			}
		});
		
		pkgView.setText(packageName);
		titleView.setText(title);
		if (icon != null) {
			dialogIconView.setImageDrawable(icon);
		}
		/*
		 * dialogIconView.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override
		 * public void onClick(View arg0) {
		 * Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		 * intent.setType("image/*");
		 * intent.putExtra("crop", "true");
		 * intent.putExtra("scale", true);
		 * intent.putExtra("scaleUpIfNeeded", false);
		 * intent.putExtra("aspectX", 1);
		 * intent.putExtra("aspectY", 1);
		 * intent.putExtra("outputX", 162);
		 * intent.putExtra("outputY", 162);
		 * intent.addCategory(Intent.CATEGORY_OPENABLE);
		 * startActivityForResult(intent, Common.CODE_REQUEST_IMAGE);
		 * }
		 * });
		 */
		// TODO Choose Custom Icon on Click
		// Currently AlertDialog will close when we return and it will give
		// NullPointerException
		builder.show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				if (!isLoading()) {
					mAppAdapter.getFilter().filter(query);
				}
				return true;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				if (!isLoading()) {
					if (newText.equals("")) {
						mAppAdapter.getFilter().filter("");
					}
				}
				return true;
			}
		});
		return true;
	}
	
	private boolean isLoading() {
		return (mProgressBar.getVisibility() == View.VISIBLE);
	}
	
	private Intent getFloatingIntent(String pkg) {
		try {
			Intent intent = new Intent(getPackageManager().getLaunchIntentForPackage(pkg));
			intent.addFlags(Common.FLAG_FLOATING_WINDOW);
			return intent;
		} catch (Exception e) {
			Toast.makeText(this, getResources().getString(R.string.get_intent_unlaunchable),
					Toast.LENGTH_LONG).show();
		} // when intent is null, app is not a Launcher app
		return null;
	}
	
	private void addShortcut(Intent shortcutIntent, String name, Drawable icon) throws Exception {
		shortcutIntent.setAction(Intent.ACTION_MAIN);
		// this is a broadcast to send to Android API our target intent
		Intent addIntent = new Intent();
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		// Place target intent in extras to let broadcast get target intent
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
		// let broadcast get the name of Shortcut
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, ((BitmapDrawable) icon).getBitmap());
		// Hack to Convert drawable to bitmapdrawable to bitmap & add in extras
		if (isSystemShortcutSelector) {
			setResult(Common.CODE_REQUEST_SHORTCUT, addIntent);
			finish();
			// send intent to ShortcutReceiver to continue
		} else {
			addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
			getApplicationContext().sendBroadcast(addIntent);
			// Send broadcast to add shortcut on homescreen.
			// This needs INSTALL_SHORTCUT permission in Manifest!
		}
		String msg = getResources().getString(R.string.system_shortcut_success) + "\n"
				+ shortcutIntent.getPackage();
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
}
