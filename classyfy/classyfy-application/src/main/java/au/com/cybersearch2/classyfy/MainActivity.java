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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import au.com.cybersearch2.classyfy.data.FieldDescriptor;
import au.com.cybersearch2.classynode.Node;
import au.com.cybersearch2.classynode.NodeFinder;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.entity.PersistenceLoader;
import au.com.cybersearch2.classytask.BackgroundTask;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classywidget.PropertiesListAdapter;
import au.com.cybersearch2.classywidget.PropertiesListAdapter.Value;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classyfy.data.RecordModel;
import au.com.cybersearch2.classyfy.interfaces.ClassyFyLauncher;

public class MainActivity extends ActionBarActivity 
{
    interface MenuOptionsHandler
    {
        void onCreateOptionsMenu(Menu menu);
    }
    
    class MainActivityNodeFinder extends NodeFinder
    {

        public MainActivityNodeFinder(int nodeId)
        {
            super(nodeId);
        }

        @Override
        public void onRollback(Throwable rollbackException) 
        {
            // Toast
        }
        
        @Override
        public void onPostExecute(boolean success) 
        {
            // Update details
            updateDetails(node);
            showDetailsDialog(node);
        }
    }
    
    public static final String TAG = "MainActivity";
    public static final String EDIT_DATA_KEY = "EDIT_DATA_KEY";
    public static final String NODE_KEY = "NODE_KEY";
    protected Dialog dialog;
    protected NodeDetailsFragment nodeDetailsFragment;
    protected ProgressFragment progressFragment;
    protected PropertiesListAdapter adapter;
    protected MenuOptionsHandler menuOptionsHandler;
    protected Executable taskHandle;
    protected PersistenceLoader loader;

    public MainActivity()
    {
		menuOptionsHandler = getMenuOptionsHandler();
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		progressFragment = (ProgressFragment) getSupportFragmentManager().findFragmentById(R.id.main_activity_progress_fragment);
		nodeDetailsFragment = (NodeDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.node_details_fragment);
		adapter = new PropertiesListAdapter(this);
		nodeDetailsFragment.setListAdapter(adapter);
		loader = new PersistenceLoader();
		final ClassyFyLauncher classyfyLauncher = (ClassyFyLauncher)getApplication();
        // Complete initialization in background
        BackgroundTask starter =  new BackgroundTask(this)
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
                    ContentResolver contentResolver  = getContentResolver();
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
		menuOptionsHandler.onCreateOptionsMenu(menu);
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
        // ProgressBar may not have been created for first intent when this activity is launched.
        ProgressBar spinner = progressFragment.getSpinner();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) 
        {
            // handles a click on a search suggestion; launches activity to show node
            Uri uri = intent.getData();
            if (uri.getPathSegments().size() < 2)
            {   
                displayToast("Invalid resource address: \"" + uri.toString() + "\"");
                return;
            }
            Integer nodeId = null;
            try
            {
                nodeId = Integer.parseInt(uri.getPathSegments().get(1)); 
            }
            catch (NumberFormatException e)
            {
                displayToast("Resource address has invalid ID: \"" + uri.toString() + "\"");
                return;
            }
            MainActivityNodeFinder nodeFinder = new MainActivityNodeFinder(nodeId);
            taskHandle = loader.execute(ClassyFyApplication.PU_NAME, nodeFinder);
            if (spinner != null)
                spinner.setVisibility(View.VISIBLE);
        }
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

    protected void showDetailsDialog(Node data)
    {
        // ProgressBar may not have been created for first intent when this activity is launched.
        ProgressBar spinner = progressFragment.getSpinner();
        if (spinner != null)
            spinner.setVisibility(View.GONE);
        Bundle args = new Bundle();
        //args.putLong(ClassyFySearchEngine.KEY_ID, nodeId);
        args.putString(ClassyFySearchEngine.KEY_TITLE, data.getTitle());
        args.putString(ClassyFySearchEngine.KEY_MODEL, RecordModel.getNameByNode(data));
        dialog = showDialog(getSupportFragmentManager(), args);
    }
    
    public Dialog showDialog(FragmentManager fragmentManager, Bundle args) 
    {
        NodeDetailsDialog newFragment = new NodeDetailsDialog();
        newFragment.setArguments(args);
        newFragment.show(fragmentManager, "dialog");
        return newFragment.getDialog();
    }

    protected void updateDetails(Node data) 
    {
        Map<String,Object> valueMap = data.getProperties();
        List<Value> fieldList = new ArrayList<Value>();
        fieldList.add(new Value(data.getTitle(), RecordModel.getNameByNode(data)));
        FieldDescriptor descriptionField = new FieldDescriptor();
        descriptionField.setOrder(1);
        descriptionField.setName("description");
        descriptionField.setTitle("Description");
        FieldDescriptor createdField = new FieldDescriptor();
        createdField.setOrder(2);
        createdField.setName("created");
        createdField.setTitle("Created");
        FieldDescriptor creatorField = new FieldDescriptor();
        creatorField.setOrder(3);
        creatorField.setName("creator");
        creatorField.setTitle("Creator");
        FieldDescriptor modifiedField = new FieldDescriptor();
        modifiedField.setOrder(4);
        modifiedField.setName("modified");
        modifiedField.setTitle("Modified");
        FieldDescriptor modifier = new FieldDescriptor();
        modifier.setOrder(5);
        modifier.setName("modifier");
        modifier.setTitle("Modifier");
        FieldDescriptor identifierField = new FieldDescriptor();
        identifierField.setOrder(6);
        identifierField.setName("identifier");
        identifierField.setTitle("Identifier");
        Set<FieldDescriptor> fieldSet = new TreeSet<FieldDescriptor>();
        fieldSet.add(descriptionField);
        fieldSet.add(createdField);
        fieldSet.add(creatorField);
        fieldSet.add(modifiedField);
        fieldSet.add(modifier);
        fieldSet.add(identifierField);
        for (FieldDescriptor descriptor: fieldSet)
        {
            Object value = valueMap.get(descriptor.getName());
            if (value == null)
                continue;
            fieldList.add(new Value(descriptor.getTitle(), value.toString()));
        }
        adapter.changeData(fieldList);
    }

    protected MenuOptionsHandler getMenuOptionsHandler()
    {
        return new MenuOptionsHandler(){

            @Override
            public void onCreateOptionsMenu(Menu menu) 
            {
                createSearchView(menu);
            }};
    }
    
}
