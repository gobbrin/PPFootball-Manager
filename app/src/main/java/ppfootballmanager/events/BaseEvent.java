package ppfootballmanager.events;

import com.ppstudios.footballmanager.api.contracts.player.IPlayer;
import com.ppstudios.footballmanager.api.contracts.team.ITeam;

/**
 * Concrete implementation of EventImpl for basic events that don't require
 * specialized behavior.
 */
public class BaseEvent extends EventImpl {
    private IPlayer player;
    
    /**
     * Constructor for BaseEventImpl
     * 
     * @param type The event type
     * @param minute The minute when the event occurred
     * @param player The player involved in the event
     * @param team The team involved in the event
     */
    public BaseEvent(EventType type, int minute, IPlayer player, ITeam team) {
        super(minute, team, type);
        this.player = player;
        generateDescription();
    }
    
    /**
     * Gets the player involved in this event
     * 
     * @return The player
     */
    public IPlayer getPlayer() {
        return player;
    }
    
    @Override
    protected void generateDescription() {
        StringBuilder sb = new StringBuilder();
        
        String playerName = (player != null) ? player.getName() : "Unknown player";
        String teamName = (team != null && team.getClub() != null) ? team.getClub().getName() : "Unknown team";
        
        sb.append(minute).append("' - ");
        
        switch (type) {
            case YELLOW_CARD:
                sb.append("YELLOW CARD! ").append(playerName).append(" (").append(teamName).append(")");
                break;
                
            case RED_CARD:
                sb.append("RED CARD! ").append(playerName).append(" (").append(teamName).append(") is sent off");
                break;
                
            case SUBSTITUTION:
                sb.append("SUBSTITUTION: ").append(playerName).append(" comes on for ").append(teamName);
                break;
                
            case INJURY:
                sb.append("INJURY: ").append(playerName).append(" (").append(teamName).append(") is injured");
                break;
                
            case CORNER:
                sb.append("CORNER KICK for ").append(teamName).append(", taken by ").append(playerName);
                break;
                
            case FREE_KICK:
                sb.append("FREE KICK for ").append(teamName).append(", taken by ").append(playerName);
                break;
                
            case PENALTY:
                sb.append("PENALTY for ").append(teamName).append(", to be taken by ").append(playerName);
                break;
                
            default:
                sb.append(type.toString()).append(": ").append(playerName).append(" (").append(teamName).append(")");
                break;
        }
        
        this.description = sb.toString();
    }
}