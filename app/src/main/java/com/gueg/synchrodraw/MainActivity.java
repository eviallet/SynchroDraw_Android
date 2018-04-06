package com.gueg.synchrodraw;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

public class MainActivity extends AppCompatActivity {

    ConnexionHandler client;
    EditText ip;
    EditText port;
    Button btn;
    ImageView v;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main_connection);

        client = new ConnexionHandler(this);

        ip = findViewById(R.id.edittext_ip);
        port = findViewById(R.id.edittext_port);
        btn = findViewById(R.id.connect);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });
    }

    private void connect() {
        client.start();
        client.connect(ip.getText().toString(), Integer.decode(port.getText().toString()));
    }

    public void connected() {
        Log.d(":-:","Connected");

        setContentView(R.layout.activity_main_connected);

        v = findViewById(R.id.mainView);

        v.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        client.sendClic(convertPos(event.getX(), v.getWidth(), 1920f), convertPos(event.getY(), v.getHeight(), 1080f));
                        return true;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        // TOOD pinch to zoom, CTRL, shift...
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        client.sendMove(convertPos(event.getX(), v.getWidth(), 1920f), convertPos(event.getY(), v.getHeight(), 1080f));
                        return true;
                    case MotionEvent.ACTION_UP:
                        client.sendRelease(convertPos(event.getX(), v.getWidth(), 1920f), convertPos(event.getY(), v.getHeight(), 1080f));
                        return true;
                }
                return false;
            }
        });

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm!=null)
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }


    private int convertPos(float pos, float viewSize, float destSize) {
        return (int)(pos*destSize/viewSize);
    }

    public void error() {
        Log.d(":-:","Error");
        client.interrupt();
    }

    public void setScreenImage(String b64) {
        byte[] imageBytes = Base64.decode(b64, Base64.DEFAULT);
        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        v.setImageBitmap(decodedImage);
    }


    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.refresh:
                client.sendAction(ConnexionHandler.SEP_REFRESH);
                break;
            case R.id.undo:
                client.sendAction(ConnexionHandler.SEP_UNDO);
                break;
            case R.id.redo:
                client.sendAction(ConnexionHandler.SEP_REDO);
                break;
        }
    }
}
