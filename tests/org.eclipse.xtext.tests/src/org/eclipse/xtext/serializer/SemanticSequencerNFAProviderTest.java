/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.serializer;

import java.util.List;
import java.util.Map;

import org.eclipse.xtext.AbstractElement;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.XtextStandaloneSetup;
import org.eclipse.xtext.grammaranalysis.impl.GrammarElementTitleSwitch;
import org.eclipse.xtext.junit4.AbstractXtextTests;
import org.eclipse.xtext.serializer.analysis.ISemanticSequencerNfaProvider;
import org.eclipse.xtext.serializer.analysis.ISemanticSequencerNfaProvider.ISemState;
import org.eclipse.xtext.serializer.analysis.SerializationContext;
import org.eclipse.xtext.util.Pair;
import org.eclipse.xtext.util.formallang.Nfa;
import org.eclipse.xtext.util.formallang.NfaListFormatter;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
public class SemanticSequencerNFAProviderTest extends AbstractXtextTests {

	private static class ToStr implements Function<ISemState, String> {
		private Nfa<ISemState> nfa;

		public ToStr(Nfa<ISemState> nfa) {
			super();
			this.nfa = nfa;
		}

		private Function<AbstractElement, String> ts = new GrammarElementTitleSwitch().showAssignments().hideCardinality().showQualified();

		@Override
		public String apply(ISemState from) {
			if (from == nfa.getStart())
				return "start";
			if (from == nfa.getStop())
				return "stop";
			AbstractElement element = from.getAssignedGrammarElement();
			return element != null ? ts.apply(element) : from.toString();
		}
	}

	final static String HEADER = "grammar org.eclipse.xtext.serializer.SemanitcSequencerNFAProviderTest"
			+ " with org.eclipse.xtext.common.Terminals "
			+ "generate semanitcSequencerNFAProviderTest \"http://www.eclipse.org/2010/tmf/xtext/SequenceParserPDAProvider\"  ";

	protected String getParserRule(String body) throws Exception {
		Grammar grammar = (Grammar) getModel(HEADER + body);
		ISemanticSequencerNfaProvider nfaProvider = get(ISemanticSequencerNfaProvider.class);
		Map<ISerializationContext, Nfa<ISemState>> pdas = nfaProvider.getSemanticSequencerNFAs(grammar);
		List<Pair<List<ISerializationContext>, Nfa<ISemState>>> grouped = SerializationContext.groupByEqualityAndSort(pdas);
		NfaListFormatter<ISemState> formatter = new NfaListFormatter<ISemState>().sortFollowers();
		List<String> result = Lists.newArrayList();
		for (Pair<List<ISerializationContext>, Nfa<ISemState>> e : grouped) {
			formatter.setStateFormatter(new ToStr(e.getSecond()));
			result.add(e.getFirst() + ":");
			String str = formatter.format(e.getSecond());
			result.add("  " + str.replace("\n", "\n  "));
		}
		return Joiner.on("\n").join(result);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		with(XtextStandaloneSetup.class);
	}

	@Test
	public void testKeyword() throws Exception {
		String actual = getParserRule("Rule: a1=ID 'kw1' a2=ID;");
		StringBuilder expected = new StringBuilder();
		expected.append("Rule returns Rule:\n");
		expected.append("  start -> a1=ID\n");
		expected.append("  a1=ID -> a2=ID\n");
		expected.append("  a2=ID -> stop");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testSplit() throws Exception {
		String actual = getParserRule("Rule: a1=ID F b1=ID | a2=ID F b2=ID; fragment F: f=ID;");
		StringBuilder expected = new StringBuilder();
		expected.append("Rule returns Rule:\n");
		expected.append("  start -> a1=ID, a2=ID\n");
		expected.append("  a1=ID -> 1:f=ID\n");
		expected.append("  1:f=ID -> b1=ID\n");
		expected.append("  2:f=ID -> b2=ID\n");
		expected.append("  b1=ID -> stop\n");
		expected.append("  a2=ID -> 2:f=ID\n");
		expected.append("  b2=ID -> stop");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testMinimizePrecedents1() throws Exception {
		String actual = getParserRule("Rule: a1=ID F | a2=ID F; fragment F: f=ID;");
		StringBuilder expected = new StringBuilder();
		expected.append("Rule returns Rule:\n");
		expected.append("  start -> a1=ID, a2=ID\n");
		expected.append("  a1=ID -> f=ID\n");
		expected.append("  f=ID -> stop\n");
		expected.append("  a2=ID -> f=ID");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testMinimizePrecedents2() throws Exception {
		String actual = getParserRule("Rule: (a1=ID F | a2=ID F) b=ID; fragment F: f=ID;");
		StringBuilder expected = new StringBuilder();
		expected.append("Rule returns Rule:\n");
		expected.append("  start -> a1=ID, a2=ID\n");
		expected.append("  a1=ID -> f=ID\n");
		expected.append("  f=ID -> b=ID\n");
		expected.append("  b=ID -> stop\n");
		expected.append("  a2=ID -> f=ID");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testRecursion1() throws Exception {
		String actual = getParserRule("R: '(' R ')' | a=ID;");
		StringBuilder expected = new StringBuilder();
		expected.append("R returns R:\n");
		expected.append("  start -> a=ID\n");
		expected.append("  a=ID -> stop");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testRecursion2() throws Exception {
		String actual = getParserRule("M: R; R: '(' R ')' | a=ID;");
		StringBuilder expected = new StringBuilder();
		expected.append("M returns R, R returns R:\n");
		expected.append("  start -> a=ID\n");
		expected.append("  a=ID -> stop");
		assertEquals(expected.toString(), actual);
	}

	@Test
	@Ignore
	public void testMultipleViaRecursion() throws Exception {
		String actual = getParserRule("R: '(' R ')' evil+=ID | a=ID;");
		StringBuilder expected = new StringBuilder();
		expected.append("R returns R:\n");
		expected.append("  start -> a=ID evil\n");
		expected.append("  a=ID -> stop");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testDoubleFragment() throws Exception {
		String actual = getParserRule("R: a=ID B C | B C d=ID; fragment B:b=ID; fragment C:c=ID;");
		StringBuilder expected = new StringBuilder();
		expected.append("R returns R:\n");
		expected.append("  start -> 2:b=ID, a=ID\n");
		expected.append("  a=ID -> 1:b=ID\n");
		expected.append("  1:b=ID -> 1:c=ID\n");
		expected.append("  2:b=ID -> 2:c=ID\n");
		expected.append("  1:c=ID -> stop\n");
		expected.append("  2:c=ID -> d=ID\n");
		expected.append("  d=ID -> stop");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testNestedFragment() throws Exception {
		String actual = getParserRule("R: a=ID B | B d=ID; fragment B:C; fragment C:c=ID;");
		StringBuilder expected = new StringBuilder();
		expected.append("R returns R:\n");
		expected.append("  start -> 2:c=ID, a=ID\n");
		expected.append("  a=ID -> 1:c=ID\n");
		expected.append("  1:c=ID -> stop\n");
		expected.append("  2:c=ID -> d=ID\n");
		expected.append("  d=ID -> stop");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testAlternativeFragment() throws Exception {
		String actual = getParserRule("R: B C; fragment B: b1=ID | b2=ID; fragment C:c=ID*;");
		StringBuilder expected = new StringBuilder();
		expected.append("R returns R:\n");
		expected.append("  start -> b1=ID, b2=ID\n");
		expected.append("  b1=ID -> c=ID, stop\n");
		expected.append("  c=ID -> c=ID, stop\n");
		expected.append("  b2=ID -> c=ID, stop");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testExpression0() throws Exception {
		StringBuilder grammar = new StringBuilder();
		grammar.append("Addition returns Expr: Prim ({Add.left=current} '+' right=Prim)*;\n");
		grammar.append("Prim returns Expr: {Val} name=ID;\n");
		String actual = getParserRule(grammar.toString());
		StringBuilder expected = new StringBuilder();
		expected.append("Addition returns Add, Addition.Add_1_0 returns Add:\n");
		expected.append("  start -> {Add.left=}\n");
		expected.append("  {Add.left=} -> right=Prim\n");
		expected.append("  right=Prim -> stop\n");
		expected.append("Addition returns Val, Addition.Add_1_0 returns Val, Prim returns Val:\n");
		expected.append("  start -> name=ID\n");
		expected.append("  name=ID -> stop");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testExpression1() throws Exception {
		StringBuilder grammar = new StringBuilder();
		grammar.append("Expr returns Expr: Prim | ({Op} op=('+' | '-') rhs=Prim);\n");
		grammar.append("Prim returns Expr: '(' Expr ')' | {NumberLiteral} value=INT;");
		String actual = getParserRule(grammar.toString());
		StringBuilder expected = new StringBuilder();
		expected.append("Expr returns NumberLiteral, Prim returns NumberLiteral:\n");
		expected.append("  start -> value=INT\n");
		expected.append("  value=INT -> stop\n");
		expected.append("Expr returns Op, Prim returns Op:\n");
		expected.append("  start -> op='+', op='-'\n");
		expected.append("  op='+' -> rhs=Prim\n");
		expected.append("  rhs=Prim -> stop\n");
		expected.append("  op='-' -> rhs=Prim");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testActionSameTypeInMultipleRules1() throws Exception {
		StringBuilder grammar = new StringBuilder();
		grammar.append("Expr returns Expr: Abs | ({Op} op=('+' | '-') rhs=Abs);\n");
		grammar.append("Abs returns Expr: Prim | ({Op} op='ABS' rhs=Prim);\n");
		grammar.append("Prim returns Expr: '(' Expr ')' | {NumberLiteral} value=INT;");
		String actual = getParserRule(grammar.toString());
		StringBuilder expected = new StringBuilder();
		expected.append("Expr returns NumberLiteral, Abs returns NumberLiteral, Prim returns NumberLiteral:\n");
		expected.append("  start -> value=INT\n");
		expected.append("  value=INT -> stop\n");
		expected.append("Expr returns Op, Abs returns Op, Prim returns Op:\n");
		expected.append("  start -> op='+', op='-', op='ABS'\n");
		expected.append("  op='+' -> rhs=Abs\n");
		expected.append("  rhs=Abs -> stop\n");
		expected.append("  op='-' -> rhs=Abs\n");
		expected.append("  op='ABS' -> rhs=Prim\n");
		expected.append("  rhs=Prim -> stop");
		assertEquals(expected.toString(), actual);
	}

	@Test
	public void testDoubleAlternative() throws Exception {
		StringBuilder grammar = new StringBuilder();
		grammar.append("A: B | C;\n");
		grammar.append("B: (a1=ID | 'x' a2=ID) (b1+=ID | 'y' b2+=ID)+;\n");
		grammar.append("C: {C};\n");
		String actual = getParserRule(grammar.toString());
		StringBuilder expected = new StringBuilder();
		expected.append("A returns B, B returns B:\n");
		expected.append("  start -> a1=ID, a2=ID\n");
		expected.append("  a1=ID -> b1+=ID, b2+=ID\n");
		expected.append("  b1+=ID -> b1+=ID, b2+=ID, stop\n");
		expected.append("  b2+=ID -> b1+=ID, b2+=ID, stop\n");
		expected.append("  a2=ID -> b1+=ID, b2+=ID\n");
		expected.append("A returns C, C returns C:\n");
		expected.append("  start -> stop");
		assertEquals(expected.toString(), actual);
	}

}
