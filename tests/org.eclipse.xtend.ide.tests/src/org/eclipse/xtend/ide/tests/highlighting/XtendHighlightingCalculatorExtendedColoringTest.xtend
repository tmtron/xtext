/*******************************************************************************
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.ide.tests.highlighting

import com.google.inject.Inject
import org.eclipse.xtend.ide.common.highlighting.XtendHighlightingStyles
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.eclipse.xtend.core.tests.AbstractXtendTestCase

/**
 * @author Christian Schneider - Initial contribution and API
 */
class XtendHighlightingCalculatorExtendedColoringTest extends AbstractXtendTestCase implements XtendHighlightingStyles {

	@Inject
	extension XtendHighlightingCalculatorTest helper
	
	@Before
	def void setUp() throws Exception {
		helper.setUp()
	}
	
	@After
	def void tearDown() throws Exception {
		helper.tearDown()
	}

	def void expectAbstractClass(int offset, int length) {
		expect(offset, length, ABSTRACT_CLASS)
	}
	
	def void expectClass(int offset, int length) {
		expect(offset, length, CLASS)
	}
	
	def void expectInterface(int offset, int length) {
		expect(offset, length, INTERFACE)
	}
	
	def void expectTypeArgument(int offset, int length) {
		expectAbsolute(offset, length, TYPE_ARGUMENT)
	}

	def void expectTypeVariable(int offset, int length) {
		expect(offset, length, TYPE_VARIABLE)
	}
	
	def highlight() {
		highlight('')
	}
	
	@Test
	def void testSimpleClass() {
		classDefString = "class Foo"
		expectClass(6, 3)
		
		highlight()
	}
	
	@Test
	def void testSimpleInterface() {
		classDefString = "interface Foo"
		expectInterface(10, 3)
		
		highlight()
	}
	
	@Test
	def void testSimpleTypeWithTypeVariable() {
		classDefString = "class Foo<Foo>"
		expectClass(6, 3)
		expectTypeVariable(10, 3)
		
		highlight()
	}
}