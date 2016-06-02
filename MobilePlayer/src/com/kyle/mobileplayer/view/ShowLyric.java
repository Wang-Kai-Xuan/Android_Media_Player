package com.kyle.mobileplayer.view;

import java.util.ArrayList;

import com.kyle.mobileplayer.domain.Lyric;

import android.R.integer;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class ShowLyric extends TextView {
	private Paint curPaint;
	private Paint nocurPaint;
	ArrayList<Lyric> lyrics;
	
	private int index;

	public ShowLyric(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initView();
	}

	private void initView() {
		// TODO Auto-generated method stub
		curPaint = new Paint();
		curPaint.setColor(Color.GREEN);
		curPaint.setAntiAlias(true);
		curPaint.setTextAlign(Paint.Align.CENTER);
		curPaint.setTextSize(66);

		nocurPaint = new Paint();
		nocurPaint.setColor(Color.YELLOW);
		nocurPaint.setAntiAlias(true);
		nocurPaint.setTextAlign(Paint.Align.CENTER);
		nocurPaint.setTextSize(66);

		// ÃÌº”ºŸ…Ë∏Ë¥ 
		lyrics = new ArrayList<Lyric>();
		for (int i = 0; i < 200; i++) {
			Lyric lyric = new Lyric();
			lyric.setContent(i +"wangkaixuan" + i);
			lyric.setTimePoint(1000 * i);
			lyric.setSleepTime(2000);
			lyrics.add(lyric);
		}
	}

	private int curWidth;
	private int curHeight;
	private float textHeight = 60;

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		curWidth = w;
		curHeight = h;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		// super.onDraw(canvas);
		if (lyrics.size() != 0 && lyrics.size() > 0) {

			// ª≠«∞“ªæ‰∏Ë¥ 
//			String curContent = lyrics.get(index).getContent();
//			canvas.drawText(curContent, curWidth / 2, curHeight / 2, curPaint);

			// ª≠µ±«∞∏Ë¥ 
			float tempY = curHeight / 2;
			for (int i = index - 1; i > 0; i--) {

				String nextContent = lyrics.get(i).getContent();
				tempY = tempY - textHeight;
				canvas.drawText(nextContent, curWidth / 2, curHeight / 2,
						nocurPaint);
				if (tempY < 0) {
					break;
			 	}

			}
			// ª≠∫Û“ªæ‰∏Ë¥ 
//			tempY = curHeight/2;
//			for (int i = index + 1; i < lyrics.size(); i++) {
//
//				String preContent = lyrics.get(i).getContent();
//				tempY = tempY + textHeight;
//				canvas.drawText(preContent, curWidth / 2, curHeight / 2,
//						nocurPaint);
//				if (tempY > curHeight) {
//					break;
//				}
//			}
//			index = index - 1;
		
			canvas.drawText("∏Ë¥ œ‘ æ", curWidth / 2, curHeight / 2, curPaint);
		}
	}
}
