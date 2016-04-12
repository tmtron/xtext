/**
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.util.formallang;

import com.google.common.base.Predicate;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.util.formallang.FollowerFunctionImpl;
import org.eclipse.xtext.util.formallang.NfaListFormatter;
import org.eclipse.xtext.util.formallang.PdaToNfa;
import org.eclipse.xtext.util.formallang.PdaUtil;
import org.eclipse.xtext.util.formallang.StringCfg;
import org.eclipse.xtext.util.formallang.StringNfa;
import org.eclipse.xtext.util.formallang.StringPda;
import org.eclipse.xtext.util.formallang.StringProduction;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
@SuppressWarnings("all")
public class PdaToNfaTest extends Assert {
  private String createPda(final StringCfg cfg) {
    FollowerFunctionImpl<StringProduction.ProdElement, String> ff = new FollowerFunctionImpl<StringProduction.ProdElement, String>(cfg);
    PdaUtil _pdaUtil = new PdaUtil();
    StringPda.StringPdaFactory<StringProduction.ProdElement> _stringPdaFactory = new StringPda.StringPdaFactory<StringProduction.ProdElement>("start", "stop");
    StringPda pda = _pdaUtil.<String, String, StringProduction.ProdElement, String, StringPda>create(cfg, ff, _stringPdaFactory);
    PdaToNfa<String, String, String> _pdaToNfa = new PdaToNfa<String, String, String>();
    final Predicate<String> _function = new Predicate<String>() {
      @Override
      public boolean apply(final String it) {
        boolean _startsWith = it.startsWith("f");
        return (!_startsWith);
      }
    };
    StringNfa.StringNfaFactory _stringNfaFactory = new StringNfa.StringNfaFactory();
    StringNfa nfa = _pdaToNfa.<StringNfa>pdaToNfa(pda, _function, _stringNfaFactory);
    NfaListFormatter<String> _nfaListFormatter = new NfaListFormatter<String>();
    String actual = _nfaListFormatter.format(nfa);
    return actual;
  }
  
  @Test
  public void testSimple() {
    StringCfg cfg = new StringCfg();
    cfg.rule("Foo: \'foo\' Bar");
    cfg.rule("Bar: \'bar\'");
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("start -> \'foo\'");
    _builder.newLine();
    _builder.append("\'foo\' -> \'bar\'");
    _builder.newLine();
    _builder.append("\'bar\' -> stop");
    _builder.newLine();
    String exp = _builder.toString();
    String _string = exp.toString();
    String _trim = _string.trim();
    String _createPda = this.createPda(cfg);
    Assert.assertEquals(_trim, _createPda);
  }
}
