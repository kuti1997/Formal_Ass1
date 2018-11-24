package il.ac.bgu.cs.fvm.impl;

import il.ac.bgu.cs.fvm.exceptions.*;
import il.ac.bgu.cs.fvm.transitionsystem.Transition;
import il.ac.bgu.cs.fvm.transitionsystem.TransitionSystem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TransitionSystemImplementation<STATE, ACTION, ATOMIC_PROPOSITION> implements TransitionSystem<STATE, ACTION, ATOMIC_PROPOSITION> {
    private String name;
    private Set<STATE> states;
    private Set<STATE> initials;
    private Set<ACTION> actions;
    private Set<Transition<STATE, ACTION>> transFunction;
    private Set<ATOMIC_PROPOSITION> atomicProps;
    private Map<STATE, Set<ATOMIC_PROPOSITION>> tagFunction;

    TransitionSystemImplementation() {
        states = new HashSet<>();
        initials = new HashSet<>();
        actions = new HashSet<>();
        transFunction = new HashSet<>();
        atomicProps = new HashSet<>();
        tagFunction = new HashMap<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void addAction(ACTION anAction) {
        actions.add(anAction);
    }

    @Override
    public void setInitial(STATE aState, boolean isInitial) throws StateNotFoundException {
        if (!states.contains(aState)) {
            throw new StateNotFoundException("State Not Found.");
        }
        if (isInitial) {
            initials.add(aState);
        } else {
            initials.remove(aState);
        }
    }

    @Override
    public void addState(STATE state) {
        states.add(state);
        tagFunction.put(state, new HashSet<>());
    }

    @Override
    public void addTransition(Transition<STATE, ACTION> t) throws FVMException {
        if (!(states.contains(t.getFrom()) && states.contains(t.getTo()) && actions.contains(t.getAction()))) {
            throw new InvalidTransitionException(t);
        }
        transFunction.add(t);
    }

    @Override
    public Set<ACTION> getActions() {
        return actions;
    }

    @Override
    public void addAtomicProposition(ATOMIC_PROPOSITION p) {
        boolean atomicExists = false;
        for (ATOMIC_PROPOSITION atomic : atomicProps) {
            if (atomic.equals(p)) {
                atomicExists = true;
            }
        }
        if (!atomicExists) {
            atomicProps.add(p);
        }
    }

    @Override
    public Set<ATOMIC_PROPOSITION> getAtomicPropositions() {
        return atomicProps;
    }

    @Override
    public void addToLabel(STATE s, ATOMIC_PROPOSITION l) throws FVMException {
        boolean isStateLabeledSame = false;
        if (!hasState(s)) {
            throw new StateNotFoundException("Cant add label to a state that is not in the set.");
        }
        if (!hasAtomic(l)) {
            throw new InvalidLablingPairException(s,l);
        }
        for (ATOMIC_PROPOSITION atomic : tagFunction.get(s)) {
            if (atomic.equals(l)) {
                isStateLabeledSame = true;
            }
        }
        if (!isStateLabeledSame) {
            tagFunction.get(s).add(l);
        }
    }

    @Override
    public Set<ATOMIC_PROPOSITION> getLabel(STATE s) {
        if (!hasState(s)) {
            throw new StateNotFoundException("State doesn't exist.");
        }
        return tagFunction.get(s);
    }

    @Override
    public Set<STATE> getInitialStates() {
        return initials;
    }

    @Override
    public Map<STATE, Set<ATOMIC_PROPOSITION>> getLabelingFunction() {
        return tagFunction;
    }

    @Override
    public Set<STATE> getStates() {
        return states;
    }

    @Override
    public Set<Transition<STATE, ACTION>> getTransitions() {
        return transFunction;
    }

    @Override
    public void removeAction(ACTION action) throws FVMException {
        boolean isUsed = false;
        for (Transition trans : transFunction) {
            if (trans.getAction().equals(action)) {
                isUsed = true;
                break;
            }
        }
        if (!isUsed) {
            actions.remove(action);
        } else {
            throw new DeletionOfAttachedActionException(action, TransitionSystemPart.ACTIONS);
        }
    }

    @Override
    public void removeAtomicProposition(ATOMIC_PROPOSITION p) throws FVMException {
        boolean isUsed = false;
        for (STATE state : states) {
            for (ATOMIC_PROPOSITION atomic : tagFunction.get(state)) {
                if (atomic.equals(p)) {
                    isUsed = true;
                    break;
                }
            }
        }
        if (!isUsed) {
            atomicProps.remove(p);
        } else {
            throw new DeletionOfAttachedAtomicPropositionException(p,TransitionSystemPart.ATOMIC_PROPOSITIONS);
        }
    }

    @Override
    public void removeLabel(STATE s, ATOMIC_PROPOSITION l) {
        if (!hasState(s)) {
            throw new StateNotFoundException("Cant remove a label from a state that is not in the set.");
        }
        if (!hasAtomic(l)) {
            throw new FVMException("Cant remove label that is not in the set.");
        }
        for (ATOMIC_PROPOSITION atomic : tagFunction.get(s)) {
            if (atomic.equals(l)) {
                tagFunction.get(s).remove(l);
                break;
            }
        }

    }

    @Override
    public void removeState(STATE state) throws FVMException {
        for (Transition transition : transFunction) {
            if (transition.getFrom().equals(state) || transition.getTo().equals(state)) {
                throw new DeletionOfAttachedStateException(state,TransitionSystemPart.STATES);
            }
        }
        if (tagFunction.get(state).size() != 0) {
            throw new DeletionOfAttachedStateException(state, TransitionSystemPart.STATES);
        }
        for (STATE initial : initials) {
            if (initial.equals(state)) {
                throw new DeletionOfAttachedStateException(state,TransitionSystemPart.STATES);
            }
        }
        states.remove(state);
        tagFunction.remove(state);
    }

    @Override
    public void removeTransition(Transition<STATE, ACTION> t) {
        transFunction.remove(t);
    }

    private boolean hasState(STATE s) {
        for (STATE state : states) {
            if (state.equals(s)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAtomic(ATOMIC_PROPOSITION l) {
        for (ATOMIC_PROPOSITION atomic : atomicProps) {
            if (atomic.equals(l)) {
                return true;
            }
        }
        return false;
    }
}
