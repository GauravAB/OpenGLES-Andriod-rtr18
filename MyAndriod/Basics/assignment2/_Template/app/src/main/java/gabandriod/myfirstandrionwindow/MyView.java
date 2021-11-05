package gabandriod.helloWorldWithClass;

import android.app.Activity;
import android.os.Bundle;

import android.content.Context;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.view.Gravity;
import android.graphics.Color;


public class MyView extends TextView
{

    MyView(Context context)
    {
        super(context);

        setTextColor(Color.rgb(255,128,0));
        setTextSize(60);
        setText("Hello World !!!");
        setGravity(Gravity.CENTER);
    }

};
