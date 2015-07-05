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


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import au.com.cybersearch2.classycontent.SuggestionCursorParameters;
import au.com.cybersearch2.classyfy.data.FieldDescriptor;
import au.com.cybersearch2.classyfy.data.FieldDescriptorSetFactory;
import au.com.cybersearch2.classyfy.data.RecordModel;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classyjpa.entity.PersistenceLoader;
import au.com.cybersearch2.classynode.Node;
import au.com.cybersearch2.classynode.NodeFinder;
import au.com.cybersearch2.classynode.NodeType;
import au.com.cybersearch2.classytask.BackgroundTask;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.BackgroundTask.TaskCallback;
import au.com.cybersearch2.classywidget.PropertiesListAdapter;
import au.com.cybersearch2.classywidget.ListItem;

/**
 * TitleSearchResultsActivity
 * @author Andrew Bowley
 * 21/04/2014
 */
public class TitleSearchResultsActivity extends FragmentActivity
{
    class QueryTask extends BackgroundTask
    {
        String searchQuery;
        List<ListItem> resultList;
        
        public QueryTask(String searchQuery, List<ListItem> resultList)
        {
            super(TitleSearchResultsActivity.this);
            this.searchQuery = searchQuery;
            this.resultList = resultList;
        }
        
        /**
         * Execute task in  background thread
         * Called on a worker thread to perform the actual load. 
         * @return Boolean object - Boolean.TRUE indicates successful result
         * @see android.support.v4.content.AsyncTaskLoader#loadInBackground()
         */
        @Override
        public Boolean loadInBackground()
        {
            resultList.addAll(doSearchQuery(searchQuery));
            return Boolean.TRUE;
        }
    }
    
    public static final String TAG = "TitleSearchResults";

    protected String REFINE_SEARCH_MESSAGE;
    protected PropertiesListAdapter adapter;
    protected TitleSearchResultsFragment resultsView;
    protected ProgressFragment progressFragment;
    protected Executable taskHandle;
    protected ContentResolver contentResolver;
    protected PersistenceLoader loader;
    protected Dialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results_list);
        progressFragment = getProgressFragment();
        adapter = new PropertiesListAdapter(this);
        resultsView = getTitleSearchResultsFragment(); 
        resultsView.setListAdapter(adapter);
        REFINE_SEARCH_MESSAGE = this.getString(R.string.refine_search);
        contentResolver = getContentResolver();
        loader = new PersistenceLoader();
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

    protected TitleSearchResultsFragment getTitleSearchResultsFragment()
    {
        return (TitleSearchResultsFragment)getSupportFragmentManager().findFragmentById(R.id.title_search_results_fragment);
    }
 
    protected ProgressFragment getProgressFragment()
    {
        return (ProgressFragment) getSupportFragmentManager().findFragmentById(R.id.activity_progress_fragment);
    }

    protected void parseIntent(Intent intent)
    {
        // If the Activity was started to service a search request, extract the search query
        if (Intent.ACTION_SEARCH.equals(intent.getAction()))
        {
            launchSearch(intent.getStringExtra(SearchManager.QUERY));
            // Define the on-click listener for the list items
            resultsView.getListView().setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Show details in a list and a dialog
                    displayNodeDetails((int)id);
                }
            });
        }
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && (intent.getData() != null)) 
            viewUri(intent.getData());
        
    }

    void viewUri(Uri uri)
    {
        // Handles a click on a search suggestion
        if (uri.getPathSegments().size() < 2)
        {
            displayToast("Invalid resource address: \"" + uri.toString() + "\"");
            return;
        }
        int nodeId;
        try
        {
            nodeId = Integer.parseInt(uri.getPathSegments().get(1)); 
        }
        catch (NumberFormatException e)
        {
            displayToast("Resource address has invalid ID: \"" + uri.toString() + "\"");
            return;
        }
        displayNodeDetails(nodeId);
    }

    protected void launchSearch(final String searchQuery)
    {
        final List<ListItem> resultList = new ArrayList<ListItem>();
        BackgroundTask queryTask = new QueryTask(searchQuery, resultList);
        queryTask.start(new TaskCallback(){

            @Override
            public void onTaskComplete(boolean success)
            {
                if (success)
                {
                    success = resultList.size() > 0;
                    if (success)
                        adapter.changeData(resultList);
                    if (adapter.getCount() >= ClassyFyApplication.SEARCH_RESULTS_LIMIT)
                        displayToast(REFINE_SEARCH_MESSAGE);  
                }
                if (!success)
                    displayToast("Search for \"" + searchQuery + "\" returned nothing");    
            }
        });
    }
    
    protected List<ListItem> doSearchQuery(String searchQuery)
    {
        // Perform the search, passing in the search query as an argument to the Cursor Loader
        SuggestionCursorParameters params = new SuggestionCursorParameters(searchQuery, ClassyFySearchEngine.LEX_CONTENT_URI, ClassyFyApplication.SEARCH_RESULTS_LIMIT); 
        Cursor cursor = contentResolver.query(
                params.getUri(), 
                params.getProjection(), 
                params.getSelection(), 
                params.getSelectionArgs(), 
                params.getSortOrder());
         List<ListItem> fieldList = new ArrayList<ListItem>();
         int nameColumnId = cursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_1);
         int valueColumnId = cursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_2);
         // Id column name set in android.support.v4.widget.CursorAdaptor
         int idColumnId = cursor.getColumnIndexOrThrow("_id");
         if (cursor.getCount() > 0) 
         {
             cursor.moveToPosition(-1);         
             while (cursor.moveToNext())
             {
                 String name = cursor.getString(nameColumnId);
                 String value = cursor.getString(valueColumnId);
                 long id = cursor.getLong(idColumnId);
                 fieldList.add(new ListItem(name, value, id));
             }
         }
         cursor.close();
         return fieldList;
    }
    
    /**
     * Display Node details in a dialog
     * @param uri Search suggestion containing node id in path segment 1
     * @return Flag to indicate dialog launched
     */
    protected boolean displayNodeDetails(int nodeId)
    {
        progressFragment.showSpinner();
        final NodeFinder nodeFinder = new NodeFinder(nodeId, new NodeFinder.Callback(){

            @Override
            public void onNodeFound(Node node)
            {
                progressFragment.hideSpinner();
                // Update details
                updateDetails(node);
                if (node.getChildren().size() > 0)
                    showDetailsDialog(node);
            }

            @Override
            public void onNodeNotFound(int nodeId)
            {
                progressFragment.hideSpinner();
                displayToast("Record not found due to database error");
            }
            
            @Override
            public void onRollback(int nodeId, Throwable rollbackException)
            {
                Log.e(TAG, "Fetch node id " + nodeId + ": failed", rollbackException);
            }
        });
        taskHandle = loader.execute(ClassyFyApplication.PU_NAME, nodeFinder);
        return true;
    }

    protected void updateDetails(Node data) 
    {
        Map<String,Object> valueMap = data.getProperties();
        List<ListItem> fieldList = new ArrayList<ListItem>();
        fieldList.add(new ListItem(data.getTitle(), RecordModel.getNameByNode(data)));
        Set<FieldDescriptor> fieldSet = FieldDescriptorSetFactory.instance(data);
        for (FieldDescriptor descriptor: fieldSet)
        {
            Object value = valueMap.get(descriptor.getName());
            if (value == null)
                continue;
            fieldList.add(new ListItem(descriptor.getTitle(), value.toString()));
        }
        adapter.changeData(fieldList);
    }

    protected void showDetailsDialog(Node data)
    {
        Bundle args = new Bundle();
        //args.putLong(ClassyFySearchEngine.KEY_ID, nodeId);
        args.putString(ClassyFySearchEngine.KEY_TITLE, data.getTitle());
        args.putString(ClassyFySearchEngine.KEY_MODEL, RecordModel.getNameByNode(data));
        ArrayList<ListItem> categoryTitles = new ArrayList<ListItem>();
        ArrayList<ListItem> folderTitles = new ArrayList<ListItem>();
        ArrayList<ListItem> hierarchy = new ArrayList<ListItem>();
        for (Node child: data.getChildren())
        {
            String title = child.getTitle();
            long id = (long)child.get_id();
            ListItem item = new ListItem("Title", title, id);
            if (RecordModel.getModel(child.getModel()) == RecordModel.recordFolder)
                folderTitles.add(item);
            else
                categoryTitles.add(item);
        }
        // Cast required because parent field declared in NodeEntity super class
        Node node = (Node)data.getParent();
        Deque<Node> nodeDeque = new ArrayDeque<Node>();
        // Walk up to top node
        while (node.getModel() != NodeType.ROOT)// Top of tree
        {
            nodeDeque.add(node);
            node = (Node)node.getParent();
        }
        Iterator<Node> nodeIterator = nodeDeque.descendingIterator();
        while (nodeIterator.hasNext())
        {
            node = nodeIterator.next();
            String title = node.getTitle();
            long id = (long)node.get_id();
            ListItem item = new ListItem("Title", title, id);
            hierarchy.add(item);
        }
        
        args.putParcelableArrayList(NodeDetailsDialog.FOLDER_LIST, folderTitles);
        args.putParcelableArrayList(NodeDetailsDialog.CATEGORY_LIST, categoryTitles);
        args.putParcelableArrayList(NodeDetailsDialog.HIERARCHY_LIST, hierarchy);
        dialog = showDialog(args);
    }
    
    public Dialog showDialog(Bundle args) 
    {
        NodeDetailsDialog newFragment = new NodeDetailsDialog();
        newFragment.setArguments(args);
        FragmentManager fragmentManager = getSupportFragmentManager();
        newFragment.show(fragmentManager, "dialog");
        fragmentManager.executePendingTransactions();
        return newFragment.getDialog();
    }

    protected void displayToast(String text)
    {
        //Log.e(TAG, text);
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();    
    }

}
