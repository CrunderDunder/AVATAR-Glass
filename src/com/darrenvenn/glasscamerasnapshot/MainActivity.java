package com.darrenvenn.glasscamerasnapshot;

import java.io.File;
import java.util.List;

import com.google.android.glass.media.CameraManager;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity{
	
	// App responds to voice trigger "test the camera", takes a picture with GlassSnapshotActivity and then returns.
	
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final String FILE_NAME = "" + System.currentTimeMillis() + ".jpg";
	private static final String IMAGE_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + 
													"/DCIM/Camera/" + FILE_NAME;

	private boolean picTaken = false; // flag to indicate if we just returned from the picture taking intent
	private TextView text1;
	private TextView text2;
	
	private ProgressBar myProgressBar;
	protected boolean mbActive;
	
	final Handler myHandler = new Handler(); // handles looking for the returned image file
	private TextToSpeech mSpeech;
    
    private GestureDetector mGestureDetector;
    
	//Another location detection method
    LocationDetector myloc;
	double myLat = 0;
	double myLong = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.v(TAG,"creating activity");
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		setContentView(R.layout.activity_main);
        text1 = (TextView) findViewById(R.id.text1);
        text2 = (TextView) findViewById(R.id.text2);
        text1.setText("");
        text2.setText("");
        myProgressBar = (ProgressBar) findViewById(R.id.my_progressBar);
        LinearLayout llResult = (LinearLayout) findViewById(R.id.resultLinearLayout);
        TextView tvResult = (TextView) findViewById(R.id.tap_instruction);
        llResult.setVisibility(View.INVISIBLE);
        tvResult.setVisibility(View.INVISIBLE);
		myProgressBar.setVisibility(View.INVISIBLE);
		

		myloc = new LocationDetector(this);		
        
        // Even though the text-to-speech engine is only used in response to a menu action, we
        // initialize it when the application starts so that we avoid delays that could occur
        // if we waited until it was needed to start it up
        mSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                // Do nothing.
            }
        });
        
        mGestureDetector = createGestureDetector(this);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onResume() {
		Log.v(TAG, "In onResume");
		super.onResume();

		if (!picTaken) {
			//Temporary disable to test video
			Intent intent = new Intent(this, GlassSnapshotActivity.class);
	        intent.putExtra("imageFileName",IMAGE_FILE_PATH);
	        intent.putExtra("previewWidth", 640);
	        intent.putExtra("previewHeight", 360);
	        intent.putExtra("snapshotWidth", 1920);
	        intent.putExtra("snapshotHeight", 1080);
	        intent.putExtra("maximumWaitTimeForCamera", 2000);
		    startActivityForResult(intent,1);
			
			//Video intent works
//			Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//			startActivityForResult(intent, 2);
			
//			Log.v(TAG, "Before Intent");
//			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//			startActivityForResult(intent, 3);
//			Log.v(TAG, "After startActivityForResult");
		}
		else {
			// do nothing
		}
	}
	
	/*
     * Send generic motion events to the gesture detector
     */
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            return mGestureDetector.onMotionEvent(event);
        }
        return false;
    }
	
	private GestureDetector createGestureDetector(Context context) {
	    GestureDetector gestureDetector = new GestureDetector(context);
	        //Create a base listener for generic gestures
	        gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
	            @Override
	            public boolean onGesture(Gesture gesture) {
	                if (gesture == Gesture.TAP) {
	                    // do something on tap
	                	Log.v(TAG,"tap");
	                	//if (readyForMenu) {
	                		openOptionsMenu();
	               		//}
	                    return true;
	                } else if (gesture == Gesture.TWO_TAP) {
	                    // do something on two finger tap
	                    return true;
	                } else if (gesture == Gesture.SWIPE_RIGHT) {
	                    // do something on right (forward) swipe
	                    return true;
	                } else if (gesture == Gesture.SWIPE_LEFT) {
	                    // do something on left (backwards) swipe
	                    return true;
	                }
	                return false;
	            }
	        });
	        gestureDetector.setFingerListener(new GestureDetector.FingerListener() {
	            @Override
	            public void onFingerCountChanged(int previousCount, int currentCount) {
	              // do something on finger count changes
	            }
	        });
	        gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
	            @Override
	            public boolean onScroll(float displacement, float delta, float velocity) {
	                // do something on scrolling
	            	return false;
	            }
	        });
	        return gestureDetector;
	    }

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stop:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.v(TAG, "In onActivityResult");
		super.onActivityResult(requestCode, resultCode, data);
		picTaken = true;
		switch(requestCode) {
	    	case (1) : {
	    		if (resultCode == Activity.RESULT_OK) {
	    			Log.v(TAG,"onActivityResult"); 
	    	  
	    			File f = new File(IMAGE_FILE_PATH);
	    			if (f.exists()) {
	    				Log.v(TAG,"image file from camera was found");
	    				Log.v(TAG,"File direcotyr: " + f.getAbsolutePath());
					   
	    				//Gets image and previews it
	    				Bitmap b = BitmapFactory.decodeFile(IMAGE_FILE_PATH);
	    				Log.v(TAG,"bmp width=" + b.getWidth() + " height=" + b.getHeight());
	    				ImageView image = (ImageView) findViewById(R.id.bgPhoto);
	    				image.setImageBitmap(b);
				      
	    				//Sets text for card
	    				text1 = (TextView) findViewById(R.id.text1);
	    				text2 = (TextView) findViewById(R.id.text2);
	    				text1.setText("The image shown was saved successfully to a file named:");
	    				text2.setText("\n" + IMAGE_FILE_PATH);
				      
	    				//Sets layout and text on card
	    				LinearLayout llResult = (LinearLayout) findViewById(R.id.resultLinearLayout);
	    				llResult.setVisibility(View.VISIBLE);
	    				TextView line1 = (TextView) findViewById(R.id.titleOfWork);
	    				TextView line2 = (TextView) findViewById(R.id.Singer);
	    				TextView tap = (TextView) findViewById(R.id.tap_instruction);
	    				line1.setText("");
	    				line2.setText("");
	    				tap.setVisibility(View.VISIBLE);
				      
	//			  	    //Uploads to FTP server
	//			  	    Log.v(TAG, "Starting  FTP upload");
	//			  	    UploadToFTP ftp = new UploadToFTP();
	//			  	    Log.v(TAG, "File Path: " + Environment.getExternalStorageDirectory()); 
	//			  	    ftp.execute(IMAGE_FILE_PATH);
	//			  	    Log.v(TAG, "Done FTP upload");
				      
	    				//Try to do the same with http
	    				Log.v(TAG, "Starting POST upload");
	    				UploadHttpPost client = new UploadHttpPost();
	    				client.execute(IMAGE_FILE_PATH);
			      
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
	    	case (3) : {
	    		Log.v(TAG, "In case: 3");
	    		if (resultCode == RESULT_OK) {
	    			Log.v(TAG, "Picture response ok");
	    			String picturePath = data.getStringExtra(CameraManager.EXTRA_PICTURE_FILE_PATH);
	    			processPictureWhenReady(picturePath);
	    		} else {
	    			Log.v(TAG, "Picture response not ok");
	    		}
	    	}
		}
	}
	
	private void processPictureWhenReady(final String picturePath) {
		Log.v(TAG, "In processPicture");
		// TODO Auto-generated method stub
		final File pictureFile = new File(picturePath);

	    if (pictureFile.exists()) {
	        // The picture is ready; process it.
	    } else {
	        // The file does not exist yet. Before starting the file observer, you
	        // can update your UI to let the user know that the application is
	        // waiting for the picture (for example, by displaying the thumbnail
	        // image and a progress indicator).

	        final File parentDirectory = pictureFile.getParentFile();
	        FileObserver observer = new FileObserver(parentDirectory.getPath(),
	                FileObserver.CLOSE_WRITE | FileObserver.MOVED_TO) {
	            // Protect against additional pending events after CLOSE_WRITE
	            // or MOVED_TO is handled.
	            private boolean isFileWritten;

	            @Override
	            public void onEvent(int event, String path) {
	                if (!isFileWritten) {
	                    // For safety, make sure that the file that was created in
	                    // the directory is actually the one that we're expecting.
	                    File affectedFile = new File(parentDirectory, path);
	                    isFileWritten = affectedFile.equals(pictureFile);

	                    if (isFileWritten) {
	                        stopWatching();

	                        // Now that the file is ready, recursively call
	                        // processPictureWhenReady again (on the UI thread).
	                        runOnUiThread(new Runnable() {
	                            @Override
	                            public void run() {
	                                processPictureWhenReady(picturePath);
	                            }
	                        });
	                    }
	                }
	            }
	        };
	        observer.startWatching();
	    }
	    Log.v(TAG, "End of processPicture");
	}

	@Override
    protected void onDestroy() {

        //Close the Text to Speech Library
        if(mSpeech != null) {
            mSpeech.stop();
            mSpeech.shutdown();
            mSpeech = null;
            Log.d(TAG, "TTS Destroyed");
        }
        super.onDestroy();
    }	
}