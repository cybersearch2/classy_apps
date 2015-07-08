/**
    Copyright (C) 2014  www.cybersearch2.com.au

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/> */
package au.com.cybersearch2.classyfy;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import au.com.cybersearch2.classyfy.interfaces.ClassyFyLauncher;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classytask.BackgroundTask;
import au.com.cybersearch2.classytask.WorkStatus;

/**
 * ClassyFy MainActivity
 * Displays node details when ACTION_VIEW intent received. Fast Text Search is provided by ClassyFyProvider.
 * @author Andrew Bowley
 * 26 Jun 2015
 */
@SuppressWarnings("deprecation")
public class MainActivity extends ActionBarActivity 
{
    public static final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final ClassyFyLauncher classyfyLauncher = (ClassyFyLauncher)getApplication();
        // Complete initialization in background
        BackgroundTask starter = new BackgroundTask(this)
        {
            /**
             * The background task
             * @see au.com.cybersearch2.classytask.BackgroundTask#loadInBackground()
             */
            @Override
            public Boolean loadInBackground()
            {
                WorkStatus status = classyfyLauncher.waitForApplicationSetup();
                if (status != WorkStatus.FINISHED)
                    return Boolean.FALSE;
                //DI.inject(MainActivity.this);
                return Boolean.TRUE;
            }

            @Override
            public void onLoadComplete(Loader<Boolean> loader, Boolean success)
            {
                if (!success)
                    displayToast("ClassyFy failed to start due to unexpected error");
                else
                {
                    parseIntent(getIntent());
                    // Call ClassyFyProvider to wait for SearchEngine start
                    ContentResolver contentResolver = getContentResolver();
                    contentResolver.getType(ClassyFySearchEngine.CONTENT_URI);
                }

            }};
         starter.onStartLoading();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		// Get the action view of the menu item whose id is action_search
        createSearchView(menu);
        return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings)
		{
			return true;
		}
        switch (item.getItemId()) 
        {
        case R.id.action_search:
            onSearchRequested();
            return true;
        default:
        }
		return super.onOptionsItemSelected(item);
	}
	
    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);      
        setIntent(intent);
        parseIntent(intent);
    }

    protected void parseIntent(Intent intent)
    {
    }

    protected void createSearchView(Menu menu)
    {
        /** Get the action view of the menu item whose id is action_search */

        // Associate searchable configuration (in res/xml/searchable.xml with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        if (searchMenuItem == null)
            throw new IllegalStateException("Search menu item not found in main menu");
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        if (searchView == null)
            throw new IllegalStateException("SearchView not found in main menu");
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
    }
    
    protected void displayToast(String text)
    {
        Log.e(TAG, text);
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();    
    }

}
