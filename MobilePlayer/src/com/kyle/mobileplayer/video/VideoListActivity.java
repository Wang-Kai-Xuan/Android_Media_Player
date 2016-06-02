package com.kyle.mobileplayer.video;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kyle.mobileplayer.BaseActivity;
import com.kyle.mobileplayer.R;
import com.kyle.mobileplayer.domain.VideoItem;
import com.kyle.mobileplayer.utils.Utils;

public class VideoListActivity extends BaseActivity {
	private ListView lv_videolist;
	private TextView tv_novideo;
	private ArrayList<VideoItem> videoItems;
	private Utils utils;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (videoItems != null && videoItems.size() > 0) {
				lv_videolist.setAdapter(new VideoListAdapter());
				tv_novideo.setVisibility(View.GONE);
			} else {
				tv_novideo.setVisibility(View.VISIBLE);
			}

		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setTitle("±æµÿ ”∆µ");
		setRightButon(View.GONE);
		lv_videolist = (ListView) findViewById(R.id.lv_videolist);
		tv_novideo = (TextView) findViewById(R.id.tv_novideo);
		utils = new Utils();
		getAllVideo();
		lv_videolist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				// VideoItem videoItem = videoItems.get(position);
				// Intent intent = new
				// Intent(VideoListActivity.this,VideoPlayerActivity.class);
				// intent.setData(Uri.parse(videoItem.getData()));
				// startActivity(intent);
				// Toast.makeText(getApplicationContext(), videoItem.getTitle(),
				// 0).show();
				Intent intent = new Intent(VideoListActivity.this,
						VideoPlayerActivity.class);
				Bundle extras = new Bundle();
				extras.putSerializable("videolist", videoItems);
				intent.putExtras(extras);
				intent.putExtra("position", position);
				startActivity(intent);
			}
		});
	}

	private class VideoListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return videoItems.size();
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
				view = View.inflate(VideoListActivity.this,
						R.layout.voide_list_item, null);
				holder = new ViewHolder();
				holder.tv_name = (TextView) view.findViewById(R.id.tv_name);
				holder.tv_duration = (TextView) view
						.findViewById(R.id.tv_duration);
				holder.tv_size = (TextView) view.findViewById(R.id.tv_size);
				view.setTag(holder);
			}
			VideoItem videoItem = videoItems.get(position);
			holder.tv_name.setText(videoItem.getTitle());

			holder.tv_duration.setText(utils.stringForTime(Integer
					.valueOf(videoItem.getDuration())));

			holder.tv_size.setText(Formatter.formatFileSize(
					VideoListActivity.this, videoItem.getSize()));
			return view;
		}

	}

	static class ViewHolder {
		TextView tv_name;
		TextView tv_duration;
		TextView tv_size;
	}

	private void getAllVideo() {
		// TODO Auto-generated method stub
		new Thread() {
			public void run() {
				videoItems = new ArrayList<VideoItem>();
				ContentResolver resolver = getContentResolver();
				Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				String[] projection = { MediaStore.Video.Media.TITLE,
						MediaStore.Video.Media.DURATION,
						MediaStore.Video.Media.SIZE,
						MediaStore.Video.Media.DATA };
				Cursor cursor = resolver.query(uri, projection, null, null,
						null);
				while (cursor.moveToNext()) {
					VideoItem item = new VideoItem();
					long size = cursor.getLong(2);
					if (size > 3 * 1024 * 1024) {
						String title = cursor.getString(0);
						item.setTitle(title);
						String duration = cursor.getString(1);
						item.setDuration(duration);
						item.setSize(size);
						String data = cursor.getString(3);
						item.setData(data);

						videoItems.add(item);
					}
				}
				handler.sendEmptyMessage(0);
			};
		}.start();
	}

	@Override
	public View setContentView() {
		// TODO Auto-generated method stub
		return View.inflate(this, R.layout.activity_video_list, null);

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
