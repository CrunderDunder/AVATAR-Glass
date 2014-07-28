package com.sate2014.avatar.glass;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

//This task uploads the media to the ftp server in the background
public class UploadHttpPost extends AsyncTask<String, String, String> {

	private static final String TAG = MainActivity.class.getSimpleName();

	public String doInBackground(String... params) {
		Log.v(TAG, "In UploadHttpPost");
		String filepath = params[0];
		Log.v(TAG, "filepath: " +filepath);
		File file =  new File(filepath);
		if (file.exists()) {
			Log.v(TAG, "file exists");
		} else {
			Log.v(TAG, "File doesn't exist");
		}
		String filename = file.getName();
		Log.v(TAG, "filename: " + filename);
		try {
			HttpURLConnection httpUrlConnection = (HttpURLConnection)new URL("http://10.0.3.240/dev/postTestImage.php?filename="+filename).openConnection();
			httpUrlConnection.setDoOutput(true);
			httpUrlConnection.setRequestMethod("POST");
			
//			File file = new File("test.png");
			long totalByte = file.length();
			Log.v(TAG, "file length: " + totalByte);
			
			OutputStream os = httpUrlConnection.getOutputStream();
			Thread.sleep(1000);
			Log.v(TAG, "After sleep");
			BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file));
			
			for (int i = 0; i < totalByte; i++) {
				os.write(fis.read());
			}
			Log.v(TAG, "After writing");
			
			os.close();
			
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							httpUrlConnection.getInputStream()));
			
			String s = null;
			while ((s = in.readLine()) != null) {
				System.out.println(s);
			}
			in.close();
			fis.close();
		} catch (IOException e){
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
}
