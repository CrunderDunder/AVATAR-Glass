package com.darrenvenn.glasscamerasnapshot;

import java.io.File;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;

public class MainActivity extends Activity{
	
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final String FILE_NAME = "" + System.currentTimeMillis() + ".jpg";
	private static final String IMAGE_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + 
													"/DCIM/Camera/" + FILE_NAME;

	private final Handler mHandler = new Handler();
	
    LocationDetector myloc;
	double myLat = 0;
	double myLong = 0;
	
    private AudioManager mAudioManager;
	
	private final GestureDetector.BaseListener mBaseListener = new GestureDetector.BaseListener() {
		@Override
		public boolean onGesture(Gesture gesture) {
			if (gesture == Gesture.TAP) {
				mAudioManager.playSoundEffect(Sounds.TAP);
				openOptionsMenu();
				return true;
			} else {
				return false;
			}
		}
	};
	
    private GestureDetector mGestureDetector;

	
	//Don't know if I really still need all of these
//	private boolean picTaken = false; // flag to indicate if we just returned from the picture taking intent
//	private TextView text1;
//	private TextView text2;
//	
//	private ProgressBar myProgressBar;
//	protected boolean mbActive;
//	
////	final Handler myHandler = new Handler(); // handles looking for the returned image file
//	private TextToSpeech mSpeech;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.v(TAG,"creating activity");
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		setContentView(R.layout.avatar_main);
		
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = new GestureDetector(this).setBaseListener(mBaseListener);
		
        myloc = new LocationDetector(this);	
		
	}

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		return mGestureDetector.onMotionEvent(event);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case R.id.img:
        		mHandler.post(new Runnable() {
        			@Override
        			public void run() {
        				recordImage();
        			}
        		});
        		return true;
        		
        	case R.id.vid:
        		mHandler.post(new Runnable() {
        			@Override
        			public void run() {
        				recordVideo();
        			}
        		});
        		return true; 
        		
        	case R.id.audio:
        		mHandler.post(new Runnable() {
        			@Override
        			public void run() {
        				recordAudio();
        			}
        		});
        		return true; 
        		
        	case R.id.txt:
        		mHandler.post(new Runnable() {
        			@Override
        			public void run() {
        				recordText();
        			}
        		});
        		return true; 
        		
            case R.id.exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	
	private void recordImage() {
		//TODO: Add a viewfinder, allow tap to capture, show picture after
		Intent intent = new Intent(this, GlassSnapshotActivity.class);
        intent.putExtra("imageFileName",IMAGE_FILE_PATH);
        intent.putExtra("previewWidth", 640);
        intent.putExtra("previewHeight", 360);
        intent.putExtra("snapshotWidth", 1920);
        intent.putExtra("snapshotHeight", 1080);
        intent.putExtra("maximumWaitTimeForCamera", 2000);
	    startActivityForResult(intent,1);
	}
	
	private void recordVideo() {
		//TODO: Allow for variable length, maybe show preview.
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		startActivityForResult(intent, 2);
	}
	
	private void recordAudio() {
		//Implement Later
	}
	
	private void recordText() {
		//Implement Later
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.v(TAG, "In onActivityResult");
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode) {
	    	case (1) : {
	    		if (resultCode == Activity.RESULT_OK) {
	    			File f = new File(IMAGE_FILE_PATH);
	    			if (f.exists()) {
	    				Log.v(TAG,"image file from camera was found");
	    				Log.v(TAG,"File direcotyr: " + f.getAbsolutePath());
	    			}
	    			if (myloc.canGetLocation) {
	    				myLat = myloc.getLatitude();
	    				myLong = myloc.getLongitude();
	    				Log.v(TAG, "Location data: " + Double.toString(myLat) + " : " + Double.toString(myLong));
	    			}
	    		} else {
	    			Log.v(TAG,"onActivityResult returned bad result code");
	    			finish();
	    		}
	    		break;
	    	} 
	    	case (2) : {
	    		if (resultCode == RESULT_OK) {
	    			Log.v(TAG, "Video response ok");
	    		} else {
	    			Log.v(TAG, "Video response not ok");
	    		}
	    		break;
	    	}
		}
	}
	

	@Override
    protected void onDestroy() {
        super.onDestroy();
    }	
}