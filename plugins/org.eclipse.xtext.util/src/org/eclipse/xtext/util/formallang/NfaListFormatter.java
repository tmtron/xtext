/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.util.formallang;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
public class NfaListFormatter<STATE> implements Function<Nfa<STATE>, String> {

	protected static class ObjToStrFunction<OBJ> implements Function<OBJ, String> {
		@Override
		public String apply(OBJ from) {
			return from == null ? "null" : from.toString();
		}
	}

	protected boolean sortFollowers = false;

	protected Function<? super STATE, String> stateFormatter = new ObjToStrFunction<STATE>();

	@Override
	public String apply(Nfa<STATE> nfa) {
		return format(nfa);
	}

	public String format(Nfa<STATE> nfa) {
		Map<STATE, String> states = new NfaUtil().uniqueNames(nfa, stateFormatter);
		List<String> result = Lists.newArrayList();
		for (STATE s : states.keySet()) {
			String format = format(nfa, s, states);
			if (format != null) {
				result.add(format);
			}
		}
		return Joiner.on('\n').join(result);
	}

	public String format(Nfa<STATE> pda, STATE state, Map<STATE, String> titles) {
		Iterable<STATE> followers2 = pda.getFollowers(state);
		if (!followers2.iterator().hasNext())
			return null;
		List<String> followers = Lists.newArrayList();
		for (STATE f : followers2)
			followers.add(titles.get(f));
		if (sortFollowers)
			Collections.sort(followers);
		return titles.get(state) + " -> " + Joiner.on(", ").join(followers);
	}

	public Function<? super STATE, String> getStateFormatter() {
		return stateFormatter;
	}

	public NfaListFormatter<STATE> setStateFormatter(Function<? super STATE, String> stateFormatter) {
		this.stateFormatter = stateFormatter;
		return this;
	}

	public NfaListFormatter<STATE> sortFollowers() {
		this.sortFollowers = true;
		return this;
	}

	protected String title(Nfa<STATE> pda, STATE state) {
		return stateFormatter.apply(state);
	}

}
