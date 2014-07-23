package com.darrenvenn.glasscamerasnapshot;

import java.io.File;

import com.glass.cuxtomcam.CuxtomCamActivity;
import com.glass.cuxtomcam.constants.CuxtomIntent;
import com.glass.cuxtomcam.constants.CuxtomIntent.CAMERA_MODE;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.net.Uri;
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
import android.widget.TextView;

public class MainActivity extends Activity{
	
	private static final String TAG = MainActivity.class.getSimpleName();
	String filename = "" + System.currentTimeMillis() + ".jpg";
	private static final String IMAGE_PATH = Environment.getExternalStorageDirectory().getPath() + 
													"/DCIM/Camera/Images/";
	private static final String VIDEO_PATH = Environment.getExternalStorageDirectory().getPath() + 
													"/DCIM/Camera/Videos/";

	private final Handler mHandler = new Handler();
	
    LocationDetector myloc;
	double myLat = 0;
	double myLong = 0;
	
	//Audio stuff
//	private static final int SAMPLING_RATE = 44100; 
//	private WaveformView mWaveformView;
//	private TextView mDecibelView;
//	
//	private RecordingThread mRecordingThread;
//	private int mBufferSize;
//	private short[] mAudioBuffer;
//	private String mDecibelFormat;
	//\\Audio stuff
	
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
//		Intent intent = new Intent(this, GlassSnapshotActivity.class);
//        intent.putExtra("imageFileName",IMAGE_FILE_PATH);
//        intent.putExtra("previewWidth", 640);
//        intent.putExtra("previewHeight", 360);
//        intent.putExtra("snapshotWidth", 1920);
//        intent.putExtra("snapshotHeight", 1080);
//        intent.putExtra("maximumWaitTimeForCamera", 2000);
//	    startActivityForResult(intent,1);
		filename = "" + System.currentTimeMillis();
		
		Intent intent = new Intent(getApplicationContext(), CuxtomCamActivity.class);
		intent.putExtra(CuxtomIntent.CAMERA_MODE, CAMERA_MODE.PHOTO_MODE);
		intent.putExtra(CuxtomIntent.ENABLE_ZOOM, true);
		intent.putExtra(CuxtomIntent.FILE_NAME, filename);
		intent.putExtra(CuxtomIntent.FOLDER_PATH, IMAGE_PATH);
		startActivityForResult(intent, 1);
	}
	
	private void recordVideo() {
		//Video is saved but can't get file name
		filename = "" + System.currentTimeMillis() + ".mp4";
		//TODO: Allow for variable length, maybe show preview.
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//		//The MediaStore.EXTRA_OUTPUT seems to be bugged
//		Uri outputUri = Uri.fromFile(new File(VIDEO_PATH + filename));
//		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
		startActivityForResult(intent, 2);
		
//		Cuxtomcam doesn't seem to work properly. No idea
//		Intent intent = new Intent(getApplicationContext(), CuxtomCamActivity.class);
//		intent.putExtra(CuxtomIntent.CAMERA_MODE, CAMERA_MODE.VIDEO_MODE);
//		intent.putExtra(CuxtomIntent.ENABLE_ZOOM, true);
//		intent.putExtra(CuxtomIntent.FILE_NAME, filename);
//		intent.putExtra(CuxtomIntent.VIDEO_DURATION, 10);
//		intent.putExtra(CuxtomIntent.FOLDER_PATH, VIDEO_PATH);
//		startActivityForResult(intent, 2);
	}
	
	private void recordAudio() {
		//Implement Later
		//Audio isnt' very loud
		//Can't find anything to do this yet
		//Could record video but only save sound
		
//		setContentView(R.layout.layout_audio);
//		
//		mWaveformView = (WaveformView) findViewById(R.id.waveform_view);
//		mDecibelView = (TextView) findViewById(R.id.decibel_view);
//		
//        // Compute the minimum required audio buffer size and allocate the buffer.
//        mBufferSize = AudioRecord.getMinBufferSize(SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO,
//                AudioFormat.ENCODING_PCM_16BIT);
//        mAudioBuffer = new short[mBufferSize / 2];
//
//        mDecibelFormat = getResources().getString(R.string.decibel_format);
//        
//        mRecordingThread = new RecordingThread();
//        mRecordingThread.start();
		Intent intent = new Intent(getApplicationContext(), RecordAudio.class);
		startActivityForResult(intent, 3);
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
	    			File f = new File(IMAGE_PATH + filename);
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
	    		//Video is saved but can't get file name
	    		//Cuxtomcam isn't working properly for videos
	    		if (resultCode == RESULT_OK) {
	    			Log.v(TAG, "Video response ok");
	    			if (data.getData() == null) {
	    				Log.v(TAG, "Video data null");
	    			} else {
	    				Log.v(TAG, "Video data not null");
	    			}
	    		} else {
	    			Log.v(TAG, "Video response not ok");
	    		}
	    		break;
	    	} case (3) : {
	    		if (resultCode == RESULT_OK) {
	    			Log.v(TAG, "Audio response ok");
	    		} else {
	    			Log.v(TAG, "Audio response not ok");
	    		}
	    	}
		}
	}
	

	@Override
    protected void onDestroy() {
        super.onDestroy();
    }	
	
	@Override
	protected void onPause() {
		super.onPause();
	}
}