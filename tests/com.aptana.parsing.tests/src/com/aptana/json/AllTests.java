/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.json;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author klindsey
 *
 */
public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite(AllTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(JSONSchemaTests.class);
		suite.addTestSuite(JSONSchemaTransitionTests.class);
		//$JUnit-END$
		return suite;
	}

}
