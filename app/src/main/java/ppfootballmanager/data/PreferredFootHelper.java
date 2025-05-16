package ppfootballmanager.data;

import com.ppstudios.footballmanager.api.contracts.player.PreferredFoot;

public class PreferredFootHelper {
    
/**
     * Converts a string representation to a PreferredFoot enum value
     * 
     * @param footString String representation of the foot preference ("right", "left", "both")
     * @return PreferredFoot enum value
     */
    public static PreferredFoot fromString(String footString) {
        if (footString == null) {
            return PreferredFoot.Right;  // Default value
        }
        
        switch (footString.toLowerCase()) {
            case "left":
                return PreferredFoot.Left;
            case "both":
                return PreferredFoot.Both;
            case "right":
            default:
                return PreferredFoot.Right;
        }
    }
    
    /**
     * Returns a string representation of the preferred foot
     * 
     * @param foot PreferredFoot enum value
     * @return String representation of the foot preference
     */
    public static String toString(PreferredFoot foot) {
        switch (foot) {
            case Left:
                return "Left";
            case Both:
                return "Both";
            case Right:
                return "Right";
            default:
                return "Unknown";
        }
    }
}
