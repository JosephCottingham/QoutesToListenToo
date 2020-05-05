package com.cws.qoutestolistentoo;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import com.cws.qoutestolistentoo.audioplayer.*;

import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
    private int index,currentState;
    private ArrayList<Text> textList;

    private TextView CCTextView, authorTextView, titleTextView;

    private ScrollView scroller;
    private Intent serviceIntent;
    private ImageView backBtn, playBtn, forwardBtn;

    boolean mBound = false;

    AudioPlayerService mService;
    AudioPlayerServiceBinder mBinder;
    ServiceConnection mConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                stopService(serviceIntent);
                startActivity(new Intent(PlayerActivity.this, MainActivity.class));
            }
        });

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        index = bundle.getInt("Index",0);
        String[] CCs = bundle.getStringArray("CC");
        String[] authors = bundle.getStringArray("author");
        String[] titles = bundle.getStringArray("title");
        int[] resids = bundle.getIntArray("resid");

        textList = new ArrayList<Text>();
        for (int x = 0; x < authors.length; x++){
            textList.add(new Text(authors[x], titles[x], CCs[x], resids[x]));
        }

        CCTextView = findViewById(R.id.CC);
        CCTextView.setText(textList.get(index).getCC());
        titleTextView = findViewById(R.id.title);
        titleTextView.setText(textList.get(index).getTitle());
        authorTextView = findViewById(R.id.author);
        authorTextView.setText(textList.get(index).getAuthor());

        playBtn = findViewById(R.id.playBtn);
        backBtn = findViewById(R.id.backBtn);
        forwardBtn = findViewById(R.id.forwardBtn);
        scroller = findViewById(R.id.scroller);

        defineServiceConnection();
        serviceIntent = new Intent(PlayerActivity.this, AudioPlayerService.class);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void defineServiceConnection(){
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                startService(new Intent(PlayerActivity.this, AudioPlayerService.class));
                mBinder = (AudioPlayerServiceBinder) iBinder;
                mService = mBinder.getService();
                currentState = mService.getState();
                playBtnClickListener();
                mService.registerGui(CCTextView, titleTextView, scroller);
                mBound = true;
                mService.setQueue(textList);
                mService.play(index);
                mService.pause();
                playBtn.setClickable(true);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mBound = false;
            }
        };
    }


    public void playBtnClickListener() {
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBound && mService.successfullyRetrievedAudioFocus()) {
                    currentState = mService.changeState();

                    switch (currentState){
                        case AudioPlayerService.PLAYING:
                            playBtn.setImageBitmap(BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_pause));
                            break;
                        case AudioPlayerService.PAUSED:
                            playBtn.setImageBitmap(BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_play));
                            break;
                    }

                }
                Log.d("STATE", "onClick: " + currentState);
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.playLast();
            }
        });
        forwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.playNext();
            }
        });
    }
}
