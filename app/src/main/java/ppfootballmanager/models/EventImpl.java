package ppfootballmanager.models;

import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;

import com.ppstudios.footballmanager.api.contracts.event.IEvent;
import com.ppstudios.footballmanager.api.contracts.player.IPlayer;
import com.ppstudios.footballmanager.api.contracts.team.ITeam;

public class EventImpl implements IEvent {
    private EventType type;
    private int minute;
    private IPlayer player;
    private IPlayer assistingPlayer;
    private ITeam team;
    private String description;
    
    /**
     * Constructor for EventImpl
     * 
     * @param type Event type
     * @param minute Match minute when event occurred
     * @param player Player involved in the event
     * @param team Team involved in the event
     */
    public EventImpl(EventType type, int minute, IPlayer player, ITeam team) {
        this.type = type;
        this.minute = validateMinute(minute);
        this.player = player;
        this.team = team;
        generateDescription();
    }
    
    /**
     * Constructor for EventImpl with assisting player (for goals)
     * 
     * @param type Event type
     * @param minute Match minute when event occurred
     * @param player Player involved in the event
     * @param assistingPlayer Player who assisted (for goals)
     * @param team Team involved in the event
     */
    public EventImpl(EventType type, int minute, IPlayer player, IPlayer assistingPlayer, ITeam team) {
        this(type, minute, player, team);
        this.assistingPlayer = assistingPlayer;
        generateDescription();
    }
    
    /**
     * Constructor for EventImpl with custom description
     * 
     * @param type Event type
     * @param minute Match minute when event occurred
     * @param player Player involved in the event
     * @param team Team involved in the event
     * @param description Custom description of the event
     */
    public EventImpl(EventType type, int minute, IPlayer player, ITeam team, String description) {
        this(type, minute, player, team);
        if (description != null && !description.isEmpty()) {
            this.description = description;
        }
    }
    
    /**
     * Validates the minute to be within range (0-90, plus stoppage time)
     * 
     * @param minute The minute to validate
     * @return The validated minute value
     */
    private int validateMinute(int minute) {
        return Math.max(0, Math.min(120, minute)); // Allow up to 120 minutes for extra time
    }
    
    /**
     * Generates a description of the event based on its type and participants
     */
    private void generateDescription() {
        StringBuilder sb = new StringBuilder();
        
        String playerName = (player != null) ? player.getName() : "Unknown player";
        String teamName = (team != null && team.getClub() != null) ? team.getClub().getName() : "Unknown team";
        
        sb.append(minute).append("' - ");
        
        switch (type) {
            case GOAL:
                sb.append("GOAL! ").append(playerName).append(" scores for ").append(teamName);
                if (assistingPlayer != null) {
                    sb.append(" (Assisted by ").append(assistingPlayer.getName()).append(")");
                }
                break;
            case YELLOW_CARD:
                sb.append("Yellow card shown to ").append(playerName).append(" of ").append(teamName);
                break;
            case RED_CARD:
                sb.append("RED CARD! ").append(playerName).append(" of ").append(teamName).append(" is sent off");
                break;
            case SUBSTITUTION:
                sb.append("Substitution for ").append(teamName).append(": ").append(playerName).append(" comes on");
                if (assistingPlayer != null) {
                    sb.append(", replacing ").append(assistingPlayer.getName());
                }
                break;
            case PENALTY:
                sb.append("Penalty awarded to ").append(teamName).append(", to be taken by ").append(playerName);
                break;
            case INJURY:
                sb.append(playerName).append(" of ").append(teamName).append(" is injured");
                break;
            default:
                sb.append(playerName).append(" of ").append(teamName);
                break;
        }
        
        this.description = sb.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void exportToJson() throws IOException {
        JSONObject eventJson = new JSONObject();
        eventJson.put("type", this.type.toString());
        eventJson.put("minute", this.minute);
        
        if (player != null) {
            eventJson.put("player", player.getName());
            eventJson.put("playerNumber", player.getNumber());
        }
        
        if (assistingPlayer != null) {
            eventJson.put("assistingPlayer", assistingPlayer.getName());
            eventJson.put("assistingPlayerNumber", assistingPlayer.getNumber());
        }
        
        if (team != null && team.getClub() != null) {
            eventJson.put("team", team.getClub().getName());
            eventJson.put("teamCode", team.getClub().getCode());
        }
        
        eventJson.put("description", this.description);
        
        // Generate a unique filename for the event
        String fileName = "event_" + type.toString() + "_" + minute + "_" + 
                          ((player != null) ? player.getName().replace(" ", "_") : "unknown") + ".json";
        
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(eventJson.toJSONString());
            file.flush();
        }
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getMinute() {
        return minute;
    }

    
    public IPlayer getPlayer() {
        return player;
    }

    
    public ITeam getTeam() {
        return team;
    }

    
    public EventType getType() {
        return type;
    }
    
    /**
     * Gets the player who assisted (for goals)
     * 
     * @return The assisting player, or null if no assist
     */
    public IPlayer getAssistingPlayer() {
        return assistingPlayer;
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
}