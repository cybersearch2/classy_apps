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

import javax.inject.Inject;

import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.SqliteAndroidDatabaseType;

/**
 * AndroidDatabaseUpgrader
 * @author Andrew Bowley
 * 21 Nov 2014
 */
public class AndroidDatabaseUpgrader extends DatabaseUpgrader 
{
    /** Factory object to create "simple" and "complex" Persistence Unit implementations */
    @Inject PersistenceFactory persistenceFactory;

	public AndroidDatabaseUpgrader() 
	{
		super();
	}
	
    protected boolean isSupportedDatabaseType(DatabaseType databaseType)
    {
    	return databaseType instanceof SqliteAndroidDatabaseType ? true : super.isSupportedDatabaseType(databaseType);
    }
}
