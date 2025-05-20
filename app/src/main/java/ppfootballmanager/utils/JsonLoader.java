package ppfootballmanager.utils;

import ppfootballmanager.models.*;
import com.ppstudios.footballmanager.api.contracts.player.*;
import com.ppstudios.footballmanager.api.contracts.team.ITeam;

import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JsonLoader {

    /**
     * Load a league with all clubs and players from JSON files
     * 
     * @param leagueName The name of the league
     * @param clubsFile  The JSON file containing club information
     * @return A configured LeagueImpl object
     */
    public static LeagueImpl loadLeague(String leagueName, String clubsFile) {
        LeagueImpl league = new LeagueImpl(leagueName);

        try {
            InputStream is = JsonLoader.class.getClassLoader().getResourceAsStream("leagues/" + clubsFile);
            if (is == null) {
                System.out.println("League clubs file not found: " + clubsFile);
                return league;
            }

            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            JSONParser parser = new JSONParser();
            JSONArray clubsArray = (JSONArray) parser.parse(reader);

            for (Object obj : clubsArray) {
                JSONObject clubJson = (JSONObject) obj;
                String clubName = (String) clubJson.get("name");
                String clubCode = (String) clubJson.get("code");
                String playersFile = (String) clubJson.get("playersFile");
                String logo = (String) clubJson.get("logo");
                String stadiumName = (String) clubJson.getOrDefault("stadium", "Stadium");
                String country = (String) clubJson.getOrDefault("country", "Unknown");
                int foundedYear = getIntOrDefault(clubJson, "founded", 1900);

                // Create club with more complete information
                ClubImpl club = new ClubImpl(clubName, clubCode, country, foundedYear, stadiumName, logo);

                TeamImpl team = new TeamImpl(club);

                // Load players for this club
                List<IPlayer> players = loadPlayers(playersFile);
                for (IPlayer player : players) {
                    team.addPlayer(player);
                    // Also add the player to club for completeness
                    club.addPlayer(player);
                }

                league.addTeam(team);
            }

        } catch (Exception e) {
            System.out.println("Error loading league: " + e.getMessage());
            e.printStackTrace();
        }

        return league;
    }

    /**
     * Load players from a JSON file
     * 
     * @param playersFile The JSON file containing player information
     * @return A list of IPlayer objects
     */
    public static List<IPlayer> loadPlayers(String playersFile) {
        List<IPlayer> players = new ArrayList<>();

        try {
            InputStream is = JsonLoader.class.getClassLoader().getResourceAsStream("players/" + playersFile);
            if (is == null) {
                System.out.println("Players file not found: " + playersFile);
                return players;
            }

            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            JSONParser parser = new JSONParser();
            JSONArray array = (JSONArray) parser.parse(reader);

            for (Object obj : array) {
                JSONObject jsonPlayer = (JSONObject) obj;
                
                // Skip incomplete player entries
                if (!jsonPlayer.containsKey("name") || !jsonPlayer.containsKey("birthDate")) {
                    continue;
                }

                String name = (String) jsonPlayer.get("name");
                String birthDateStr = (String) jsonPlayer.get("birthDate");
                LocalDate birthDate = LocalDate.parse(birthDateStr);
                String nationality = (String) jsonPlayer.getOrDefault("nationality", "Unknown");
                String positionStr = (String) jsonPlayer.getOrDefault("basePosition", "CM");
                String photo = (String) jsonPlayer.getOrDefault("photo", null);
                int number = getIntOrDefault(jsonPlayer, "number", 99);

                // Get player attributes if available or use defaults
                int shooting = getIntOrDefault(jsonPlayer, "shooting", 60);
                int passing = getIntOrDefault(jsonPlayer, "passing", 65);
                int stamina = getIntOrDefault(jsonPlayer, "stamina", 70);
                int speed = getIntOrDefault(jsonPlayer, "speed", 68);
                float height = getFloatOrDefault(jsonPlayer, "height", 1.75f);
                float weight = getFloatOrDefault(jsonPlayer, "weight", 70.0f);

                // Handle preferred foot
                PreferredFoot foot = PreferredFoot.Right;
                if (jsonPlayer.containsKey("foot")) {
                    String footStr = ((String) jsonPlayer.get("foot")).toLowerCase();
                    if (footStr.equals("left")) {
                        foot = PreferredFoot.Left;
                    } else if (footStr.equals("both")) {
                        foot = PreferredFoot.Both;
                    }
                }

                IPlayerPosition position = new PlayerPositionImpl(positionStr);

                PlayerImpl player = new PlayerImpl(name, number, birthDate, nationality,
                        height, weight, position, foot, photo,
                        shooting, passing, speed, stamina);

                players.add(player);
            }

        } catch (Exception e) {
            System.out.println("Error loading players from " + playersFile + ": " + e.getMessage());
            e.printStackTrace();
        }

        return players;
    }

    /**
     * Load a season configuration from JSON
     * 
     * @param seasonFile The JSON file containing season configuration
     * @return A configured SeasonImpl object
     */
    public static SeasonImpl loadSeason(String seasonFile) {
        try {
            InputStream is = JsonLoader.class.getClassLoader().getResourceAsStream("seasons/" + seasonFile);
            if (is == null) {
                System.out.println("Season file not found: " + seasonFile);
                return null;
            }

            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            JSONParser parser = new JSONParser();
            JSONObject seasonJson = (JSONObject) parser.parse(reader);

            String name = (String) seasonJson.get("name");
            int year = ((Long) seasonJson.get("year")).intValue();
            String leagueFile = (String) seasonJson.get("leagueFile");

            // Create the season
            SeasonImpl season = new SeasonImpl(year, name);

            // Load and add the league's clubs
            LeagueImpl league = loadLeague(name, leagueFile);
            ITeam[] teams = league.getTeams();
            for (ITeam team : teams) {
                if (team != null && team.getClub() != null) {
                    season.addClub(team.getClub());
                }
            }

            // Generate schedule
            if (season.getNumberOfCurrentTeams() >= 2) {
                season.generateSchedule();
                System.out.println("Schedule generated successfully with " + 
                                   season.getNumberOfCurrentTeams() + " teams and " + 
                                   season.getMaxRounds() + " rounds.");
            } else {
                System.out.println("Not enough teams to generate a schedule. Need at least 2 teams.");
            }

            return season;

        } catch (Exception e) {
            System.out.println("Error loading season: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Try loading a team from a JSON file
     * 
     * @param teamFile The JSON file containing team information
     * @return The loaded team, or null if loading failed
     */
    public static TeamImpl loadTeam(String teamFile) {
        try {
            InputStream is = JsonLoader.class.getClassLoader().getResourceAsStream("teams/" + teamFile);
            if (is == null) {
                System.out.println("Team file not found: " + teamFile);
                return null;
            }
            
            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            JSONParser parser = new JSONParser();
            JSONObject teamJson = (JSONObject) parser.parse(reader);
            
            // Get club info
            String clubName = (String) teamJson.get("club");
            String code = (String) teamJson.getOrDefault("code", clubName.substring(0, Math.min(3, clubName.length())).toUpperCase());
            String stadiumName = (String) teamJson.getOrDefault("stadium", "Stadium");
            String country = (String) teamJson.getOrDefault("country", "Unknown");
            int foundedYear = getIntOrDefault(teamJson, "founded", 1900);
            String logo = (String) teamJson.getOrDefault("logo", null);
            
            // Create club
            ClubImpl club = new ClubImpl(clubName, code, country, foundedYear, stadiumName, logo);
            
            // Create team
            TeamImpl team = new TeamImpl(club);
            
            // Load players if present
            if (teamJson.containsKey("players")) {
                JSONArray playersArray = (JSONArray) teamJson.get("players");
                for (Object obj : playersArray) {
                    JSONObject playerJson = (JSONObject) obj;
                    
                    // Create player with basic information
                    String name = (String) playerJson.get("name");
                    int number = ((Long) playerJson.get("number")).intValue();
                    String position = (String) playerJson.get("position");
                    
                    // Create a minimal player for the team
                    LocalDate birthDate = LocalDate.now().minusYears(25); // Default to 25 years old
                    IPlayerPosition playerPosition = new PlayerPositionImpl(position);
                    
                    PlayerImpl player = new PlayerImpl(
                        name, number, birthDate, country,
                        1.80f, 75.0f, playerPosition, PreferredFoot.Right, null,
                        70, 70, 70, 70 // Default attributes
                    );
                    
                    team.addPlayer(player);
                    club.addPlayer(player);
                }
            }
            
            return team;
            
        } catch (Exception e) {
            System.out.println("Error loading team: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Safely extract an integer from a JSON object with default value
     */
    private static int getIntOrDefault(JSONObject json, String key, int defaultValue) {
        if (json.containsKey(key)) {
            Object value = json.get(key);
            if (value instanceof Long) {
                return ((Long) value).intValue();
            } else if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof String) {
                try {
                    return Integer.parseInt((String) value);
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            }
        }
        return defaultValue;
    }

    /**
     * Safely extract a float from a JSON object with default value
     */
    private static float getFloatOrDefault(JSONObject json, String key, float defaultValue) {
        if (json.containsKey(key)) {
            Object value = json.get(key);
            if (value instanceof Double) {
                return ((Double) value).floatValue();
            } else if (value instanceof Long) {
                return ((Long) value).floatValue();
            } else if (value instanceof Float) {
                return (Float) value;
            } else if (value instanceof String) {
                try {
                    return Float.parseFloat((String) value);
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            }
        }
        return defaultValue;
    }
}