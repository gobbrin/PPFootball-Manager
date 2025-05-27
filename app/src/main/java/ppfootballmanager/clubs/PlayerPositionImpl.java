package ppfootballmanager.clubs;

import com.ppstudios.footballmanager.api.contracts.player.IPlayerPosition;

/**
 * Implementation of IPlayerPosition that handles player positions
 * with specific codes and full descriptions
 */
public class PlayerPositionImpl implements IPlayerPosition {
    private final String code;
    private final String description;
    private final PositionArea area;
    
    /**
     * Enumeration of position areas on the pitch
     */
    public enum PositionArea {
        GOALKEEPER,
        DEFENSE,
        MIDFIELD,
        ATTACK
    }
    
    /**
     * Constructor for PlayerPositionImpl
     * 
     * @param code Short code for the position (e.g., "GK", "CB", "ST")
     * @param description Full description of the position
     * @param area Area of the pitch this position is in
     */
    public PlayerPositionImpl(String code, String description, PositionArea area) {
        this.code = code;
        this.description = description;
        this.area = area;
    }
    
    /**
     * Simplified constructor using default descriptions based on position code
     * 
     * @param code Short code for the position (e.g., "GK", "CB", "ST")
     */
    public PlayerPositionImpl(String code) {
        this.code = code;
        
        // Set description and area based on code
        switch (code) {
            // Goalkeepers
            case "GK":
                this.description = "Goalkeeper";
                this.area = PositionArea.GOALKEEPER;
                break;
                
            // Defenders
            case "CB":
                this.description = "Center Back";
                this.area = PositionArea.DEFENSE;
                break;
            case "RB":
                this.description = "Right Back";
                this.area = PositionArea.DEFENSE;
                break;
            case "LB":
                this.description = "Left Back";
                this.area = PositionArea.DEFENSE;
                break;
            case "WB":
                this.description = "Wing Back";
                this.area = PositionArea.DEFENSE;
                break;
                
            // Midfielders
            case "CDM":
                this.description = "Defensive Midfielder";
                this.area = PositionArea.MIDFIELD;
                break;
            case "CM":
                this.description = "Central Midfielder";
                this.area = PositionArea.MIDFIELD;
                break;
            case "CAM":
                this.description = "Attacking Midfielder";
                this.area = PositionArea.MIDFIELD;
                break;
            case "RM":
                this.description = "Right Midfielder";
                this.area = PositionArea.MIDFIELD;
                break;
            case "LM":
                this.description = "Left Midfielder";
                this.area = PositionArea.MIDFIELD;
                break;
                
            // Forwards
            case "RW":
                this.description = "Right Winger";
                this.area = PositionArea.ATTACK;
                break;
            case "LW":
                this.description = "Left Winger";
                this.area = PositionArea.ATTACK;
                break;
            case "CF":
                this.description = "Center Forward";
                this.area = PositionArea.ATTACK;
                break;
            case "ST":
                this.description = "Striker";
                this.area = PositionArea.ATTACK;
                break;
                
            // Default case
            default:
                this.description = code; // Use code as description if not recognized
                this.area = PositionArea.MIDFIELD; // Default to midfield for unknown positions
        }
    }
    
    /**
     * Get the shorthand code for this position
     * 
     * @return The position code (e.g., "GK", "CB", "ST")
     */
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }
    
    /**
     * Get the general area of the pitch for this position
     * 
     * @return The position area (GOALKEEPER, DEFENSE, MIDFIELD, ATTACK)
     */
    public PositionArea getArea() {
        return area;
    }
    
    /**
     * Check if this position is compatible with another position
     * Generally, positions in the same area are more compatible
     * 
     * @param otherPosition The position to check compatibility with
     * @return A compatibility score (0-100) where higher is more compatible
     */
    public int getCompatibility(PlayerPositionImpl otherPosition) {
        // Same position is 100% compatible
        if (this.code.equals(otherPosition.code)) {
            return 100;
        }
        
        // Same area is 70% compatible
        if (this.area == otherPosition.area) {
            return 70;
        }
        
        // Adjacent areas are 40% compatible
        if (isAdjacentArea(this.area, otherPosition.area)) {
            return 40;
        }
        
        // Otherwise, low compatibility
        return 20;
    }
    
    /**
     * Check if two areas are adjacent on the pitch
     */
    private boolean isAdjacentArea(PositionArea area1, PositionArea area2) {
        if (area1 == PositionArea.GOALKEEPER && area2 == PositionArea.DEFENSE) return true;
        if (area1 == PositionArea.DEFENSE && area2 == PositionArea.GOALKEEPER) return true;
        
        if (area1 == PositionArea.DEFENSE && area2 == PositionArea.MIDFIELD) return true;
        if (area1 == PositionArea.MIDFIELD && area2 == PositionArea.DEFENSE) return true;
        
        if (area1 == PositionArea.MIDFIELD && area2 == PositionArea.ATTACK) return true;
        if (area1 == PositionArea.ATTACK && area2 == PositionArea.MIDFIELD) return true;
        
        return false;
    }
    
    /**
     * Factory method to create a position from a string code
     * 
     * @param code The position code
     * @return A new PlayerPositionImpl object
     */
    public static PlayerPositionImpl fromCode(String code) {
        return new PlayerPositionImpl(code);
    }
    
    @Override
    public String toString() {
        return description + " (" + code + ")";
    }
    
    /**
     * Get all valid position codes
     * 
     * @return Array of valid position codes
     */
    public static String[] getAllPositionCodes() {
        return new String[] {
            "GK",  // Goalkeeper
            "CB", "RB", "LB", "WB",  // Defenders
            "CDM", "CM", "CAM", "RM", "LM",  // Midfielders
            "RW", "LW", "CF", "ST"  // Forwards
        };
    }
    
    /**
     * Check if a position code is valid
     * 
     * @param code The position code to check
     * @return True if the code is valid
     */
    public static boolean isValidPositionCode(String code) {
        String[] validCodes = getAllPositionCodes();
        for (String validCode : validCodes) {
            if (validCode.equals(code)) {
                return true;
            }
        }
        return false;
    }
}