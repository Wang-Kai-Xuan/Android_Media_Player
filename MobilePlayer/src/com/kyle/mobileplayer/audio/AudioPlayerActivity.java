package com.kyle.mobileplayer.audio;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.kyle.mobileplayer.BaseActivity;
import com.kyle.mobileplayer.R;
import com.kyle.mobileplayer.utils.Utils;

public class AudioPlayerActivity extends BaseActivity {

	private static final String TAG = "VALUE";
	/**
	 * PROCESS：handle发送的消息内容
	 */
	protected static final int PROCESS = 0;
	/**
	 * An object used to create frame-by-frame animations, defined by a series
	 * of Drawable objects, which can be used as a View object's background.
	 */
	private AnimationDrawable animationDrawable;
	// 代表了服务，链接成功后实例化,然后就可以得到父类的方法
	private ImageView iv_lyric;
	/**
	 * service:service的实例化，通过它可以服务服务的方法
	 */
	private IMusicPlayerService service;

	private TextView tv_artist;
	private TextView tv_name;
	private TextView tv_time;
	private Button btn_mode;
	private Button btn_pre;
	private Button btn_play_pause;
	private Button btn_next;
	private Button btn_lyric;
	private SeekBar seekbar_audio;
	/**
	 * isPlaying:正在播放音乐的标志，用于播放与暂停的切换标志
	 */
	private boolean isPlaying = false;
	/**
	 * 
	 */
	private MyBroadCastReceiver receiver;
	/**
	 * utils:工具类，用于转换歌曲时间、大小的格式
	 */
	private Utils utils;

	/**
	 * 当前activity是否被销毁的标志。用于判断是否需要更新数据， 比如歌曲显示的时间更新，滚动条的位置更新
	 */
	private boolean isDestory = false;
	/**
	 * from_noti：接收状态栏发送的消息，true表示用户点击了状态栏
	 */
	private boolean from_noti;
	/**
	 * handler：通俗一点讲就是用来在各个进程之间发送数据的处理对象。
	 */
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case PROCESS:
				try {
					int curPosi = service.getCurrentPosition();
					seekbar_audio.setProgress(curPosi);
					tv_time.setText(utils.stringForTime(service
							.getCurrentPosition())
							+ "/"
							+ utils.stringForTime(service.getDuration()));
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!isDestory) {
					// 循环一秒发送 收到后延时一秒有发送 如此循环
					handler.sendEmptyMessageDelayed(PROCESS, 1000);
				}
				break;

			default:
				break;
			}
		};
	};
	private ServiceConnection conn = new ServiceConnection() {
		// 取消绑定回调这个方法
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			service = null;
		}

		// 绑定成功回调这个方法， IBinder是从服务中穿过来的，没有实现就要创建
		@Override
		public void onServiceConnected(ComponentName name, IBinder iBinder) {
			// TODO Auto-generated method stub
			service = IMusicPlayerService.Stub.asInterface(iBinder);
			if (service != null) {
				try {
					// !from_noti：含义是如果不是从状态栏发送的消息才重新打开音乐
					if (!from_noti) {
						service.openAudio(position);
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	private OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_play_pause:
				try {
					if (isPlaying) {
						service.pause();
					} else {
						service.play();
					}
					setButtonStatus();
					isPlaying = !isPlaying;
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case R.id.btn_mode:
				try {
					changeMode();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case R.id.btn_next:
				try {
					service.next();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case R.id.btn_pre:
				try {
					service.pre();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		initData();
		isDestory = false;
		initView();
		// 绑定成功后会调用onServiceConnected（）
		setListener();
		bindService();

		getDate();
	}

	protected void changeMode() throws RemoteException {
		// TODO Auto-generated method stub
		int playMode = service.getPlayMode()+1;
		if(playMode > 2)
			playMode = 0;
		service.setPlayMode(playMode);
		setPlayModeButton();
	}

	/**
	 * 设置按钮状态
	 * 
	 * @throws RemoteException
	 */
	private void setPlayModeButton() throws RemoteException {
		// TODO Auto-generated method stub
		int playMode = service.getPlayMode();

		if (playMode == MusicPlayerService.REPEAT_MODE_ALL) {
			btn_mode.setBackgroundResource(R.drawable.btn_audio_all_mode_selector);
			Toast.makeText(getApplicationContext(), "全部循环", 1).show();

		} else if (playMode == MusicPlayerService.REPEAT_MODE_NORMAL) {
			btn_mode.setBackgroundResource(R.drawable.btn_audio_normal_selector);
			Toast.makeText(getApplicationContext(), "正常播放", 1).show();
		} else if (playMode == MusicPlayerService.REPEAT_MODE_CURRENT) {
			btn_mode.setBackgroundResource(R.drawable.btn_audio_single_mode_selector);
			Toast.makeText(getApplicationContext(), "单曲循环", 1).show();
		}
	}

	protected void setButtonStatus() {
		// TODO Auto-generated method stub
		if (isPlaying) {
			btn_play_pause
					.setBackgroundResource(R.drawable.btn_audio_pause_selector);
		} else {
			btn_play_pause
					.setBackgroundResource(R.drawable.btn_audio_play_selector);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// 程序销毁时取消注册的广播接收者
		unregisterReceiver(receiver);
		isDestory = true;
		receiver = null;
	}

	private void initData() {
		// TODO Auto-generated method stub
		IntentFilter filter = new IntentFilter();
		utils = new Utils();

		receiver = new MyBroadCastReceiver();
		filter.addAction(MusicPlayerService.PREPARED_MESSAGE);
		// receiver是一个抽象类，需要实例化
		registerReceiver(receiver, filter);
	}

	private class MyBroadCastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			try {
				setViewStatus();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void setListener() {
		// TODO Auto-generated method stub
		btn_play_pause.setOnClickListener(mClickListener);
		btn_mode.setOnClickListener(mClickListener);
		btn_next.setOnClickListener(mClickListener);
		btn_pre.setOnClickListener(mClickListener);

		seekbar_audio.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

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
					try {
						service.seekTo(progress);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}

	public void setViewStatus() throws RemoteException {
		// TODO Auto-generated method stub
		tv_artist.setText(service.getArtist());
		tv_name.setText(service.getName());
		Log.d(TAG, "我在这里");
		tv_time.setText(utils.stringForTime(service.getCurrentPosition()) + "/"
				+ utils.stringForTime(service.getDuration()));
		Log.d(TAG, "" + service.getCurrentPosition() + service.getDuration());
		seekbar_audio.setMax(service.getDuration());

		isPlaying = service.isPlaying();
		handler.sendEmptyMessage(PROCESS);
		setButtonStatus();
	}

	private void initView() {
		setTitle("手机音乐");
		setRightButon(View.GONE);
		iv_lyric = (ImageView) findViewById(R.id.iv_icon);
		tv_artist = (TextView) findViewById(R.id.tv_artist);
		tv_name = (TextView) findViewById(R.id.tv_name);
		tv_time = (TextView) findViewById(R.id.tv_time);
		btn_mode = (Button) findViewById(R.id.btn_mode);
		btn_pre = (Button) findViewById(R.id.btn_pre);
		btn_play_pause = (Button) findViewById(R.id.btn_play_pause);
		btn_next = (Button) findViewById(R.id.btn_next);
		btn_lyric = (Button) findViewById(R.id.btn_lyric);
		seekbar_audio = (SeekBar) findViewById(R.id.seekbar_audio);
		iv_lyric.setBackgroundResource(R.drawable.animation_list);
		animationDrawable = (AnimationDrawable) iv_lyric.getBackground();
		animationDrawable.start();

	}

	private int position;

	// 得到数据
	private void getDate() {
		// TODO Auto-generated method stub
		/**
		 * 接收状态栏发送的消息：是否被点击
		 */
		from_noti = getIntent().getBooleanExtra("from_noti", false);
		/**
		 * 如果没有被点击，那么就是程序刚刚启动，这时才从0位置开始播放，
		 */
		if (!from_noti) {
			/**
			 * 接收音乐列表发送过来的数据，就一个：位置
			 */
			position = getIntent().getIntExtra("position", 0);
		}
	}

	// 绑定方式启动服务
	private void bindService() {
		// TODO Auto-generated method stub
		Bundle bundle = new Bundle();
		Intent intent = new Intent();
		intent.setAction("com.kyle.mobileplayer.bindservice");
		intent.putExtras(bundle);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
		startService(intent); // 已经bind，加上不会多次绑定，不会产生新的实例
	}

	@Override
	public View setContentView() {
		// TODO Auto-generated method stub
		return View.inflate(this, R.layout.activity_audioplayer, null);
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
