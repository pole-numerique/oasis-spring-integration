package org.oasis_eu.spring.kernel.security;

/**
 * User: schambon
 * Date: 8/4/14
 */
public class State {

    StateType type;
    String random;

    public StateType getType() {
        return type;
    }

    public void setType(StateType type) {
        this.type = type;
    }

    public String getRandom() {
        return random;
    }

    public void setRandom(String random) {
        this.random = random;
    }
}
