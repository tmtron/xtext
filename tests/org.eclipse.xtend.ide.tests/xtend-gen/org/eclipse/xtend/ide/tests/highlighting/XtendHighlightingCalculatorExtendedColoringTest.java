/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.ide.tests.highlighting;

import com.google.inject.Inject;
import org.eclipse.xtend.core.tests.AbstractXtendTestCase;
import org.eclipse.xtend.ide.common.highlighting.XtendHighlightingStyles;
import org.eclipse.xtend.ide.tests.highlighting.XtendHighlightingCalculatorTest;
import org.eclipse.xtext.xbase.ide.highlighting.XbaseHighlightingStyles;
import org.eclipse.xtext.xbase.lib.Extension;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Christian Schneider - Initial contribution and API
 */
@SuppressWarnings("all")
public class XtendHighlightingCalculatorExtendedColoringTest extends AbstractXtendTestCase implements XtendHighlightingStyles {
  @Inject
  @Extension
  private XtendHighlightingCalculatorTest helper;
  
  @Before
  public void setUp() throws Exception {
    this.helper.setUp();
  }
  
  @After
  public void tearDown() throws Exception {
    this.helper.tearDown();
  }
  
  public void expectAbstractClass(final int offset, final int length) {
    this.helper.expect(offset, length, XbaseHighlightingStyles.ABSTRACT_CLASS);
  }
  
  public void expectClass(final int offset, final int length) {
    this.helper.expect(offset, length, XbaseHighlightingStyles.CLASS);
  }
  
  public void expectInterface(final int offset, final int length) {
    this.helper.expect(offset, length, XbaseHighlightingStyles.INTERFACE);
  }
  
  public void expectTypeArgument(final int offset, final int length) {
    this.helper.expectAbsolute(offset, length, XbaseHighlightingStyles.TYPE_ARGUMENT);
  }
  
  public void expectTypeVariable(final int offset, final int length) {
    this.helper.expect(offset, length, XbaseHighlightingStyles.TYPE_VARIABLE);
  }
  
  public void highlight() {
    this.helper.highlight("");
  }
  
  @Test
  public void testSimpleClass() {
    this.helper.classDefString = "class Foo";
    this.expectClass(6, 3);
    this.highlight();
  }
  
  @Test
  public void testSimpleInterface() {
    this.helper.classDefString = "interface Foo";
    this.expectInterface(10, 3);
    this.highlight();
  }
  
  @Test
  public void testSimpleTypeWithTypeVariable() {
    this.helper.classDefString = "class Foo<Foo>";
    this.expectClass(6, 3);
    this.expectTypeVariable(10, 3);
    this.highlight();
  }
}
