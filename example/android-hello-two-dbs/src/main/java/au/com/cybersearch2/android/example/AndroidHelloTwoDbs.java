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
package au.com.cybersearch2.android.example;

import javax.inject.Singleton;

import com.example.hellotwodbs.HelloTwoDbs;

import android.content.Context;
import au.com.cybersearch2.classyapp.ApplicationContext;
import au.com.cybersearch2.classyapp.ApplicationLocale;
import au.com.cybersearch2.classydb.DatabaseAdminImpl;
import au.com.cybersearch2.classydb.NativeScriptDatabaseWork;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.example.HelloTwoDbsMain;
import dagger.Component;

/**
 * AndroidHelloTwoDbs
 * @author Andrew Bowley
 * 23 Sep 2014
 */
public class AndroidHelloTwoDbs extends HelloTwoDbsMain
{
    @Singleton
    @Component(modules = AndroidHelloTwoDbsModule.class)  
    static interface AndroidHelloTwoDbsComponent extends ApplicationModule
    {
        void inject(AndroidHelloTwoDbs helloTwoDbs);
        void inject(HelloTwoDbs helloTwoDbs);
        void inject(ApplicationContext applicationContext);
        void inject(ApplicationLocale ApplicationLocale);
        void inject(PersistenceContext persistenceContext);
        void inject(PersistenceFactory persistenceFactory);
        void inject(NativeScriptDatabaseWork nativeScriptDatabaseWork);
        void inject(DatabaseAdminImpl databaseAdminImpl);
    }

    protected AndroidHelloTwoDbsModule androidHelloTwoDbsModule;
    protected Context context;
   
    public AndroidHelloTwoDbs(final Context context)
    {
        super();
        this.context = context;
    }

 
    @Override
    protected void createObjectGraph()
    {
        // Set up dependency injection, which creates an ObjectGraph from a HelloTwoDbsModule configuration object
        AndroidHelloTwoDbsComponent component = 
            DaggerAndroidHelloTwoDbs_AndroidHelloTwoDbsComponent.builder().androidHelloTwoDbsModule(new AndroidHelloTwoDbsModule(context)).build(); 
        DI.getInstance(component).validate();
    }
}
