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

import static org.fest.assertions.api.Assertions.assertThat;
import android.app.Instrumentation;
import android.app.SearchManager;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Intent;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.view.View;
import android.widget.ProgressBar;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classywidget.PropertiesListAdapter;
import au.com.cybersearch2.classywidget.PropertiesListAdapter.Value;

/**
 * TitileSearchResultsActivityTest
 * @author Andrew Bowley
 * 24/07/2014
 */
public class TitleSearchResultsActivityTest extends ActivityInstrumentationTestCase2<TitleSearchResultsActivity>
{
    static boolean firstTime = true;
    private static final String[][] RECORD_DETAILS_ARRAY =
    {
        { "description", "" },
        { "created", "2014-02-12 10:58:00.000000" },
        { "creator", "admin" },
        { "modified", "2014-02-12 11:28:35.000000" },
        { "modifier", "admin" },
        { "identifier", "2014-1392163053802" }
    };
 

    /**
     * 
     */
    public TitleSearchResultsActivityTest()
    {
        super(TitleSearchResultsActivity.class);
    }

    @Override
    protected void setUp() throws Exception 
    {
        if (firstTime)
        {
            firstTime = false;
            System.setProperty( "dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath() );
            System.setProperty("java.util.logging.config.file", "src/logging.properties");
            super.setUp();
            assertThat(ClassyFyApplication.getInstance().waitForApplicationSetup()).isEqualTo(WorkStatus.FINISHED);
        }
    }

    @UiThreadTest
    public void test_onCreate()
    {
        TitleSearchResultsActivity activity = getActivity();
        assertThat(activity.adapter).isNotNull();
        assertThat(activity.loaderManager).isNotNull();
        assertThat(activity.loaderCallbacks).isNotNull();
        assertThat(activity.resultsView).isNotNull();
        assertThat(activity.resultsView.getListAdapter()).isEqualTo(activity.adapter);
    }

    public void test_action_view() throws Throwable
    {
        Intent intent = getNewIntent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.withAppendedPath(ClassyFySearchEngine.CONTENT_URI, String.valueOf(34)));
        setActivityIntent(intent); 
        Instrumentation instrumentation = getInstrumentation();
        ActivityMonitor am = instrumentation.addMonitor(MainActivity.class.getName(), null, false);
        TitleSearchResultsActivity activity = getActivity();
        assertThat(activity).isNotNull();
        MainActivity mainActivity = (MainActivity) getInstrumentation().waitForMonitorWithTimeout(am, 10000);
        assertThat(mainActivity).isNotNull();
        synchronized(mainActivity.taskHandle)
        {
            try
            {
                mainActivity.taskHandle.wait(10000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        assertThat(mainActivity.taskHandle.getStatus()).isEqualTo(WorkStatus.FINISHED);
        PropertiesListAdapter adapter = mainActivity.adapter;
        for (int i = 0; (i < adapter.getCount()) && (i < RECORD_DETAILS_ARRAY.length); i++)
        {
            Value item = (Value)adapter.getItem(i);
            assertThat(item.getName().equals(RECORD_DETAILS_ARRAY[i][0]));
            assertThat(item.getValue().equals(RECORD_DETAILS_ARRAY[i][1]));
        }
        ProgressBar spinner = mainActivity.progressFragment.getSpinner();
        assertThat(spinner).isNotNull();
        assertThat(spinner.getVisibility()).isEqualTo(View.GONE);
    }
 
    public void test_action_search_bad_url() throws Throwable
    {
        Intent intent = getNewIntent();
        intent.setAction(Intent.ACTION_SEARCH);
        intent.putExtra(SearchManager.QUERY, Uri.withAppendedPath(ClassyFySearchEngine.CONTENT_URI, String.valueOf(Integer.MAX_VALUE)).toString());
        setActivityIntent(intent); 
        TitleSearchResultsActivity activity = getActivity();
        assertThat(activity).isNotNull();
        if ((activity.taskHandle.getStatus() == WorkStatus.RUNNING) || (activity.taskHandle.getStatus() == WorkStatus.PENDING))
	        synchronized(activity.taskHandle)
	        {
	            try
	            {
	                activity.taskHandle.wait(10000);
	            }
	            catch (InterruptedException e)
	            {
	                e.printStackTrace();
	            }
	        }
        assertThat(activity.taskHandle.getStatus()).isEqualTo(WorkStatus.FAILED);
    }
    
    private Intent getNewIntent() 
    {
        Intent intent = new Intent();
        return intent;
    }

}
