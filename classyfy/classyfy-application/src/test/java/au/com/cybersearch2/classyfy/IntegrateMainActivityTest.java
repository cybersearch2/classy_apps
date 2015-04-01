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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import au.com.cybersearch2.robolectric.ClassyTestRunner;
import org.robolectric.util.ActivityController;
import org.robolectric.annotation.Config;
import java.util.Properties;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import au.com.cybersearch2.classyfy.MainActivity.MenuOptionsHandler;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classywidget.PropertiesListAdapter;
import au.com.cybersearch2.classywidget.PropertiesListAdapter.Value;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;

/**
 * IntegrateMainActivityTest
 * @author Andrew Bowley
 * 26/05/2014
 */
@RunWith(ClassyTestRunner.class)
public class IntegrateMainActivityTest
{
    //private static final String TITLE = "Corporate Management";
    //private static final String TOP_TITLE = "Cybersearch2 Records";
    private static final String[][] RECORD_DETAILS_ARRAY =
    {
        { "description", "" },
        { "created", "2014-02-12 10:58:00.000000" },
        { "creator", "admin" },
        { "modified", "2014-02-12 11:28:35.000000" },
        { "modifier", "admin" },
        { "identifier", "2014-1392163053802" }
    };
    private ActivityController<MainActivity> controller;
    private MainActivity mainActivity;
    private MenuOptionsHandler menuOptionsHandler;
    private static boolean firstTime = true;

    @Before
    public void setUp() throws Exception 
    {
        controller = Robolectric.buildActivity(MainActivity.class);
        try
        {
            mainActivity = controller.create().get();
        }
        catch(IllegalStateException e)
        {
            // Ignore this first-time exception. Cause unknown.
        }
        if (mainActivity == null)
            mainActivity = controller.create().get();
        menuOptionsHandler = mainActivity.menuOptionsHandler;
        // Prevent NPE caused by Robolectric lack of support for SearchView
        mainActivity.menuOptionsHandler = new MenuOptionsHandler(){

            @Override
            public void onCreateOptionsMenu(Menu menu) {
            }};
    }
    
    @After
    public void tearDown() 
    {
        Robolectric.reset(Config.Implementation.fromProperties(new Properties()));
    }

    private Intent getNewIntent()
    {
        return new Intent(Robolectric.application, MainActivity.class);
    }
    
    @Test
    public void test_OnCreate() throws Exception
    {
         // Test onCreateLoader returns null if args parameter is null
        assertThat(mainActivity.menuOptionsHandler).isNotNull();
        Menu menu = mock(Menu.class);
        MenuItem searchMenuItem = mock(MenuItem.class);
        SearchView searchView = mock(SearchView.class);
        when(menu.findItem(R.id.action_search)).thenReturn(searchMenuItem);
        when(searchMenuItem.getActionView()).thenReturn(searchView);
        menuOptionsHandler.onCreateOptionsMenu(menu);
        // Shadow SearchManager returns null for SearchablInfo
        //verify(searchView).setSearchableInfo(isA(SearchableInfo.class));
        verify(searchView).setIconifiedByDefault(false);
    }
    
    @Test
    public void test_parseIntent_action_view()
    {
        if (firstTime)
        {
            firstTime = false;
            TestClassyFyApplication.getTestInstance().startup();
            assertThat(TestClassyFyApplication.getTestInstance().waitForApplicationSetup()).isEqualTo(WorkStatus.FINISHED);
        }
        Intent intent = getNewIntent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri actionUri = Uri.withAppendedPath(ClassyFySearchEngine.CONTENT_URI, "34");
        intent.setData(actionUri);
        PropertiesListAdapter adapter = mainActivity.adapter;
        controller.start().visible();
        mainActivity.parseIntent(intent);
        //ShadowActivity shadowActivity = Robolectric.shadowOf(mainActivity);
        for (int i = 0; (i < adapter.getCount()) && (i < RECORD_DETAILS_ARRAY.length); i++)
        {
            Value item = (Value)adapter.getItem(i);
            assertThat(item.getName().equals(RECORD_DETAILS_ARRAY[i][0]));
            assertThat(item.getValue().equals(RECORD_DETAILS_ARRAY[i][1]));
        }
        /*
        Node node = (Node) args.get("NODE_KEY");
        assertThat(node).isNotNull();
        assertThat(node.get_id()).isEqualTo(34);
        assertThat(node.get_parent_id()).isEqualTo(1);
        assertThat(node.getChildren()).isNotNull();
        assertThat(node.getChildren().size()).isEqualTo(8);
        assertThat(node.getParent()).isNotNull();
        assertThat(node.getParent() instanceof Node).isTrue();
        Node parent = (Node)node.getParent();
        assertThat(parent.get_id()).isEqualTo(1);
        assertThat(parent.get_parent_id()).isEqualTo(1);
        assertThat(parent.getChildren().size()).isEqualTo(7);
        assertThat(parent.getChildren().contains(node)).isTrue();
        assertThat(parent.getTitle()).isEqualTo(TOP_TITLE);
        assertThat(parent.getModel()).isEqualTo(Model.recordCategory);
        Node root = (Node)parent.getParent();
        assertThat(root.getChildren().size()).isEqualTo(1);
        assertThat(root.getChildren().contains(parent)).isTrue();
        assertThat(root.getModel()).isEqualTo(Model.root);
        Map<String,Object> properties = node.getProperties();
        assertThat(properties).isNotNull();
        verifyStringProperty(properties, RecordField.identifier, "2014-1392163053802");
        verifyStringProperty(properties, RecordField.creator, "admin");
        verifyStringProperty(properties, RecordField.modifier, "admin");
        verifyStringProperty(properties, RecordField.description, "");
        verifyDateProperty(properties, RecordField.created, "2014-02-12 10:58:00.000000");
        verifyDateProperty(properties, RecordField.modified, "2014-02-12 11:28:35.000000");
        */
     }
/*
    private void verifyStringProperty(Map<String,Object> properties, RecordField key, String validValue)
    {
        Object value = properties.get(key.toString());
        if (value == null)
            assertThat(validValue).isNull();
        else
        {
            assertThat(value instanceof String);
            assertThat(value).isEqualTo(validValue);
        }
    }

    private void verifyDateProperty(Map<String,Object> properties, RecordField key, String validValue)
    {
        Object value = properties.get(key.toString());
        if (value == null)
            assertThat(validValue).isNull();
        else
        {
            assertThat(value instanceof Date);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS"); //, new Locale("en", "AU"));
            assertThat(sdf.format((Date)value)).isEqualTo(validValue);
        }
    }
*/    
}
