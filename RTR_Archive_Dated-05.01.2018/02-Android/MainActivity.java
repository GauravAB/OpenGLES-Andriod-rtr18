package com.astromedicomp.winvm_gles;

// default supplied packages by android SDK
import android.app.Activity;
import android.os.Bundle;

// later added packages
import android.view.Window; // for "Window" class
import android.view.WindowManager; // for "WindowManager" class
import android.content.pm.ActivityInfo; // for "ActivityInfo" class

public class MainActivity extends Activity
{
    private GLESView glesView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // this is done to get rid of ActionBar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // this is done to make Fullscreen
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        
        // force activity window orientation to Landscape
        MainActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        glesView=new GLESView(this);
        
        // set view as content view of the activity
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
