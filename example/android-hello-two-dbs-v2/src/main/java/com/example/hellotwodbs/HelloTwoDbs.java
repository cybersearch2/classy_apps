package com.example.hellotwodbs;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import au.com.cybersearch2.example.v2.AndroidHelloTwoDbs;
import au.com.cybersearch2.example.v2.ComplexTask;
import au.com.cybersearch2.example.v2.HelloTwoDbsMain;
import au.com.cybersearch2.example.v2.SimpleTask;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

/**
 * Sample Android UI activity which displays a text window when it is run.
 * 
 * <p>
 * <b>NOTE:</b> This does <i>not</i> extend the {@link OrmLiteBaseActivity} but instead manages the helper itself
 * locally using the {@link #databaseHelper1} field, the {@link #getHelper1()} private method, and the call to
 * {@link OpenHelperManager#releaseHelper()} inside of the {@link #onDestroy()} method.
 * </p>
 */
public class HelloTwoDbs extends Activity 
{

	private final String LOG_TAG = getClass().getSimpleName();
	protected AndroidHelloTwoDbs androidHelloTwoDbs;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		Log.i(LOG_TAG, "creating " + getClass() + " at " + System.currentTimeMillis());
		TextView tv = new TextView(this);
	    androidHelloTwoDbs = HelloTwoDbsApplication.getAndroidHelloTwoDbsSingleton();
		doSampleDatabaseStuff("onCreate", tv);
		setContentView(tv);
	}

	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
        if (androidHelloTwoDbs != null)
        	androidHelloTwoDbs.shutdown();
	}


	/**
	 * Do our sample database stuff as an example.
	 */
	private void doSampleDatabaseStuff(String action, TextView tv) 
	{
        // Run version 1 of example which will leave 2 database tables populated with version 1 objects.
        try
        {
        	androidHelloTwoDbs.setUp();
        	SimpleTask simpleTask = new SimpleTask(action);
        	androidHelloTwoDbs.performPersistenceWork(AndroidHelloTwoDbs.PU_NAME1, simpleTask);
			// Our string builder for building the content-view
			StringBuilder sb = new StringBuilder();
			ComplexTask complexTask = new ComplexTask(action);
			androidHelloTwoDbs.performPersistenceWork(AndroidHelloTwoDbs.PU_NAME2, complexTask);
			androidHelloTwoDbs.logMessage(AndroidHelloTwoDbs.TAG, "Test completed successfully at " + System.currentTimeMillis());
			tv.setText(sb
					.append(AndroidHelloTwoDbs.SEPARATOR_LINE)
					.append(simpleTask.getMessage())
					.append(AndroidHelloTwoDbs.SEPARATOR_LINE)
					.append(complexTask.getMessage())
					.toString());
        }
        catch (InterruptedException e)
        {
        }
		Log.i(LOG_TAG, "Done with page at " + System.currentTimeMillis());
	}

}