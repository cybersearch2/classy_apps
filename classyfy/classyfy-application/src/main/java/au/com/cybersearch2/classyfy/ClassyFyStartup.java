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

import java.sql.SQLException;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;
import au.com.cybersearch2.classyapp.ContextModule;
import au.com.cybersearch2.classydb.DatabaseAdmin;
import au.com.cybersearch2.classynode.EntityByNodeIdGenerator;
import au.com.cybersearch2.classyfy.data.RecordCategory;
import au.com.cybersearch2.classyfy.data.RecordFolder;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngine;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.Persistence;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.ThreadHelper;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classytask.WorkTracker;

import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

/**
 * ClassyFyStartup
 * @author Andrew Bowley
 * 23/07/2014
 */
public class ClassyFyStartup
{
   
    public static final String TAG = "ClassyFyStartup";
    protected ClassyFyApplicationModule classyFyApplicationModule;
    protected WorkTracker applicationSetup;
    protected PersistenceAdmin persistenceAdmin;
    protected DatabaseAdmin databaseAdmin;
    @Inject PersistenceFactory persistenceFactory;
    @Inject ThreadHelper threadHelper;

    /**
     * 
     */
    public ClassyFyStartup()
    {
        applicationSetup = new WorkTracker();
    }
    
    public void start(final Context context)
    {
        DaoManager.clearCache();
        classyFyApplicationModule = new ClassyFyApplicationModule();
        DI dependencyInjection = new DI(classyFyApplicationModule, new ContextModule(context));
        dependencyInjection.validate();
        DI.inject(this);
        persistenceFactory.initializeAllDatabases();
        Persistence persistence = persistenceFactory.getPersistenceUnit(ClassyFyApplication.PU_NAME);
        persistenceAdmin = persistence.getPersistenceAdmin();
        databaseAdmin = persistence.getDatabaseAdmin();
        Runnable setupInBackground = new Runnable()
        {
            @Override
            public void run() 
            {
                threadHelper.setBackgroundPriority();
                applicationSetup.setStatus(WorkStatus.RUNNING);
                WorkStatus status = WorkStatus.FAILED;
                if (setUpDatabase())
                {
                    try
                    {
                        EntityByNodeIdGenerator entityByNodeIdGenerator = new EntityByNodeIdGenerator();
                        persistenceAdmin.addNamedQuery(RecordCategory.class, ClassyFyApplication.CATEGORY_BY_NODE_ID, entityByNodeIdGenerator);
                        persistenceAdmin.addNamedQuery(RecordFolder.class, ClassyFyApplication.FOLDER_BY_NODE_ID, entityByNodeIdGenerator);
                        status = WorkStatus.FINISHED;
                    }
                    catch (PersistenceException e)
                    {
                        Log.e(TAG, "Database failed last stage of set up", e);
                    }
                }
                applicationSetup.setStatus(status);
                synchronized(applicationSetup)
                {
                    applicationSetup.notifyAll();
                }
                if (status == WorkStatus.FINISHED)
                {   // Call ClassyFyProvider to start SearchEngine
                    ContentResolver contentResolver  = context.getContentResolver();
                    contentResolver.getType(ClassyFySearchEngine.CONTENT_URI);
                }
            }
        };
        Thread thread = new Thread(setupInBackground);
        thread.setName("ClassyFy_start");
        thread.start();
    }

    Executable getApplicationSetup()
    {
        return applicationSetup;
    }

    public WorkStatus waitForApplicationSetup()
    {
        if ((applicationSetup.getStatus() != WorkStatus.FINISHED) &&
             (applicationSetup.getStatus() != WorkStatus.FAILED))
            synchronized(applicationSetup)
            {
                try
                {
                    applicationSetup.wait();
                }
                catch (InterruptedException e)
                {
                }
            }
        return applicationSetup.getStatus();
    }
    
    public boolean setUpDatabase() 
    {
        ConnectionSource connectionSource = persistenceAdmin.getConnectionSource();
        try
        {
            connectionSource.getReadWriteConnection();
        }
        catch (SQLException e)
        {
            Log.e(TAG, "Database error on startup", e);
            return false;
        }
        // TODO - Implement synchronization
        //WorkStatus status = databaseAdmin.waitForTask(0);
        //return (status == WorkStatus.FINISHED) || (status == WorkStatus.PENDING);
        return true;
    }

}
