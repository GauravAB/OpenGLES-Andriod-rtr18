package gabandriod.myFirstTextOut;

//default packages
import android.app.Activity;
import android.os.Bundle;

// later added packages
import android.view.Window;
import android.view.WindowManager;
import android.content.pm.ActivityInfo;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;


public class MainActivity extends Activity
 {

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
			super.onCreate(savedInstanceState);
	       //     setContentView(R.layout.activity_main);


    	requestWindowFeature(Window.FEATURE_NO_TITLE);

    	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

    	MainActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		getWindow().getDecorView().setBackgroundColor(Color.rgb(0,0,0));

        //green coloured hello world
    	TextView myTextView = new TextView(this);
    	myTextView.setText("Hello World!");
    	myTextView.setTextSize(60);
    	myTextView.setTextColor(Color.GREEN);
    	myTextView.setGravity(Gravity.CENTER);

    	setContentView(myTextView);

    }
}





