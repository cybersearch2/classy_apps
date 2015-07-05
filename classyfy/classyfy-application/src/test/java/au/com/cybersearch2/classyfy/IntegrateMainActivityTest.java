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

import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.util.SimpleFuture;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.content.AsyncTaskLoader;
import android.view.Menu;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classywidget.PropertiesListAdapter;
import au.com.cybersearch2.classywidget.ListItem;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;

/**
 * IntegrateMainActivityTest
 * @author Andrew Bowley
 * 26/05/2014
 */
@RunWith(RobolectricTestRunner.class)
public class IntegrateMainActivityTest
{
    @Implements(value = SystemClock.class, callThroughByDefault = true)
    public static class MyShadowSystemClock {
        public static long elapsedRealtime() {
            return 0;
        }
    }

    @Implements(AsyncTaskLoader.class)
    public static class MyShadowAsyncTaskLoader<D> 
    {
          @RealObject private AsyncTaskLoader<D> realLoader;
          private SimpleFuture<D> future;

          public void __constructor__(Context context) {
            BackgroundWorker worker = new BackgroundWorker();
            future = new SimpleFuture<D>(worker) {
              @Override protected void done() {
                try {
                  final D result = get();
                  ShadowLooper.getUiThreadScheduler().post(new Runnable() {
                    @Override public void run() {
                      realLoader.deliverResult(result);
                    }
                  });
                } catch (InterruptedException e) {
                  // Ignore
                }
              }
            };
          }

          @Implementation
          public void onForceLoad() {
            ShadowApplication.getInstance().getBackgroundScheduler().post(new Runnable() {
              @Override
              public void run() {
                future.run();
              }
            });
          }

          private final class BackgroundWorker implements Callable<D> {
            @Override public D call() throws Exception {
              return realLoader.loadInBackground();
            }
          }
    }
    
    public static class TestMainActivity extends MainActivity
    {
        /**
         * Bypass search action view creation
         * which Robolectric does not support because of Shadow MenuItemCompat.getActionView() bug
         * @return 
         */
        @Override
        protected void createSearchView(Menu menu)
        {
        }
        
        public void doCreateSearchView(Menu menu)
        {
            super.createSearchView(menu);
        }
    }

    private static final String[][] RECORD_DETAILS_ARRAY =
    {
        { "description", "" },
        { "created", "2014-02-12 10:58:00.000000" },
        { "creator", "admin" },
        { "modified", "2014-02-12 11:28:35.000000" },
        { "modifier", "admin" },
        { "identifier", "2014-1392163053802" }
    };

    static final String TITLE = "Corporate Management";
    
    @Before
    public void setUp() throws Exception 
    {
        TestClassyFyApplication classyfyLauncher = TestClassyFyApplication.getTestInstance();
        classyfyLauncher.startup();
        classyfyLauncher.waitForApplicationSetup();
    }
    
    @After
    public void tearDown() 
    {
    }

    private Intent getNewIntent()
    {
        return new Intent(RuntimeEnvironment.application, MainActivity.class);
    }

    @Config(shadows = { MyShadowSystemClock.class, MyShadowAsyncTaskLoader.class })
    @Test
    public void test_parseIntent_action_view() throws InterruptedException
    {
        // Create MainActivity
        MainActivity mainActivity = Robolectric.buildActivity(TestMainActivity.class).create().get();
        // Check that ContentProvider is available for search operations
        ContentResolver contentResolver  = mainActivity.getContentResolver();
        assertThat(contentResolver.getType(ClassyFySearchEngine.CONTENT_URI)).isEqualTo("vnd.android.cursor.dir/vnd.classyfy.node");
        // Send view intent to TitleSearchResultsActivity
        Intent intent = getNewIntent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri actionUri = Uri.withAppendedPath(ClassyFySearchEngine.CONTENT_URI, "34");
        intent.setData(actionUri);
        TitleSearchResultsActivity titleSearchResultsActivity = Robolectric.buildActivity(TitleSearchResultsActivity.class)
        .create()
        .start()
        .visible()
        .get();
        ShadowLooper.getUiThreadScheduler().pause();
        ShadowApplication.getInstance().getBackgroundScheduler().pause();
        titleSearchResultsActivity.parseIntent(intent);
        ShadowApplication.getInstance().getBackgroundScheduler().runOneTask();
        Robolectric.flushForegroundScheduler();
        titleSearchResultsActivity.taskHandle.waitForTask();
        assertThat(titleSearchResultsActivity.taskHandle.getStatus()).isEqualTo(WorkStatus.FINISHED);
        PropertiesListAdapter adapter = titleSearchResultsActivity.adapter;
        for (int i = 0; (i < adapter.getCount()) && (i < RECORD_DETAILS_ARRAY.length); i++)
        {
            ListItem item = (ListItem)adapter.getItem(i);
            assertThat(item.getName().equals(RECORD_DETAILS_ARRAY[i][0]));
            assertThat(item.getValue().equals(RECORD_DETAILS_ARRAY[i][1]));
        }
        assertThat(titleSearchResultsActivity.dialog).isNotNull();
        ShadowDialog dialog = Shadows.shadowOf(ShadowDialog.getLatestDialog());
        assertThat(dialog.getTitle()).isEqualTo("Category: " + TITLE);
        assertThat(dialog.isCancelableOnTouchOutside()).isTrue();
    }
}
