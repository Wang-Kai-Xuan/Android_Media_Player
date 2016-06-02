package com.kyle.mobileplayer.audio;

import java.util.ArrayList;

import com.kyle.mobileplayer.R;
import com.kyle.mobileplayer.domain.AudioItem;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

public class MusicPlayerService extends Service {
	private static final String TAG = "VALUE";
	protected static final String PREPARED_MESSAGE = "PREPARED_MESSAGE";
	private ArrayList<AudioItem> audioItems;
	private AudioItem currentAudioItem;
	private int currentPosition;
	private MediaPlayer mediaPlayer;

	public static int REPEAT_MODE_NORMAL = 0;
	public static int REPEAT_MODE_CURRENT = 1;
	public static int REPEAT_MODE_ALL = 2;
	public static int playMode = REPEAT_MODE_NORMAL;

	private OnPreparedListener mOnPreparedListener = new OnPreparedListener() {

		@Override
		public void onPrepared(MediaPlayer mp) {
			// TODO Auto-generated method stub
			play();
			// 准备好了就发广播
			notifyChange(PREPARED_MESSAGE);
		}
	};
	private OnCompletionListener mOnCompletionListener = new OnCompletionListener() {

		@Override
		public void onCompletion(MediaPlayer mp) {
			// TODO Auto-generated method stub
			next();
		}
	};
	private OnErrorListener mOnErrorListener = new OnErrorListener() {

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			// TODO Auto-generated method stub
//			Toast.makeText(getApplicationContext(), "播放出错了", 1).show();
			return true;
		}
	};
	private IMusicPlayerService.Stub iBinder = new IMusicPlayerService.Stub() {

		MusicPlayerService service = MusicPlayerService.this;

		// 这些实现/MobilePlayer/gen/com/itheima/mobileplayer/audio/IMusicPlayerService.java
		// 中的函数，这个类是AIDL文件生成的JAVA接口
		@Override
		public void setPlayMode(int mode) throws RemoteException {
			// TODO Auto-generated method stub
			service.setPlayMode(mode);
		}

		@Override
		public void seekTo(int position) throws RemoteException {
			// TODO Auto-generated method stub
			service.seekTo(position);
		}

		@Override
		public void pre() throws RemoteException {
			// TODO Auto-generated method stub
			service.pre();
		}

		@Override
		public void play() throws RemoteException {
			// TODO Auto-generated method stub
			service.play();
		}

		@Override
		public void pause() throws RemoteException {
			// TODO Auto-generated method stub
			service.pause();
		}

		@Override
		public void openAudio(int position) throws RemoteException {
			// TODO Auto-generated method stub
			try {
				service.openAudio(position);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void next() throws RemoteException {
			// TODO Auto-generated method stub
			service.next();
		}

		@Override
		public String getName() throws RemoteException {
			// TODO Auto-generated method stub
			return service.getName();
		}

		@Override
		public int getDuration() throws RemoteException {
			// TODO Auto-generated method stub
			return service.getDuration();
		}

		@Override
		public int getCurrentPosition() throws RemoteException {
			// TODO Auto-generated method stub
			return service.getCurrentPosition();
		}

		@Override
		public String getArtist() throws RemoteException {
			// TODO Auto-generated method stub
			return service.getArtist();
		}

		@Override
		public boolean isPlaying() throws RemoteException {
			// TODO Auto-generated method stub
			return service.isPlaying();
		}

		@Override
		public int getPlayMode() throws RemoteException {
			// TODO Auto-generated method stub
			return service.getPlayMode();
		}
	};

	// 向客户端暴露接口, onBind()方法中返回该接口，当我们绑定该接口时调用该方法。
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return iBinder;
	}

	protected int getPlayMode() {
		// TODO Auto-generated method stub
		return playMode;
	}

	protected boolean isPlaying() {
		// TODO Auto-generated method stub
		if (mediaPlayer != null) {
			return mediaPlayer.isPlaying();
		}
		return false;
	}

	protected void notifyChange(String action) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.setAction(action);
		sendBroadcast(intent);
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		getAllAudio();
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
			};
		}.start();
	}

	// 抛出最大的异常，为了美观
	private void openAudio(int position) throws Exception {
		currentPosition = position;
		currentAudioItem = audioItems.get(position);
		// 释放上一次播放的资源
		if (mediaPlayer != null) {
			mediaPlayer.reset();
			mediaPlayer = null;
		}
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnPreparedListener(mOnPreparedListener);
		mediaPlayer.setOnCompletionListener(mOnCompletionListener);
		mediaPlayer.setOnErrorListener(mOnErrorListener);
		mediaPlayer.setDataSource(currentAudioItem.getData());
		Log.d(TAG, "播放路径：" + currentAudioItem.getData());

		// 异步准备 同步准备在播放本地资源的时候使用
		// 异步准备 网络本地都可以使用
		mediaPlayer.prepareAsync();
	}

	private void play() {
		if (mediaPlayer != null) {
			mediaPlayer.start();

		}
		int icon = R.drawable.music;
		CharSequence tickerText = "正在播放" + getName();
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);
		// 设置为点击后仍然还在
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		// 设置事件
		Intent intent = new Intent(this, AudioPlayerActivity.class);
		intent.putExtra("from_noti", true);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, 0);
		notification.setLatestEventInfo(this, "音乐播放器", tickerText,
				contentIntent);

		// 设置优先级最高
		startForeground(1, notification);
	}

	private void pause() {
		if (mediaPlayer != null) {
			mediaPlayer.pause();
		}
		// 取消状态栏的音乐显示
		stopForeground(true);
	}

	private void pre() {
		setPrePositioin();
		openNextAudio();
	}

	private void setPrePositioin() {
		// TODO Auto-generated method stub
		if (playMode == MusicPlayerService.REPEAT_MODE_NORMAL) {
			currentPosition--;
			if (currentPosition < 0) {
				currentPosition = 0;
			}
		} else if (playMode == MusicPlayerService.REPEAT_MODE_ALL) {
			currentPosition--;
			if (currentPosition == 0){
				currentPosition = audioItems.size() - 1;
				Log.d(TAG, "0位置上一曲全部循环" + currentPosition);
				Toast.makeText(getApplicationContext(), "歌曲位置"+currentPosition, 0);
			}
		} else if (playMode == MusicPlayerService.REPEAT_MODE_CURRENT) {
		
		}
	
	}

	private void next() {
		setNextPosition();
		openNextAudio();
	}

	private void setNextPosition() {
		// TODO Auto-generated method stub
		if (playMode == MusicPlayerService.REPEAT_MODE_NORMAL) {
			currentPosition++;
			if (currentPosition > audioItems.size() - 1) {
				currentPosition = audioItems.size() - 1;
			}
		} else if (playMode == MusicPlayerService.REPEAT_MODE_ALL) {
			currentPosition++;
			if (currentPosition == audioItems.size() - 1) {
				currentPosition = 0;
			}
		} else if (playMode == MusicPlayerService.REPEAT_MODE_CURRENT) {
		
		}
	}

	private void openNextAudio() {
		// TODO Auto-generated method stub
		if (playMode == MusicPlayerService.REPEAT_MODE_NORMAL) {
			if(currentPosition > audioItems.size()){
				return ;
			}
			try {
				openAudio(currentPosition);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (playMode == MusicPlayerService.REPEAT_MODE_ALL) {
			try {
				openAudio(currentPosition);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (playMode == MusicPlayerService.REPEAT_MODE_CURRENT) {
			try {
				openAudio(currentPosition);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private String getArtist() {
		if (currentAudioItem != null) {
			return currentAudioItem.getArtist();
		}
		return "";
	}

	private int getDuration() {
		if (mediaPlayer != null) {
			return mediaPlayer.getDuration();
		}
		return 0;
	}

	private String getName() {
		if (currentAudioItem != null) {
			return currentAudioItem.getTitle();
		}
		return "";
	}

	private int getCurrentPosition() {
		if (mediaPlayer != null) {
			return mediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	private void seekTo(int position) {
		if (mediaPlayer != null) {
			mediaPlayer.seekTo(position);
		}
	}

	private void setPlayMode(int mode) {
		playMode = mode;
	}
}
