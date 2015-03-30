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

import android.app.Application;
import android.content.Context;
import au.com.cybersearch2.classyfy.data.RecordModel;
import au.com.cybersearch2.classynode.Node;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.WorkStatus;

public class ClassyFyApplication extends Application
{

    public static final String PU_NAME = "classyfy";
    public static final String CATEGORY_BY_NODE_ID = Node.NODE_BY_PRIMARY_KEY_QUERY + RecordModel.recordCategory.ordinal();
    public static final String FOLDER_BY_NODE_ID = Node.NODE_BY_PRIMARY_KEY_QUERY + RecordModel.recordFolder.ordinal();
    public static final int SEARCH_RESULTS_LIMIT = 50; // Same as Android
    
    public static final String TAG = "ClassyFyApplication";
    private static ClassyFyApplication singleton;
    protected ClassyFyStartup startup;

    public ClassyFyApplication()
    {
        singleton = this;
        startup = new ClassyFyStartup();
    }
    
    @Override public void onCreate() 
    {
        super.onCreate();
        init(this);
     }

    
    public void init(final Context context)
    {
        startup.start(context);
    }

    Executable getApplicationSetup()
    {
        return startup.getApplicationSetup();
    }

    public WorkStatus waitForApplicationSetup()
    {
        return startup.waitForApplicationSetup();
    }
    
    public static ClassyFyApplication getInstance()
    {
        if (singleton == null)
            throw new IllegalStateException("ClassyFyApplication called while not initialized");
        return singleton;
    }
}
