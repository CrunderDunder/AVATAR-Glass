package com.sate2014.avatar.glass;

import android.os.Environment;

public class Constants {
	public static final String IMAGE_PATH = Environment.getExternalStorageDirectory().getPath() + 
			"/AVATAR/media/images/";
	public static final String VIDEO_PATH = Environment.getExternalStorageDirectory().getPath() + 
			"/AVATAR/media/videos/";
	public static final String AUDIO_PATH = Environment.getExternalStorageDirectory().getPath() + 
			"/AVATAR/media/audio/";
	public static final String SPEAK_PATH = Environment.getExternalStorageDirectory().getPath() + 
			"/AVATAR/media/text/";
	
	public static final int IMAGE_REQUEST = 1;
	public static final int VIDEO_REQUEST = 2;
	public static final int AUDIO_REQUEST = 3;
	public static final int SPEAK_REQUEST = 4;
	public static final int NEW_NAME_REQUEST = 5;
	public static final int ADD_DESCRIPTION_REQUEST = 6;
	public static final int SQL_UPLOAD = 7;
	
	public static final String SQL_SERVER_ADDRESS = "avatar.deep-horizons.net";
	public static final String FTP_SERVER_ADDRESS = "10.0.3.73";
	
}
