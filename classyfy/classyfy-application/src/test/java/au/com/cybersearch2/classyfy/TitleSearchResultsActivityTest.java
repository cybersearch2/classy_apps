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
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Singleton;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.util.SimpleFuture;

import com.j256.ormlite.support.ConnectionSource;

import dagger.Module;
import dagger.Provides;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.content.AsyncTaskLoader;
import android.view.View;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import au.com.cybersearch2.classyapp.ContextModule;
import au.com.cybersearch2.classybean.BeanMap;
import au.com.cybersearch2.classyfy.data.RecordCategory;
import au.com.cybersearch2.classyfy.data.RecordModel;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.entity.EntityManagerImpl;
import au.com.cybersearch2.classyjpa.entity.PersistenceLoader;
import au.com.cybersearch2.classyjpa.persist.Persistence;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classyjpa.persist.TestEntityManagerFactory;
import au.com.cybersearch2.classyjpa.query.EntityQuery;
import au.com.cybersearch2.classynode.Node;
import au.com.cybersearch2.classynode.NodeEntity;
import au.com.cybersearch2.classynode.NodeFinder;
import au.com.cybersearch2.classytask.ThreadHelper;
import au.com.cybersearch2.classytask.WorkerRunnable;
import au.com.cybersearch2.classywidget.PropertiesListAdapter;
import au.com.cybersearch2.classywidget.ListItem;


/**
 * TitleSearchResultsActivityTest
 * @author Andrew Bowley
 * 29/04/2014
 */
@RunWith(RobolectricTestRunner.class)
public class TitleSearchResultsActivityTest
{
    @Module(injects = { PersistenceContext.class, WorkerRunnable.class })
    static class TestModule implements ApplicationModule
    {
        @Provides @Singleton PersistenceFactory providePersistenceModule() 
        {
            PersistenceFactory persistenceFactory = mock(PersistenceFactory.class);
            Persistence persistence = mock(Persistence.class);
            when(persistenceFactory.getPersistenceUnit(isA(String.class))).thenReturn(persistence);
            PersistenceAdmin persistenceAdmin = mock(PersistenceAdmin.class);
            ConnectionSource connectionSource = mock(ConnectionSource.class);
            when(persistenceAdmin.isSingleConnection()).thenReturn(false);
            when(persistenceAdmin.getConnectionSource()).thenReturn(connectionSource);
            when(persistence.getPersistenceAdmin()).thenReturn(persistenceAdmin);
            when(persistenceAdmin.getEntityManagerFactory()).thenReturn(new TestEntityManagerFactory());
            return persistenceFactory;
        }
        
        @Provides @Singleton ThreadHelper provideThreadHelper()
        {
            return new ClassyFyThreadHelper();
        }
    }
    
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
    
    public static final String TITLE = "Corporate Management";
    public static final String MODEL = "Category";
    private final static String SEARCH_TEXT = "Information";
    private static final String RECORD_NAME = "Cybersearch2-Records";
    private static final String RECORD_VALUE = "Cybersearch2 Records";
    private static final long NODE_ID = 34L;
    private static final int RECORD_ID = 77;
    private EntityManagerImpl entityManager;

    @Before
    public void setUp() 
    {
        TestClassyFyApplication classyfyLauncher = TestClassyFyApplication.getTestInstance();
        ContextModule contextModule = new ContextModule(classyfyLauncher);
        new DI(new TestModule(), contextModule);
        TestEntityManagerFactory.setEntityManagerInstance();
        entityManager = (EntityManagerImpl) TestEntityManagerFactory.getEntityManager();
    }

    @After
    public void tearDown() 
    {
    }
    
    private Intent getNewIntent()
    {
        return new Intent(RuntimeEnvironment.application, TitleSearchResultsActivity.class);
    }
    
    @SuppressWarnings("unchecked")
    @Config(shadows = { MyShadowSystemClock.class, MyShadowAsyncTaskLoader.class })
    @Test 
    public void test_onCreate() throws Exception
    {
        Intent intent = getNewIntent();
        intent.setAction(Intent.ACTION_DEFAULT);
        TitleSearchResultsActivity titleSearchResultsActivity = Robolectric.buildActivity(TitleSearchResultsActivity.class).withIntent(intent)
        .create()
        .start()
        .visible()
        .get();
        // Check activity fields initialization
        assertThat(titleSearchResultsActivity.adapter.getCount()).isEqualTo(0);
        assertThat(titleSearchResultsActivity.resultsView).isNotNull();
        assertThat(titleSearchResultsActivity.resultsView.getListAdapter()).isEqualTo(titleSearchResultsActivity.adapter);
        assertThat(titleSearchResultsActivity.resultsView.getId()).isEqualTo(R.id.title_search_results_fragment);
        assertThat(titleSearchResultsActivity.REFINE_SEARCH_MESSAGE).isEqualTo("Only first 50 hits displayed. Please refine search.");

        // Test updateDetails() sets adapter correctly
        Node node = mock(Node.class);
        Map<String, Object> testNodeProperties = getNodeProperties();
        when(node.getProperties()).thenReturn(testNodeProperties);
        when(node.getTitle()).thenReturn(TITLE);
        when(node.getModel()).thenReturn(RecordModel.recordCategory.ordinal());
        titleSearchResultsActivity.updateDetails(node);
        assertThat(titleSearchResultsActivity.adapter.getCount()).isEqualTo(7);
        ListItem item1 = (ListItem) titleSearchResultsActivity.adapter.getItem(0);
        assertThat(item1).isNotNull();
        assertThat(item1.getName()).isEqualTo(TITLE);
        assertThat(item1.getValue()).isEqualTo(MODEL);
        
        // Test search suggesion initiated by intent
        ContentResolver contentResolver = mock(ContentResolver.class);
        titleSearchResultsActivity.contentResolver = contentResolver;
        intent.setAction(Intent.ACTION_SEARCH);
        intent.putExtra(SearchManager.QUERY, SEARCH_TEXT);
        ShadowLooper.getUiThreadScheduler().pause();
        ShadowApplication.getInstance().getBackgroundScheduler().pause();
        titleSearchResultsActivity.parseIntent(intent);
        // Navigate doSearchQuery() and onLoadComplete()
        Cursor cursor = mock(Cursor.class);
        when(cursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_1)).thenReturn(1);
        when(cursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_2)).thenReturn(2);
        when(cursor.getColumnIndexOrThrow("_id")).thenReturn(0);
        when(cursor.getCount()).thenReturn(1);
        when(cursor.moveToNext()).thenReturn(true, false);
        when(cursor.getString(1)).thenReturn(RECORD_NAME);
        when(cursor.getString(2)).thenReturn(RECORD_VALUE);
        when(cursor.getLong(0)).thenReturn(NODE_ID);
        Uri uri = Uri.parse(ClassyFySearchEngine.LEX_CONTENT_URI + "?" + 
                            SearchManager.SUGGEST_PARAMETER_LIMIT + "=" +
                            String.valueOf(ClassyFyApplication.SEARCH_RESULTS_LIMIT));
        when(contentResolver.query(uri, null, "word MATCH ?", new String[] { SEARCH_TEXT }, null)).thenReturn(cursor);
        ShadowApplication.getInstance().getBackgroundScheduler().runOneTask();
        PropertiesListAdapter adapter = mock(PropertiesListAdapter.class);
        when(adapter.getCount()).thenReturn(1);
        titleSearchResultsActivity.adapter = adapter;
        //Robolectric.flushForegroundScheduler();
        assertThat(ShadowLooper.getUiThreadScheduler().runOneTask()).isTrue();
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<ArrayList> valueList = ArgumentCaptor.forClass(ArrayList.class);
        verify(adapter).changeData(valueList.capture());
        ArrayList<ListItem> singletonList = valueList.getValue();
        ListItem value = singletonList.get(0);
        assertThat(value.getName()).isEqualTo(RECORD_NAME);
        assertThat(value.getValue()).isEqualTo(RECORD_VALUE);
        assertThat(value.getId()).isEqualTo(NODE_ID);
        
        // Test item selection by activating the onItemClickListener
        OnItemClickListener onItemClickListener = titleSearchResultsActivity.resultsView.getListView().getOnItemClickListener();
        ShadowApplication.getInstance().getBackgroundScheduler().advanceToLastPostedRunnable();
        PersistenceLoader loader = mock(PersistenceLoader.class);
        titleSearchResultsActivity.loader = loader;
        onItemClickListener.onItemClick(null, null, 0, NODE_ID);
        //assertThat(titleSearchResultsActivity.progressFragment.getSpinner().getVisibility()).isEqualTo(View.VISIBLE);
        ShadowApplication.getInstance().getBackgroundScheduler().runOneTask();
        // Navigate NodeFinder execution. Requires mocking JPA activity to get NodeEntity and CategoryRecord
        ArgumentCaptor<NodeFinder> nodeFinder = ArgumentCaptor.forClass(NodeFinder.class);
        verify(loader).execute(eq(ClassyFyApplication.PU_NAME), nodeFinder.capture());
        NodeEntity nodeEntity = new NodeEntity();
        // Set id == parent_id so only one node returned
        nodeEntity.set_id(1);
        nodeEntity.set_parent_id(1);
        nodeEntity.set_id((int)NODE_ID);
        nodeEntity.setModel(1);
        nodeEntity.setName(RECORD_NAME);
        nodeEntity.setTitle(RECORD_VALUE);
        when(entityManager.find(NodeEntity.class, (int)NODE_ID)).thenReturn(nodeEntity);
        EntityQuery<?> query = mock(EntityQuery.class);
        RecordCategory category = getRecordCategory();
        category.set_id(RECORD_ID);
        category.set_nodeId((int)NODE_ID);
        when(query.getSingleResult()).thenReturn(category);
        when(entityManager.createNamedQuery(Node.NODE_BY_PRIMARY_KEY_QUERY + 1)).thenReturn(query);
        nodeFinder.getValue().doTask(entityManager);
        nodeFinder.getValue().onPostExecute(true);
        @SuppressWarnings("rawtypes")
        // Check list view is updated correctly
        ArgumentCaptor<ArrayList> fieldsList = ArgumentCaptor.forClass(ArrayList.class);
        verify(adapter, times(2)).changeData(fieldsList.capture());
        ArrayList<ListItem> fieldValues = fieldsList.getValue();
        value = fieldValues.get(0);
        assertThat(fieldValues.size()).isEqualTo(7);
        assertThat(value.getName()).isEqualTo(RECORD_VALUE);
        assertThat(value.getValue()).isEqualTo(MODEL);
        value = fieldValues.get(1);
        assertThat(value.getName()).isEqualTo("Description");
        assertThat(value.getValue()).isEqualTo(category.getDescription());
        Robolectric.flushForegroundScheduler();
        // Check dialog is displayed
        ShadowDialog dialog = Shadows.shadowOf(ShadowDialog.getLatestDialog());
        assertThat(dialog.getTitle()).isEqualTo("Category: " + RECORD_VALUE);
        assertThat(dialog.isCancelableOnTouchOutside()).isTrue();
    }
 
    @Test
    public void test_parseIntent_action_view_invalid_uri()
    {
        Intent intent = getNewIntent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri actionUri = ClassyFySearchEngine.CONTENT_URI;
        intent.setData(actionUri);
        Robolectric.buildActivity(TitleSearchResultsActivity.class).withIntent(intent)
        .create()
        .start()
        .visible()
        .get();
        ShadowToast.showedToast("Invalid resource address: \"" + ClassyFySearchEngine.CONTENT_URI.toString() + "\"");
    }

    @Test
    public void test_parseIntent_action_view_invalid_node_id()
    {
        Intent intent = getNewIntent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri actionUri = Uri.withAppendedPath(ClassyFySearchEngine.CONTENT_URI, "x4");
        intent.setData(actionUri);
        Robolectric.buildActivity(TitleSearchResultsActivity.class).withIntent(intent)
        .create()
        .start()
        .visible()
        .get();
        ShadowToast.showedToast("Resource address has invalid ID: \"" + actionUri.toString() + "\"");
    }
    
    @Test
    public void test_parseIntent_action_edit() throws Exception
    {
        TitleSearchResultsActivity titleSearchResultsActivity = Robolectric.buildActivity(TitleSearchResultsActivity.class)
        .create()
        .start()
        .visible()
        .get();
        Node root = Node.rootNodeNewInstance();
        Node data = new Node(RecordModel.recordCategory.ordinal(), root);
        data.setTitle(TITLE);
        data.setProperties(getNodeProperties());
        titleSearchResultsActivity.showDetailsDialog(data);
        ShadowDialog dialog = Shadows.shadowOf(ShadowDialog.getLatestDialog());
        assertThat(dialog.getTitle()).isEqualTo("Node Details");
        assertThat(dialog.isCancelableOnTouchOutside()).isTrue();
        //TextView tv1 = (TextView) ShadowDialog.getLatestDialog().findViewById(R.id.node_detail_title);
        //assertThat(tv1.getText()).isEqualTo(TITLE);
        //TextView tv2 = (TextView) ShadowDialog.getLatestDialog().findViewById(R.id.node_detail_model);
        //assertThat(tv2.getText()).isEqualTo(RecordModel.recordCategory.toString());
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
/*
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
    */
    private Map<String, Object> getNodeProperties() throws Exception
    {
        return new BeanMap(getRecordCategory());
    }

    private RecordCategory getRecordCategory() throws Exception
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", new Locale("en", "AU"));
        RecordCategory recordCategory = new RecordCategory();
        Date created = sdf.parse("2014-02-05 18:45:46.000000");
        Date modified = sdf.parse("2014-02-12 11:55:23.000000");
        recordCategory.setCreated(created);
        recordCategory.setModified(modified);
        recordCategory.setCreator("admin");
        recordCategory.setModifier("prole");
        recordCategory.setDescription("Information Technology");
        recordCategory.setIdentifier("2014-1391586274589");
        return recordCategory;
    }
    
}
