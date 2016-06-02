package com.kyle.mobileplayer.audio;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kyle.mobileplayer.BaseActivity;
import com.kyle.mobileplayer.R;
import com.kyle.mobileplayer.domain.AudioItem;
import com.kyle.mobileplayer.utils.Utils;

public class AudioListActivity extends BaseActivity {
	private ListView lv_audiolist;
	private TextView tv_noaudio;
	private ArrayList<AudioItem> audioItems;
	private Utils utils;
	private static final String TAG = "VALUE";
	/**
	 * Each Handler instance is associated with a single thread and that
	 * thread's message queue.
	 */
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (audioItems != null && audioItems.size() > 0) {
				/**
				 * 加载完成了，就设置�?�配器显示音乐列�?
				 */
				lv_audiolist.setAdapter(new AudioListAdapter());
				tv_noaudio.setVisibility(View.GONE);
			} else {
				tv_noaudio.setVisibility(View.VISIBLE);
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setTitle("本地音乐");
		setRightButon(View.GONE);
		lv_audiolist = (ListView) findViewById(R.id.lv_audiolist);
		tv_noaudio = (TextView) findViewById(R.id.tv_noaudio);
		utils = new Utils();
		getAllAudio();
		/**
		 * 设置音乐列表的监�?
		 */
		lv_audiolist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(AudioListActivity.this,
						AudioPlayerActivity.class);
				intent.putExtra("position", position);
				startActivity(intent);
//				Log.d(TAG, "点击了");
			}
		});
	}

	/**
	 * 设置音乐列表的�?�配�?
	 * 
	 * @author iuc
	 * 
	 */
	private class AudioListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			/**
			 * How many items are in the data set represented by this Adapter.
			 * represent vt. 代表；表现；描绘；回忆；再赠�? vi. 代表；提出异�?
			 */
			return audioItems.size();
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
			View view;
			ViewHolder holder;
			if (convertView != null) {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			} else {
				view = View.inflate(AudioListActivity.this,
						R.layout.audio_list_item, null);
				holder = new ViewHolder();
				holder.tv_name = (TextView) view.findViewById(R.id.tv_name);
				holder.tv_duration = (TextView) view
						.findViewById(R.id.tv_duration);
				holder.tv_size = (TextView) view.findViewById(R.id.tv_size);
				view.setTag(holder);
			}
			AudioItem audioItem = audioItems.get(position);
			holder.tv_name.setText(audioItem.getTitle());
			holder.tv_duration.setText(utils.stringForTime(Integer
					.valueOf(audioItem.getDuration())));
			holder.tv_size.setText(Formatter.formatFileSize(
					AudioListActivity.this, audioItem.getSize()));
			return view;
		}
	}

	static class ViewHolder {
		TextView tv_name;
		TextView tv_duration;
		TextView tv_size;
	}

	private void getAllAudio() {
		// TODO Auto-generated method stub
		new Thread() {
			public void run() {
				audioItems = new ArrayList<AudioItem>();
				ContentResolver resolver = getContentResolver();
				Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				String[] projection = { MediaStore.Audio.Media.TITLE,
						MediaStore.Audio.Media.DURATION,
						MediaStore.Audio.Media.SIZE,
						MediaStore.Audio.Media.DATA,
						MediaStore.Audio.Media.ARTIST };
				Cursor cursor = resolver.query(uri, projection, null, null,
						null);
				while (cursor.moveToNext()) {
					AudioItem item = new AudioItem();
					long size = cursor.getLong(2);
					if (size > 1 * 1024 * 1024) {
						String title = cursor.getString(0);
						item.setTitle(title);
						String duration = cursor.getString(1);
						item.setDuration(duration);
						item.setSize(size);
						String data = cursor.getString(3);
						item.setData(data);
						String artist = cursor.getString(4);
						item.setArtist(artist);

						audioItems.add(item);
					}

				}
				/**
				 * Sends a Message containing only the what value. 加载完成发�?�消�?
				 */
				handler.sendEmptyMessage(0);
			};
		}.start();
	}

	@Override
	public View setContentView() {
		// TODO Auto-generated method stub
		return View.inflate(this, R.layout.activity_audio_list, null);

	}

	@Override
	public void rightButOnclick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void leftButOnclick() {
		// TODO Auto-generated method stub
		finish();
	}

}
