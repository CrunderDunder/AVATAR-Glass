package com.sate2014.avatar.glass;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.darrenvenn.glasscamerasnapshot.R;
import com.glass.cuxtomcam.CuxtomCamActivity;
import com.glass.cuxtomcam.constants.CuxtomIntent;
import com.glass.cuxtomcam.constants.CuxtomIntent.CAMERA_MODE;
import com.google.android.glass.media.CameraManager;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends Activity{
	
	private static final String TAG = MainActivity.class.getSimpleName();
	
	public String finalfilepath = "";
	public String finaltype = "";
	
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
		String filename = "" + System.currentTimeMillis();
		
		Intent intent = new Intent(getApplicationContext(), CuxtomCamActivity.class);
		intent.putExtra(CuxtomIntent.CAMERA_MODE, CAMERA_MODE.PHOTO_MODE);
		intent.putExtra(CuxtomIntent.ENABLE_ZOOM, true);
		intent.putExtra(CuxtomIntent.FILE_NAME, filename);
		intent.putExtra(CuxtomIntent.FOLDER_PATH, Constants.IMAGE_PATH);
		finalfilepath = Constants.IMAGE_PATH + filename + ".jpg";
		startActivityForResult(intent, Constants.IMAGE_REQUEST);
	}
	
	private void recordVideo() {
		
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//		//The MediaStore.EXTRA_OUTPUT seems to be bugged
//		Uri outputUri = Uri.fromFile(new File(VIDEO_PATH + filename));
//		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
		startActivityForResult(intent, Constants.VIDEO_REQUEST);
		
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
		String filename = "" + System.currentTimeMillis();
		Intent intent = new Intent(getApplicationContext(), RecordAudio.class);
		intent.putExtra("filename", filename);
		intent.putExtra("filepath", Constants.AUDIO_PATH);
		finalfilepath = Constants.AUDIO_PATH + filename + ".pcm";
		startActivityForResult(intent, Constants.AUDIO_REQUEST);
	}
	
	private void recordText() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		startActivityForResult(intent, Constants.SPEAK_REQUEST);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.v(TAG, "In onActivityResult");
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode) {
	    	case (Constants.IMAGE_REQUEST) : {
	    		if (resultCode == Activity.RESULT_OK) {
	    			getLocation();
	    		} else {
	    			Log.v(TAG,"onActivityResult returned bad result code");
	    		}
	    		break;
	    	} 
	    	case (Constants.VIDEO_REQUEST) : {
	    		if (resultCode == Activity.RESULT_OK){
	    			finalfilepath = data.getStringExtra(CameraManager.EXTRA_VIDEO_FILE_PATH);
	    			getLocation();
	    			
	    		} else {
	    			Log.v(TAG, "Bad result code");
	    		}
	    		break;
	    	} case (Constants.AUDIO_REQUEST) : {
	    		if (resultCode == RESULT_OK) {
	    			getLocation();
	    		} else {
	    			Log.v(TAG, "Audio response not ok");
	    		}
	    		break;
	    	} case (Constants.SPEAK_REQUEST) : {
	    		if (resultCode == Activity.RESULT_OK) {
		        	List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
		        	String textResult = results.get(0);
		        	try {
		        		String filename = "" + System.currentTimeMillis() + ".txt";
		        		File dir = new File(Constants.SPEAK_PATH);
		        		if (!dir.exists()) {
		        			dir.mkdirs();
		        		}
		        		File f = new File(dir, filename);
		        		FileOutputStream fos = new FileOutputStream(f);
						fos.write(textResult.getBytes());
						fos.close();
						finalfilepath = Constants.SPEAK_PATH + filename;
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
		        	
		        	Log.v(TAG, "CASE 4 result: " + results.get(0).toString());
		        	
		        	getLocation();
	    		} else {
	    			Log.v(TAG, "bad result code");
	    		}
	    		break;
	    	} case (Constants.SQL_UPLOAD) : {
	    		if (resultCode == Activity.RESULT_OK) {
	    			Toast confirmToast = Toast.makeText(getApplicationContext(), "Point sent", Toast.LENGTH_SHORT);
	    			confirmToast.show();
	    		} else if (resultCode == Activity.RESULT_CANCELED) {
	    			Toast failToast = Toast.makeText(getApplicationContext(), "Point canceled", Toast.LENGTH_SHORT);
	    			failToast.show();
	    		}
	    	}
		}
	}
	

	private void getLocation() {
		if (myloc.canGetLocation) {
			myLat = myloc.getLatitude();
			myLong = myloc.getLongitude();
			Log.v(TAG, "Location data: " + Double.toString(myLat) + " : " + Double.toString(myLong));
		}
		
		Log.v(TAG, "finalfilepath: " + finalfilepath);
		Intent sqlIntent = new Intent(getApplicationContext(), UploadToSQL.class);
		sqlIntent.putExtra("latitude", myLat);
		sqlIntent.putExtra("longitude", myLong);
		sqlIntent.putExtra("filepath", finalfilepath);
		startActivityForResult(sqlIntent, Constants.SQL_UPLOAD);
	}

	@Override
    protected void onDestroy() {
        super.onDestroy();
    }	
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	//This is the class that gets http commands to actually enter data into the sql server
		public static class HttpSender extends AsyncTask<String, String, String> {

			//There are five paramters that are passed here.
			//point name, latitude, longitude, altitude, and its url
			@Override
			protected String doInBackground(String... params) {
				params[0] = params[0].replaceAll(" ", "%20");
				params[4] = params[4].replaceAll(" ", "%20");
				try {
					HttpClient client = new DefaultHttpClient();
					//This returns the entire path with all of the preceding folders in the android filesystem
					File path = new File(params[4]);
					//Gets just the filename
					String fileName = path.getName();
					//Sets the url for the media on the FTP server
					String url = "http://" + Constants.FTP_SERVER_ADDRESS + "/media/" + fileName;
					//Executes the server script.
					HttpGet get = new HttpGet(new URI(
							"http://" + Constants.SQL_SERVER_ADDRESS + "/sqlAddRow.php?name=" + params[0]
									+ "&lat=" + params[1] + "&long=" + params[2]
									+ "&alt=" + params[3] + "&url=" + url));
					System.out.println(
							"http://" + Constants.SQL_SERVER_ADDRESS + "/sqlAddRow.php?name=" + params[0]
									+ "&lat=" + params[1] + "&long=" + params[2]
									+ "&alt=" + params[3] + "&url=" + url);
					client.execute(get);
					System.out.println("THINGS ARE HAPPENING");
				} catch (Exception e) {
					System.out.println("SOMETHING WENT BOOM!");
					e.printStackTrace();
				}
				return null;
			}
		}
}