package com.sate2014.avatar.glass;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.commons.net.ftp.FTPClient;

//This task uploads the media to the ftp server in the background
public class UploadToFTP extends AsyncTask<String, String, String> {

	private static final String TAG = MainActivity.class.getSimpleName();

	public String doInBackground(String... params) {
		System.out.println("FTP START");
		Log.v(TAG, "Starting FTP upload");
		FTPClient ftpClient = new FTPClient();
		Log.d("HELP", ftpClient.toString());
		String retVal = params[0];
		try {
			//Connects and logs into the server
			ftpClient.connect(Constants.FTP_SERVER_ADDRESS, 21);
			ftpClient.login("loumcgu", "apple123");
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

			//Finds the file and gets the filename.
			File file = new File(params[0]);
			if (file.exists()) {
				Log.v(TAG, "File exists in UtoFTP");			
			}
			String objectName = file.getName();
			InputStream input = new FileInputStream(file);
			//Saves the file to the ftp server
			ftpClient.storeFile(objectName, input);

			ftpClient.logout();
			ftpClient.disconnect();

		} catch (IOException e) {
			System.out.println("WHOOPS");
			e.printStackTrace();
		}
		System.out.println("COMPLETE LOOPER");
		return retVal;
	}
}