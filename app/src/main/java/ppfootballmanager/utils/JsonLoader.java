package ppfootballmanager.utils;

import ppfootballmanager.clubs.ClubImpl;
import ppfootballmanager.clubs.PlayerImpl;
import ppfootballmanager.clubs.PlayerPositionImpl;
import ppfootballmanager.clubs.TeamImpl;
import ppfootballmanager.game.*;

import com.ppstudios.footballmanager.api.contracts.match.IMatch;
// Player-related interfaces from the API contracts
import com.ppstudios.footballmanager.api.contracts.player.*;
import com.ppstudios.footballmanager.api.contracts.team.IClub;
// Team interface from the API contracts
import com.ppstudios.footballmanager.api.contracts.team.ITeam;

// File handling imports for reading JSON files
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
// Date handling for player birthdays
import java.time.LocalDate;

// JSON parsing library imports
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Utility class for loading game data from JSON files
 * Uses arrays instead of Java Collections Framework
 */
public class JsonLoader {

    /**
     * Maximum number of items to load per collection
     * These are used for fixed-size arrays
     */
    private static final int MAX_PLAYERS = 100;
    private static final int MAX_TEAMS = 20;
    private static final int MAX_CLUBS = 20;

    /**
     * Loads a complete league with all its clubs and players from JSON files
     * 
     * @param leagueName The name to give the new league
     * @param clubsFile  The filename of the JSON file containing club information
     * @return A fully configured league with teams and players
     */
    public static LeagueImpl loadLeague(String leagueName, String clubsFile) {
        // Create an empty league with the specified name
        LeagueImpl league = new LeagueImpl(leagueName);

        try {
            // Load the clubs file from the "leagues" resource directory
            InputStream is = JsonLoader.class.getClassLoader().getResourceAsStream("leagues/" + clubsFile);

            // If file not found, return the empty league with a warning
            if (is == null) {
                System.out.println("League clubs file not found: " + clubsFile);
                return league;
            }

            // Create a UTF-8 reader for the input stream
            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            // Initialize the JSON parser
            JSONParser parser = new JSONParser();
            // Parse the entire file as a JSON array (list of clubs)
            JSONArray clubsArray = (JSONArray) parser.parse(reader);

            // Iterate through each club entry in the array
            for (int i = 0; i < clubsArray.size() && i < MAX_CLUBS; i++) {
                // Get the club JSON object
                JSONObject clubJson = (JSONObject) clubsArray.get(i);

                // Extract required club properties
                String clubName = (String) clubJson.get("name");
                String clubCode = (String) clubJson.get("code");
                String playersFile = (String) clubJson.get("playersFile");

                // Extract optional club properties with defaults if not present
                String logo = (String) clubJson.get("logo");
                String stadiumName = (String) clubJson.getOrDefault("stadium", "Stadium");
                String country = (String) clubJson.getOrDefault("country", "Unknown");
                int foundedYear = getIntOrDefault(clubJson, "founded", 1900);

                // Create a club instance with all the extracted information
                ClubImpl club = new ClubImpl(clubName, clubCode, country, foundedYear, stadiumName, logo);

                // Create a team for this club
                TeamImpl team = new TeamImpl(club);

                // Load all players for this club from the referenced players file
                IPlayer[] players = loadPlayers(playersFile);

                // Add each player to both the team and the club
                for (int j = 0; j < players.length; j++) {
                    if (players[j] != null) {
                        team.addPlayer(players[j]);
                        club.addPlayer(players[j]);
                    }
                }

                // Add the completed team to the league
                league.addTeam(team);
            }

        } catch (Exception e) {
            // Handle any errors during loading
            System.out.println("Error loading league: " + e.getMessage());
            e.printStackTrace();
        }

        // Return the league with whatever teams were successfully loaded
        return league;
    }

    /**
     * Loads players from a JSON file
     * 
     * @param playersFile The filename containing player information
     * @return An array of IPlayer objects created from the JSON data
     */
    public static IPlayer[] loadPlayers(String playersFile) {
        // Create a fixed-size array to store the loaded players
        IPlayer[] players = new IPlayer[MAX_PLAYERS];
        int playerCount = 0;

        try {
            // Load the players file from the "players" resource directory
            InputStream is = JsonLoader.class.getClassLoader().getResourceAsStream("players/" + playersFile);

            // If file not found, return the empty array with a warning
            if (is == null) {
                System.out.println("Players file not found: " + playersFile);
                return new IPlayer[0];
            }

            // Create a UTF-8 reader for the input stream
            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            // Initialize the JSON parser
            JSONParser parser = new JSONParser();
            // Parse the entire file as a JSON array (list of players)
            JSONArray array = (JSONArray) parser.parse(reader);

            // Iterate through each player entry in the array
            for (int i = 0; i < array.size() && playerCount < MAX_PLAYERS; i++) {
                // Get the player JSON object
                JSONObject jsonPlayer = (JSONObject) array.get(i);

                // Skip player entries that don't have the required fields
                if (!jsonPlayer.containsKey("name") || !jsonPlayer.containsKey("birthDate")) {
                    continue;
                }

                // Extract required player properties
                String name = (String) jsonPlayer.get("name");
                String birthDateStr = (String) jsonPlayer.get("birthDate");
                LocalDate birthDate = LocalDate.parse(birthDateStr);

                // Extract optional player properties with defaults if not present
                String nationality = (String) jsonPlayer.getOrDefault("nationality", "Unknown");
                String positionStr = (String) jsonPlayer.getOrDefault("basePosition", "CM");
                String photo = (String) jsonPlayer.getOrDefault("photo", null);
                int number = getIntOrDefault(jsonPlayer, "number", 99);

                // Extract player attributes with defaults if not specified
                int shooting = getIntOrDefault(jsonPlayer, "shooting", 60);
                int passing = getIntOrDefault(jsonPlayer, "passing", 65);
                int stamina = getIntOrDefault(jsonPlayer, "stamina", 70);
                int speed = getIntOrDefault(jsonPlayer, "speed", 68);
                float height = getFloatOrDefault(jsonPlayer, "height", 1.75f);
                float weight = getFloatOrDefault(jsonPlayer, "weight", 70.0f);

                // Handle preferred foot with right as default
                PreferredFoot foot = PreferredFoot.Right; // Default to right foot
                if (jsonPlayer.containsKey("foot")) {
                    String footStr = ((String) jsonPlayer.get("foot")).toLowerCase();
                    if (footStr.equals("left")) {
                        foot = PreferredFoot.Left;
                    } else if (footStr.equals("both")) {
                        foot = PreferredFoot.Both;
                    }
                }

                // Create a player position object from the position string
                IPlayerPosition position = new PlayerPositionImpl(positionStr);

                // Create a fully populated player instance with all extracted data
                PlayerImpl player = new PlayerImpl(name, number, birthDate, nationality,
                        height, weight, position, foot, photo,
                        shooting, passing, speed, stamina);

                // Add the player to the array
                players[playerCount++] = player;
            }

        } catch (Exception e) {
            // Handle any errors during loading
            System.out.println("Error loading players from " + playersFile + ": " + e.getMessage());
            e.printStackTrace();
        }

        // Create a properly sized array with only the loaded players
        IPlayer[] result = new IPlayer[playerCount];
        System.arraycopy(players, 0, result, 0, playerCount);

        // Return all successfully loaded players
        return result;
    }

    /**
     * Loads a complete season configuration from JSON
     * 
     * @param seasonFile The filename containing season configuration
     * @return A configured SeasonImpl object ready for gameplay
     */
    public static SeasonImpl loadSeason(String seasonFile) {
        try {
            // Load the season file from the "seasons" resource directory
            InputStream is = JsonLoader.class.getClassLoader().getResourceAsStream("seasons/" + seasonFile);

            // If file not found, return null with a warning
            if (is == null) {
                System.out.println("Season file not found: " + seasonFile);
                return null;
            }

            // Create a UTF-8 reader for the input stream
            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            // Initialize the JSON parser
            JSONParser parser = new JSONParser();
            // Parse the file as a JSON object containing season configuration
            JSONObject seasonJson = (JSONObject) parser.parse(reader);

            // Extract season properties
            String name = (String) seasonJson.get("name");
            int year = ((Long) seasonJson.get("year")).intValue();
            String leagueFile = (String) seasonJson.get("leagueFile");

            // Create a new season with the year and name
            SeasonImpl season = new SeasonImpl(year, name);

            // Load the league specified in the season configuration
            LeagueImpl league = loadLeague(name, leagueFile);

            // Add all teams from the league to the season
            ITeam[] teams = league.getTeams();
            for (int i = 0; i < teams.length; i++) {
                if (teams[i] != null && teams[i].getClub() != null) {
                    season.addClub(teams[i].getClub());
                }
            }

            // Generate a match schedule if there are enough teams
            if (season.getNumberOfCurrentTeams() >= 2) {
                season.generateSchedule();
                System.out.println("Schedule generated successfully with " +
                        season.getNumberOfCurrentTeams() + " teams and " +
                        season.getMaxRounds() + " rounds.");
            } else {
                System.out.println("Not enough teams to generate a schedule. Need at least 2 teams.");
            }

            System.out.println("Loading player data for teams...");

            // Get all clubs from the season
            IClub[] clubs = season.getCurrentClubs();
            if (clubs != null) {
                for (IClub club : clubs) {
                    if (club != null) {
                        String clubCode = club.getCode().toLowerCase();
                        // Try to load player data for this club
                        String playerFile = "players/" + clubCode + ".json";
                        IPlayer[] players = loadPlayers(playerFile);

                        if (players.length == 0) {
                            // Fallback to default.json if no specific file exists
                            players = loadPlayers("players/default.json");
                        }

                        // Add players to club
                        for (IPlayer player : players) {
                            if (player != null) {
                                club.addPlayer(player);
                            }
                        }

                        System.out.println("Loaded " + players.length + " players for " + club.getName());

                        // Add players to team as well
                        IMatch[] matches = season.getSchedule().getAllMatches();
                        if (matches != null) {
                            for (IMatch match : matches) {
                                if (match != null) {
                                    // Check home team
                                    if (match.getHomeTeam() != null &&
                                            match.getHomeTeam().getClub() != null &&
                                            match.getHomeTeam().getClub().equals(club)) {

                                        // Synchronize players
                                        syncPlayersFromClubToTeam(club, match.getHomeTeam());
                                    }

                                    // Check away team
                                    if (match.getAwayTeam() != null &&
                                            match.getAwayTeam().getClub() != null &&
                                            match.getAwayTeam().getClub().equals(club)) {

                                        // Synchronize players
                                        syncPlayersFromClubToTeam(club, match.getAwayTeam());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Return the fully configured season
            return season;

        } catch (Exception e) {
            // Handle any errors during loading
            System.out.println("Error loading season: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper method to synchronize players from club to team
     */
    private static void syncPlayersFromClubToTeam(IClub club, ITeam team) {
        if (club == null || team == null) {
            return;
        }

        // First clear the team's players
        if (team instanceof TeamImpl) {
            ((TeamImpl) team).clearPlayers();
        }

        // Now add all club players to the team
        IPlayer[] clubPlayers = club.getPlayers();
        if (clubPlayers != null) {
            for (IPlayer player : clubPlayers) {
                if (player != null) {
                    team.addPlayer(player);
                }
            }
        }
    }

    /**
     * Loads a single team from a JSON file
     * 
     * @param teamFile The filename containing team information
     * @return A configured TeamImpl object, or null if loading failed
     */
    public static TeamImpl loadTeam(String teamFile) {
        try {
            // Load the team file from the "teams" resource directory
            InputStream is = JsonLoader.class.getClassLoader().getResourceAsStream("teams/" + teamFile);

            // If file not found, return null with a warning
            if (is == null) {
                System.out.println("Team file not found: " + teamFile);
                return null;
            }

            // Create a UTF-8 reader for the input stream
            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            // Initialize the JSON parser
            JSONParser parser = new JSONParser();
            // Parse the file as a JSON object containing team configuration
            JSONObject teamJson = (JSONObject) parser.parse(reader);

            // Extract club information
            String clubName = (String) teamJson.get("club");

            // Generate a code from the club name if not provided
            String code = (String) teamJson.getOrDefault("code",
                    clubName.substring(0, Math.min(3, clubName.length())).toUpperCase());

            // Extract optional club properties with defaults if not present
            String stadiumName = (String) teamJson.getOrDefault("stadium", "Stadium");
            String country = (String) teamJson.getOrDefault("country", "Unknown");
            int foundedYear = getIntOrDefault(teamJson, "founded", 1900);
            String logo = (String) teamJson.getOrDefault("logo", null);

            // Create a club with the extracted information
            ClubImpl club = new ClubImpl(clubName, code, country, foundedYear, stadiumName, logo);

            // Create a team for this club
            TeamImpl team = new TeamImpl(club);

            // If player information is included in the team file, load them directly
            if (teamJson.containsKey("players")) {
                JSONArray playersArray = (JSONArray) teamJson.get("players");

                // Process each player in the array
                for (int i = 0; i < playersArray.size() && i < MAX_PLAYERS; i++) {
                    JSONObject playerJson = (JSONObject) playersArray.get(i);

                    // Extract required player properties
                    String name = (String) playerJson.get("name");
                    int number = ((Long) playerJson.get("number")).intValue();
                    String position = (String) playerJson.get("position");

                    // Create a player with default values for optional properties
                    LocalDate birthDate = LocalDate.now().minusYears(25); // Default to 25 years old
                    IPlayerPosition playerPosition = new PlayerPositionImpl(position);

                    // Create a player with minimal information and default attributes
                    PlayerImpl player = new PlayerImpl(
                            name, number, birthDate, country,
                            1.80f, 75.0f, playerPosition, PreferredFoot.Right, null,
                            70, 70, 70, 70 // Default attributes (all set to 70)
                    );

                    // Add the player to both the team and club
                    team.addPlayer(player);
                    club.addPlayer(player);
                }
            }

            // Return the fully configured team
            return team;

        } catch (Exception e) {
            // Handle any errors during loading
            System.out.println("Error loading team: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Utility method to safely extract an integer from a JSON object
     *
     * @param json         The JSON object to extract from
     * @param key          The key of the value to extract
     * @param defaultValue The value to return if extraction fails
     * @return The extracted integer or the default value
     */
    private static int getIntOrDefault(JSONObject json, String key, int defaultValue) {
        // Check if the key exists in the JSON object
        if (json.containsKey(key)) {
            // Get the value associated with the key
            Object value = json.get(key);

            // Handle different numeric types that could represent an integer
            if (value instanceof Long) {
                return ((Long) value).intValue();
            } else if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof String) {
                // Try to parse a string as an integer
                try {
                    return Integer.parseInt((String) value);
                } catch (NumberFormatException e) {
                    // If parsing fails, return the default
                    return defaultValue;
                }
            }
        }
        // If key doesn't exist or value type is unsupported, return default
        return defaultValue;
    }

    /**
     * Utility method to safely extract a float from a JSON object
     *
     * @param json         The JSON object to extract from
     * @param key          The key of the value to extract
     * @param defaultValue The value to return if extraction fails
     * @return The extracted float or the default value
     */
    private static float getFloatOrDefault(JSONObject json, String key, float defaultValue) {
        // Check if the key exists in the JSON object
        if (json.containsKey(key)) {
            // Get the value associated with the key
            Object value = json.get(key);

            // Handle different numeric types that could represent a float
            if (value instanceof Double) {
                return ((Double) value).floatValue();
            } else if (value instanceof Long) {
                return ((Long) value).floatValue();
            } else if (value instanceof Float) {
                return (Float) value;
            } else if (value instanceof String) {
                // Try to parse a string as a float
                try {
                    return Float.parseFloat((String) value);
                } catch (NumberFormatException e) {
                    // If parsing fails, return the default
                    return defaultValue;
                }
            }
        }
        // If key doesn't exist or value type is unsupported, return default
        return defaultValue;
    }
}