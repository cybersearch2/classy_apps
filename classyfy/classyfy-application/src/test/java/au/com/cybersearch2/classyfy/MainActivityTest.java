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
import static org.mockito.Mockito.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;

import au.com.cybersearch2.robolectric.ClassyTestRunner;

import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.util.ActivityController;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import au.com.cybersearch2.classybean.BeanMap;
import au.com.cybersearch2.classyfy.data.RecordModel;
import au.com.cybersearch2.classyfy.data.RecordCategory;
import au.com.cybersearch2.classynode.Node;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classywidget.PropertiesListAdapter.Value;

/**
 * MainActivityTest
 * @author Andrew Bowley
 * 14/05/2014
 */
@RunWith(ClassyTestRunner.class)
public class MainActivityTest
{
    public static class TestMainActivity extends MainActivity
    {
        @Override
        protected MenuOptionsHandler getMenuOptionsHandler()
        {
            return new MenuOptionsHandler(){

                @Override
                public void onCreateOptionsMenu(Menu menu) 
                {
                    MenuItem searchMenuItem = menu.findItem(R.id.action_search);
                    if (searchMenuItem == null)
                        throw new IllegalStateException("Search menu item not found in main menu");
                    // SearchView not accessible in Robolectric
                }};
        }
    }
    
    public static final long ID = 1L;
    public static final String NAME = "name";
    public static final String TITLE = "Corporate Management";
    public static final String MODEL = "recordCategory";

    private static ActivityController<TestMainActivity> controller;
    private MainActivity mainActivity;

    @Before
    public void setUp() 
    {
        if (controller == null)
            TestClassyFyApplication.getTestInstance().init(); // For DI
        controller = Robolectric.buildActivity(TestMainActivity.class);
    }

    @After
    public void tearDown() 
    {
        controller.destroy();
    }
    

    private Intent getNewIntent()
    {
        return new Intent(RuntimeEnvironment.application, MainActivity.class);
    }
    
    @Test
    public void test_OnCreate() throws Exception
    {
        mainActivity = controller.create().get();
        // Test onCreateLoader returns null if ars parameter is null
        assertThat(mainActivity.menuOptionsHandler).isNotNull();
        assertThat(mainActivity.progressFragment).isNotNull();
        assertThat(mainActivity.nodeDetailsFragment).isNotNull();
        assertThat(mainActivity.adapter.getCount()).isEqualTo(0);
        // Test updateDetails() sets adapter correctly
        Node node = mock(Node.class);
        Map<String, Object> testNodeProperties = getNodeProperties();
        when(node.getProperties()).thenReturn(testNodeProperties);
        when(node.getTitle()).thenReturn(TITLE);
        when(node.getModel()).thenReturn(RecordModel.recordCategory.ordinal());
        mainActivity.updateDetails(node);
        assertThat(mainActivity.adapter.getCount()).isEqualTo(7);
        Value item1 = (Value) mainActivity.adapter.getItem(0);
        assertThat(item1).isNotNull();
        assertThat(item1.getName()).isEqualTo(TITLE);
        assertThat(item1.getValue()).isEqualTo(RecordModel.recordCategory.toString());
    }
    
    @Test
    public void test_createSearchView()
    {
        Menu menu = mock(Menu.class);
        MenuItem searchMenuItem = mock(MenuItem.class);
        SearchView searchView = mock(SearchView.class);
        mainActivity = controller.create().get();
        when(menu.findItem(R.id.action_search)).thenReturn(searchMenuItem);
        when(searchMenuItem.getActionView()).thenReturn(searchView);
        mainActivity.createSearchView(menu);
        // Shadow SearchManager returns null for SearchablInfo
        //verify(searchView).setSearchableInfo(isA(SearchableInfo.class));
        verify(searchView).setIconifiedByDefault(false);
        
    }
    
/*    
    @Test
    public void test_parseIntent_action_view() throws Exception
    {
        mainActivity = controller.create().get();
        Intent intent = getNewIntent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri actionUri = Uri.withAppendedPath(ClassyFySearchEngine.CONTENT_URI, "44");
        intent.setData(actionUri);
        mainActivity.parseIntent(intent);
        
        ArgumentCaptor<MainActivityNodeFinder> nodeFinderArg = ArgumentCaptor.forClass(MainActivityNodeFinder.class);
        verify(taskHandler).runTask(nodeFinderArg.capture());//.execute(Integer.valueOf(44));
        EntityManagerLite entityManager = mock(ClassyEntityManager.class);
        when(entityManager.find(NodeBean.class, 44)).thenReturn(null);
        Query query = mock(EntityQuery.class);
        when(entityManager.createNamedQuery(isA(String.class))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(getRecordCategory());
        MainActivityNodeFinder nodeFinder = nodeFinderArg.getValue();
        nodeFinder.doInBackground(entityManager);
        Node node = nodeFinder.getData();
        assertThat(node).isNotNull();
        assertThat(node.getProperties()).isNotNull();
        assertThat(node.getProperties().get("identifier")).isEqualTo("2014-1391586274589");
    }
*/
    @Test
    public void test_parseIntent_action_view_invalid_uri()
    {
        mainActivity = controller.create().get();
        Intent intent = getNewIntent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri actionUri = ClassyFySearchEngine.CONTENT_URI;
        intent.setData(actionUri);
        mainActivity.parseIntent(intent);
        ShadowToast.showedToast("Invalid resource address: \"" + ClassyFySearchEngine.CONTENT_URI.toString() + "\"");
    }

    @Test
    public void test_parseIntent_action_view_invalid_node_id()
    {
        mainActivity = controller.create().get();
        Intent intent = getNewIntent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri actionUri = Uri.withAppendedPath(ClassyFySearchEngine.CONTENT_URI, "x4");
        intent.setData(actionUri);
        mainActivity.parseIntent(intent);
        ShadowToast.showedToast("Resource address has invalid ID: \"" + actionUri.toString() + "\"");
    }

    
    @Test
    public void test_parseIntent_action_edit() throws Exception
    {
        // Cannot test ProgressBar when test intent launches activity as onCreate() is called
        // before PlaceHolderFragment view is created.
        mainActivity = (MainActivity) controller
                .create()
                .start()
                .visible()
                .get();
        Node root = Node.rootNodeNewInstance();
        Node data = new Node(RecordModel.recordCategory.ordinal(), root);
        data.setTitle(TITLE);
        data.setProperties(getNodeProperties());
        mainActivity.showDetailsDialog(data);
        ShadowDialog dialog = Shadows.shadowOf(ShadowDialog.getLatestDialog());
        assertThat(dialog.getTitle()).isEqualTo("Node Details");
        assertThat(dialog.isCancelableOnTouchOutside()).isTrue();
        TextView tv1 = (TextView) ShadowDialog.getLatestDialog().findViewById(R.id.node_detail_title);
        assertThat(tv1.getText()).isEqualTo(TITLE);
        TextView tv2 = (TextView) ShadowDialog.getLatestDialog().findViewById(R.id.node_detail_model);
        assertThat(tv2.getText()).isEqualTo(RecordModel.recordCategory.toString());
        assertThat(mainActivity.progressFragment.getSpinner().getVisibility()).isEqualTo(View.GONE);
    }

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
