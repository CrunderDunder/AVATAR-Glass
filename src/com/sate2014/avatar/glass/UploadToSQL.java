package com.sate2014.avatar.glass;

import java.io.File;
import java.util.List;

import com.darrenvenn.glasscamerasnapshot.R;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

//This class prepares data to be sent to the SQL server
public class UploadToSQL extends Activity {
	
	private static final String TAG = MainActivity.class.getSimpleName();

	public String ptFilePath;
	public double ptLat;
	public double ptLng;
	public String ptName;
	public String ptDescription;

	private File mediaFile;
	private String fileName;
	private String fileNameOnFTP;
	
	public TextView tvPtName;
	public TextView tvFileName;
	public TextView tvPtDesc;
	
	private final Handler mHandler = new Handler();
	
	private AudioManager mAudioManager;

	private Context c;
	
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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		c = super.getApplicationContext();
		setContentView(R.layout.avatar_sqlupload);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mGestureDetector =  new GestureDetector(this).setBaseListener(mBaseListener);
		
		tvPtName = (TextView) findViewById(R.id.pt_name_label);
		tvFileName = (TextView) findViewById(R.id.file_name_label);
		tvPtDesc = (TextView) findViewById(R.id.pt_desc_label);
		

		Intent thisIntent = getIntent();
		ptFilePath = thisIntent.getStringExtra("filepath");
		ptLat = thisIntent.getDoubleExtra("latitude", 39.7);
		ptLng = thisIntent.getDoubleExtra("longitude", -84.2);

		mediaFile = new File(ptFilePath);
		fileName = mediaFile.getName();
		ptName = fileName;
		fileNameOnFTP = "http://" + Constants.FTP_SERVER_ADDRESS + "/media/" + fileName;
		
		tvPtName.setText(fileName);
		tvFileName.setText(fileName);
		tvPtDesc.setText("No Description");
	}
	
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		return mGestureDetector.onMotionEvent(event);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.upload_sql, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.change_name:
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						getNewName();
					}
				});
				return true;
			case R.id.add_description:
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						addDescription();
					}
				});
				return true;
			case R.id.send:
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						sendPoint();
					}
				});
				return true;
			case R.id.exit:
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						exit();
					}
				});
				return true;
			default:
				return super.onOptionsItemSelected(item);
					
		}
	}
	
	private void getNewName() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		startActivityForResult(intent, Constants.NEW_NAME_REQUEST);
	}
	
	private void addDescription() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		startActivityForResult(intent, Constants.ADD_DESCRIPTION_REQUEST);
	}
	
	private void sendPoint() {
		//Do the thing to send things
		MainActivity.HttpSender httpSender = new MainActivity.HttpSender();
		httpSender.execute(tvPtName.getText().toString(), ptLat + "", ptLng + "", "0", fileNameOnFTP);
		UploadToFTP ftp = new UploadToFTP();
		ftp.execute(ptFilePath);
		setResult(Activity.RESULT_OK);
		finish();
	}
	
	private void exit() {
		//Do that exit thing
		setResult(Activity.RESULT_CANCELED);
		finish();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode) {
			case (Constants.NEW_NAME_REQUEST) : {
				if (resultCode == Activity.RESULT_OK) {
					List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
					ptName = results.get(0);
					tvPtName.setText(ptName);
				} else {
					
				}
			}
			case (Constants.ADD_DESCRIPTION_REQUEST) : {
				if (resultCode == Activity.RESULT_OK) {
					List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
					ptDescription = results.get(0);
					tvPtDesc.setText(ptDescription);
				} else {
					
				}
			}
		}
	}

	//Goes back to the main map.
	public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED, null);
        finish();
    }
}
