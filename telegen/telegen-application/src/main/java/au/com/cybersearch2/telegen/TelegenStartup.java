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

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;
import au.com.cybersearch2.classyapp.ContextModule;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.ThreadHelper;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classytask.WorkTracker;

import com.j256.ormlite.dao.DaoManager;

/**
 * TelegenStartup
 * Creates application Object Graph for Dependency Injection and
 * Initializes persistence. 
 * @author Andrew Bowley
 * 23/07/2014
 */
public class TelegenStartup
{
   
    public static final String TAG = "TelegenStartup";
    /** Application  Dependency Injection configuration */
    protected TelegenApplicationModule classyFyApplicationModule;
    /** Tracks progress of start up and signals completion */
    protected WorkTracker applicationSetup;
    /** Persistence system configured by persistence.xml contains one or more Persistence Units */
    protected PersistenceContext persistenceContext;
    /** Allows thread priority to be adjusted for background priority */
    @Inject ThreadHelper threadHelper;

    /**
     * Construct TelegenStartup object
     */
    public TelegenStartup()
    {
        applicationSetup = new WorkTracker();
    }

    /**
     * Start application
     * @param context Android Context
     */
    public void start(final Context context)
    {
    	// Clear out ORMLite internal caches.
        DaoManager.clearCache();
        // Create application Object Graph for Dependency Injection
        classyFyApplicationModule = new TelegenApplicationModule();
        DI dependencyInjection = new DI(classyFyApplicationModule, new ContextModule(context));
        dependencyInjection.validate();
        persistenceContext = new PersistenceContext();
        // Inject threadHelper
        DI.inject(this);
        // Set up thread to initialize persistence
        Runnable setupInBackground = new Runnable()
        {
            @Override
            public void run() 
            {
                threadHelper.setBackgroundPriority();
                applicationSetup.setStatus(WorkStatus.RUNNING);
                WorkStatus status = WorkStatus.FAILED;
                try
                {
                	// Persistence system configured by persistence.xml contains one or more Persistence Unitst
                    persistenceContext.initializeAllDatabases();
                    status = WorkStatus.FINISHED;
                }
                catch (PersistenceException e)
                {   // All SQLExceptions are rethrown as PersistenceExceptions
                    Log.e(TAG, "Database initialisation failed", e);
                }
                applicationSetup.setStatus(status);
                synchronized(applicationSetup)
                {
                    applicationSetup.notifyAll();
                }
                if (status == WorkStatus.FINISHED)
                {   // Call TelegenProvider to start SearchEngine
                    // TODO - Uncomment when provider established
                    //ContentResolver contentResolver  = context.getContentResolver();
                    //contentResolver.getType(TelegenSearchEngine.CONTENT_URI);
                }
            }
        };
        Thread thread = new Thread(setupInBackground);
        thread.setName("Telegen_start");
        thread.start();
    }

    /**
     * Returns read-only application WorkTracker
     * @return Executable
     */
    Executable getApplicationSetup()
    {
        return applicationSetup;
    }

    /**
     * Wait for start up to complete
     * @return WorkStatus indicating final status of finished or failed
     */
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
    
}
