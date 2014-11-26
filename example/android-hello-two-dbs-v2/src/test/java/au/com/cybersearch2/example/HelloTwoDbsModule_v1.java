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
package au.com.cybersearch2.example;


import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classyinject.ApplicationModule;

/**
 * HelloTwoDbsModule_v1
 * Dependency injection data object. @see HelloTwoDbsMain_v1.
 * @author Andrew Bowley
 * 23 Sep 2014
 */
@Module(injects = HelloTwoDbsMain_v1.class, includes = HelloTwoDbsEnvironmentModule.class) 
public class HelloTwoDbsModule_v1 implements ApplicationModule
{
	@Provides @Singleton ConnectionType providesConnectionType()
	{
		return ConnectionType.file;
	}
}
 
