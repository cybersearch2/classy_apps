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
package au.com.cybersearch2.example.v2;

import javax.inject.Singleton;

import com.example.hellotwodbs.v2.HelloTwoDbs;

import android.content.Context;
import au.com.cybersearch2.classydb.DatabaseAdminImpl;
import au.com.cybersearch2.classydb.NativeScriptDatabaseWork;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import dagger.Component;
import au.com.cybersearch2.classyapp.ApplicationContext;
import au.com.cybersearch2.classyapp.ApplicationLocale;

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
        void inject(HelloTwoDbs helloTwoDbs);
        void inject(AndroidHelloTwoDbs androidHelloTwoDbs);
        void inject(SimpleOpenHelperCallbacks simpleOpenHelperCallbacks);
        void inject(ComplexOpenHelperCallbacks complexOpenHelperCallbacks);
        void inject(ApplicationContext applicationContext);
        void inject(ApplicationLocale applicationLocale);
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
       if (context == null)
            throw new IllegalArgumentException("Paramemter \"context\" is null");
       this.context = context;
    }

    @Override
    protected void createObjectGraph()
    {
        // Set up dependency injection, which creates an ObjectGraph from a HelloTwoDbsModule configuration object
        AndroidHelloTwoDbsComponent component = 
                DaggerAndroidHelloTwoDbs_AndroidHelloTwoDbsComponent.builder().androidHelloTwoDbsModule(new AndroidHelloTwoDbsModule(context)).build(); 
        DI.getInstance(component).validate();
        ApplicationContext applicationContex = new ApplicationContext();
       if (applicationContex.getContext() == null)
            throw new IllegalStateException("ApplicationContext \"context\" is null");
       if (applicationContex.getContext().getAssets() == null)
            throw new IllegalStateException("ApplicationContext \"assets\" is null");
    }
}
