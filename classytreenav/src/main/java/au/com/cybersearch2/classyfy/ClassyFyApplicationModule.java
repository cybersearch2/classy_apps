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

import javax.inject.Named;
import javax.inject.Singleton;

import au.com.cybersearch2.classyfy.provider.ClassyFyProvider;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyjpa.AndroidPersistenceEnvironment;
import au.com.cybersearch2.classyjpa.AndroidPersistenceFactory;
import dagger.Module;
import dagger.Provides;

/**
 * ClassyFyApplicationModule
 * @author Andrew Bowley
 * 08/07/2014
 */
@Module(injects = { ClassyFyStartup.class, ClassyFyProvider.class }, includes = ClassyFyEnvironmentModule.class)
public class ClassyFyApplicationModule implements ApplicationModule
{
    @Provides @Singleton @Named(ClassyFyApplication.PU_NAME)
    AndroidPersistenceEnvironment provideAndroidPersistenceEnvironment()
    {
        return new AndroidPersistenceFactory().getAndroidPersistenceEnvironment(ClassyFyApplication.PU_NAME);
    }

}
