package com.kyle.mobileplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		isEnterMained = false;
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				enterMain();
			}
		}, 2000);
	}

	private boolean isEnterMained = false;

	protected void enterMain() {
		if (!isEnterMained) {
			isEnterMained = true;
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		enterMain();
		return super.onTouchEvent(event);
	}
}
