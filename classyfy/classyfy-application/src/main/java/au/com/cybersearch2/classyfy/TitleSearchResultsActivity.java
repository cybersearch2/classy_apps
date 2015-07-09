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

import javax.inject.Inject;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import au.com.cybersearch2.classyfy.helper.TicketManager;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classytask.BackgroundTask;
import au.com.cybersearch2.classywidget.ListItem;
import au.com.cybersearch2.classywidget.PropertiesListAdapter;
//import au.com.cybersearch2.classynode.Node;
//import au.com.cybersearch2.classynode.NodeFinder;

/**
 * TitleSearchResultsActivity
 * Display record lists requested by search action and record details from search action or record selection
 * @author Andrew Bowley
 * 21/04/2014
 */
public class TitleSearchResultsActivity extends FragmentActivity
{
    public static final String TAG = "TitleSearchResults";

    /** Refine search message displayed when too many records are retrieved by a search */
    protected String REFINE_SEARCH_MESSAGE;
    /** Progress spinner fragment */
    protected ProgressFragment progressFragment;
    @Inject /** Persistence queries to obtain record details */
    ClassyfyLogic classyfyLogic;
    @Inject /* Intent tracker */
    TicketManager ticketManager;

    /**
     * onCreate
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results_list);
        progressFragment = getProgressFragment();
        REFINE_SEARCH_MESSAGE = this.getString(R.string.refine_search);
        DI.inject(this);
        // Process intent
        parseIntent(getIntent());
    }

    /**
     * onResume
     * @see android.support.v4.app.FragmentActivity#onResume()
     */
    @Override
    protected void onResume()
    {
        super.onResume();
    }

    /**
     * onNewIntent    
     * @see android.support.v4.app.FragmentActivity#onNewIntent(android.content.Intent)
     */
    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);      
        setIntent(intent);
        parseIntent(intent);
    }

    /**
     * Returns progress fragment
     * @return ProgressFragment object
     */
    protected ProgressFragment getProgressFragment()
    {
        return (ProgressFragment) getSupportFragmentManager().findFragmentById(R.id.activity_progress_fragment);
    }

    /**
     * Parse intent - ACTION_SEARCH or ACTION_VIEW
     * @param intent Intent object
     */
    protected void parseIntent(Intent intent)
    {
        if (Intent.ACTION_SEARCH.equals(intent.getAction()))
            launchSearch(intent.getStringExtra(SearchManager.QUERY), ticketManager.addIntent(intent));
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && (intent.getData() != null)) 
            viewUri(intent.getData(), ticketManager.addIntent(intent));
        
    }

    /**
     * View record by id contained in Uri
     * @param uri Uri appended with record id
     * @param ticket Intent tracker id
     */
    void viewUri(Uri uri, int ticket)
    {
        // Handles a click on a search suggestion
        if (uri.getPathSegments().size() < 2)
        {
            displayToast("Invalid resource address: \"" + uri.toString() + "\"");
            ticketManager.removeIntent(ticket);
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
            ticketManager.removeIntent(ticket);
            return;
        }
        displayNodeDetails(nodeId, ticket);
    }

    /**
     * Launch record search query
     * @param searchQuery Search string 
     * @param ticket Intent tracker id
     */
    protected void launchSearch(final String searchQuery, final int ticket)
    {
        final List<ListItem> resultList = new ArrayList<ListItem>();
        BackgroundTask queryTask = new BackgroundTask(this)
        {
            /**
             * Execute task in  background thread
             * Called on a worker thread to perform the actual load. 
             * @return Boolean object - Boolean.TRUE indicates successful result
             * @see android.support.v4.content.AsyncTaskLoader#loadInBackground()
             */
            @Override
            public Boolean loadInBackground()
            {
                resultList.addAll(classyfyLogic.doSearchQuery(searchQuery));
                return Boolean.TRUE;
            }

            @Override
            public void onLoadComplete(Loader<Boolean> loader, Boolean success)
            {
                if (success)
                {
                    success = resultList.size() > 0;
                    if (success)
                    {
                        TextView tv1 = (TextView)findViewById(R.id.node_detail_title);
                        tv1.setText("Search: " + searchQuery);
                        LinearLayout propertiesLayout = (LinearLayout) findViewById(R.id.node_properties);
                        propertiesLayout.removeAllViews();
                        propertiesLayout.addView(createDynamicLayout("Titles", resultList, false));
                    }
                    if (resultList.size() >= ClassyFyApplication.SEARCH_RESULTS_LIMIT)
                        displayToast(REFINE_SEARCH_MESSAGE);  
                }
                if (!success)
                    displayToast("Search for \"" + searchQuery + "\" returned nothing");    
                ticketManager.removeIntent(ticket);
            }
        };
        queryTask.onStartLoading();
    }
    
    /**
     * Display Node details in a dialog
     * @param uri Search suggestion containing node id in path segment 1
     * @param ticket Intent tracker id
     */
    protected void displayNodeDetails(final int nodeId, final int ticket)
    {
        progressFragment.showSpinner();
        BackgroundTask getDetailsTask = new BackgroundTask(this)
        {
            NodeDetailsBean nodeDetails;
            
            @Override
            public Boolean loadInBackground()
            {
                nodeDetails = classyfyLogic.getNodeDetails(nodeId);
                return nodeDetails != null ? Boolean.TRUE : Boolean.FALSE;
            }
            @Override
            public void onLoadComplete(Loader<Boolean> loader, Boolean success)
            {
                progressFragment.hideSpinner();
                if (success)
                {
                    String errorMessage = nodeDetails.getErrorMessage();
                    if (errorMessage != null)
                        displayToast(errorMessage);
                    else
                        showRecordDetails(nodeDetails);
                }
                else
                    displayToast(ClassyfyLogic.RECORD_NOT_FOUND);
                ticketManager.removeIntent(ticket);
            }
       };
       getDetailsTask.onStartLoading();
    }

    /**
     * Display record details
     * @param nodeDetails NodeDetailsBean object
     */
    protected void showRecordDetails(NodeDetailsBean nodeDetails)
    {
        TextView tv1 = (TextView)findViewById(R.id.node_detail_title);
        tv1.setText(nodeDetails.getHeading());
        LinearLayout propertiesLayout = (LinearLayout) findViewById(R.id.node_properties);
        propertiesLayout.removeAllViews();
        if (nodeDetails.getHierarchy().size() > 0)
            propertiesLayout.addView(createDynamicLayout("Hierarchy", nodeDetails.getHierarchy(), true));
        if (nodeDetails.getCategoryTitles().size() > 0)
            propertiesLayout.addView(createDynamicLayout("Categories", nodeDetails.getCategoryTitles(), true));
        if (nodeDetails.getFolderTitles().size() > 0)
            propertiesLayout.addView(createDynamicLayout("Folders", nodeDetails.getFolderTitles(), true));
        propertiesLayout.addView(createDynamicLayout("Details", nodeDetails.getFieldList(), false));
    }

    /**
     * Returns view containg a title and list of items
     * @param title Title text
     * @param items ListItem list
     * @param isSingleLine flag to indicate whether to show only value or value and name
     * @return View object
     */
    protected View createDynamicLayout(String title, List<ListItem> items, boolean isSingleLine)
    {
        LinearLayout dynamicLayout = new LinearLayout(this);
        dynamicLayout.setOrientation(LinearLayout.VERTICAL);
        int layoutHeight = LinearLayout.LayoutParams.MATCH_PARENT;
        int layoutWidth = LinearLayout.LayoutParams.MATCH_PARENT;
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(Color.BLUE);
        LinearLayout titleLayout = new LinearLayout(this);
        titleLayout.setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams titleLayoutParms = new LinearLayout.LayoutParams(layoutWidth, layoutHeight);
        titleLayout.addView(titleView, titleLayoutParms);
        dynamicLayout.addView(titleLayout);
        ListView itemList = new ListView(this);
        PropertiesListAdapter listAdapter = new PropertiesListAdapter(this, items);
        listAdapter.setSingleLine(isSingleLine);
        itemList.setAdapter(listAdapter);
        itemList.setOnItemClickListener(new OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                displayNodeDetails((int)id, ticketManager.voidTicket());
            }
        });
        dynamicLayout.addView(itemList);
        return dynamicLayout;
    }

    /**
     * Display toast
     * @param text Message
     */
    protected void displayToast(String text)
    {
        //Log.e(TAG, text);
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();    
    }

}
