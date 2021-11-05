package com.gabandriod.assignment;
			
import android.app.Activity;
import android.os.Bundle;

import android.content.Context;			//for drawing context related
import android.opengl.GLSurfaceView;	//for drawing surface view and all related
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;
import android.opengl.GLES32;
import android.view.GestureDetector;	//for detecting all gesture inputs
import android.view.GestureDetector.OnGestureListener;	//OnGestureListener
import android.view.GestureDetector.OnDoubleTapListener;	//for OnDoubleTapListener

import android.view.MotionEvent;

public class GLESView extends GLSurfaceView implements GLSurfaceView.Renderer,OnGestureListener,OnDoubleTapListener
{
	private final Context context;

	private GestureDetector gestureDetector;


	//constructor

	public GLESView(Context drawingContext)
	{
		super(drawingContext);
		context = drawingContext;

		//accordingly set EGLContext to current supported version of openGL-ES
		setEGLContextClientVersion(3);

		//set renderer for drawing on the GLSurfaceView
		setRenderer(this);

		//Render the view only when there is change in the drawing data
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);


		gestureDetector = new GestureDetector(context, this , null , false);
		gestureDetector.setOnDoubleTapListener(this);	//this means 'handler' window
	}



    @Override
    public void onSurfaceCreated(GL10 gl , EGLConfig config) 
    {

    	//Opengl ES version check
    	String version = gl.glGetString(GL10.GL_VERSION);
    	System.out.println("GAB:" + version);
        initialize(gl);
    }

    @Override
    public void onSurfaceChanged(GL10 unused , int width , int height)
    {
    	resize(width,height);
    }

    @Override
    public void onDrawFrame(GL10 unused)
    {
    	draw();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
    	int eventaction = e.getAction();
    	if(!gestureDetector.onTouchEvent(e))
    	{
    		super.onTouchEvent(e);
    	}

    	return true;

    }

    @Override
    public boolean onDoubleTap(MotionEvent e)
    {
    	System.out.println("GAB: "+"Double Tap");
    	return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e)
    {
        // Do Not Write Any code Here Because Already Written 'onDoubleTap'
        return(true);
    }
    
    // abstract method from OnDoubleTapListener so must be implemented
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e)
    {
        System.out.println("GAB: "+"Single Tap");
        return(true);
    }
        
    // abstract method from OnGestureListener so must be implemented
    @Override
    public boolean onDown(MotionEvent e)
    {
        // Do Not Write Any code Here Because Already Written 'onSingleTapConfirmed'
        return(true);
    }
    
    // abstract method from OnGestureListener so must be implemented
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        return(true);
    }
    
    // abstract method from OnGestureListener so must be implemented
    @Override
    public void onLongPress(MotionEvent e)
    {
        System.out.println("GAB: "+"Long Press");
    }
    
    // abstract method from OnGestureListener so must be implemented
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        System.out.println("GAB: "+"Scroll");
        System.exit(0);
        return(true);
    }
    
    // abstract method from OnGestureListener so must be implemented
    @Override
    public void onShowPress(MotionEvent e)
    {
    }
    
    // abstract method from OnGestureListener so must be implemented
    @Override
    public boolean onSingleTapUp(MotionEvent e)
    {
        return(true);
    }

    private void initialize(GL10 gl)
    {
    	GLES32.glClearColor(0.0f,0.0f,1.0f,1.0f);

    }

    private void resize(int width , int height)
    {
    	GLES32.glViewport(0,0,width,height);
    }

    public void draw()
    {
    	GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);
    
    }
}
