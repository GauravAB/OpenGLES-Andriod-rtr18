package com.gabandroid.ThreeDimensionalAnimation;

import android.app.Activity;
import android.os.Bundle;

import android.view.Window;
import android.view.WindowManager;
import android.content.pm.ActivityInfo;
import android.graphics.Color;

public class MainActivity extends Activity 
{

    private GLESView glesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
    
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

    	//force activity window orientation to landscape
    	MainActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    	//set black background color

        glesView = new GLESView(this);
		setContentView(glesView);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override 
    protected void onResume()
    {
        super.onResume();
    }

}
