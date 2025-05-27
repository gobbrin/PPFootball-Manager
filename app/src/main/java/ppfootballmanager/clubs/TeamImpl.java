package ppfootballmanager.clubs;

import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.ppstudios.footballmanager.api.contracts.player.IPlayer;
import com.ppstudios.footballmanager.api.contracts.player.IPlayerPosition;
import com.ppstudios.footballmanager.api.contracts.team.IClub;
import com.ppstudios.footballmanager.api.contracts.team.IFormation;
import com.ppstudios.footballmanager.api.contracts.team.ITeam;

public class TeamImpl implements ITeam {
    
    private IClub club;
    private IFormation formation;
    private IPlayer[] players;
    private int playerCount;
    private static final int MAX_PLAYERS = 30; // Reasonable max for a football team
    
    // Simple position count tracker (without HashMap)
    private String[] positionKeys;
    private int[] positionValues;
    private int positionCount;
    private static final int MAX_POSITIONS = 15; // GK, CB, LB, RB, CDM, CM, CAM, RM, LM, RW, LW, CF, ST, etc.
    
    /**
     * Constructor for TeamImpl
     * 
     * @param club The club this team belongs to
     */
    public TeamImpl(IClub club) {
        this.club = club;
        this.players = new IPlayer[MAX_PLAYERS];
        this.playerCount = 0;
        this.positionKeys = new String[MAX_POSITIONS];
        this.positionValues = new int[MAX_POSITIONS];
        this.positionCount = 0;
    }
    
    /**
     * Constructor for TeamImpl with formation
     * 
     * @param club The club this team belongs to
     * @param formation The initial formation for the team
     */
    public TeamImpl(IClub club, IFormation formation) {
        this(club);
        this.formation = formation;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void exportToJson() throws IOException {
        JSONObject teamJson = new JSONObject();
        
        // Add club details
        if (club != null) {
            teamJson.put("club", club.getName());
            teamJson.put("stadium", club.getStadiumName());
        }
        
        // Add formation
        if (formation != null) {
            teamJson.put("formation", formation.getDisplayName());
        }
        
        // Add players
        JSONArray playersArray = new JSONArray();
        for (int i = 0; i < playerCount; i++) {
            if (players[i] != null) {
                JSONObject playerObject = new JSONObject();
                playerObject.put("name", players[i].getName());
                playerObject.put("number", players[i].getNumber());
                playerObject.put("position", players[i].getPosition().getDescription());
                playersArray.add(playerObject);
            }
        }
        teamJson.put("players", playersArray);
        
        // Write to file
        String fileName = (club != null) ? 
                          "team_" + club.getName().replace(" ", "_") + ".json" : 
                          "team_unnamed.json";
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(teamJson.toJSONString());
            file.flush();
        }
    }

    // Helper method for position count tracking (replacement for HashMap)
    private int getPositionCountValue(String key) {
        for (int i = 0; i < positionCount; i++) {
            if (positionKeys[i] != null && positionKeys[i].equals(key)) {
                return positionValues[i];
            }
        }
        return 0;
    }
    
    // Helper method for position count tracking (replacement for HashMap)
    private void updatePositionCount(String key, int value) {
        // Update existing key
        for (int i = 0; i < positionCount; i++) {
            if (positionKeys[i] != null && positionKeys[i].equals(key)) {
                positionValues[i] = value;
                return;
            }
        }
        
        // Add new key if not found
        if (positionCount < MAX_POSITIONS) {
            positionKeys[positionCount] = key;
            positionValues[positionCount] = value;
            positionCount++;
        }
    }
    
    // Helper method for position count tracking (replacement for HashMap)
    private void removePositionCount(String key) {
        for (int i = 0; i < positionCount; i++) {
            if (positionKeys[i] != null && positionKeys[i].equals(key)) {
                // Shift elements to fill the gap
                System.arraycopy(positionKeys, i + 1, positionKeys, i, positionCount - i - 1);
                System.arraycopy(positionValues, i + 1, positionValues, i, positionCount - i - 1);
                positionCount--;
                return;
            }
        }
    }

    @Override
    public void addPlayer(IPlayer player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        
        if (playerCount >= MAX_PLAYERS) {
            throw new IllegalStateException("Team is full, cannot add more players");
        }
        
        // Check if player is already in the team
        for (int i = 0; i < playerCount; i++) {
            if (players[i] != null && players[i].equals(player)) {
                return; // Player already in team
            }
        }
        
        players[playerCount++] = player;
        
        // Update position counts
        String position = player.getPosition().getDescription();
        int currentCount = getPositionCountValue(position);
        updatePositionCount(position, currentCount + 1);
    }

    @Override
    public IClub getClub() {
        return club;
    }

    @Override
    public IFormation getFormation() {
        return formation;
    }

    @Override
    public IPlayer[] getPlayers() {
        // If the team has no players directly, try to get them from the club
        if (playerCount == 0 && club != null) {
            IPlayer[] clubPlayers = club.getPlayers();
            for (IPlayer player : clubPlayers) {
                if (player != null) {
                    addPlayer(player);
                }
            }
        }
        
        // Create a new array with exactly the right size
        IPlayer[] result = new IPlayer[playerCount];
        for (int i = 0; i < playerCount; i++) {
            result[i] = players[i];
        }
        
        return result;
    }

    @Override
    public int getPositionCount(IPlayerPosition position) {
        if (position == null) {
            return 0;
        }
        
        int count = 0;
        for (int i = 0; i < playerCount; i++) {
            if (players[i] != null && players[i].getPosition() != null) {
                // Compare by position code when possible
                if (position instanceof PlayerPositionImpl && players[i].getPosition() instanceof PlayerPositionImpl) {
                    String posCode1 = ((PlayerPositionImpl) position).getCode();
                    String posCode2 = ((PlayerPositionImpl) players[i].getPosition()).getCode();
                    if (posCode1.equals(posCode2)) {
                        count++;
                    }
                }
                // Fallback to description comparison
                else if (position.getDescription().equalsIgnoreCase(players[i].getPosition().getDescription())) {
                    count++;
                }
            }
        }
        
        return count;
    }

    @Override
    public int getTeamStrength() {
        if (playerCount == 0) {
            return 0;
        }
        
        int totalStrength = 0;
        int validPlayers = 0;
        
        for (int i = 0; i < playerCount; i++) {
            if (players[i] != null) {
                // Calculate player strength based on their attributes
                if (players[i] instanceof PlayerImpl) {
                    totalStrength += ((PlayerImpl) players[i]).getOverallRating();
                    validPlayers++;
                }
            }
        }
        
        return validPlayers == 0 ? 0 : totalStrength / validPlayers;
    }

    @Override
    public boolean isValidPositionForFormation(IPlayerPosition position) {
        if (formation == null || position == null) {
            return false;
        }
        
        String pos = position.getDescription();
        int currentCount = getPositionCount(position);
        
        // This is a simplistic implementation - adjust as needed
        switch (formation.getDisplayName()) {
            case "4-4-2":
                if (pos.equals("GK")) return currentCount < 1;
                if (pos.equals("CB") || pos.equals("LB") || pos.equals("RB")) return currentCount < 4;
                if (pos.equals("CM") || pos.equals("LM") || pos.equals("RM")) return currentCount < 4;
                if (pos.equals("ST")) return currentCount < 2;
                break;
            case "4-3-3":
                if (pos.equals("GK")) return currentCount < 1;
                if (pos.equals("CB") || pos.equals("LB") || pos.equals("RB")) return currentCount < 4;
                if (pos.equals("CM") || pos.equals("LM") || pos.equals("RM")) return currentCount < 3;
                if (pos.equals("ST") || pos.equals("LW") || pos.equals("RW")) return currentCount < 3;
                break;
            // Add more formations as needed
        }
        
        return false;
    }

    @Override
    public void setFormation(IFormation formation) {
        this.formation = formation;
    }
    
    /**
     * Removes a player from the team
     * 
     * @param player The player to remove
     * @return true if the player was successfully removed, false otherwise
     */
    public boolean removePlayer(IPlayer player) {
        if (player == null) {
            return false;
        }
        
        for (int i = 0; i < playerCount; i++) {
            if (players[i] != null && players[i].equals(player)) {
                // Update position counts
                String position = player.getPosition().getDescription();
                int count = getPositionCountValue(position);
                if (count > 1) {
                    updatePositionCount(position, count - 1);
                } else {
                    removePositionCount(position);
                }
                
                // Remove player and shift array to fill the gap
                System.arraycopy(players, i + 1, players, i, playerCount - i - 1);
                players[--playerCount] = null;
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets a player by their jersey number
     * 
     * @param number The jersey number to search for
     * @return The player with the specified number, or null if not found
     */
    public IPlayer getPlayerByNumber(int number) {
        for (int i = 0; i < playerCount; i++) {
            if (players[i] != null && players[i].getNumber() == number) {
                return players[i];
            }
        }
        return null;
    }
    
    /**
     * Gets all players of a specific position
     * 
     * @param position The position to filter by
     * @return Array of players with the specified position
     */
    public IPlayer[] getPlayersByPosition(IPlayerPosition position) {
        if (position == null) {
            return new IPlayer[0];
        }
        
        // First count matching players
        int count = 0;
        for (int i = 0; i < playerCount; i++) {
            if (players[i] != null && players[i].getPosition() != null && 
                players[i].getPosition().getDescription().equals(position.getDescription())) {
                count++;
            }
        }
        
        // Create result array
        IPlayer[] result = new IPlayer[count];
        int index = 0;
        
        // Fill result array
        for (int i = 0; i < playerCount; i++) {
            if (players[i] != null && players[i].getPosition() != null && 
                players[i].getPosition().getDescription().equals(position.getDescription())) {
                result[index++] = players[i];
            }
        }
        
        return result;
    }
    
    /**
     * Clears all players from the team (but not from the club)
     */
    public void clearPlayers() {
        for (int i = 0; i < playerCount; i++) {
            players[i] = null;
        }
        playerCount = 0;
        
        // Reset position counts
        for (int i = 0; i < positionCount; i++) {
            positionKeys[i] = null;
            positionValues[i] = 0;
        }
        positionCount = 0;
    }
}