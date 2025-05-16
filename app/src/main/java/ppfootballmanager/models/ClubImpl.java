package ppfootballmanager.models;
// please make the javadoc comments for the methods in this class more descriptive

import java.io.FileWriter;      // Write data to a file
import java.io.IOException;     //  Handles exceptions

import org.json.simple.JSONArray;   // JSON array is a collection of JSON objects
import org.json.simple.JSONObject;  // JSON object is a collection of key-value pairs

import com.ppstudios.footballmanager.api.contracts.player.IPlayer;
import com.ppstudios.footballmanager.api.contracts.player.IPlayerPosition;
import com.ppstudios.footballmanager.api.contracts.team.IClub;
import com.ppstudios.footballmanager.api.contracts.team.IPlayerSelector;

public class ClubImpl implements IClub {

    private String name;
    private String code;
    private String country;
    private int foundedYear;
    private String logo;
    private String stadiumName;
    private IPlayer[] players;
    private int playerCount;
    private static final int MAX_PLAYERS = 50; // Maximum size for player array

    /**
     * Constructor for ClubImpl
     * 
     * @param name Club name
     * @param code Club code/abbreviation
     * @param country Country where the club is based
     * @param foundedYear Year the club was founded
     * @param stadiumName Name of the club's stadium
     * @param logo URL to the club's logo
     */
    public ClubImpl(String name, String code, String country, int foundedYear, String stadiumName, String logo) {
        this.name = name;
        this.code = code;
        this.country = country;
        this.foundedYear = foundedYear;
        this.stadiumName = stadiumName;
        this.logo = logo;
        this.players = new IPlayer[MAX_PLAYERS];
        this.playerCount = 0;
    }
    
    /**
     * Constructor for ClubImpl with default logo
     * 
     * @param name Club name
     * @param code Club code/abbreviation
     * @param country Country where the club is based
     * @param foundedYear Year the club was founded
     * @param stadiumName Name of the club's stadium
     */
    public ClubImpl(String name, String code, String country, int foundedYear, String stadiumName) {
        this(name, code, country, foundedYear, stadiumName, "");
    }

    
    @Override
    @SuppressWarnings("unchecked")
    public void exportToJson() throws IOException {
        JSONObject clubJson = new JSONObject();
        
        // Add club details
        clubJson.put("name", this.name);
        clubJson.put("code", this.code);
        clubJson.put("country", this.country);
        clubJson.put("founded", this.foundedYear);
        clubJson.put("isNationalTeam", false);
        clubJson.put("stadium", this.stadiumName);
        clubJson.put("logo", this.logo);
        
        // Add players array
        JSONArray playersArray = new JSONArray();
        for (int i = 0; i < playerCount; i++) {
            JSONObject playerObject = new JSONObject();
            playerObject.put("name", players[i].getName());
            playerObject.put("number", players[i].getNumber());
            playerObject.put("position", players[i].getPosition().getDescription());
            playersArray.add(playerObject);
        }
        clubJson.put("squad", playersArray);
        
        // Write to file
        String fileName = "club_" + this.name.replace(" ", "_") + ".json";
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(clubJson.toJSONString());
            file.flush();
        }
    }
     
    /**
     * Adds a player to the club
     * 
     * @param player The player to add
     * @throws IllegalArgumentException if the player is null
     * @throws IllegalStateException if the maximum number of players is reached
     */
    @Override
    public void addPlayer(IPlayer player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        
        if (playerCount >= MAX_PLAYERS) {
            throw new IllegalStateException("Maximum number of players reached");
        }
        
        // Check if player already exists in the club
        for (int i = 0; i < playerCount; i++) {
            if (players[i].equals(player)) {
                return; // Player already exists, don't add duplicate
            }
        }
        
        players[playerCount++] = player;
    }

    /**
     * Gets the name of the club
     * 
     * @return Club name
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * Gets the country of the club
     * 
     * @return Country where the club is based
     */
    @Override
    public String getCountry() {
        return country;
    }

    /**
     * Gets the year the club was founded
     * 
     * @return Year the club was founded
     */
    @Override
    public int getFoundedYear() {
        return foundedYear;
    }

    /**
     * Gets the logo URL of the club
     * 
     * @return URL to the club's logo
     */
    @Override
    public String getLogo() {
        return logo;
    }

    /**
     * Gets the name of the club
     * 
     * @return Club name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Gets the number of players in the club
     * 
     * @return Number of players in the club
     */
    @Override
    public int getPlayerCount() {
        return playerCount;
    }

    /**
     * Gets the players in the club
     * 
     * @return Array of players in the club
     */
    @Override
    public IPlayer[] getPlayers() {
        IPlayer[] result = new IPlayer[playerCount];
        System.arraycopy(players, 0, result, 0, playerCount);
        return result;
    }

    /**
     * Gets the name of the club's stadium
     * 
     * @return Name of the club's stadium
     */
    @Override
    public String getStadiumName() {
        return stadiumName;
    }

    /**
     * Checks if a player is in the club
     * 
     * @param player The player to check
     * @return true if the player is in the club, false otherwise  
     */
    @Override
    public boolean isPlayer(IPlayer player) {
        if (player == null) {
            return false;
        }
        
        for (int i = 0; i < playerCount; i++) {
            if (players[i].equals(player)) {
                return true;
            }
        }
        
        return false;
    }

    // TODO: check whats a valid club, right now its weird
    /**
     * Checks if the club is valid
     * 
     * @return true if the club is valid, false otherwise
     */
    @Override
    public boolean isValid() {
        // A valid club needs at least a name, a code, and a stadium name
        return name != null && !name.isEmpty() && 
               code != null && !code.isEmpty() && 
               stadiumName != null && !stadiumName.isEmpty();
    }

    /**
     * Removes a player from the club
     * 
     * @param playerToRemove The player to remove
     */
    @Override
    public void removePlayer(IPlayer playerToRemove) {
        if (playerToRemove == null) {
            return;
        }
        
        for (int i = 0; i < playerCount; i++) {
            if (players[i].equals(playerToRemove)) {
                // Remove player by shifting all subsequent players
                for (int j = i; j < playerCount - 1; j++) {
                    players[j] = players[j + 1];
                }
                players[playerCount - 1] = null;
                playerCount--;
                return;
            }
        }
    }

    /**
     * Selects a player based on their position or relevance
     * 
     * @param selector The player selector
     * @param position The position to filter by
     * @return The selected player, or null if no player is found
     */
    @Override
    public IPlayer selectPlayer(IPlayerSelector selector, IPlayerPosition position) {
        if (selector == null || position == null || playerCount == 0) {
            return null;
        }
        
        // Create an array of players with the specified position
        int count = 0;
        for (int i = 0; i < playerCount; i++) {
            if (players[i].getPosition().getDescription().equalsIgnoreCase(position.getDescription())) {
                count++;
            }
        }
        
        if (count == 0) {
            return null; // No players with that position
        }
        
        // TODO: Array stores position players but not used here, used in the selector
        /**
        IPlayer[] positionPlayers = new IPlayer[count];
        int index = 0;
        for (int i = 0; i < playerCount && index < count; i++) {
            if (players[i].getPosition().getDescription().equalsIgnoreCase(position.getDescription())) {
                positionPlayers[index++] = players[i];
            }
        }
        */
        
        // Use the selector to choose a player
        return selector.selectPlayer(this, position);
    }
    
    /**
     * Gets a player by their name
     * 
     * @param name The name to search for
     * @return The player with the specified name, or null if not found
     */
    public IPlayer getPlayerByName(String name) {
        if (name == null) {
            return null;
        }
        
        for (int i = 0; i < playerCount; i++) {
            if (players[i].getName().equalsIgnoreCase(name)) {
                return players[i];
            }
        }
        return null;
    }
    
    /**
     * Gets a player by their jersey number
     * 
     * @param number The jersey number to search for
     * @return The player with the specified number, or null if not found
     */
    public IPlayer getPlayerByNumber(int number) {
        for (int i = 0; i < playerCount; i++) {
            if (players[i].getNumber() == number) {
                return players[i];
            }
        }
        return null;
    }
    
    /**
     * String representation of the club
     * 
     * @return Club name, code, and player count
     */
    @Override
    public String toString() {
        return name + " (" + code + ") - " + playerCount + " players";
    }
    
    /**
     * Set or update the stadium name
     * 
     * @param stadiumName New stadium name
     */
    public void setStadiumName(String stadiumName) {
        this.stadiumName = stadiumName;
    }
    
    /**
     * Set or update the logo URL
     * 
     * @param logo New logo URL
     */
    public void setLogo(String logo) {
        this.logo = logo;
    }
}