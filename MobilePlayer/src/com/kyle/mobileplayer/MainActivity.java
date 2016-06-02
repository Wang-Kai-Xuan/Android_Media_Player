package com.kyle.mobileplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.kyle.mobileplayer.audio.AudioListActivity;
import com.kyle.mobileplayer.video.VideoListActivity;

public class MainActivity extends BaseActivity {
	private GridView gridView;

	private int[] ids = { R.drawable.video, R.drawable.audio};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setleftButon(View.GONE);
		setTitle("手机影音播放器");

		gridView = (GridView) findViewById(R.id.gridview);
		gridView.setAdapter(new MyMainAdapter());
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Intent intent;
				switch (position) {
				case 0:
					intent = new Intent(MainActivity.this,
							VideoListActivity.class);
					startActivity(intent);
					break;
				case 1:
					intent = new Intent(MainActivity.this,
							AudioListActivity.class);
					startActivity(intent);
					break;
				default:
					Toast.makeText(getApplicationContext(), "该模块还没有实现", 0)
							.show();
					break;
				}
			}
		});
	}

	private class MyMainAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return ids.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View view;
			ViewHolder holder;
			if (convertView != null) {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			} else {
				view = View
						.inflate(MainActivity.this, R.layout.main_item, null);
				holder = new ViewHolder();
				// 不能少写view
				holder.iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
				 /**
			     * Sets the tag associated with this view. A tag can be used to mark
			     * a view in its hierarchy and does not have to be unique within the
			     * hierarchy. Tags can also be used to store data within a view without
			     * resorting to another data structure.
			     */
				view.setTag(holder);
			}

			holder.iv_icon.setImageResource(ids[position]);
			return view;
		}

	};

	static class ViewHolder {
		ImageView iv_icon;
	}

	@Override
	public void rightButOnclick() {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), "你点击了右边按钮", Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public void leftButOnclick() {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), "你点击了左边按钮", Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public View setContentView() {
		// TODO Auto-generated method stub
		return View.inflate(this, R.layout.activity_main, null);

	}
}
