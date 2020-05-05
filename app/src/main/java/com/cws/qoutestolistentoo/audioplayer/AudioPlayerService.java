package com.cws.qoutestolistentoo.audioplayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.cws.qoutestolistentoo.R;
import com.cws.qoutestolistentoo.Text;


public class AudioPlayerService extends Service implements AudioPlayerServiceInterface, AudioManager.OnAudioFocusChangeListener{
	private String TAG = "AudioPlayer";

	public final static int PAUSED = 0;
	public final static int PLAYING = 1;

	private int state;

	private AudioPlayerServiceBinder mAudioPlayerServiceBinder;
	private Queue mNowPlaying;
	private MediaPlayer mMediaPlayer;
	private OnCompletionListener mCompletionListener;

	private HeadPhoneBroadcastReceiver mHeadPhoneBroadcastReceiver;
	private TextView textViewCC;
	private TextView titleTextView;
	private ScrollView scroller;
	private AsyncTask<Void, Void, Void> seekBarChanger;
	private Thread passingTime;

	NotificationManager notificationManager;

	@Override
	public IBinder onBind(Intent intent) {
		mAudioPlayerServiceBinder = new AudioPlayerServiceBinder(this, this);
		state = PLAYING;

		mNowPlaying = new Queue();			// setup the now playing queue
		mMediaPlayer = new MediaPlayer();	// setup the media player

		mCompletionListener = new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				playNext();
			}
		};
		mMediaPlayer.setOnCompletionListener(mCompletionListener);

		mHeadPhoneBroadcastReceiver = new HeadPhoneBroadcastReceiver();
		registerReceiver(mHeadPhoneBroadcastReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		mHeadPhoneBroadcastReceiver.registerMusicPlayerService(this);

		return mAudioPlayerServiceBinder;
	}


	public void setQueue(ArrayList<Text> texts){
		mNowPlaying.clearQueue();
		mNowPlaying.setQueue(texts);
	}


	public synchronized void play() {
	    if (successfullyRetrievedAudioFocus()) {
            state = PLAYING;
            mMediaPlayer.start();
			CreateNotification.createNotification(getApplicationContext(), mNowPlaying.getCurrentText(), R.drawable.ic_simple_pause_button_white_foreground, 0);
        }
	}

	public synchronized void play(int position) {
		Text text = mNowPlaying.playGet(position);
		playFetched(text, true);
		CreateNotification.createNotification(getApplicationContext(), mNowPlaying.getCurrentText(), R.drawable.ic_simple_pause_button_white_foreground, 0);
	}

	public synchronized void playNext() {
	    Text temp = mNowPlaying.next();
		if(temp != null) {
		    textViewCC.setText(temp.getCC());
		    titleTextView.setText(temp.getTitle());
			playFetched(temp, true);
		}
	}

    public synchronized void playLast() {
        Text temp = mNowPlaying.last();
        if(temp != null) {
            textViewCC.setText(temp.getCC());
            titleTextView.setText(temp.getTitle());
            playFetched(temp, true);
        }
    }

	public void createNotificationChannel(){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
			NotificationChannel channel = new NotificationChannel(CreateNotification.CHANNEL_ID, "Dev", NotificationManager.IMPORTANCE_LOW);

			notificationManager = getSystemService(NotificationManager.class);
			if(notificationManager != null){
				notificationManager.createNotificationChannel(channel);
			}
		}
	}

	private synchronized void playFetched(final Text text, final boolean beginPlaying) {
		state = PLAYING;
		mMediaPlayer.stop();
		mMediaPlayer.reset();
		try {
			mMediaPlayer = MediaPlayer.create(getApplicationContext(), text.getResid());

			NotificationConfig();

			runScroll();
			play();
			if (!beginPlaying) changeState();

			mMediaPlayer.prepare();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void runScroll() {
		if (seekBarChanger != null)
			seekBarChanger.cancel(false);
		seekBarChanger = null;

		passingTime = new Thread(new Runnable() {
			@Override
			public void run() {
				int tempPos;
				while (mMediaPlayer != null){
					try {
						if (mMediaPlayer.getCurrentPosition() >= mMediaPlayer.getDuration()){
							playNext();
						}

						tempPos = (int) (textViewCC.getBottom() * (((float) mMediaPlayer.getCurrentPosition())/ ((float)mMediaPlayer.getDuration()))) - 400;
						if (tempPos>0)
							scroller.smoothScrollTo(0,tempPos);
						else
							scroller.smoothScrollTo(0,0);
						Message msg = new Message();
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
				}

			}
		});
		passingTime.start();
	}

	public void pause() {
		state = PAUSED;
		mMediaPlayer.pause();
		CreateNotification.createNotification(getApplicationContext(), mNowPlaying.getCurrentText(), R.drawable.ic_simple_play_button_white_foreground, 0);
	}

	public int changeState() {
		switch(state){
		case PLAYING:
			pause(); break;
		case PAUSED:
			play(); break;
		}

		return state;									// return the value of the changed state as confirmation
	}

	public int getState() {
		return state;
	}


	@Override
	public void skipToPoint(int point) {
		mMediaPlayer.seekTo(point);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		unregisterReceiver(mHeadPhoneBroadcastReceiver);


		if (seekBarChanger != null)
			seekBarChanger.cancel(false);
		seekBarChanger = null;

		mMediaPlayer.stop();
		mMediaPlayer.reset();
		mMediaPlayer.release();
		Toast.makeText(this, "unBind with state: " + ((state == PLAYING) ? "PLAYING" : "PAUSED"), Toast.LENGTH_SHORT).show();
		return true;
	}

	public void registerGui(TextView textViewCC, TextView titleTextView, ScrollView scroller) {
		this.textViewCC = textViewCC;
		this.titleTextView = titleTextView;
		this.scroller = scroller;
	}

    public boolean successfullyRetrievedAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        return result == AudioManager.AUDIOFOCUS_GAIN;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch( focusChange ) {
            case AudioManager.AUDIOFOCUS_LOSS: {
                if( state == PLAYING ) {
                    changeState();
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                if( state == PLAYING ) {
                    pause();
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                if( mMediaPlayer != null ) {
                    mMediaPlayer.setVolume(0.3f, 0.3f);
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_GAIN: {
                if( mMediaPlayer != null ) {
                    if( state == PAUSED ) {
                        changeState();
                    }
                    mMediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;
            }
        }
    }

    private void NotificationConfig(){
		createNotificationChannel();
		registerReceiver(notificationBroadcastReceiver, new IntentFilter("NotificationAction"));

	}

	BroadcastReceiver notificationBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getExtras().getString("actionname");

			switch (action) {
				case CreateNotification.ACTION_PLAY:
					changeState();
					break;
			}
		}
	};
}
