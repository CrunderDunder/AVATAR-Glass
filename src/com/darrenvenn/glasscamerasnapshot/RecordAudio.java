package com.darrenvenn.glasscamerasnapshot;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

public class RecordAudio extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();

	private static final int SAMPLING_RATE = 44100;
	
	private final Handler mHandler = new Handler();
	
	private WaveformView mWaveformView;
	
	private RecordingThread mRecordingThread;
	private int mBufferSize;
	private short[] mAudioBuffer;
	
    private AudioManager mAudioManager;
	
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
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record_audio);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = new GestureDetector(this).setBaseListener(mBaseListener);

		
		mWaveformView = (WaveformView) findViewById(R.id.waveform_view);
        mBufferSize = AudioRecord.getMinBufferSize(SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mAudioBuffer = new short[mBufferSize / 2];
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
        super.onResume();

        mRecordingThread = new RecordingThread();
        mRecordingThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mRecordingThread != null) {
            mRecordingThread.stopRunning();
            mRecordingThread = null;
        }
    }
    
    @Override
    protected void onDestroy() {
    	if (mRecordingThread != null) {
            mRecordingThread.stopRunning();
            mRecordingThread = null;
        }
    	super.onDestroy();
    	finish();
    	
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

    private void stopRecording() {
		// TODO Auto-generated method stub
    	if (mRecordingThread != null) {
            mRecordingThread.stopRunning();
            mRecordingThread = null;
        }
    	finish();
		
	}
    
	private void startRecording() {
		// TODO Auto-generated method stub
	}




	/**
     * A background thread that receives audio from the microphone and sends it to the waveform
     * visualizing view.
     */
    private class RecordingThread extends Thread {

        private boolean mShouldContinue = true;
        DataOutputStream dos;

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

            AudioRecord record = new AudioRecord(AudioSource.MIC, SAMPLING_RATE,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, mBufferSize);
            record.startRecording();
            
            String filename = "" + System.currentTimeMillis() + ".pcm";
            String filepath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/Audio/";
            File file = new File(filepath + filename);
            OutputStream os;
			try {
				os = new FileOutputStream(file);
	            BufferedOutputStream bos = new BufferedOutputStream(os);
	            dos = new DataOutputStream(bos);
	            while (shouldContinue()) {
	            	int bufferReadResult = record.read(mAudioBuffer,  0, mBufferSize / 2);
	            	for (int i = 0; i < bufferReadResult; i++) {
	            		dos.writeShort(mAudioBuffer[i]);
	            	}
	            }
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            	


            record.stop();
            record.release();
            try {
            	dos.close();
            } catch (IOException e) {
            	e.printStackTrace();
            }
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
