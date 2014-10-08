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
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import au.com.cybersearch2.robolectric.ClassyTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.util.ActivityController;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import au.com.cybersearch2.classycontent.CursorAdapaterParameters;
import au.com.cybersearch2.classycontent.SuggestionCursorParameters;
import au.com.cybersearch2.classyfy.data.FieldDescriptor;
import au.com.cybersearch2.classyfy.data.RecordCategory;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;

import org.apache.commons.beanutils.PropertyUtils;


/**
 * TitleSearchResultsActivityTest
 * @author Andrew Bowley
 * 29/04/2014
 */
@RunWith(ClassyTestRunner.class)
public class TitleSearchResultsActivityTest
{
    static class TestTitleSearchResultsActivity extends TitleSearchResultsActivity
    {
        int titleSearchResultsFragmentId;
        
        public TestTitleSearchResultsActivity()
        {
        }
        
        protected SimpleCursorAdapter getSimpleCursorAdapter()
        {
           return simpleCursorAdapter;
        }
        
        protected TitleSearchResultsFragment getTitleSearchResultsFragment()
        {
            titleSearchResultsFragmentId = super.getTitleSearchResultsFragment().getId();
            return titleSearchResultsFragment;
        }
        
        protected LoaderManager getActivityLoaderManager()
        {
            return TitleSearchResultsActivityTest.loaderManager;
        }
    }
    
    private final static String SEARCH_TEXT = "Information";
    private ActivityController<TestTitleSearchResultsActivity> controller;
    private TitleSearchResultsActivity titleSearchResultsActivity;
    protected static LoaderManager loaderManager;
    protected static TitleSearchResultsFragment titleSearchResultsFragment;
    protected static ListView listView;
    protected static SimpleCursorAdapter simpleCursorAdapter;


    @Before
    public void setUp() 
    {
        if (controller == null)
        {
            // Set up dependency injection
            TestClassyFyApplication.getTestInstance().init(); 
        }
        controller = Robolectric.buildActivity(TestTitleSearchResultsActivity.class);
        loaderManager = mock(LoaderManager.class);
        titleSearchResultsFragment = mock(TitleSearchResultsFragment.class);
        listView = mock(ListView.class);
        when(titleSearchResultsFragment.getListView()).thenReturn(listView);
        simpleCursorAdapter = mock(SimpleCursorAdapter.class);
    }

    @After
    public void tearDown() 
    {
        controller.destroy();
    }
    

    private void createWithIntent(Intent intent) {
        titleSearchResultsActivity = (TitleSearchResultsActivity) controller
            .withIntent(intent)
            .create()
            .start()
            .visible()
            .get();
    }

    private Intent getNewIntent()
    {
        return new Intent(Robolectric.application, TitleSearchResultsActivity.class);
    }
    @Test
    public void test_Intents() 
    {
        // Launch activity with ACTION_SEARCH intent and then trigger new ACTION_VIEW intent
        
        // Launch activity
        Intent intent = getNewIntent();
        intent.setAction(Intent.ACTION_SEARCH);
        intent.putExtra(SearchManager.QUERY, SEARCH_TEXT);
        createWithIntent(intent);
        // Verify onCreate() resulted in LoaderCallbacks object being created 
        assertThat(titleSearchResultsActivity.loaderCallbacks).isNotNull();
        // Verify LoaderManager restartLoader called with correct search term
        ArgumentCaptor<Bundle> arguments = ArgumentCaptor.forClass(Bundle.class);
        verify(loaderManager).restartLoader(eq(0), arguments.capture(), eq(titleSearchResultsActivity.loaderCallbacks));
        assertThat(arguments.getValue().containsKey("QUERY_TEXT_KEY"));
        assertThat(arguments.getValue().get("QUERY_TEXT_KEY")).isEqualTo(SEARCH_TEXT);
        // Verify setOnItemClickListener called on view
        verify(listView).setOnItemClickListener(isA(OnItemClickListener.class));
        // Verify Loader constructor arguments have correct details
        SuggestionCursorParameters params = new SuggestionCursorParameters(arguments.getValue(), ClassyFySearchEngine.LEX_CONTENT_URI, 50);
        assertThat(params.getUri()).isEqualTo( Uri.parse("content://au.com.cybersearch2.classyfy.ClassyFyProvider/lex/" + SearchManager.SUGGEST_URI_PATH_QUERY));
        assertThat(params.getProjection()).isNull();
        assertThat(params.getSelection()).isEqualTo("word MATCH ?");
        assertThat(params.getSelectionArgs()).isEqualTo(new String[] { SEARCH_TEXT } );
        assertThat(params.getSortOrder()).isNull();
        // Verify Loader callbacks in sequence onCreateLoader, onLoadFinished and onLoaderReset
        CursorLoader cursorLoader = (CursorLoader) titleSearchResultsActivity.loaderCallbacks.onCreateLoader(0, arguments.getValue());
        Cursor cursor = mock(Cursor.class);
        titleSearchResultsActivity.loaderCallbacks.onLoadFinished(cursorLoader, cursor);
        verify(simpleCursorAdapter).swapCursor(cursor);
        titleSearchResultsActivity.loaderCallbacks.onLoaderReset(cursorLoader);
        verify(simpleCursorAdapter).swapCursor(null);
        // Trigger new ACTION_VIEW intent and confirm MainActivity started with ACTION_VIEW intent
        intent = getNewIntent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri actionUri = Uri.withAppendedPath(ClassyFySearchEngine.CONTENT_URI, "44");
        intent.setData(actionUri);
        titleSearchResultsActivity.onNewIntent(intent);
        ShadowActivity shadowActivity = Robolectric.shadowOf(titleSearchResultsActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent.getAction()).isEqualTo(Intent.ACTION_VIEW);
        assertThat(startedIntent.getData()).isEqualTo(actionUri);
        ShadowIntent shadowIntent = Robolectric.shadowOf(startedIntent);
        assertThat(shadowIntent.getComponent().getClassName()).isEqualTo("au.com.cybersearch2.classyfy.MainActivity");
    }
   /*
    @Test
    public void pausesAndResumesActivity() 
    {
        createWithIntent(Intent.ACTION_SEARCH);
        controller.pause().resume();
        // Assertions go here
    }

    @Test
    public void recreatesActivity() 
    {
        createWithIntent(Intent.ACTION_SEARCH);
        titleSearchResultsActivity.recreate();
        // Assertions go here
    }
    */
    @Test
    public void test_OnCreate() throws Exception
    {
        titleSearchResultsActivity = (TitleSearchResultsActivity) controller.create().get(); 
        assertThat(titleSearchResultsActivity.adapter).isNotNull();
        verify(titleSearchResultsFragment).setListAdapter(eq(simpleCursorAdapter));
        CursorAdapaterParameters params = new CursorAdapaterParameters();
        assertThat(params.getLayout()).isEqualTo(android.R.layout.simple_list_item_2);
        assertThat(params.getCursor()).isNull();
        assertThat(params.getUiBindFrom()).isEqualTo(new String[] { SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2 });
        assertThat(params.getUiBindTo()).isEqualTo(new int[] { android.R.id.text1, android.R.id.text2 } );
        assertThat(params.getFlags()).isEqualTo(0);
        assertThat(((TestTitleSearchResultsActivity)titleSearchResultsActivity).titleSearchResultsFragmentId).isEqualTo(R.id.title_search_results_fragment);
    }

    @Test
    public void test_dynamic_layout() throws Exception
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", new Locale("en", "AU"));
        RecordCategory recordCategory = new RecordCategory();
        Date created = sdf.parse("2014-02-05 18:45:46.145000");
        Date modified = sdf.parse("2014-02-12 11:55:23.121000");
        recordCategory.setCreated(created);
        recordCategory.setModified(modified);
        recordCategory.setCreator("admin");
        recordCategory.setDescription("Information Technology");
        recordCategory.setIdentifier("2014-1391586274589");
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
        @SuppressWarnings("unchecked")
        Map<String,Object> valueMap = PropertyUtils.describe(recordCategory);
        titleSearchResultsActivity = (TitleSearchResultsActivity) controller.create().get(); 
        LinearLayout dynamicLayout = new LinearLayout(titleSearchResultsActivity);
        dynamicLayout.setOrientation(LinearLayout.VERTICAL);
        int layoutHeight = LinearLayout.LayoutParams.MATCH_PARENT;
        int layoutWidth = LinearLayout.LayoutParams.WRAP_CONTENT;
        for (FieldDescriptor descriptor: fieldSet)
        {
            Object value = valueMap.get(descriptor.getName());
            if (value == null)
                continue;
            TextView titleView = new TextView(titleSearchResultsActivity);
            titleView.setText(descriptor.getTitle());
            TextView valueView = new TextView(titleSearchResultsActivity);
            valueView.setText(value.toString());
            LinearLayout fieldLayout = new LinearLayout(titleSearchResultsActivity);
            fieldLayout.setOrientation(LinearLayout.HORIZONTAL);
            fieldLayout.addView(titleView, new LinearLayout.LayoutParams(layoutWidth, layoutHeight));
            fieldLayout.addView(valueView, new LinearLayout.LayoutParams(layoutWidth, layoutHeight));
        }
    }
    
}
