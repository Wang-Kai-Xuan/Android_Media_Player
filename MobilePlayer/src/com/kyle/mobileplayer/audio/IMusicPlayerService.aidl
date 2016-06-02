package com.kyle.mobileplayer.audio;

import android.content.Intent;
import android.os.IBinder;

interface IMusicPlayerService {
	void openAudio(int position);

	boolean isPlaying();
	void play();

	void pause();
	void pre();

	void next();
	String getArtist() ;

	int getDuration() ;
	String getName();

	int getCurrentPosition();

	void seekTo(int position);

	void setPlayMode(int mode);
	int getPlayMode();
}
