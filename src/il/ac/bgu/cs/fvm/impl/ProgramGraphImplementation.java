package il.ac.bgu.cs.fvm.impl;

import il.ac.bgu.cs.fvm.programgraph.PGTransition;
import il.ac.bgu.cs.fvm.programgraph.ProgramGraph;

import java.util.*;

public class ProgramGraphImplementation<L,A> implements ProgramGraph<L,A> {
    private Set<List<String>> variables ;
    private Set<L> initials;
    private Set<L> locations;
    private Set<PGTransition<L,A>> transitions;
    private String name;

    ProgramGraphImplementation()
    {
        variables = new HashSet<>();
        initials = new HashSet<>();
        locations = new HashSet<>();
        transitions = new HashSet<>();
    }
    @Override
    public void addInitalization(List<String> init) {
        variables.add(init);
    }

    @Override
    public void setInitial(L location, boolean isInitial) {
        if(!hasLocation(location))
        {
            throw new IllegalArgumentException("Location wasn't found");
        }
        if(isInitial)
        {
            initials.add(location);
        }
        else
        {
            initials.remove(location);
        }
    }

    @Override
    public void addLocation(L l) {
        locations.add(l);
    }

    @Override
    public void addTransition(PGTransition<L, A> t) {
        transitions.add(t);
    }

    @Override
    public Set<List<String>> getInitalizations() {
        return variables;
    }

    @Override
    public Set<L> getInitialLocations() {
        return initials;
    }

    @Override
    public Set<L> getLocations() {
        return locations;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<PGTransition<L, A>> getTransitions() {
        return transitions;
    }

    @Override
    public void removeLocation(L l) {
        locations.remove(l);
    }

    @Override
    public void removeTransition(PGTransition<L, A> t) {
        transitions.remove(t);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    private boolean hasLocation(L l) {
        for (L location : locations) {
            if (location.equals(l)) {
                return true;
            }
        }
        return false;
    }
}
