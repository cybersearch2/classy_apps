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

import java.lang.reflect.Method;

import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestLifecycleApplication;

import android.app.Application;
import au.com.cybersearch2.classyapp.ContextModule;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.WorkStatus;

/**
 * TestTelegenApplication
 * @author Andrew Bowley
 * 14/04/2014
 */
public class TestTelegenApplication extends Application implements TestLifecycleApplication
{
    private static TelegenApplicationModule classyFyApplicationModule;
    public static final String TAG = "TestTelegenApplication";
    public static final String PU_NAME = "telegen";
    private static TestTelegenApplication singleton;
    protected TelegenStartup startup;
 
    public TestTelegenApplication()
    {
        singleton = this;
        RuntimeEnvironment.application = singleton;
        startup = new TelegenStartup();
    }

    @Override 
    public void onCreate() 
    {
        super.onCreate();
    }

    @Override
    public void beforeTest(Method method) 
    {
    }

    @Override
    public void prepareTest(Object test) 
    {
    }

    @Override
    public void afterTest(Method method) 
    {
    }

    public static TestTelegenApplication getTestInstance()
    {
        if (singleton == null)
            throw new IllegalStateException("TestTelegenApplication called while not initialized");
        return singleton;
    }
    
    public static TelegenApplicationModule getTestApplicationModule()
    {
        return classyFyApplicationModule;
    }

    public void init(Object... extraModules)
    {
        classyFyApplicationModule = new TelegenApplicationModule();
        ContextModule contextModule = new ContextModule(this);
        Object[] initModules = new Object[extraModules.length + 1];
        initModules[0] = contextModule;
        if (extraModules.length > 0)
            for (int i = 0; i < extraModules.length; i++)
                initModules[i + 1] = extraModules[i];
        new DI(classyFyApplicationModule, initModules);
    }
    
    public void startup()
    {
        startup.start(this);
    }

    Executable getApplicationSetup()
    {
        return startup.getApplicationSetup();
    }

    public WorkStatus waitForApplicationSetup()
    {
        return startup.waitForApplicationSetup();
    }
}
