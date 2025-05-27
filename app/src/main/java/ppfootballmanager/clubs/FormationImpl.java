package ppfootballmanager.clubs;

import com.ppstudios.footballmanager.api.contracts.team.IFormation;

/**
 * Implementation of the IFormation interface representing tactical formations in football
 */
public class FormationImpl implements IFormation {
    
    private String name;
    private int defenders;
    private int midfielders;
    private int forwards;
    
    /**
     * Constructor for FormationImpl
     * 
     * @param name Formation name (e.g. "4-4-2", "4-3-3", etc.)
     */
    public FormationImpl(String name) {
        this.name = name;
        parseFormation(name);
    }
    
    /**
     * Parse the formation string into defenders, midfielders, and forwards
     * 
     * @param formation Formation string (e.g. "4-4-2")
     */
    private void parseFormation(String formation) {
        // Default to 4-4-2 if parsing fails
        defenders = 4;
        midfielders = 4;
        forwards = 2;
        
        if (formation == null || formation.isEmpty()) {
            return;
        }
        
        // Parse formations like "4-4-2", "4-3-3", etc.
        String[] parts = formation.split("-");
        if (parts.length >= 3) {
            try {
                defenders = Integer.parseInt(parts[0]);
                midfielders = Integer.parseInt(parts[1]);
                forwards = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                // If parsing fails, use default values (4-4-2)
                defenders = 4;
                midfielders = 4;
                forwards = 2;
            }
        }
    }

    /**
     * Get the name of this formation
     * 
     * @return Formation name
     */
    public String getName() {
        return name;
    }

    // TODO: Display Name and Name are the same for now
    /**
     * Get the display name of this formation
     * 
     * @return Display name
     */
    @Override
    public String getDisplayName() {
        return name;
    }

    /**
     * Calculate the tactical advantage of this formation against another formation
     * 
     * A formation has an advantage when:
     * 1. It has more midfielders (midfield control)
     * 2. It has the right balance of defenders to counter opponent's forwards
     * 3. It has enough forwards to pressure opponent's defense
     * 
     * @param opponent The opponent's formation
     * @return A number between -2 and 2 indicating tactical advantage:
     *         - Positive: this formation has an advantage
     *         - Zero: formations are evenly matched
     *         - Negative: opponent's formation has an advantage
     */
    @Override
    public int getTacticalAdvantage(IFormation opponent) {
        if (opponent == null) {
            return 0;
        }
        
        // Get opponent's formation details
        int opponentDefenders = 4;
        int opponentMidfielders = 4;
        int opponentForwards = 2;
        
        if (opponent instanceof FormationImpl) {
            FormationImpl opponentFormation = (FormationImpl) opponent;
            opponentDefenders = opponentFormation.defenders;
            opponentMidfielders = opponentFormation.midfielders;
            opponentForwards = opponentFormation.forwards;
        }
        
        int advantage = 0;
        
        // Midfield control
        if (midfielders > opponentMidfielders) {
            advantage += 1;
        } else if (midfielders < opponentMidfielders) {
            advantage -= 1;
        }
        
        // Defense vs opponent forwards
        if (defenders >= opponentForwards + 2) {
            advantage += 1; // Strong defensive advantage
        } else if (defenders < opponentForwards) {
            advantage -= 1; // Defensive weakness
        }
        
        // Attack vs opponent defense
        if (forwards > opponentDefenders - 2) {
            advantage += 1; // Strong attacking advantage
        } else if (forwards < opponentDefenders - 3) {
            advantage -= 1; // Not enough attacking power
        }
        
        // Clamp advantage between -2 and 2
        return Math.max(-2, Math.min(2, advantage));
    }
    
    /**
     * Get the number of defenders in this formation
     * 
     * @return Number of defenders
     */
    public int getDefenders() {
        return defenders;
    }
    
    /**
     * Get the number of midfielders in this formation
     * 
     * @return Number of midfielders
     */
    public int getMidfielders() {
        return midfielders;
    }
    
    /**
     * Get the number of forwards in this formation
     * 
     * @return Number of forwards
     */
    public int getForwards() {
        return forwards;
    }
    
    /**
     * Check if this formation is valid (defenders + midfielders + forwards = 10)
     * 
     * @return true if the formation is valid, false otherwise
     */
    public boolean isValid() {
        // A formation is valid if it has 10 outfield players (excluding goalkeeper)
        return defenders + midfielders + forwards == 10;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * Creates a classic 4-4-2 formation
     * 
     * @return A 4-4-2 formation
     */
    public static FormationImpl createClassic442() {
        return new FormationImpl("4-4-2");
    }
    
    /**
     * Creates an attacking 4-3-3 formation
     * 
     * @return A 4-3-3 formation
     */
    public static FormationImpl createAttacking433() {
        return new FormationImpl("4-3-3");
    }
    
    /**
     * Creates a defensive 5-3-2 formation
     * 
     * @return A 5-3-2 formation
     */
    public static FormationImpl createDefensive532() {
        return new FormationImpl("5-3-2");
    }
    
    /**
     * Creates a balanced 4-2-3-1 formation
     * 
     * @return A 4-2-3-1 formation
     */
    public static FormationImpl createBalanced4231() {
        return new FormationImpl("4-2-3-1");
    }
}