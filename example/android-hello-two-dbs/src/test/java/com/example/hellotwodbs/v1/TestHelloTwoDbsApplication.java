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
package com.example.hellotwodbs.v1;

import java.lang.reflect.Method;

import org.robolectric.TestLifecycleApplication;

import com.example.hellotwodbs.v1.HelloTwoDbsApplication;

/**
 * TestHelloTwoDbsApplication
 * @author Andrew Bowley
 * 22 Nov 2014
 */
public class TestHelloTwoDbsApplication extends HelloTwoDbsApplication implements TestLifecycleApplication 
{
    private static TestHelloTwoDbsApplication singleton;
    
	public TestHelloTwoDbsApplication()
	{
		singleton = this;
	}
	
    @Override 
    public void onCreate() 
    {
        super.onCreate();
    }

	@Override
	public void afterTest(Method arg0) 
	{
	}

	@Override
	public void beforeTest(Method arg0) 
	{
	}

	@Override
	public void prepareTest(Object arg0) 
	{
	}

    public static TestHelloTwoDbsApplication getTestInstance()
    {
        if (singleton == null)
            throw new IllegalStateException("TestHelloTwoDbsApplication called while not initialized");
        return singleton;
    }
    
}
