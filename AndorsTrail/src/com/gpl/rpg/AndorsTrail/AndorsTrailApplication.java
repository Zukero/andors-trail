package com.gpl.rpg.AndorsTrail;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import com.gpl.rpg.AndorsTrail.context.ControllerContext;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.controller.Constants;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Environment;
import android.view.Window;
import android.view.WindowManager;

public final class AndorsTrailApplication extends Application {

	public static final boolean DEVELOPMENT_DEBUGRESOURCES = false;
	public static final boolean DEVELOPMENT_FORCE_STARTNEWGAME = false;
	public static final boolean DEVELOPMENT_FORCE_CONTINUEGAME = false;
	public static final boolean DEVELOPMENT_DEBUGBUTTONS = false;
	public static final boolean DEVELOPMENT_FASTSPEED = false;
	public static final boolean DEVELOPMENT_VALIDATEDATA = false;
	public static final boolean DEVELOPMENT_DEBUGMESSAGES = false;
	public static final boolean DEVELOPMENT_INCOMPATIBLE_SAVEGAMES = DEVELOPMENT_DEBUGRESOURCES || DEVELOPMENT_DEBUGBUTTONS || DEVELOPMENT_FASTSPEED;
	public static final int CURRENT_VERSION = DEVELOPMENT_INCOMPATIBLE_SAVEGAMES ? 999 : 47;
	public static final String CURRENT_VERSION_DISPLAY = "0.7.6";
	public static final boolean IS_RELEASE_VERSION = !CURRENT_VERSION_DISPLAY.matches(".*[a-d].*");

	private final AndorsTrailPreferences preferences = new AndorsTrailPreferences();
	private WorldContext world = new WorldContext();
	private ControllerContext controllers = new ControllerContext(this, world);
	private WorldSetup setup = new WorldSetup(world, controllers, this);
	public WorldContext getWorld() { return world; }
	public WorldSetup getWorldSetup() { return setup; }
	public AndorsTrailPreferences getPreferences() { return preferences; }
	public ControllerContext getControllerContext() { return controllers; }

	public static AndorsTrailApplication getApplicationFromActivity(Activity activity) {
		return ((AndorsTrailApplication) activity.getApplication());
	}
	public static AndorsTrailApplication getApplicationFromActivityContext(Context context) {
		return getApplicationFromActivity(getActivityFromActivityContext(context));
	}
	private static Activity getActivityFromActivityContext(Context context) {
		return (Activity) context;
	}

	public boolean isInitialized() { return world.model != null; }

	public void setWindowParameters(Activity activity) {
		activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (preferences.fullscreen) {
			activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			activity.getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}

	//Get default locale at startup, as somehow it seems that changing the app's 
	//configured locale impacts the value returned by Locale.getDefault() nowadays.
	private final Locale defaultLocale = Locale.getDefault();

	@SuppressLint("NewApi")
	public boolean setLocale(Activity context) {
		Resources res = context.getResources();
		Configuration conf = res.getConfiguration();
		final Locale targetLocale = preferences.useLocalizedResources ? defaultLocale : Locale.US;
		if (targetLocale.equals(conf.locale)) {
			return false;
		}

		conf.locale = targetLocale;
		res.updateConfiguration(conf, res.getDisplayMetrics());
		this.getResources().updateConfiguration(conf, res.getDisplayMetrics());
		
		return true;
	}
	
	/**
	 * Logging to text file system as found on https://stackoverflow.com/questions/19565685/saving-logcat-to-a-text-file-in-android-device
	 */
	
	public void onCreate() {
		super.onCreate();

		if ( DEVELOPMENT_DEBUGMESSAGES && isExternalStorageWritable() ) {

			File root = Environment.getExternalStorageDirectory();
			File appDirectory = new File(root, Constants.FILENAME_SAVEGAME_DIRECTORY);
			File logDirectory = new File( appDirectory, "log" );
			File logFile = new File( logDirectory, "logcat" + System.currentTimeMillis() + ".txt" );

			// create app folder
			if ( !appDirectory.exists() ) {
				appDirectory.mkdir();
			}

			// create log folder
			if ( !logDirectory.exists() ) {
				logDirectory.mkdir();
			}

			// clear the previous logcat and then write the new one to the file
			try {
				Process process = Runtime.getRuntime().exec("logcat -c");
				process = Runtime.getRuntime().exec("logcat -f " + logFile+" *:W");
			} catch ( IOException e ) {
				e.printStackTrace();
			}

		}
	}

	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
			return true;
		}
		return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if ( Environment.MEDIA_MOUNTED.equals( state ) ||
				Environment.MEDIA_MOUNTED_READ_ONLY.equals( state ) ) {
			return true;
		}
		return false;
	}
	public void discardWorld() {
		world = new WorldContext();
		controllers = new ControllerContext(this, world);
		setup = new WorldSetup(world, controllers, getApplicationContext());
	}
}
