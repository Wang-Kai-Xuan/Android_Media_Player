package com.kyle.mobileplayer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;


public abstract class BaseActivity extends Activity {

	private Button btn_left;
	private Button btn_right;
	private TextView tv_title;
	private FrameLayout frameLayout;
	private LinearLayout ll_child_content;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_base);
		initView();
		setOnClickListerner();
	}

	/**
	 * 设置点击事件
	 */
	private void setOnClickListerner() {
		// TODO Auto-generated method stub
		btn_left.setOnClickListener(mOnClickListener);
		btn_right.setOnClickListener(mOnClickListener);
	}

	OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_left:
				leftButOnclick();
				break;
			case R.id.btn_right:
				rightButOnclick();
				break;

			default:
				break;
			}
		}
	};

	public void setTitle(String text) {
		tv_title.setText(text);
	}

	public void setleftButon(int visibility) {
		btn_left.setVisibility(visibility);
	}

	public void setRightButon(int visibility) {
		btn_right.setVisibility(visibility);
	}

	public void setTitleBar(int visibility) {
		frameLayout.setVisibility(visibility);
	}

	private void initView() {
		// TODO Auto-generated method stub
		btn_left = (Button) findViewById(R.id.btn_left);
		btn_right = (Button) findViewById(R.id.btn_right);
		tv_title = (TextView) findViewById(R.id.tv_title);
		ll_child_content = (LinearLayout) findViewById(R.id.ll_child_content);
		frameLayout = (FrameLayout) findViewById(R.id.fl_titlebar);
		View child = setContentView();

		if (child != null) {
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			ll_child_content.addView(child, params);

		}
	}

	/**
	 * 点击按钮由之类实现
	 * 
	 * @return
	 */
	public abstract View setContentView();

	public abstract void rightButOnclick();

	public abstract void leftButOnclick();
}
