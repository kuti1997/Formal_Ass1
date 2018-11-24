package il.ac.bgu.cs.fvm.impl;

import il.ac.bgu.cs.fvm.FvmFacade;
import il.ac.bgu.cs.fvm.automata.Automaton;
import il.ac.bgu.cs.fvm.automata.MultiColorAutomaton;
import il.ac.bgu.cs.fvm.channelsystem.ChannelSystem;
import il.ac.bgu.cs.fvm.circuits.Circuit;
import il.ac.bgu.cs.fvm.exceptions.ActionNotFoundException;
import il.ac.bgu.cs.fvm.exceptions.StateNotFoundException;
import il.ac.bgu.cs.fvm.ltl.LTL;
import il.ac.bgu.cs.fvm.programgraph.ActionDef;
import il.ac.bgu.cs.fvm.programgraph.ConditionDef;
import il.ac.bgu.cs.fvm.programgraph.ProgramGraph;
import il.ac.bgu.cs.fvm.transitionsystem.AlternatingSequence;
import il.ac.bgu.cs.fvm.transitionsystem.Transition;
import il.ac.bgu.cs.fvm.transitionsystem.TransitionSystem;
import il.ac.bgu.cs.fvm.util.Pair;
import il.ac.bgu.cs.fvm.verification.VerificationResult;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implement the methods in this class. You may add additional classes as you
 * want, as long as they live in the {@code impl} package, or one of its
 * sub-packages.
 */
public class FvmFacadeImpl implements FvmFacade {

    @Override
    public <S, A, P> TransitionSystem<S, A, P> createTransitionSystem() {
        return new TransitionSystemImplementation<>();
    }

    @Override
    public <S, A, P> boolean isActionDeterministic(TransitionSystem<S, A, P> ts) {
        Set<Transition<S, A>> trans = ts.getTransitions();
        for (Transition<S, A> t : trans) {
            for (Transition<S, A> t2 : trans) {
                if (t.getFrom().equals(t2.getFrom()) && t.getAction().equals(t2.getAction())) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public <S, A, P> boolean isAPDeterministic(TransitionSystem<S, A, P> ts) {
        Set<Transition<S, A>> trans = ts.getTransitions();
        for (Transition<S, A> t : trans) {
            for (Transition<S, A> t2 : trans) {
                if (t.getFrom().equals(t2.getFrom())) {
                    Set<P> atomic = ts.getLabel(t.getTo());
                    Set<P> atomic2 = ts.getLabel(t2.getTo());
                    for (P a : atomic)
                    {
                        if(atomic2.contains(a))
                        {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public <S, A, P> boolean isExecution(TransitionSystem<S, A, P> ts, AlternatingSequence<S, A> e) {
        if(!ts.getInitialStates().contains(e.head()))
        {
            return false;
        }
        return isExecutionFragment(ts, e) && isStateTerminal(ts,e.last());
    }

    @Override
    public <S, A, P> boolean isExecutionFragment(TransitionSystem<S, A, P> ts, AlternatingSequence<S, A> e) {
        AlternatingSequence seq = e;
        while(seq.size() > 1)
        {
            AlternatingSequence tail = seq.tail();
            Set states = ts.getStates();
            if(!states.contains(seq.head()))
            {
                throw new StateNotFoundException(seq.head());
            }
            Set actions = ts.getActions();
            if(!actions.contains(tail.head()))
            {
                throw new ActionNotFoundException(tail.head());
            }
            if(!states.contains(tail.tail().head()))
            {
                throw new StateNotFoundException(tail.tail().head());
            }
            Transition t = new Transition<>(seq.head(),tail.head(),tail.tail().head());
            if(!ts.getTransitions().contains(t))
            {
                return false;
            }
            seq = tail.tail();
        }
        return true;
    }

    @Override
    public <S, A, P> boolean isInitialExecutionFragment(TransitionSystem<S, A, P> ts, AlternatingSequence<S, A> e) {
        if(!ts.getInitialStates().contains(e.head()))
        {
            return false;
        }
        return isExecutionFragment(ts, e);
    }

    @Override
    public <S, A, P> boolean isMaximalExecutionFragment(TransitionSystem<S, A, P> ts, AlternatingSequence<S, A> e) {
        return isExecutionFragment(ts, e) && isStateTerminal(ts,e.last());
    }

    @Override
    public <S, A> boolean isStateTerminal(TransitionSystem<S, A, ?> ts, S s) {
        if(!ts.getStates().contains(s))
        {
            throw new StateNotFoundException(s);
        }
        Set<? extends Transition<S, ?>> transitions = ts.getTransitions();
        for(Transition t : transitions)
        {
            if(t.getFrom().equals(s))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public <S> Set<S> post(TransitionSystem<S, ?, ?> ts, S s) {
        if(!ts.getStates().contains(s))
        {
            throw new StateNotFoundException(s);
        }
        Set<S> toReturn = new HashSet<>();
        Set<? extends Transition<S, ?>> transitions = ts.getTransitions();
        for(Transition<S, ?> t : transitions)
        {
            if(t.getFrom().equals(s))
            {
                toReturn.add(t.getTo());
            }
        }
        return toReturn;
    }

    @Override
    public <S> Set<S> post(TransitionSystem<S, ?, ?> ts, Set<S> c) {
        Set<S> toReturn = new HashSet<>();
        Set<? extends Transition<S, ?>> transitions = ts.getTransitions();
        for(S state : c)
        {
            if(!ts.getStates().contains(state))
            {
                throw new StateNotFoundException(state);
            }
        }
        for(Transition<S, ?> t : transitions)
        {
            if(c.contains(t.getFrom()))
            {
                toReturn.add(t.getTo());
            }
        }
        return toReturn;
    }

    @Override
    public <S, A> Set<S> post(TransitionSystem<S, A, ?> ts, S s, A a) {
        if(!ts.getStates().contains(s))
        {
            throw new StateNotFoundException(s);
        }
        Set<S> toReturn = new HashSet<>();
        Set<? extends Transition<S, ?>> transitions = ts.getTransitions();
        for(Transition<S, ?> t : transitions)
        {
            if(t.getFrom().equals(s) && t.getAction().equals(a))
            {
                toReturn.add(t.getTo());
            }
        }
        return toReturn;
    }

    @Override
    public <S, A> Set<S> post(TransitionSystem<S, A, ?> ts, Set<S> c, A a) {
        Set<S> toReturn = new HashSet<>();
        Set<? extends Transition<S, ?>> transitions = ts.getTransitions();
        for(S state : c)
        {
            if(!ts.getStates().contains(state))
            {
                throw new StateNotFoundException(state);
            }
        }
        for(Transition<S, ?> t : transitions)
        {
            if(c.contains(t.getFrom()) && t.getAction().equals(a))
            {
                toReturn.add(t.getTo());
            }
        }
        return toReturn;
    }

    @Override
    public <S> Set<S> pre(TransitionSystem<S, ?, ?> ts, S s) {
        if(!ts.getStates().contains(s))
        {
            throw new StateNotFoundException(s);
        }
        Set<S> toReturn = new HashSet<>();
        Set<? extends Transition<S, ?>> transitions = ts.getTransitions();
        for(Transition<S, ?> t : transitions)
        {
            if(t.getTo().equals(s))
            {
                toReturn.add(t.getFrom());
            }
        }
        return toReturn;
    }

    @Override
    public <S> Set<S> pre(TransitionSystem<S, ?, ?> ts, Set<S> c) {
        Set<S> toReturn = new HashSet<>();
        Set<? extends Transition<S, ?>> transitions = ts.getTransitions();
        for(S state : c)
        {
            if(!ts.getStates().contains(state))
            {
                throw new StateNotFoundException(state);
            }
        }
        for(Transition<S, ?> t : transitions)
        {
            if(c.contains(t.getTo()))
            {
                toReturn.add(t.getFrom());
            }
        }
        return toReturn;
    }

    @Override
    public <S, A> Set<S> pre(TransitionSystem<S, A, ?> ts, S s, A a) {
        if(!ts.getStates().contains(s))
        {
            throw new StateNotFoundException(s);
        }
        Set<S> toReturn = new HashSet<>();
        Set<? extends Transition<S, ?>> transitions = ts.getTransitions();
        for(Transition<S, ?> t : transitions)
        {
            if(t.getTo().equals(s) && t.getAction().equals(a))
            {
                toReturn.add(t.getFrom());
            }
        }
        return toReturn;
    }

    @Override
    public <S, A> Set<S> pre(TransitionSystem<S, A, ?> ts, Set<S> c, A a) {
        Set<S> toReturn = new HashSet<>();
        Set<? extends Transition<S, ?>> transitions = ts.getTransitions();
        for(S state : c)
        {
            if(!ts.getStates().contains(state))
            {
                throw new StateNotFoundException(state);
            }
        }
        for(Transition<S, ?> t : transitions)
        {
            if(c.contains(t.getTo()) && t.getAction().equals(a))
            {
                toReturn.add(t.getFrom());
            }
        }
        return toReturn;
    }

    @Override
    public <S, A> Set<S> reach(TransitionSystem<S, A, ?> ts) {
        Set<S> toReturn = new HashSet<>();
        Set<S> states = ts.getStates();
        for(S state : states)
        {
            if(hasOrIsInitialFather(ts,state))
            {
                toReturn.add(state);
            }
        }
        return toReturn;
    }

    @Override
    public <S1, S2, A, P> TransitionSystem<Pair<S1, S2>, A, P> interleave(TransitionSystem<S1, A, P> ts1, TransitionSystem<S2, A, P> ts2) {
        return interleave(ts1,ts2,new HashSet<>());
    }

    @Override
    public <S1, S2, A, P> TransitionSystem<Pair<S1, S2>, A, P> interleave(TransitionSystem<S1, A, P> ts1, TransitionSystem<S2, A, P> ts2, Set<A> handShakingActions) {
        TransitionSystem<Pair<S1, S2>, A, P> toReturn = new TransitionSystemImplementation<>();
        Set<A> firstActions = ts1.getActions();
        for(A a : firstActions)
        {
            toReturn.addAction(a);
        }
        Set<A> secondActions = ts2.getActions();
        for(A a : secondActions)
        {
            toReturn.addAction(a);
        }
        Set<S1> firstInitialStates = ts1.getInitialStates();
        Set<S2> secondInitialStates = ts2.getInitialStates();
        Set<Transition<S1, A>> transitions1 = ts1.getTransitions();
        Set<Transition<S2, A>> transitions2 = ts2.getTransitions();
        Set<Pair<S1,S2>> previous = new HashSet<>();
        for(S1 state1 : firstInitialStates)
        {
            for(S2 state2 : secondInitialStates)
            {
                Pair<S1,S2> newState = new Pair<>(state1,state2);
                Set<Transition<Pair<S1,S2>,A>> got = iAmTryingMF(ts1,ts2,newState,previous,handShakingActions);
                for(Transition<Pair<S1,S2>,A> t : got)
                {
                    previous.add(t.getFrom());
                    previous.add(t.getTo());
                    if(!toReturn.getStates().contains(t.getFrom()))
                    {
                        toReturn.addState(t.getFrom());
                        if(firstInitialStates.contains(t.getFrom().first) && secondInitialStates.contains(t.getFrom().second))
                        {
                            toReturn.setInitial(t.getFrom(),true);
                        }
                    }
                    if(!toReturn.getStates().contains(t.getTo()))
                    {
                        toReturn.addState(t.getTo());
                        if(firstInitialStates.contains(t.getTo().first) && secondInitialStates.contains(t.getTo().second))
                        {
                            toReturn.setInitial(t.getTo(),true);
                        }
                    }
                    toReturn.addTransition(t);
                }
            }
        }
        Set<P> atomics1 = ts1.getAtomicPropositions();
        for(P p : atomics1)
        {
            toReturn.addAtomicProposition(p);
        }
        atomics1 = ts2.getAtomicPropositions();
        for(P p : atomics1)
        {
            toReturn.addAtomicProposition(p);
        }
        for(Pair<S1,S2> pair : toReturn.getStates())
        {
            for(P p : ts1.getLabelingFunction().get(pair.first))
            {
                toReturn.addToLabel(pair,p);
            }
            for(P p : ts2.getLabelingFunction().get(pair.second))
            {
                toReturn.addToLabel(pair,p);
            }
        }
        return toReturn;
    }

    @Override
    public <L, A> ProgramGraph<L, A> createProgramGraph() {
        return new ProgramGraphImplementation<>();
    }

    @Override
    public <L1, L2, A> ProgramGraph<Pair<L1, L2>, A> interleave(ProgramGraph<L1, A> pg1, ProgramGraph<L2, A> pg2) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO: Implement interleave
    }

    @Override
    public TransitionSystem<Pair<Map<String, Boolean>, Map<String, Boolean>>, Map<String, Boolean>, Object> transitionSystemFromCircuit(Circuit c) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO: Implement transitionSystemFromCircuit
    }

    @Override
    public <L, A> TransitionSystem<Pair<L, Map<String, Object>>, A, String> transitionSystemFromProgramGraph(ProgramGraph<L, A> pg, Set<ActionDef> actionDefs, Set<ConditionDef> conditionDefs) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO: Implement transitionSystemFromProgramGraph
    }

    @Override
    public <L, A> TransitionSystem<Pair<List<L>, Map<String, Object>>, A, String> transitionSystemFromChannelSystem(ChannelSystem<L, A> cs) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO: Implement transitionSystemFromChannelSystem
    }

    @Override
    public <Sts, Saut, A, P> TransitionSystem<Pair<Sts, Saut>, A, Saut> product(TransitionSystem<Sts, A, P> ts, Automaton<Saut, P> aut) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO: Implement product
    }

    @Override
    public ProgramGraph<String, String> programGraphFromNanoPromela(String filename) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); // TODO: Implement programGraphFromNanoPromela
    }


    /////////////////////////////
    ////////////////////////////
    /////////////////////////////
    ////////////////////////
    @Override
    public ProgramGraph<String, String> programGraphFromNanoPromelaString(String nanopromela) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); // TODO: Implement programGraphFromNanoPromelaString
    }

    @Override
    public ProgramGraph<String, String> programGraphFromNanoPromela(InputStream inputStream) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); // TODO: Implement programGraphFromNanoPromela
    }

    @Override
    public <S, A, P, Saut> VerificationResult<S> verifyAnOmegaRegularProperty(TransitionSystem<S, A, P> ts, Automaton<Saut, P> aut) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO: Implement verifyAnOmegaRegularProperty
    }

    @Override
    public <L> Automaton<?, L> LTL2NBA(LTL<L> ltl) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO: Implement LTL2NBA
    }

    @Override
    public <L> Automaton<?, L> GNBA2NBA(MultiColorAutomaton<?, L> mulAut) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO: Implement GNBA2NBA
    }

    private <S,A> boolean hasOrIsInitialFather(TransitionSystem<S, A, ?> ts, S state)
    {
        if(ts.getInitialStates().contains(state))
        {
            return true;
        }
        Set<S> pres = pre(ts, state);
        if(pres.size() == 0)
        {
            return false;
        }
        for(S state1 : pres)
        {
            if(ts.getInitialStates().contains(state1))
            {
                return true;
            }
            boolean ans = hasOrIsInitialFather(ts, state1);
            if(ans)
            {
                return true;
            }
        }
        return false;
    }

    private <S1,S2,A> Set<Transition<Pair<S1,S2>,A>>  iAmTryingMF(TransitionSystem<S1, A, ?> ts1, TransitionSystem<S2, A, ?> ts2, Pair<S1,S2> state, Set<Pair<S1,S2>> previous, Set<A> actionsGroup)
    {
        Set<Transition<S1,A>> firstTransitions = ts1.getTransitions();
        Set<Transition<S2,A>> secondTransitions = ts2.getTransitions();
        Set<Transition<Pair<S1,S2>,A>> toReturn = new HashSet<>();
        if(previous.contains(state))
        {
            return new HashSet<>();
        }
        previous.add(state);
        for(Transition<S1,A> t : firstTransitions)
        {
            if(t.getFrom().equals(state.first))
            {
                if(actionsGroup.contains(t.getAction()))
                {
                    for(Transition<S2,A> t2 : secondTransitions)
                    {
                        if(t2.getFrom().equals(state.second)) {
                            if (t2.getAction().equals(t.getAction())) {
                                Pair<S1, S2> toAdd = new Pair<>(t.getTo(), t2.getTo());
                                toReturn.add(new Transition<>(state, t.getAction(), toAdd));
                                toReturn.addAll(iAmTryingMF(ts1, ts2, toAdd, previous, actionsGroup));
                            }
                        }
                    }
                }
                else
                {
                    Pair<S1,S2> toAdd = new Pair<>(t.getTo(),state.second);
                    toReturn.add(new Transition<>(state,t.getAction(),toAdd));
                    toReturn.addAll(iAmTryingMF(ts1,ts2,toAdd,previous,actionsGroup));
                }
            }
        }
        for(Transition<S2,A> t : secondTransitions)
        {
            if(t.getFrom().equals(state.second))
            {
                if(!actionsGroup.contains(t.getAction()))
                {
                    Pair<S1,S2> toAdd = new Pair<>(state.first,t.getTo());
                    toReturn.add(new Transition<>(state,t.getAction(),toAdd));
                    toReturn.addAll(iAmTryingMF(ts1,ts2,toAdd,previous,actionsGroup));
                }
            }
        }
        return toReturn;
    }
}
