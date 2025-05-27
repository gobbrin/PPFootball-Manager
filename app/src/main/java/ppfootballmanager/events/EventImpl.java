package ppfootballmanager.events;

import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;

import com.ppstudios.footballmanager.api.contracts.event.IEvent;
import com.ppstudios.footballmanager.api.contracts.team.ITeam;

/**
 * Abstract base implementation of IEvent interface
 */
public abstract class EventImpl implements IEvent {
    protected int minute;
    protected String description;
    protected ITeam team;
    protected EventType type;
    
    /**
     * Constructor for EventImpl
     * 
     * @param minute The minute when the event occurred
     * @param team The team involved in the event
     * @param type The type of event
     */
    protected EventImpl(int minute, ITeam team, EventType type) {
        this.minute = validateMinute(minute);
        this.team = team;
        this.type = type;
    }
    
    /**
     * Validates the minute to be within range (0-120 for extra time)
     * 
     * @param minute The minute to validate
     * @return The validated minute value
     */
    protected int validateMinute(int minute) {
        return Math.max(0, Math.min(120, minute));
    }
    
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getMinute() {
        return minute;
    }
    
    /**
     * Gets the type of this event
     * 
     * @return The event type
     */
    public EventType getType() {
        return type;
    }
    
    /**
     * Gets the team involved in this event
     * 
     * @return The team
     */
    public ITeam getTeam() {
        return team;
    }
    
    /**
     * Sets a custom description for the event
     * 
     * @param description The custom description
     */
    public void setDescription(String description) {
        if (description != null && !description.isEmpty()) {
            this.description = description;
        }
    }
    
    @Override
    public String toString() {
        return getDescription();
    }
    
    /**
     * Generate the description of the event
     * Each subclass should implement this to create an appropriate description
     */
    protected abstract void generateDescription();
    
    @Override
    @SuppressWarnings("unchecked")
    public void exportToJson() throws IOException {
        JSONObject eventJson = new JSONObject();
        eventJson.put("type", this.type.toString());
        eventJson.put("minute", this.minute);
        
        if (team != null && team.getClub() != null) {
            eventJson.put("team", team.getClub().getName());
            eventJson.put("teamCode", team.getClub().getCode());
        }
        
        eventJson.put("description", this.description);
        
        // Generate a filename for the event
        String fileName = type.toString().toLowerCase() + "_" + minute + ".json";
        
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(eventJson.toJSONString());
            file.flush();
        }
    }
}