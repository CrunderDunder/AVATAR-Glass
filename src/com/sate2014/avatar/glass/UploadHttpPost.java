package com.sate2014.avatar.glass;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;

//This task uploads the media to the ftp server in the background
public class UploadHttpPost extends AsyncTask<String, String, String> {

	private static final String TAG = MainActivity.class.getSimpleName();
	private HttpEntity resEntity;

	public String doInBackground(String... params) {
		HttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		
		HttpPost httppost = new HttpPost("http://10.0.3.17/scripts/glass/uploadglassmedia.php");
		File file = new File(params[0]);
		
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		
		FileBody fileBody = new FileBody(file);
		builder.addPart("file", fileBody);
		
		httppost.setEntity(builder.build());
		Log.v(TAG, "Executing Http resquest " + httppost.getRequestLine());
		try {
			Log.v(TAG, "trying http response");
			HttpResponse response = httpclient.execute(httppost);
			Log.v(TAG, "Getting http response");
			resEntity = response.getEntity();
			Log.v(TAG, "resEntity string: " + resEntity.toString());
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		try {
//			resEntity.consumeContent();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		httpclient.getConnectionManager().shutdown();
		
		
//		MultipartEntity mpEntity = new MultipartEntity();
		
		
		return params[0];
		
		
		
//		System.out.println("FTP START");
//		Log.v(TAG, "Starting FTP upload");
//		FTPClient ftpClient = new FTPClient();
//		Log.d("HELP", ftpClient.toString());
//		String retVal = params[0];
//		try {
//			//Connects and logs into the server
//			ftpClient.connect("10.0.3.17", 21);
////			ftpClient.enterLocalPassiveMode();
//			ftpClient.login("loumcgu", "apple123");
////			System.out.println(ftpClient.getStatus());
////			System.out.println(ftpClient.printWorkingDirectory());
//			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
//
//			//Finds the file and gets the filename.
//			File file = new File(params[0]);
//			if (file.exists()) {
//				Log.v(TAG, "File exists in UtoFTP");			
//			}
//			String objectName = file.getName();
//			InputStream input = new FileInputStream(file);
//			//Saves the file to the ftp server
//			ftpClient.storeFile(objectName, input);
//
//			ftpClient.logout();
//			ftpClient.disconnect();
//
//		} catch (IOException e) {
//			System.out.println("WHOOPS");
//			e.printStackTrace();
//		}
//		System.out.println("COMPLETE LOOPER");
//		return retVal;
	}
}
