package com.sate2014.avatar.glass;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

public class RecordAudio extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();

	private static final int SAMPLING_RATE = 44100;
	int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
	int BytesPerElement = 2; // 2 bytes in 16bit format
	
	private WaveformView mWaveformView;
	
	private RecordingThread mRecordingThread;
	private int mBufferSize;
	private short[] mAudioBuffer;
	
    private AudioManager mAudioManager;
    
    private String filename;
    private String filepath;
	
	private final GestureDetector.BaseListener mBaseListener = new GestureDetector.BaseListener() {
		
		@Override
		public boolean onGesture(Gesture gesture) {
			if (gesture == Gesture.TAP){
				mAudioManager.playSoundEffect(Sounds.TAP);
				stopRecording();
				return true;
			} else {
				return false;
			}
		}
	};
	
	private GestureDetector mGestureDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "RecordAudio onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record_audio);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = new GestureDetector(this).setBaseListener(mBaseListener);

		
		mWaveformView = (WaveformView) findViewById(R.id.waveform_view);
        mBufferSize = AudioRecord.getMinBufferSize(SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        Log.v(TAG, "mBufferSize: " + mBufferSize);
        mAudioBuffer = new short[mBufferSize / 2];
        
        Intent mainIntent = getIntent();
        if (mainIntent.hasExtra("filename")) {
        	filename = mainIntent.getStringExtra("filename");
        }
        Log.v(TAG, "filename: " + filename);
        if (mainIntent.hasExtra("filepath")) {
        	filepath = mainIntent.getStringExtra("filepath");
        }
        Log.v(TAG, "filepath: " + filepath);
	}

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		return mGestureDetector.onMotionEvent(event);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}
	
    @Override
    protected void onResume() {
    	Log.v(TAG, "RecordAudio onResume");
        super.onResume();

        mRecordingThread = new RecordingThread();
        mRecordingThread.start();
    }

    @Override
    protected void onPause() {
    	Log.v(TAG, "RecordAudio onPause");
        super.onPause();

        if (mRecordingThread != null) {
            mRecordingThread.stopRunning();
            mRecordingThread = null;
        }
    }
    
    @Override
    protected void onDestroy() {
    	Log.v(TAG, "RecordAudio onDestroy");
    	if (mRecordingThread != null) {
            mRecordingThread.stopRunning();
            mRecordingThread = null;
        }
        setResult(RESULT_OK);
    	super.onDestroy();
    	finish();
    	
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

    private void stopRecording() {
    	Log.v(TAG, "RecordAudio stopRecording");
    	if (mRecordingThread != null) {
            mRecordingThread.stopRunning();
            mRecordingThread = null;
        }
        setResult(RESULT_OK);
		finish();
	}
	/**
     * A background thread that receives audio from the microphone and sends it to the waveform
     * visualizing view.
     */
    private class RecordingThread extends Thread {

        private boolean mShouldContinue = true;

        @Override
        public void run() {
        	Log.v(TAG, "RecordAudio in run Thread");
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

            AudioRecord record = new AudioRecord(AudioSource.MIC, SAMPLING_RATE,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, mBufferSize);
            try {
	            File dir = new File(filepath);
	            if (!dir.exists()) {
	            	dir.mkdirs();
	            }
	            File f =  new File(dir, filename+".pcm");
	            FileOutputStream fos = new FileOutputStream(f);
	            
	            Log.v(TAG, "RecordAudio started recording");
	            
	
	            
	            record.startRecording();
	            
	            while (shouldContinue()) {
//	            	Log.v(TAG, "In shouldContinue()");
	            	record.read(mAudioBuffer, 0, mBufferSize / 2);   
//	            	Log.v(TAG, "sData after read: "+mAudioBuffer.toString());
	
	            	mWaveformView.updateAudioData(mAudioBuffer);
	            	
            		byte bData[] = short2byte(mAudioBuffer);
//            		fos.write(bData, 0, BufferElements2Rec * BytesPerElement);
            		fos.write(bData);
            		Log.v(TAG, "AFTER fos.write try");
	            }
	            
            	fos.close();
            	Log.v(TAG, "After fos.close()");
	
	            record.stop();
	            record.release();
	            
	            if (f.exists()) {
	            	Log.v(TAG, "F exists.  Length: " + f.length());
	            } else {
	            	Log.v(TAG, "F no exisito");
	            }
            } catch (FileNotFoundException e) {
            	e.printStackTrace();
            } catch (IOException e) {
            	e.printStackTrace();
            }
        }
        
        private byte[] short2byte(short[] sData) {
            int shortArrsize = sData.length;
            byte[] bytes = new byte[shortArrsize * 2];
            for (int i = 0; i < shortArrsize; i++) {
                bytes[i * 2] = (byte) (sData[i] & 0x00FF);
                bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
                sData[i] = 0;
            }
            return bytes;

        }

        /**
         * Gets a value indicating whether the thread should continue running.
         *
         * @return true if the thread should continue running or false if it should stop
         */
        private synchronized boolean shouldContinue() {
            return mShouldContinue;
        }

        /** Notifies the thread that it should stop running at the next opportunity. */
        public synchronized void stopRunning() {
            mShouldContinue = false;
        }
        
    }       
}
