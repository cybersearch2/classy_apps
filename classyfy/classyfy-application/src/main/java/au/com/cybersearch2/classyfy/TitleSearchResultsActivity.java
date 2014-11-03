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


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.app.SearchManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.database.Cursor;
import au.com.cybersearch2.classycontent.CursorAdapaterParameters;
import au.com.cybersearch2.classycontent.SuggestionCursorParameters;
import au.com.cybersearch2.classycontent.SearchCursorLoader;
import au.com.cybersearch2.classyfy.R;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.WorkStatus;

/**
 * TitleSearchResultsActivity
 * @author Andrew Bowley
 * 21/04/2014
 */
public class TitleSearchResultsActivity extends FragmentActivity
{
    public static final String TAG = "TitleSearchResults";

    protected String REFINE_SEARCH_MESSAGE;
    protected SimpleCursorAdapter adapter;
    protected TitleSearchResultsFragment resultsView;
    protected LoaderManager loaderManager;
    protected LoaderCallbacks<Cursor> loaderCallbacks;
    protected Executable taskHandle;

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results_list);
        adapter = getSimpleCursorAdapter();
        loaderManager = getActivityLoaderManager();
        resultsView = getTitleSearchResultsFragment(); 
        resultsView.setListAdapter(adapter);
        REFINE_SEARCH_MESSAGE = this.getString(R.string.refine_search);
        // Initiate the Cursor Loader
        loaderCallbacks = new LoaderCallbacks<Cursor>() 
        {
            /**
             * Instantiate and return a new Loader for the given ID.
             *
             * @param id The ID whose loader is to be created.
             * @param args Any arguments supplied by the caller.
             * @return Return a new Loader instance that is ready to start loading.
             */
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) 
            {
                // Extract the search query from the arguments
                SuggestionCursorParameters params = new SuggestionCursorParameters(args, ClassyFySearchEngine.LEX_CONTENT_URI, ClassyFyApplication.SEARCH_RESULTS_LIMIT); 
                // Create the new Cursor Loader
                SearchCursorLoader searchCursorLoader = new SearchCursorLoader(TitleSearchResultsActivity.this, params);
                searchCursorLoader.setStatus(WorkStatus.RUNNING);
                taskHandle = searchCursorLoader.getWorkTracker();
                return searchCursorLoader;
           }

            /**
             * Called when a previously created loader has finished its load.  Note
             * that normally an application is <em>not</em> allowed to commit fragment
             * transactions while in this call, since it can happen after an
             * activity's state is saved.  See {@link FragmentManager#beginTransaction()
             * FragmentManager.openTransaction()} for further discussion on this.
             * 
             * <p>This function is guaranteed to be called prior to the release of
             * the last data that was supplied for this Loader.  At this point
             * you should remove all use of the old data (since it will be released
             * soon), but should not do your own release of the data since its Loader
             * owns it and will take care of that.  The Loader will take care of
             * management of its data so you don't have to.  In particular:
             *
             * <ul>
             * <li> <p>The Loader will monitor for changes to the data, and report
             * them to you through new calls here.  
             * <li> The Loader will release the data once it knows the application
             * is no longer using it.  
             * </ul>
             *
             * @param loader The Loader that has finished.
             * @param cursor Cursor.
             */
            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) 
            {
                adapter.swapCursor(cursor);
                boolean success = (cursor != null) && (adapter.getCount() > 0);
                SearchCursorLoader searchCursorLoader = (SearchCursorLoader)loader;
                searchCursorLoader.setStatus(
                        success ? WorkStatus.FINISHED : WorkStatus.FAILED);
                if (adapter.getCount() == 0)
                    displayToast("Search for \"" + searchCursorLoader.getSearchTerm() + "\" returned nothing");    
                if (adapter.getCount() >= ClassyFyApplication.SEARCH_RESULTS_LIMIT)
                    displayToast(REFINE_SEARCH_MESSAGE);    

            }

           /**
            * Called when a previously created loader is being reset, and thus
            * making its data unavailable.  The application should at this point
            * remove any references it has to the Loader's data.
            *
            * @param loader The Loader that is being reset.
            */
             @Override
            public void onLoaderReset(Loader<Cursor> loader) 
            {
                adapter.swapCursor(null);
                ((SearchCursorLoader)loader).setStatus(WorkStatus.PENDING);
            }
            
        };
        loaderManager.initLoader(0, null, loaderCallbacks);
        // Process intent
        parseIntent(getIntent());
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        // Restart the database loader
   }


    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);      
        setIntent(intent);
        parseIntent(intent);
    }

    protected SimpleCursorAdapter getSimpleCursorAdapter()
    {
       CursorAdapaterParameters params = new CursorAdapaterParameters();
       return new SimpleCursorAdapter(
               this, 
               params.getLayout(),  
               params.getCursor(), 
               params.getUiBindFrom(), 
               params.getUiBindTo(), 
               params.getFlags());
    }
    
    protected TitleSearchResultsFragment getTitleSearchResultsFragment()
    {
        return (TitleSearchResultsFragment)getSupportFragmentManager().findFragmentById(R.id.title_search_results_fragment);
    }
    
    protected LoaderManager getActivityLoaderManager()
    {
        return getSupportLoaderManager();
    }

    private void parseIntent(Intent intent)
    {
        // If the Activity was started to service a search request, extract the search query
        if (Intent.ACTION_SEARCH.equals(intent.getAction()))
        {
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);
            // Perform the search, passing in the search query as an argument to the Cursor Loader
            Bundle args = new Bundle();
            args.putString(SuggestionCursorParameters.QUERY_TEXT_KEY, searchQuery);
            // Restart the Cursor Loader to execute the new query
            /**
             * Starts a new or restarts an existing Loader in
             * this manager, registers the callbacks to it,
             * and (if the activity/fragment is currently started) starts loading it.
             * If a loader with the same id has previously been
             * started it will automatically be destroyed when the new loader completes
             * its work. The callback will be delivered before the old loader
             * is destroyed.
             *
             * @param id A unique identifier for this loader.  Can be whatever you want.
             * Identifiers are scoped to a particular LoaderManager instance.
             * @param args Optional arguments to supply to the loader at construction.
             * @param callback Interface the LoaderManager will call to report about
             * changes in the state of the loader.  Required.
             */
            loaderManager.restartLoader(0, args, loaderCallbacks);
            // Define the on-click listener for the list items
            resultsView.getListView().setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Build the Intent used to open details dialog with a specific node Uri
                    showDetailsDialog(Uri.withAppendedPath(ClassyFySearchEngine.CONTENT_URI, String.valueOf(id)));
                }
            });
        }
        if (Intent.ACTION_VIEW.equals(intent.getAction())) 
        {
            // handles a click on a search suggestion; launches activity to show node
            showDetailsDialog(intent.getData());
        }
        
    }

    private void showDetailsDialog(Uri uri)
    {
        Intent wordIntent = new Intent(this, MainActivity.class);
        wordIntent.setData(uri);
        wordIntent.setAction(Intent.ACTION_VIEW);
        startActivity(wordIntent);
        finish();
    }

    protected void displayToast(String text)
    {
        //Log.e(TAG, text);
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();    
    }

}
