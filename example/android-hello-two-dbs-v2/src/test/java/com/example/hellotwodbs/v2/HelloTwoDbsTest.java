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
package com.example.hellotwodbs.v2;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import com.example.hellotwodbs.v2.HelloTwoDbs;

/**
 * HelloTwoDebsTest
 * @author Andrew Bowley
 * 24 Nov 2014
 */
@RunWith(RobolectricTestRunner.class)
public class HelloTwoDbsTest 
{

	@Test
	public void test_doSampleDatabaseStuff()
	{
		HelloTwoDbs helloTwoDbs = new HelloTwoDbs();
		System.out.println(helloTwoDbs.doSampleDatabaseStuff("robolectric"));
	}
}
