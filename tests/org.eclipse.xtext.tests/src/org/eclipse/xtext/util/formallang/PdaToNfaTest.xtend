/** 
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.util.formallang

import org.eclipse.xtext.util.formallang.StringPda.StringPdaFactory
import org.eclipse.xtext.util.formallang.StringProduction.ProdElement
import org.junit.Assert
import org.junit.Test

/** 
 * @author Moritz Eysholdt - Initial contribution and API
 */
class PdaToNfaTest extends Assert {
	def private String createPda(StringCfg cfg) {
		var ff = new FollowerFunctionImpl<ProdElement, String>(cfg)
		var pda = new PdaUtil().create(cfg, ff, new StringPdaFactory<ProdElement>("start", "stop"))
		var nfa = new PdaToNfa().pdaToNfa(pda, [!startsWith("f")], new StringNfa.StringNfaFactory())
		var actual = new NfaListFormatter<String>().format(nfa)
		return actual
	}

	@Test def void testSimple() {
		var cfg = new StringCfg()
		cfg.rule("Foo: 'foo' Bar")
		cfg.rule("Bar: 'bar'")
		var exp = '''
			start -> 'foo'
			'foo' -> 'bar'
			'bar' -> stop
		'''
		Assert.assertEquals(exp.toString.trim, createPda(cfg))
	}

//	@Test def void testSplit() {
//		var cfg = new StringCfg()
//		cfg.rule("Foo: ('a1' Bar 'b1') | ('a2' Bar 'b2')")
//		cfg.rule("Bar: 'bar'")
//		var exp = '''
//			start -> 'foo'
//			'bar' -> stop
//			'foo' -> 'bar'
//		'''
//		Assert.assertEquals(exp.toString.trim, createPda(cfg))
//	}
}
