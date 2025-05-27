package ppfootballmanager.events;

/**
 * Enum representing different types of events that can occur during a match
 */
public enum EventType {
    GOAL("Goal"),
    YELLOW_CARD("Yellow Card"),
    RED_CARD("Red Card"),
    SUBSTITUTION("Substitution"),
    PENALTY("Penalty"),
    INJURY("Injury"),
    CORNER("Corner"),
    FREE_KICK("Free Kick"),
    OFFSIDE("Offside"),
    SAVE("Save"),
    SHOT("Shot"),
    POSSESSION_CHANGE("Possession Change"),
    HALF_TIME("Half Time"),
    FULL_TIME("Full Time");
    
    private final String displayName;
    
    EventType(String displayName) {
        this.displayName = displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}