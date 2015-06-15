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
package au.com.cybersearch2.telegen;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ActivityController;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import au.com.cybersearch2.classy_logic.list.AxiomTermList;
import au.com.cybersearch2.classyjpa.AndroidPersistenceEnvironment;
import au.com.cybersearch2.telegen.entity.TestIssues;

/**
 * MainActivityTest
 * @author Andrew Bowley
 * 14/05/2014
 */
@RunWith(RobolectricTestRunner.class)
public class MainActivityTest
{
   
    public static final long ID = 1L;
    static String DATABASE_NAME = "telegen.db";
    private ActivityController<MainActivity> controller;
    private MainActivity mainActivity;

    @Before
    public void setUp() 
    {
        TestTelegenApplication.getTestInstance().init(); // For DI
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
        controller = Robolectric.buildActivity(MainActivity.class);
        mainActivity = controller.create().get();
        AndroidPersistenceEnvironment androidPersistenceEnvironment = mainActivity.androidPersistenceEnvironment;
        assertThat(androidPersistenceEnvironment).isNotNull();
        SQLiteOpenHelper sqlLiteOpenHelper = androidPersistenceEnvironment.getSQLiteOpenHelper();
        assertThat(sqlLiteOpenHelper).isNotNull();
        assertThat(sqlLiteOpenHelper.getDatabaseName()).isEqualTo(DATABASE_NAME);
        SQLiteDatabase sqliteDatabase = null;
        try
        {
            sqliteDatabase = sqlLiteOpenHelper.getWritableDatabase();
            assertThat(sqliteDatabase.isDatabaseIntegrityOk()).isTrue();
            assertThat(sqliteDatabase.getVersion()).isEqualTo(1);
            Iterator<AxiomTermList> iterator = mainActivity.getIssues();
            int index = 0;
            while (iterator.hasNext())
            {
                AxiomTermList issue = iterator.next();
                String[] issueItem = TestIssues.ISSUE_DATA[index++];
                assertThat(issue.getItem(0).getValue().toString()).isEqualTo(issueItem[0]);
                assertThat(issue.getItem(1).getValue().toString()).isEqualTo(issueItem[1]);
            }
        }
        finally
        {
            if ((sqliteDatabase != null) && sqliteDatabase.isOpen())
                sqliteDatabase.close();
        }
    }
 
    @Test
    public void test_displayIssues()
    {
        Intent intent = getNewIntent();
        intent.setAction(Intent.ACTION_MAIN);
        controller = Robolectric.buildActivity(MainActivity.class).withIntent(intent);
        mainActivity = (MainActivity) controller
                .create()
                .start()
                .visible()
                .get();
        assertThat(mainActivity.displayListFragment.isVisible()).isTrue();
        assertThat(mainActivity.displayListFragment.getListView().getCount()).isEqualTo(TestIssues.ISSUE_DATA.length);
    }
}
