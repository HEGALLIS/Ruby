package org.ruby.client.phone;

public class Call {
    /* Enums */

    public enum State {
        IDLE, ACTIVE, HOLDING, DIALING, ALERTING, INCOMING, WAITING, DISCONNECTED;

        public boolean isAlive() {
            return !(this == IDLE || this == DISCONNECTED);
        }

        public boolean isRinging() {
            return this == INCOMING || this == WAITING;
        }

        public boolean isDialing() {
            return this == DIALING || this == ALERTING;
        }
    }
    State mState = State.IDLE;
    Connection earliest;
    public long base;

    /* Instance Methods */

    /** Do not modify the List result!!! This list is not yours to keep
     *  It will change across event loop iterations            top
     */

    public State getState() {
    	return mState;
    }
    public void setState(State state) {
    	mState = state;
    }
    public void setConn(Connection conn) {
    	earliest = conn;
    }
     
    /**
     * hasConnections
     * @return true if the call contains one or more connections
     */
    public boolean hasConnections() {
    	return true;
    }
    
    /**
     * isIdle
     * 
     * FIXME rename
     * @return true if the call contains only disconnected connections (if any)
     */
    public boolean isIdle() {
        return !getState().isAlive();
    }

    /**
     * Returns the Connection associated with this Call that was created
     * first, or null if there are no Connections in this Call
     */
    public Connection
    getEarliestConnection() {
        return earliest;
    }
    
    public boolean
    isDialingOrAlerting() {
        return getState().isDialing();
    }

    public boolean
    isRinging() {
        return getState().isRinging();
    }

}