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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import au.com.cybersearch2.classyfy.data.RecordCategory;
import au.com.cybersearch2.classyfy.data.RecordFolder;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classynode.EntityByNodeIdGenerator;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classywidget.PropertiesListAdapter;
import au.com.cybersearch2.classywidget.PropertiesListAdapter.Value;

/**
 * MainActivityTest
 * @author Andrew Bowley
 * 23/07/2014
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity>
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
 
    private static final String[] INF_LIST =
    {
        "Information Management",
        "Information Management Policy",
        "Information & Communications Technology"
    };

    protected Context context;
   
    /**
     * @param activityClass
     */
    public MainActivityTest()
    {
        super(MainActivity.class);

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
        PersistenceContext persistenceContext = new PersistenceContext();
        PersistenceAdmin persistenceAdmin = persistenceContext.getPersistenceAdmin(ClassyFyApplication.PU_NAME);
        EntityByNodeIdGenerator entityByNodeIdGenerator = new EntityByNodeIdGenerator();
        persistenceAdmin.addNamedQuery(RecordCategory.class, ClassyFyApplication.CATEGORY_BY_NODE_ID, entityByNodeIdGenerator);
        persistenceAdmin.addNamedQuery(RecordFolder.class, ClassyFyApplication.FOLDER_BY_NODE_ID, entityByNodeIdGenerator);
    }

    @UiThreadTest
    public void test_onCreate()
    {
        MainActivity activity = getActivity();
        assertThat(activity.adapter).isNotNull();
        assertThat(activity.adapter.getCount()).isEqualTo(0);
        assertThat(activity.menuOptionsHandler).isNotNull();
        assertThat(activity.progressFragment).isNotNull();
        assertThat(activity.progressFragment.getActivity()).isEqualTo(activity);
        assertThat(activity.nodeDetailsFragment).isNotNull();
        assertThat(activity.nodeDetailsFragment.getListAdapter()).isEqualTo(activity.adapter);
    }
    
    public void test_parseIntent_action_view() throws Throwable
    {
        Intent intent = getNewIntent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri actionUri = Uri.withAppendedPath(ClassyFySearchEngine.CONTENT_URI, "34");
        intent.setData(actionUri);
        setActivityIntent(intent); 
        final MainActivity mainActivity = getActivity(); 
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

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    public void test_search() throws Throwable
    {
        final MainActivity mainActivity = getActivity(); 
        final View view = mainActivity.findViewById(au.com.cybersearch2.classyfy.R.id.action_search);
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                view.requestFocus();
                view.callOnClick();
            }});
        ActionBar actionBar = mainActivity.getSupportActionBar();
        assertThat(actionBar).isNotNull();
        final FragmentManager sfm = mainActivity.getSupportFragmentManager();
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                sfm.executePendingTransactions();
            }});
        Instrumentation instrumentation = getInstrumentation();
        ActivityMonitor am = instrumentation.addMonitor(TitleSearchResultsActivity.class.getName(), null, false);
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_SEARCH); 
        instrumentation.sendCharacterSync(KeyEvent.KEYCODE_I); 
        instrumentation.sendCharacterSync(KeyEvent.KEYCODE_N); 
        instrumentation.sendCharacterSync(KeyEvent.KEYCODE_F);
        instrumentation.sendCharacterSync(KeyEvent.KEYCODE_ENTER);
        instrumentation.waitForIdleSync();
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                sfm.executePendingTransactions();
            }});
        TitleSearchResultsActivity titleSearchResultsActivity = (TitleSearchResultsActivity) getInstrumentation().waitForMonitorWithTimeout(am, 10000);
        assertThat(titleSearchResultsActivity).isNotNull();
        assertThat(titleSearchResultsActivity.taskHandle).isNotNull();
        synchronized(titleSearchResultsActivity.taskHandle)
        {
            titleSearchResultsActivity.taskHandle.wait(10000);
        }
        assertThat(titleSearchResultsActivity.taskHandle.getStatus()).isEqualTo(WorkStatus.FINISHED);
        SimpleCursorAdapter adapter = titleSearchResultsActivity.adapter;
        for (int i = 0; i < adapter.getCount(); i++)
        {
            Cursor cursor = (Cursor)adapter.getItem(i);
            int column = cursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_1);
            assertThat(INF_LIST[i]).isEqualTo(cursor.getString(column));
        }
    }
    private Intent getNewIntent() 
    {
        Intent intent = new Intent();
        return intent;
    }
}
