package com.example.whiteboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import static com.example.whiteboard.Constant.*;

enum GESTURE { ZOOM, SCROLL, NONE };

public class MainView extends SurfaceView implements SurfaceHolder.Callback{
	
	private Point origin;
	private Point legend;
	private GESTURE gesture;
	private float offset;
	private float width;
	private int magnitude;
	private int num_countries;
	private static boolean[] selected;
	private static boolean[] visible;
	
	private Paint paint;
	private MainActivity activity;
	private MainViewDrawThread mainViewDrawThread;
	private GestureDetector mGestureDetector;

	private int timestamp = 0;
	private int chosen = -1;
	
	private List<List<Integer>> data;
	private List<RectF> rects;
	private List<Integer> offsets;
	private List<Paint> paints;
	private List<String> countries;
	
	public MainView(MainActivity activity) {
		super(activity);
		getHolder().addCallback(this);
		this.activity = activity;
		
		data = new ArrayList<List<Integer>>();
		rects = new ArrayList<RectF>();
		offsets = new ArrayList<Integer>();
		paints = new ArrayList<Paint>();
		countries = new ArrayList<String>();
		mGestureDetector = new GestureDetector(this.getContext(), new GestureListener());
		
		DisplayMetrics displaymetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int screen_height = displaymetrics.heightPixels;
		int screen_width = displaymetrics.widthPixels;
		SCREEN_WIDTH = screen_width;
		SCREEN_HEIGHT = screen_height;
		origin = new Point(screen_width / 10, screen_height * 7 / 8);
		legend = new Point(screen_width * 8 / 10, screen_height * 1 / 8);
		
		offset = 0;
		width = 0;
		timestamp = 0;
		chosen = -1;
		width = width_default;
		
		paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setAntiAlias(true);
		paint.setTextSize(18);
		paint.setStyle(Paint.Style.STROKE);
		
		loadDataSet();
	}
	
	private void loadData() {
		for (int i = 0; i < original_data.length; i++)
		{
			ArrayList<Integer> values = new ArrayList<Integer>();
			for (int j = 0; j < original_data[i].length; j++)
			{
				values.add(original_data[i][j]);
			}
			data.add(values);
		}
	}
	
	private void loadData_2() {
		for (int i = 0; i < original_data_2.length; i++)
		{
			ArrayList<Integer> values = new ArrayList<Integer>();
			for (int j = 0; j < original_data_2[i].length; j++)
			{
				values.add(original_data_2[i][j]);
			}
			data.add(values);
		}
	}
	
	private void loadDataSet() {
		int maxValue = Integer.MIN_VALUE;
		for (int i = 0; i < original_data.length; i++)
		{
			ArrayList<Integer> values = new ArrayList<Integer>();
			for (int j = 0; j < original_data[i].length; j++)
			{
				int current = original_data[i][j];
				values.add(current);
				if(j != 0)
				{
					if(current > maxValue)
					{
						maxValue = current;
					}
				}
			}
			data.add(values);
		}
		
		int range = maxValue / 5;
		magnitude = range - range % 10;
		paints.add(initPaint(Color.BLUE));
		paints.add(initPaint(Color.GREEN));
		paints.add(initPaint(Color.GRAY));
		
		gesture = GESTURE.NONE;
		
		selected = new boolean[original_data.length];
		Arrays.fill(selected, Boolean.FALSE);
		
		visible = new boolean[country.length];
		Arrays.fill(visible, Boolean.TRUE);
		
		num_countries = country.length;
		
		for(int i = 0; i < country.length; i++)
		{
			countries.add(country[i]);
			offsets.add(getTextHeight(country[i]));
			RectF rect = new RectF(legend.x + 20, legend.y + 20 + 40 * i, legend.x + 80, legend.y + 50 + 40 * i);
			rects.add(rect);
		}
	}
	
	private void loadDataSet_2() {
		int maxValue = Integer.MIN_VALUE;
		for (int i = 0; i < original_data_2.length; i++)
		{
			ArrayList<Integer> values = new ArrayList<Integer>();
			for (int j = 0; j < original_data_2[i].length; j++)
			{
				int current = original_data_2[i][j];
				values.add(current);
				if(j != 0)
				{
					if(current > maxValue)
					{
						maxValue = current;
					}
				}
			}
			data.add(values);
		}
		
		int range = maxValue / 5;
		magnitude = range - range % 10;
		paints.add(initPaint(Color.BLUE));
		paints.add(initPaint(Color.GREEN));
		paints.add(initPaint(Color.GRAY));
		paints.add(initPaint(Color.RED));
		
		gesture = GESTURE.NONE;
		
		selected = new boolean[original_data_2.length];
		Arrays.fill(selected, Boolean.FALSE);
		
		visible = new boolean[country_2.length];
		Arrays.fill(visible, Boolean.TRUE);
		
		num_countries = country_2.length;
		
		for(int i = 0; i < country_2.length; i++)
		{
			countries.add(country_2[i]);
			offsets.add(getTextHeight(country_2[i]));
			RectF rect = new RectF(legend.x + 20, legend.y + 20 + 40 * i, legend.x + 80, legend.y + 50 + 40 * i);
			rects.add(rect);
		}
	}
	
	public synchronized void reloadData() {
		data.clear();
		if (timestamp % 2 == 1)
		{
			loadData_2();
		}
		else
		{
			loadData();
		}
	}
	
	public synchronized void changeDataSet(){
		data.clear();
		countries.clear();
		offsets.clear();
		rects.clear();
		paints.clear();
		
		timestamp += 1;
		if (timestamp % 2 == 1)
		{
			loadDataSet_2();
		}
		else
		{
			loadDataSet();
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(Color.WHITE); 
		
		// button
		canvas.drawRect(legend.x + 20, SCREEN_HEIGHT - 3 * legend.y, legend.x + 50, SCREEN_HEIGHT - 3 * legend.y + 30, paint);
		canvas.drawText("Reload", legend.x + 60, SCREEN_HEIGHT - 3 * legend.y + 15 + getTextHeight("Reload") / 2, paint);
		canvas.drawRect(legend.x + 20, SCREEN_HEIGHT - 3 * legend.y + 40, legend.x + 50, SCREEN_HEIGHT - 3 * legend.y + 70, paint);
		canvas.drawText("Change Dataset", legend.x + 60, SCREEN_HEIGHT - 3 * legend.y + 55 + getTextHeight("Change Dataset") / 2, paint);
		
		// legend
		canvas.drawRect(legend.x + 10, legend.y, SCREEN_WIDTH - 10, SCREEN_HEIGHT - 2 * legend.y, paint);
		
		for(int i = 0; i < countries.size(); i++)
		{
			if(visible[i])
			{
				canvas.drawRect(legend.x + 20, legend.y + 20 + 40 * i, legend.x + 50, legend.y + 50 + 40 * i, paints.get(i));
				canvas.drawText(countries.get(i), legend.x + 60, legend.y + 35 + 40 * i + offsets.get(i) / 2, paint);
			}
			else
			{
				canvas.drawRect(legend.x + 20, legend.y + 20 + 40 * i, legend.x + 50, legend.y + 50 + 40 * i, paint);
				canvas.drawText(countries.get(i), legend.x + 60, legend.y + 35 + 40 * i + offsets.get(i) / 2, paint);
			}
		}
		
		// axis
		canvas.drawLine(origin.x, origin.y, SCREEN_WIDTH - 2 * origin.x, origin.y, paint);
		canvas.drawLine(origin.x, origin.y, origin.x, SCREEN_HEIGHT - origin.y, paint);
		
		// arrow
		canvas.drawLine(origin.x, SCREEN_HEIGHT - origin.y, origin.x - 10, SCREEN_HEIGHT - origin.y + 10, paint);
		canvas.drawLine(origin.x, SCREEN_HEIGHT - origin.y, origin.x + 10, SCREEN_HEIGHT - origin.y + 10, paint);
		
		// y axis
		for (int i = 0; i < 6; i++)
		{
			if (chosen == -1)
			{
				float offset = paint.measureText(Integer.toString(i * magnitude));
				canvas.drawText(Integer.toString(i * magnitude), origin.x - offset, origin.y - i * 100, paint);
			}
			else
			{
				if (i < chosen)
				{
					float offset = paint.measureText(Integer.toString(i * magnitude));
					canvas.drawText(Integer.toString(i * magnitude), origin.x - offset, origin.y - (i * 500) / chosen, paint);
				}
				
				if (i == chosen)
				{
					float offset = paint.measureText(Integer.toString(i * magnitude));
					canvas.drawText(Integer.toString(i * magnitude), origin.x - offset, origin.y - 5 * 100, paint);
				}
			}
		}
		
		for (int i = 0; i < data.size(); i++)
		{
			float position = origin.x + i * (num_countries * width + interval) + offset 
					+ (num_countries * width - paint.measureText(Integer.toString(data.get(i).get(0)))) / 2;
			if (position >= origin.x && position < legend.x)
			{
				canvas.drawText(Integer.toString(data.get(i).get(0)), position , origin.y + 20, paint);
			}
			
			int num_visible = 0;
			
			for (int j = 0; j < countries.size(); j++)
			{
				if (visible[j] == true)
				{
					float left = origin.x + i * (num_countries * width + interval) + num_visible * width + offset;
					float right = origin.x + i * (num_countries * width + interval) + (num_visible + 1) * width + offset;
					float top = origin.y - data.get(i).get(j+1) / (float)magnitude * 100;
					float bottom = origin.y;
				
					if (left >= origin.x && right < legend.x)
					{
						if (selected[i] == false)
						{
							if(chosen == -1)
							{
								canvas.drawRect(left, top, right, bottom, paints.get(j));
							}
							else
							{
								if(data.get(i).get(j+1) < chosen * 10)
								{
									top = origin.y - data.get(i).get(j+1) / (float)magnitude * (500 / chosen);
									canvas.drawRect(left, top, right, bottom, paints.get(j));
								}
								else if (data.get(i).get(j+1) == chosen * 10)
								{
									canvas.drawRect(left, origin.y - 500, right, bottom, paints.get(j));
								}
							}
						}
						else
						{ 
							float offset = paint.measureText(Integer.toString(data.get(i).get(1)));
							//canvas.drawRect(left, top, right, bottom, paint);
							
							if(chosen == -1)
							{
								canvas.drawRect(left, top, right, bottom, paint);
							}
							else
							{
								if(data.get(i).get(j+1) < chosen * 10)
								{
									top = origin.y - data.get(i).get(j+1) / (float)magnitude * (500 / chosen);
									canvas.drawRect(left, top, right, bottom, paint);
								}
								else if(data.get(i).get(j+1) == chosen * 10)
								{
									canvas.drawRect(left, origin.y - 500, right, bottom, paint);
								}
							}
							
							canvas.drawText(Integer.toString(data.get(i).get(j+1)), (left + right - offset) / 2, top - 10, paint);
						}
					}
					
					num_visible += 1;
				}
			}
		}
	}

	
	
	private Paint initPaint(int color)
	{
		Paint paint = new Paint();
		paint.setColor(color);
		paint.setAntiAlias(true);
		paint.setTextSize(24);
		return paint;
	}
	
	private class GestureListener extends GestureDetector.SimpleOnGestureListener 
	{
		public boolean onDown(MotionEvent e) {
			float x = e.getX();
			float y = e.getY();
			if(x > legend.x + 20 && x < legend.x + 50 && y > SCREEN_HEIGHT - 3 * legend.y && y < SCREEN_HEIGHT - 3 * legend.y + 30)
			{
				reloadData();
			}
			else if(x > legend.x + 20 && x < legend.x + 50 && y > SCREEN_HEIGHT - 3 * legend.y + 40 && y < SCREEN_HEIGHT - 3 * legend.y + 70)
			{
				changeDataSet();
			}
			return true;
		}
		
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			float x = e1.getX();
			float y = e1.getY();
			
			if (e1.getY() > e2.getY())
			{
				for (int i = 0; i < 6; i++)
				{
					if(x > 0 && x < origin.x && y > origin.y - i * 100 - 50 && y < origin.y - i * 100 + 50)
					{
						chosen = i;
						break;
					}
				}
			}
			else
			{
				gesture = GESTURE.NONE;
				chosen = -1;
			}
			
			return true;
		}

		public boolean onSingleTapUp(MotionEvent e) {
			for(int i = 0; i < countries.size(); i++)
			{
				if (rects.get(i).contains(e.getX(), e.getY()))
				{
					visible[i] = !visible[i];
					if (visible[i])
					{
						num_countries += 1;
					}
					else
					{
						num_countries -= 1;
					}
				}
			}
			return true;
		}
	}
	
	private int getTextHeight(String text)
	{
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);
		return bounds.height();
	}
	
	float xInit = -1;
	float yInit = -1;
	float xPre = -1;
	float yPre = -1;
	boolean moveFlag = false;
	float oldLineDistance = 0;
	int zoom_index = -1;
	int remove_index = -1;
	
	public boolean onTouchEvent(MotionEvent event) 
	{
		mGestureDetector.onTouchEvent(event);
		
		int pointerCount = event.getPointerCount();
		float x = 0;
		float y = 0;
		float p = 0;
		float q = 0;
		
		if (pointerCount == 2)
		{
			gesture = GESTURE.ZOOM;
			x = event.getX(0);
			y = event.getY(0);
			p = event.getX(1);
			q = event.getY(1);
		}
		else if (pointerCount == 1)
		{
			x = event.getX();
			y = event.getY();
		}
		
		if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_POINTER_DOWN)
		{
			moveFlag=false;
			xInit = x;
			yInit = y;
			xPre = x;
			yPre = y;
				
			if (gesture == GESTURE.NONE)
			{	
				for (int i = 0; i < data.size(); i++)
				{
					float left = origin.x + i * (num_countries * width + interval) + offset;
					float right = origin.x + i * (num_countries * width + interval) + num_countries * width + offset;
					float top = origin.y;
					float maxvalue = Math.max(data.get(i).get(1), Math.max(data.get(i).get(2), data.get(i).get(3)));
					if (x > left && x > origin.x && x < right && x < legend.x && y > origin.y - maxvalue / (float)magnitude * 100 && y < top)
					{
						selected[i] = true;
						break;
					}
				}
			}
			else
			{
				Arrays.fill(selected, Boolean.FALSE);
			}
		}
		else if(event.getAction() == MotionEvent.ACTION_MOVE)
		{			
			if(Math.abs(x-xPre)>10 || Math.abs(y-yPre)>10)
			{
				moveFlag = true;
				if (x < legend.x)
				{
					if (gesture != GESTURE.ZOOM)
					{
						if (Math.abs(x-xPre) > 0 && Math.abs(y-yPre) < 10)
						{
							gesture = GESTURE.SCROLL;
							offset += 1 * (x - xPre);
						}
						else if (Math.abs(x-xPre) < 10 && y-yPre > 0)
						{
							for (int i = 0; i < data.size(); i++)
							{
								float left = origin.x + i * (num_countries * width + interval) + offset;
								float right = origin.x + i * (num_countries * width + interval) + num_countries * width + offset;
								float top = origin.y;
								float maxvalue = Math.max(data.get(i).get(1), Math.max(data.get(i).get(2), data.get(i).get(3)));
								if (x > left && x < right && y > origin.y - maxvalue / (float)magnitude * 100 && y < top && remove_index == -1)
								{
									remove_index = i;
									break;
								}
							}
							if (y > origin.y && remove_index != -1)
							{
								data.remove(remove_index);
								remove_index = -1;
								Arrays.fill(selected, Boolean.FALSE);
							}
						}
						
						moveFlag = true;
						xPre = x;
						yPre = y;
					}
					else
					{
						float newLineDistance = (float) Math.sqrt(Math.pow(p - x, 2) + Math.pow(q - y, 2));
						
						for (int i = 0; i < data.size(); i++)
						{
							float left = origin.x + i * (num_countries * width + interval) + offset;
							float right = origin.x + (i + 1)  * (num_countries * width + interval) + offset;
							float top = origin.y;
							if ((p + x) / 2 > left && (p + x) / 2 < right && (q + y) / 2 < top && zoom_index == -1)
							{
								zoom_index = i;
								break;
							}
						}
						
						if (newLineDistance > oldLineDistance)
						{
							if (width < 50 && zoom_index != -1)
							{
								width += 1;
								offset -= num_countries * (zoom_index + 0.5);
							}
						}
						else
						{
							if (width > 10 && zoom_index != -1)
							{
								width -= 1;
								offset += num_countries * (zoom_index + 0.5);
							}
						}
						oldLineDistance = newLineDistance;
					}
				}
			}
		}
		else if(event.getAction() == MotionEvent.ACTION_UP)
		{
			if (gesture == GESTURE.SCROLL)
			{
				Arrays.fill(selected, Boolean.FALSE);
				gesture = GESTURE.NONE;
			}
			else if(gesture == GESTURE.ZOOM)
			{
				Arrays.fill(selected, Boolean.FALSE);
				gesture = GESTURE.NONE;
				oldLineDistance = 0;
				zoom_index = -1;
			}
			else
			{
				if (moveFlag)
				{
					Arrays.fill(selected, Boolean.FALSE);
					for(int i = 0; i < countries.size(); i++)
					{
						if (rects.get(i).intersects(xInit, yInit, event.getX(), event.getY()))
						{
							visible[i] = !visible[i];
							if (visible[i])
							{
								num_countries += 1;
							}
							else
							{
								num_countries -= 1;
							}
						}
					}
				}
				else
				{
					for (int i = 0; i < data.size(); i++)
					{
						float left = origin.x + i * (num_countries * width + interval) + offset;
						float right = origin.x + i * (num_countries * width + interval) + num_countries * width + offset;
						float top = origin.y;
						if (x > left && x < right && y < top)
						{
							selected[i] = false;
							break;
						}
					}
				}
			}
			
			moveFlag=false;	
			xInit = -1;
			yInit = -1;
			xPre = -1;
			yPre = -1;
		}
		return true;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mainViewDrawThread = new MainViewDrawThread(this);
		this.mainViewDrawThread.flag=true;
		mainViewDrawThread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean flag = true;
		mainViewDrawThread.flag=false;
        while (flag) {
            try {
            	mainViewDrawThread.join();
            	flag = false;
            } 
            catch (Exception e) {
            	e.printStackTrace();
            }
        }
	}

}
