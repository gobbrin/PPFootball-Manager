package ppfootballmanager.models;

import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;

import com.ppstudios.footballmanager.api.contracts.event.IGoalEvent;
import com.ppstudios.footballmanager.api.contracts.player.IPlayer;
import com.ppstudios.footballmanager.api.contracts.team.ITeam;

public class GoalEventImpl implements IGoalEvent {
    private int minute;
    private IPlayer player;
    private IPlayer assist;
    private ITeam team;
    private String description;
    private GoalType goalType;
    
    /**
     * Enum representing different types of goals
     */
    public enum GoalType {
        REGULAR("Regular Goal"),
        PENALTY("Penalty"),
        HEADER("Header"),
        FREE_KICK("Free Kick"),
        OWN_GOAL("Own Goal"),
        LONG_RANGE("Long Range");
        
        private final String displayName;
        
        GoalType(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    /**
     * Constructor for GoalEventImpl
     * 
     * @param minute Match minute when the goal was scored
     * @param player Player who scored the goal
     * @param team Team that scored the goal
     */
    public GoalEventImpl(int minute, IPlayer player, ITeam team) {
        this.minute = validateMinute(minute);
        this.player = player;
        this.team = team;
        this.goalType = GoalType.REGULAR;
        generateDescription();
    }
    
    /**
     * Constructor for GoalEventImpl with assist
     * 
     * @param minute Match minute when the goal was scored
     * @param player Player who scored the goal
     * @param assist Player who assisted the goal
     * @param team Team that scored the goal
     */
    public GoalEventImpl(int minute, IPlayer player, IPlayer assist, ITeam team) {
        this(minute, player, team);
        this.assist = assist;
        generateDescription();
    }
    
    /**
     * Constructor for GoalEventImpl with goal type
     * 
     * @param minute Match minute when the goal was scored
     * @param player Player who scored the goal
     * @param team Team that scored the goal
     * @param goalType Type of goal (penalty, header, etc.)
     */
    public GoalEventImpl(int minute, IPlayer player, ITeam team, GoalType goalType) {
        this(minute, player, team);
        this.goalType = goalType;
        generateDescription();
    }
    
    /**
     * Constructor for GoalEventImpl with assist and goal type
     * 
     * @param minute Match minute when the goal was scored
     * @param player Player who scored the goal
     * @param assist Player who assisted the goal
     * @param team Team that scored the goal
     * @param goalType Type of goal (penalty, header, etc.)
     */
    public GoalEventImpl(int minute, IPlayer player, IPlayer assist, ITeam team, GoalType goalType) {
        this(minute, player, team);
        this.assist = assist;
        this.goalType = goalType;
        generateDescription();
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
     * Generates a description of the goal based on its type and participants
     */
    private void generateDescription() {
        StringBuilder sb = new StringBuilder();
        
        String playerName = (player != null) ? player.getName() : "Unknown player";
        String teamName = (team != null && team.getClub() != null) ? team.getClub().getName() : "Unknown team";
        
        sb.append(minute).append("' - GOAL! ");
        
        switch (goalType) {
            case PENALTY:
                sb.append(playerName).append(" scores from the penalty spot for ").append(teamName);
                break;
            case HEADER:
                sb.append(playerName).append(" scores with a header for ").append(teamName);
                break;
            case FREE_KICK:
                sb.append("Brilliant free kick by ").append(playerName).append(" for ").append(teamName);
                break;
            case OWN_GOAL:
                sb.append("Own goal by ").append(playerName).append(". ").append(teamName).append(" score");
                break;
            case LONG_RANGE:
                sb.append("What a strike! ").append(playerName).append(" scores from distance for ").append(teamName);
                break;
            default: // REGULAR
                sb.append(playerName).append(" scores for ").append(teamName);
                break;
        }
        
        if (assist != null && goalType != GoalType.PENALTY && goalType != GoalType.FREE_KICK && goalType != GoalType.OWN_GOAL) {
            sb.append(" (Assisted by ").append(assist.getName()).append(")");
        }
        
        this.description = sb.toString();
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getMinute() {
        return minute;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void exportToJson() throws IOException {
        JSONObject goalJson = new JSONObject();
        goalJson.put("type", "GOAL");
        goalJson.put("goalType", this.goalType.toString());
        goalJson.put("minute", this.minute);
        
        if (player != null) {
            goalJson.put("player", player.getName());
            goalJson.put("playerNumber", player.getNumber());
        }
        
        if (assist != null) {
            goalJson.put("assist", assist.getName());
            goalJson.put("assistNumber", assist.getNumber());
        }
        
        if (team != null && team.getClub() != null) {
            goalJson.put("team", team.getClub().getName());
            goalJson.put("teamCode", team.getClub().getCode());
        }
        
        goalJson.put("description", this.description);
        
        // Generate a unique filename for the goal
        String fileName = "goal_" + minute + "_" + 
                         ((player != null) ? player.getName().replace(" ", "_") : "unknown") + ".json";
        
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(goalJson.toJSONString());
            file.flush();
        }
    }

    @Override
    public IPlayer getPlayer() {
        return player;
    }
    
    /**
     * Gets the team that scored the goal
     * 
     * @return The team that scored
     */
    public ITeam getTeam() {
        return team;
    }
    
    /**
     * Gets the player who assisted the goal
     * 
     * @return The assisting player, or null if no assist
     */
    public IPlayer getAssist() {
        return assist;
    }
    
    /**
     * Gets the type of goal
     * 
     * @return The goal type
     */
    public GoalType getGoalType() {
        return goalType;
    }
    
    /**
     * Sets a custom description for the goal
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