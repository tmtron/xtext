/*******************************************************************************
 * Copyright (c) 2016 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.util.formallang;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
public class PdaToNfa<S, I, D> {

	protected class NfaImpl implements Nfa<State> {
		private final State start;
		private final Set<State> states = Sets.newLinkedHashSet();
		private final State stop;

		public NfaImpl(S start, S stop) {
			super();
			this.start = new State(this, start);
			this.stop = new State(this, stop);
		}

		@Override
		public Iterable<State> getFollowers(State state) {
			return state.followers;
		}

		@Override
		public State getStart() {
			return start;
		}

		@Override
		public State getStop() {
			return stop;
		}

		@Override
		public String toString() {
			return new NfaListFormatter<State>().format(this);
		}

	}

	protected class StackItem {
		private final I item;
		private final StackItem parent;
		private final Map<S, State> src2dst = Maps.newHashMap();

		public StackItem(StackItem parent, I item) {
			super();
			this.parent = parent;
			this.item = item;
		}
	}

	protected class State {
		private final Set<State> followers = Sets.newLinkedHashSet();
		private final S origin;
		private final Set<State> predecessors = Sets.newLinkedHashSet();

		public State(NfaImpl nfa, S origin) {
			super();
			nfa.states.add(this);
			this.origin = origin;
		}

		public void addFollower(State follower) {
			followers.add(follower);
			follower.predecessors.add(this);
		}

		public Set<State> getFollowers() {
			return followers;
		}

		@Override
		public String toString() {
			return origin.toString();
		}
	}

	protected <P extends Nfa<D>> P clone(NfaFactory<P, D, ? super S> factory, NfaImpl nfa) {
		Map<State, D> src2dest = Maps.newHashMap();
		P result = factory.create(nfa.start.origin, nfa.stop.origin);
		src2dest.put(nfa.start, result.getStart());
		src2dest.put(nfa.stop, result.getStop());
		for (State state : nfa.states) {
			if (state != nfa.start && state != nfa.stop) {
				D dest = factory.createState(result, state.origin);
				src2dest.put(state, dest);
			}
		}
		for (State state : nfa.states) {
			List<D> followers = Lists.newArrayList();
			for (State f : getFollowers(state)) {
				followers.add(src2dest.get(f));
			}
			factory.setFollowers(result, src2dest.get(state), followers);
		}
		return result;
	}

	protected NfaImpl createNfa(Pda<S, I> pda, Predicate<S> filter) {
		StackItem stack = new StackItem(null, null);
		S sstart = pda.getStart();
		S sstop = pda.getStop();
		NfaImpl nfa = new NfaImpl(sstart, sstop);
		State dstart = nfa.getStart();
		State dstop = nfa.getStop();
		stack.src2dst.put(sstart, dstart);
		stack.src2dst.put(sstop, dstop);
		Iterable<S> followers = pda.getFollowers(sstart);
		for (S follower : followers) {
			process(pda, nfa, filter, follower, stack, dstart, Sets.<S> newHashSet(followers));
		}
		return nfa;
	}

	protected Iterable<State> getFollowers(State state) {
		return state.followers;
	}

	protected void normalize(NfaImpl nfa) {
		Multimap<S, State> groups = LinkedHashMultimap.create();
		for (State s : nfa.states) {
			groups.put(s.origin, s);
		}
		removeKeysWithOnlyOneValue(groups);
		boolean changed = true;
		while (!groups.isEmpty() && changed) {
			changed = false;
			for (Map.Entry<S, Collection<State>> e : Lists.newArrayList(groups.asMap().entrySet())) {
				S key = e.getKey();
				List<State> states = Lists.newArrayList(e.getValue());
				LOOP: for (int i = 0; i < states.size(); i++) {
					State istate = states.get(i);
					for (int j = i + 1; j < states.size(); j++) {
						State jstate = states.get(j);
						if (canMerge(istate, jstate)) {
							merge(nfa, istate, jstate);
							states.remove(j);
							groups.remove(key, jstate);
							changed = true;
							continue LOOP;
						}
					}
				}
				if (states.size() == 1) {
					groups.remove(key, states.get(0));
				}
			}
		}
	}

	protected void merge(NfaImpl nfa, State keep, State remove) {
		remove.followers.remove(remove);
		remove.predecessors.remove(remove);
		for (State p : remove.predecessors) {
			p.followers.remove(remove);
			keep.predecessors.add(p);
			p.followers.add(keep);
		}
		for (State f : remove.followers) {
			f.predecessors.remove(remove);
			keep.followers.add(f);
			f.predecessors.add(keep);
		}
		nfa.states.remove(remove);
	}

	protected boolean canMerge(State istate, State jstate) {
		if (istate.origin != jstate.origin)
			return false;
		if (istate.followers.equals(jstate.followers))
			return true;
		if (istate.predecessors.equals(jstate.predecessors))
			return true;
		if (istate.followers.size() == jstate.followers.size() && istate.followers.contains(istate) && jstate.followers.contains(jstate)) {
			HashSet<State> f1 = Sets.newHashSet(istate.followers);
			f1.remove(istate);
			HashSet<State> f2 = Sets.newHashSet(jstate.followers);
			f2.remove(jstate);
			return f1.equals(f2);
		}
		return false;
	}

	protected void removeKeysWithOnlyOneValue(Multimap<S, State> groups) {
		Iterator<Entry<S, Collection<State>>> it = groups.asMap().entrySet().iterator();
		while (it.hasNext()) {
			Entry<S, Collection<State>> next = it.next();
			if (next.getValue().size() <= 1) {
				it.remove();
			}
		}
	}

	public <P extends Nfa<D>> P pdaToNfa(Pda<S, I> pda, Predicate<S> filter, NfaFactory<P, D, ? super S> factory) {
		NfaImpl nfa = createNfa(pda, filter);
		normalize(nfa);
		P result = clone(factory, nfa);
		return result;
	}

	protected void process(Pda<S, I> pda, NfaImpl nfa, Predicate<S> filter, S state, StackItem stack, State receiver, Set<S> visiting) {
		boolean filtered = false;
		I push = pda.getPush(state);
		StackItem newStack = stack;
		if (push != null) {
			newStack = new StackItem(stack, push);
			filtered = true;
		} else {
			I pop = pda.getPop(state);
			if (pop != null) {
				if (stack.item != pop) {
					return;
				}
				newStack = stack.parent;
				if (newStack == null) {
					throw new IllegalStateException("Cant pop " + state + ": " + "stack empty");
				}
				filtered = true;
			}
		}
		filtered |= !filter.apply(state);
		Iterable<S> followers = pda.getFollowers(state);
		if (filtered) {
			HashSet<S> newVisiting = Sets.<S> newHashSet(Iterables.concat(followers, visiting));
			for (S follower : followers) {
				if (!visiting.contains(follower)) {
					process(pda, nfa, filter, follower, newStack, receiver, newVisiting);
				}
			}
		} else {
			State dest = newStack.src2dst.get(state);
			if (dest == null) {
				dest = new State(nfa, state);
				newStack.src2dst.put(state, dest);
				for (S follower : followers) {
					process(pda, nfa, filter, follower, newStack, dest, Sets.<S> newHashSet(followers));
				}
			}
			receiver.addFollower(dest);
		}
	}

}
