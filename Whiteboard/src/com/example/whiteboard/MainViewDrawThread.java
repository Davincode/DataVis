package com.example.whiteboard;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class MainViewDrawThread extends Thread{
	MainView myMainView;
	private int sleepSpan = 40;
	boolean flag = true;
	private SurfaceHolder surfaceHolder;
	public MainViewDrawThread(MainView myMainView)
	{
		this.myMainView = myMainView;
		surfaceHolder = myMainView.getHolder();
	}
	
	public void run() {
		Canvas c;
        while (this.flag) {
            c = null;
            try {
                c = this.surfaceHolder.lockCanvas(null);
                synchronized (this.surfaceHolder) {
                	myMainView.onDraw(c);
                }
            } finally {
                if (c != null) {                	
                    this.surfaceHolder.unlockCanvasAndPost(c);
                }
            }
            try{
            	Thread.sleep(sleepSpan);
            }catch(Exception e){
            	e.printStackTrace();
            }
        }
	}
	public void setFlag(boolean flag){
		this.flag = flag;
	}
}

