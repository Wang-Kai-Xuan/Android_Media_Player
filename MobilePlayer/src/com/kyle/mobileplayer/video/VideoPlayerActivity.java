package com.kyle.mobileplayer.video;

import java.util.ArrayList;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.kyle.mobileplayer.BaseActivity;
import com.kyle.mobileplayer.R;
import com.kyle.mobileplayer.domain.VideoItem;
import com.kyle.mobileplayer.utils.Utils;
import com.kyle.mobileplayer.view.VideoView;

public class VideoPlayerActivity extends BaseActivity {

	protected static final int PROGRESS = 1;
	/**
	 * 这个VideoView是自定义的，新增了屏幕全屏的功能，
	 * 因为系统类没有支持全屏显示的方法
	 */
	private VideoView videoView;
	private Uri uri;
	private Utils utils;

	private TextView tv_title;
	private ImageView iv_battery;

	private TextView tv_system_time;
	private Button btn_voice;
	private SeekBar seekbar_voice;

	private TextView tv_current_time;
	private SeekBar seekbar_video;
	private TextView tv_duration;

	private Button btn_exit;
	private Button btn_pre;
	private Button btn_play_pause;
	private Button btn_next;

	private boolean isPlaying = false;
	private boolean isDestroyed = false;
	private MyBroadcastReceiver receiver;
	/**
	 * 更新电池的电量信息 用于接收系统的电量广播消息，并且在handle 1秒循环中的更新
	 */
	private int level;

	private ArrayList<VideoItem> videoItems;
	private int position;

	private GestureDetector detector;
	private LinearLayout ll_control_player;
	private LinearLayout ll_loading;
	private boolean isShowControl = false;
	private boolean isFullScreen = false;
	/**
	 * 隐藏控制面板的消息内容
	 */
	protected static final int DELAYED_HIDECONTROLPLAYER = 2;

	private static final int FULL_SCREEN = 3;
	private static final int DEFAULT_SCREEN = 4;

	private static final int FINSH = 5;
	private WindowManager wm;

	private int screenWidth = 0;

	private int screenHight = 0;

	private AudioManager am;
	private int currentVolume;
	private int maxVolume;

	private boolean isMute = false;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case PROGRESS:
				int currentPostion = videoView.getCurrentPosition();
				tv_current_time.setText(utils.stringForTime(currentPostion));

				// 消息死循环
				seekbar_video.setProgress(currentPostion);

				setBattery();
				if (!isDestroyed) {
					handler.removeMessages(PROGRESS);
					// 1000 与0 不一样，0会消耗资源
					handler.sendEmptyMessageDelayed(PROGRESS, 1000);
				}
				int percentage = videoView.getBufferPercentage();
				int total = percentage * seekbar_video.getMax();
				int secondaryProgress = total / 100;
				seekbar_video.setSecondaryProgress(secondaryProgress);
				tv_system_time.setText(utils.getSystemTime());
				break;

			case DELAYED_HIDECONTROLPLAYER:
				hideControlPlayer();
				break;
			case FINSH:
				if (videoView != null) {
					videoView.stopPlayback();
				}
				finish();
				break;
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		initView();
		initData();
		getDate();
		setData();
		setListener();
	}

	private void setData() {
		// TODO Auto-generated method stub

		if (videoItems != null && videoItems.size() > 0) {
			VideoItem videoItem = videoItems.get(position);
			videoView.setVideoPath(videoItem.getData());
			tv_title.setText(videoItem.getTitle());
		} else if (uri != null) { // 经if的判断，这里筛选出来只有一个视频地址的情况
			videoView.setVideoURI(uri);
			tv_title.setText(uri.toString());
			btn_pre.setEnabled(false);
			btn_next.setEnabled(false);
		}
		seekbar_voice.setMax(maxVolume);
		seekbar_voice.setProgress(currentVolume);
	}

	@SuppressWarnings("unchecked")
	private void getDate() {
		videoItems = (ArrayList<VideoItem>) getIntent().getSerializableExtra(
				"videolist");
		position = getIntent().getIntExtra("position", 0);
		/**
		 * 获得路径
		 */
		uri = getIntent().getData();

	}

	protected void setBattery() {
		// TODO Auto-generated method stub
		if (level <= 0) {
			iv_battery.setImageResource(R.drawable.ic_battery_0);
		} else if (level <= 10) {
			iv_battery.setImageResource(R.drawable.ic_battery_10);
		} else if (level <= 20) {
			iv_battery.setImageResource(R.drawable.ic_battery_20);
		} else if (level <= 30) {
			iv_battery.setImageResource(R.drawable.ic_battery_30);
		} else if (level <= 40) {
			iv_battery.setImageResource(R.drawable.ic_battery_40);
		} else if (level <= 50) {
			iv_battery.setImageResource(R.drawable.ic_battery_50);
		} else if (level <= 60) {
			iv_battery.setImageResource(R.drawable.ic_battery_60);
		} else if (level <= 70) {
			iv_battery.setImageResource(R.drawable.ic_battery_70);
		} else if (level <= 80) {
			iv_battery.setImageResource(R.drawable.ic_battery_80);
		} else if (level <= 90) {
			iv_battery.setImageResource(R.drawable.ic_battery_90);
		}
	}

	private void initData() {

		utils = new Utils();
		isDestroyed = false;
		isFullScreen = false;

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		screenWidth = wm.getDefaultDisplay().getWidth();
		screenHight = wm.getDefaultDisplay().getHeight();

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		receiver = new MyBroadcastReceiver();
		registerReceiver(receiver, filter);

		detector = new GestureDetector(this,
				new GestureDetector.SimpleOnGestureListener() {

					@Override
					public void onLongPress(MotionEvent e) {
						// TODO Auto-generated method stub
						// Toast.makeText(getApplicationContext(), "长按屏幕", 0)
						// .show();
						// super.onLongPress(e);
						startOrPause();
					}

					@Override
					public boolean onDoubleTap(MotionEvent e) {
						// TODO Auto-generated method stub
						// Toast.makeText(getApplicationContext(), "双击屏幕", 0)
						// .show();
						if (isFullScreen) {
							setVideoType(DEFAULT_SCREEN);
						} else {
							setVideoType(FULL_SCREEN);
						}
						return true;
					}

					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						// TODO Auto-generated method stub
						// Toast.makeText(getApplicationContext(), "单击屏幕", 0)
						// .show();
						// return super.onSingleTapConfirmed(e);
						if (isShowControl) {
							removeDelayedHideControlPlayer();
							hideControlPlayer();
						} else {
							showControlPlayer();
							sendDelayedHideControlPlayer();
						}

						return true;
					}

				});
		am = (AudioManager) getSystemService(AUDIO_SERVICE);
		currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

	}

	private void sendDelayedHideControlPlayer() {
		handler.sendEmptyMessageDelayed(DELAYED_HIDECONTROLPLAYER, 5000);
	}

	protected void removeDelayedHideControlPlayer() {
		// TODO Auto-generated method stub
		handler.removeMessages(DELAYED_HIDECONTROLPLAYER);
	}

	private float startY;
	private float audioTouchRang;
	private float mVol;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		super.onTouchEvent(event);
		detector.onTouchEvent(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			startY = event.getY();
			removeDelayedHideControlPlayer();
			audioTouchRang = Math.min(screenHight, screenWidth);
			mVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);

			break;

		case MotionEvent.ACTION_MOVE:
			float endY = event.getY();
			float distanceY = startY - endY;
			float datel = distanceY / audioTouchRang;
			float volume = datel * maxVolume;
			float voulmes = Math.min(Math.max(volume + mVol, 0), maxVolume);
			if (datel != 0) {
				updateVolume((int) voulmes);
			}
			break;

		case MotionEvent.ACTION_UP:
			sendDelayedHideControlPlayer();
			break;

		default:
			break;
		}
		return true;
	}

	private class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			/**
			 * 广播在这里接收数据，参数level是Android规定的
			 */
			level = intent.getIntExtra("level", 0);
		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		isDestroyed = true;
		unregisterReceiver(receiver);
		receiver = null;
	}

	private void setListener() {
		btn_voice.setOnClickListener(mOnClickListener);
		btn_play_pause.setOnClickListener(mOnClickListener);
		btn_next.setOnClickListener(mOnClickListener);
		btn_pre.setOnClickListener(mOnClickListener);
		btn_exit.setOnClickListener(mOnClickListener);
		seekbar_video.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				sendDelayedHideControlPlayer();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				removeDelayedHideControlPlayer();
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				if (fromUser) {
					videoView.seekTo(progress);
				}
			}
		});
		videoView.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				// TODO Auto-generated method stub
				startVitamioPlayer();
				return false;
			}
		});
		seekbar_voice.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				if (fromUser) {
					updateVolume(progress);
				}
			}
		});
		videoView.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				// TODO Auto-generated method stub
				videoView.start();
				isPlaying = true;
				handler.sendEmptyMessage(PROGRESS);
				setVideoType(DEFAULT_SCREEN);
				int duration = videoView.getDuration();
				tv_duration.setText(utils.stringForTime(duration));
				seekbar_video.setMax(duration);
				hideControlPlayer();
				ll_loading.setVisibility(View.GONE);
			}
		});
		videoView.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				playNextVideo();
			}
		});
	}

	protected void startVitamioPlayer() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, VitamioPlayerActivity.class);
		Bundle extras = new Bundle();
		extras.putSerializable("videolist", videoItems);
		intent.putExtras(extras);
		intent.setData(uri);
		intent.putExtra("position", position);
		startActivity(intent);

		// 关闭当前的activity，关闭当前播放器
		// 延时后再进入

		handler.sendEmptyMessageDelayed(FINSH, 2000);

	}

	protected void updateVolume(int progress) {
		// TODO Auto-generated method stub
		if (isMute) {
			am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
			seekbar_voice.setProgress(0);
		} else {
			am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
			seekbar_voice.setProgress(progress);
		}
		currentVolume = progress;

	}

	OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			removeDelayedHideControlPlayer();
			sendDelayedHideControlPlayer();
			switch (v.getId()) {
			case R.id.btn_play_pause:
				startOrPause();
				break;
			case R.id.btn_next:
				playNextVideo();
				break;
			case R.id.btn_pre:
				playPreVideo();
				break;
			case R.id.btn_voice:
				isMute = !isMute;
				updateVolume(currentVolume);
				break;
			case R.id.btn_exit:
				handler.sendEmptyMessage(FINSH);
				break;
			default:
				break;
			}
		}
	};

	private void initView() {
		setTitleBar(View.GONE);
		videoView = (VideoView) findViewById(R.id.videoview);

		tv_title = (TextView) findViewById(R.id.tv_video_title);
		iv_battery = (ImageView) findViewById(R.id.iv_battery);
		tv_system_time = (TextView) findViewById(R.id.tv_system_time);
		btn_voice = (Button) findViewById(R.id.btn_voice);
		seekbar_voice = (SeekBar) findViewById(R.id.seekbar_voice);

		tv_current_time = (TextView) findViewById(R.id.tv_current_time);
		tv_duration = (TextView) findViewById(R.id.tv_duration);
		seekbar_video = (SeekBar) findViewById(R.id.seekbar_video);
		btn_exit = (Button) findViewById(R.id.btn_exit);
		btn_pre = (Button) findViewById(R.id.btn_pre);
		btn_play_pause = (Button) findViewById(R.id.btn_play_pause);
		btn_next = (Button) findViewById(R.id.btn_next);
		ll_control_player = (LinearLayout) findViewById(R.id.ll_control_player);
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
	}

	@Override
	public View setContentView() {
		// TODO Auto-generated method stub
		/**
		 * 设置父窗体的界面
		 */
		return View.inflate(this, R.layout.activity_video_player, null);
	}

	@Override
	public void rightButOnclick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void leftButOnclick() {
		// TODO Auto-generated method stub

	}

	private void setPlayOrPauseStatus() {
		if (position == 0) {
			// btn_pre.setBackgroundResource(R.drawable.btn_pr)
			btn_pre.setVisibility(View.GONE);
			btn_pre.setEnabled(false);
		} else if (position == videoItems.size() - 1) {
			btn_next.setVisibility(View.GONE);
			btn_next.setEnabled(false);
		} else {
			btn_pre.setVisibility(View.VISIBLE);
			btn_pre.setEnabled(true);

			btn_next.setVisibility(View.VISIBLE);
			btn_next.setEnabled(true);
		}
	}

	private void playNextVideo() {
		if (videoItems != null && videoItems.size() > 0) {
			position++;
			if (position < videoItems.size()) {
				VideoItem videoItem = videoItems.get(position);
				videoView.setVideoPath(videoItem.getData());
				tv_title.setText(videoItem.getTitle());
				setPlayOrPauseStatus();
			} else {
				position = videoItems.size() - 1;
				Toast.makeText(getApplicationContext(), "已到最后一个视频", 0).show();
				finish();
			}
		} else if (uri != null) {
			Toast.makeText(getApplicationContext(), "视频播放完成", 0).show();
			finish();
		}
	}

	private void playPreVideo() {
		if (videoItems != null && videoItems.size() > 0) {
			position--;
			if (position > 0) {
				VideoItem videoItem = videoItems.get(position);
				videoView.setVideoPath(videoItem.getData());
				tv_title.setText(videoItem.getTitle());
				setPlayOrPauseStatus();
			} else {
				position = 0;
				Toast.makeText(getApplicationContext(), "已到第一个视频", 0).show();
				finish();
			}
		}
	}

	private void hideControlPlayer() {
		ll_control_player.setVisibility(View.GONE);
		isShowControl = false;
	}

	private void showControlPlayer() {
		ll_control_player.setVisibility(View.VISIBLE);
		isShowControl = true;
	}

	private void startOrPause() {
		if (isPlaying) {
			videoView.pause();
			btn_play_pause.setBackgroundResource(R.drawable.btn_play_selector);

		} else {
			videoView.start();
			btn_play_pause.setBackgroundResource(R.drawable.btn_pause_selector);
		}
		isPlaying = !isPlaying;
	}

	private void setVideoType(int type) {
		switch (type) {

		case FULL_SCREEN:
			videoView.setVideoSize(screenWidth, screenHight);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			isFullScreen = true;
			break;
		case DEFAULT_SCREEN:
			int mVideoWidth = videoView.getVideoWidth();
			int mVideoHeight = videoView.getVideoHeight();

			int width = screenWidth;
			int height = screenHight;

			if (mVideoWidth > 0 && mVideoHeight > 0) {
				if (mVideoWidth * height > width * mVideoHeight) {
					// Log.i("@@@", "image too tall, correcting");
					height = width * mVideoHeight / mVideoWidth;
				} else if (mVideoWidth * height < width * mVideoHeight) {
					// Log.i("@@@", "image too wide, correcting");
					width = height * mVideoWidth / mVideoHeight;
				} else {
					// Log.i("@@@", "aspect ratio is correct: " +
					// width+"/"+height+"="+
					// mVideoWidth+"/"+mVideoHeight);
				}

			}

			videoView.setVideoSize(width, height);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			isFullScreen = false;
			break;
		}
	}
}
