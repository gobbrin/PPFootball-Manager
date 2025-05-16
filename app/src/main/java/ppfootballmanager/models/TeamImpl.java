package ppfootballmanager.models;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    private ArrayList<IPlayer> players;
    private Map<String, Integer> positionCounts;
    
    /**
     * Constructor for TeamImpl
     * 
     * @param club The club this team belongs to
     */
    public TeamImpl(IClub club) {
        this.club = club;
        this.players = new ArrayList<>();
        this.positionCounts = new HashMap<>();
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
        for (IPlayer player : players) {
            JSONObject playerObject = new JSONObject();
            playerObject.put("name", player.getName());
            playerObject.put("number", player.getNumber());
            playerObject.put("position", player.getPosition().getDescription());
            playersArray.add(playerObject);
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

    @Override
    public void addPlayer(IPlayer player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        
        players.add(player);
        
        // Update position counts
        String position = player.getPosition().getDescription();
        positionCounts.put(position, positionCounts.getOrDefault(position, 0) + 1);
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
        return players.toArray(new IPlayer[0]);
    }

    @Override
    public int getPositionCount(IPlayerPosition position) {
        if (position == null) {
            return 0;
        }
        
        String positionDesc = position.getDescription();
        return positionCounts.getOrDefault(positionDesc, 0);
    }

    @Override
    public int getTeamStrength() {
        if (players.isEmpty()) {
            return 0;
        }
        
        int totalStrength = 0;
        for (IPlayer player : players) {
            // Calculate player strength based on their attributes
            // Assuming PlayerImpl has a getOverallRating method
            if (player instanceof PlayerImpl) {
                totalStrength += ((PlayerImpl) player).getOverallRating();
            }
        }
        
        return players.isEmpty() ? 0 : totalStrength / players.size();
    }

    @Override
    public boolean isValidPositionForFormation(IPlayerPosition position) {
        if (formation == null || position == null) {
            return false;
        }
        
        // Check if the position is valid for the current formation
        // This will depend on your formation implementation
        // For example, if we have a 4-4-2 formation, we need:
        // - 1 goalkeeper (GK)
        // - 4 defenders (CB, LB, RB)
        // - 4 midfielders (CM, LM, RM)
        // - 2 forwards (ST)
        
        String pos = position.getDescription();
        int currentCount = getPositionCount(position);
        
        // This is a simplistic implementation - you'll need to adjust based on
        // how your formation class actually works
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
        if (player == null || !players.contains(player)) {
            return false;
        }
        
        // Update position counts
        String position = player.getPosition().getDescription();
        int count = positionCounts.getOrDefault(position, 0);
        if (count > 1) {
            positionCounts.put(position, count - 1);
        } else {
            positionCounts.remove(position);
        }
        
        return players.remove(player);
    }
    
    /**
     * Gets a player by their jersey number
     * 
     * @param number The jersey number to search for
     * @return The player with the specified number, or null if not found
     */
    public IPlayer getPlayerByNumber(int number) {
        for (IPlayer player : players) {
            if (player.getNumber() == number) {
                return player;
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
        
        ArrayList<IPlayer> positionPlayers = new ArrayList<>();
        for (IPlayer player : players) {
            if (player.getPosition().getDescription().equals(position.getDescription())) {
                positionPlayers.add(player);
            }
        }
        
        return positionPlayers.toArray(new IPlayer[0]);
    }
}