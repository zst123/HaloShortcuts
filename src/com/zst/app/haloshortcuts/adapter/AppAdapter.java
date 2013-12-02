package com.zst.app.haloshortcuts.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.zst.app.haloshortcuts.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

public class AppAdapter extends BaseAdapter implements Filterable {
	
	View mProgressBar;
	Context mContext;
	Handler mHandler;
	PackageManager mPackageManager;
	LayoutInflater mLayoutInflater;
	
	protected List<PackageInfo> mInstalledAppInfo;
	protected List<AppItem> mInstalledApps = new LinkedList<AppItem>();
	protected List<PackageInfo> mTemporarylist;
	
	// temp. list holding the filtered items
	
	public AppAdapter(Context context, View progress_bar) {
		mProgressBar = progress_bar;
		mContext = context;
		mHandler = new Handler();
		mPackageManager = mContext.getPackageManager();
		mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mInstalledAppInfo = mPackageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);
		mTemporarylist = mInstalledAppInfo;
		update();
	}
	
	public void update() {
		toggleProgressBarVisible(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (mInstalledApps) {
					mInstalledApps.clear();
					for (PackageInfo info : mTemporarylist) {
						final AppItem item = new AppItem();
						item.title = info.applicationInfo.loadLabel(mPackageManager);
						item.icon = info.applicationInfo.loadIcon(mPackageManager);
						item.packageName = info.packageName;
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								final int index = Collections.binarySearch(mInstalledApps, item);
								if (index < 0) {
									mInstalledApps.add((-index - 1), item);
									notifyDataSetChanged();
								}
							}
						});
					}
					toggleProgressBarVisible(false);
				}
			}
		}).start();
	}
	
	private void toggleProgressBarVisible(final boolean b) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (mProgressBar != null) {
					mProgressBar.setVisibility(b ? View.VISIBLE : View.GONE);
				}
			}
		});
	}
	
	@Override
	public int getCount() {
		return mInstalledApps.size();
	}
	
	@Override
	public AppItem getItem(int position) {
		return mInstalledApps.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return mInstalledApps.get(position).hashCode();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView != null) {
			holder = (ViewHolder) convertView.getTag();
		} else {
			convertView = mLayoutInflater.inflate(R.layout.view_app_list, null, false);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.icon = (ImageView) convertView.findViewById(android.R.id.icon);
			holder.pkg = (TextView) convertView.findViewById(R.id.pkg);
			convertView.setTag(holder);
		}
		AppItem appInfo = getItem(position);
		
		holder.name.setText(appInfo.title);
		holder.pkg.setText(appInfo.packageName);
		holder.icon.setImageDrawable(appInfo.icon);
		return convertView;
	}
	
	@Override
	public Filter getFilter() {
		return new Filter() {
			@Override
			@SuppressWarnings("unchecked")
			protected void publishResults(CharSequence constraint, FilterResults results) {
				mTemporarylist = (List<PackageInfo>) results.values;
				update();
			}
			
			@Override
			@SuppressLint("DefaultLocale")
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();
				ArrayList<PackageInfo> FilteredList = new ArrayList<PackageInfo>();
				
				if (TextUtils.isEmpty(constraint)) {
					// No filter implemented we return all the list
					results.values = mInstalledAppInfo;
					results.count = mInstalledAppInfo.size();
					return results;
				}
				
				for (int i = 0; i < mInstalledAppInfo.size(); i++) {
					String filterText = constraint.toString().toLowerCase();
					try {
						PackageInfo data = mInstalledAppInfo.get(i);
						if (data.applicationInfo.loadLabel(mPackageManager).toString()
								.toLowerCase().contains(filterText)) {
							FilteredList.add(data);
						} else if (data.packageName.contains(filterText)) {
							FilteredList.add(data);
						}
					} catch (Exception e) {
					}
				}
				results.values = FilteredList;
				results.count = FilteredList.size();
				return results;
			}
		};
	}
	
	public class AppItem implements Comparable<AppItem> {
		public CharSequence title;
		public String packageName;
		public Drawable icon;
		
		@Override
		public int compareTo(AppItem another) {
			return this.title.toString().compareTo(another.title.toString());
		}
	}
	
	static class ViewHolder {
		TextView name;
		ImageView icon;
		TextView pkg;
	}
}
