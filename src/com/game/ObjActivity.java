package com.game;



import java.io.IOException;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Build;
import android.os.Bundle;

public class ObjActivity extends Activity{

	private BoardView gameView;
	private ObjLoader loader;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		gameView = new BoardView(this);

		try {
			loader = new ObjLoader(getAssets());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.out.println("loader failed");		
			}
	
	    // Check if the system supports OpenGL ES 2.0.
	    final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
	    final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000 || Build.FINGERPRINT.startsWith("generic");
	    
	    if (supportsEs2)
	    {
	        // Request an OpenGL ES 2.0 compatible context.
	        gameView.setEGLContextClientVersion(2);

	        // Set the renderer to our demo renderer, defined below.
	        gameView.setRenderer(new ObjRenderer(loader, this));

	    }
	    else
	    {
	    	return;
	    }
	    
	    setContentView(gameView);
	}
	
	@Override
	protected void onResume()
	{
	    // The activity must call the GL surface view's onResume() on activity onResume().
	    super.onResume();
	    gameView.onResume();
	}
	 
	@Override
	protected void onPause()
	{
	    // The activity must call the GL surface view's onPause() on activity onPause().
	    super.onPause();
	    gameView.onPause();
	}
	

}
