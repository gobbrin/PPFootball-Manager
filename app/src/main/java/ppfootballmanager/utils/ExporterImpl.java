package ppfootballmanager.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.ppstudios.footballmanager.api.contracts.data.IExporter;
import com.ppstudios.footballmanager.api.contracts.league.IStanding;
import com.ppstudios.footballmanager.api.contracts.match.IMatch;
import com.ppstudios.footballmanager.api.contracts.player.IPlayer;
import com.ppstudios.footballmanager.api.contracts.team.IClub;
import com.ppstudios.footballmanager.api.contracts.team.ITeam;

import ppfootballmanager.models.SeasonImpl;
import ppfootballmanager.models.StandingImpl;
import ppfootballmanager.models.MatchImpl;
import ppfootballmanager.models.LeagueImpl;

/**
 * Implementation of IExporter interface for exporting data to JSON format
 */
public class ExporterImpl implements IExporter {
    private String exportDirectory;
    private SeasonImpl season;
    
    /**
     * Constructor with season data
     * 
     * @param season The season to export
     */
    public ExporterImpl(SeasonImpl season) {
        this.season = season;
        this.exportDirectory = "exports";
        
        // Create exports directory if it doesn't exist
        File dir = new File(exportDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    /**
     * Constructor with season data and custom export directory
     * 
     * @param season The season to export
     * @param exportDirectory The directory to export to
     */
    public ExporterImpl(SeasonImpl season, String exportDirectory) {
        this.season = season;
        this.exportDirectory = exportDirectory;
        
        // Create exports directory if it doesn't exist
        File dir = new File(exportDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    public void exportToJson() throws IOException {
        if (season == null) {
            throw new IOException("No season data to export");
        }
        
        // Generate filename with timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = exportDirectory + File.separator + "season_" + season.getYear() + "_" + timestamp + ".json";
        
        // Create JSON object for the season
        JSONObject seasonJson = createSeasonJson();
        
        // Write to file
        try (FileWriter file = new FileWriter(filename)) {
            file.write(seasonJson.toJSONString());
            System.out.println("Successfully exported season data to " + filename);
        } catch (IOException e) {
            throw new IOException("Failed to export season: " + e.getMessage(), e);
        }
    }
    
    /**
     * Export a specific team to JSON
     * 
     * @param team The team to export
     * @throws IOException If export fails
     */
    public void exportTeamToJson(ITeam team) throws IOException {
        if (team == null || team.getClub() == null) {
            throw new IOException("Invalid team data to export");
        }
        
        // Generate filename with timestamp and team code
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String teamCode = team.getClub().getCode();
        String filename = exportDirectory + File.separator + "team_" + teamCode + "_" + timestamp + ".json";
        
        // Create JSON object for the team
        JSONObject teamJson = createTeamJson(team);
        
        // Write to file
        try (FileWriter file = new FileWriter(filename)) {
            file.write(teamJson.toJSONString());
            System.out.println("Successfully exported team data to " + filename);
        } catch (IOException e) {
            throw new IOException("Failed to export team: " + e.getMessage(), e);
        }
    }
    
    // TODO: EXPORTER DOESNT SEEM TO BE CALLING INDIVIDUAL EXPORTERS
    /**
     * Export league standings to JSON
     * 
     * @throws IOException If export fails
     */
    public void exportStandingsToJson() throws IOException {
        if (season == null) {
            throw new IOException("No season data to export");
        }
        
        // Generate filename with timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = exportDirectory + File.separator + "standings_" + season.getYear() + "_" + timestamp + ".json";
        
        // Create JSON object for standings
        JSONObject rootJson = new JSONObject();
        rootJson.put("season", season.getName());
        rootJson.put("year", season.getYear());
        rootJson.put("currentRound", season.getCurrentRound());
        
        // Add standings
        JSONArray standingsArray = createStandingsArray();
        rootJson.put("standings", standingsArray);
        
        // Write to file
        try (FileWriter file = new FileWriter(filename)) {
            file.write(rootJson.toJSONString());
            System.out.println("Successfully exported standings to " + filename);
        } catch (IOException e) {
            throw new IOException("Failed to export standings: " + e.getMessage(), e);
        }
    }
    
    /**
     * Creates a complete JSON representation of the season
     * 
     * @return JSONObject containing season data
     */
    private JSONObject createSeasonJson() {
        JSONObject seasonJson = new JSONObject();
        
        // Season info
        seasonJson.put("name", season.getName());
        seasonJson.put("year", season.getYear());
        seasonJson.put("currentRound", season.getCurrentRound());
        seasonJson.put("isComplete", season.isSeasonComplete());
        
        // Add clubs
        JSONArray clubsArray = new JSONArray();
        IClub[] clubs = season.getCurrentClubs();
        if (clubs != null) {
            for (IClub club : clubs) {
                if (club != null) {
                    JSONObject clubJson = new JSONObject();
                    clubJson.put("name", club.getName());
                    clubJson.put("code", club.getCode());
                    clubJson.put("stadium", club.getStadiumName());
                    
                    // Find the team for this club to access players
                    ITeam team = findTeamByClub(club);
                    if (team != null && team.getPlayers() != null) {
                        JSONArray playersArray = createPlayersArray(team);
                        clubJson.put("players", playersArray);
                    }
                    
                    clubsArray.add(clubJson);
                }
            }
        }
        seasonJson.put("clubs", clubsArray);
        
        // Add standings
        JSONArray standingsArray = createStandingsArray();
        seasonJson.put("standings", standingsArray);
        
        // Add matches
        JSONArray matchesArray = new JSONArray();
        IMatch[] matches = season.getMatches();
        if (matches != null) {
            for (IMatch match : matches) {
                if (match != null) {
                    JSONObject matchJson = new JSONObject();
                    matchJson.put("round", match.getRound());
                    
                    if (match.getHomeTeam() != null && match.getHomeTeam().getClub() != null) {
                        matchJson.put("homeTeam", match.getHomeTeam().getClub().getName());
                    }
                    
                    if (match.getAwayTeam() != null && match.getAwayTeam().getClub() != null) {
                        matchJson.put("awayTeam", match.getAwayTeam().getClub().getName());
                    }
                    
                    matchJson.put("isPlayed", match.isPlayed());
                    
                    // Add score if match is played and is our implementation
                    if (match.isPlayed() && match instanceof MatchImpl) {
                        MatchImpl matchImpl = (MatchImpl) match;
                        matchJson.put("homeGoals", matchImpl.getHomeGoals());
                        matchJson.put("awayGoals", matchImpl.getAwayGoals());
                    }
                    
                    matchesArray.add(matchJson);
                }
            }
        }
        seasonJson.put("matches", matchesArray);
        
        return seasonJson;
    }
    
    /**
     * Creates a JSON representation of standings
     * 
     * @return JSONArray containing standings data
     */
    private JSONArray createStandingsArray() {
        JSONArray standingsArray = new JSONArray();
        IStanding[] standings = season.getLeagueStandings();
        
        if (standings != null) {
            for (IStanding standing : standings) {
                if (standing != null && standing instanceof StandingImpl) {
                    StandingImpl standingImpl = (StandingImpl) standing;
                    JSONObject standingJson = new JSONObject();
                    
                    if (standingImpl.getClub() != null) {
                        standingJson.put("club", standingImpl.getClub().getName());
                    }
                    
                    standingJson.put("position", standingImpl.getPosition());
                    standingJson.put("played", standingImpl.getPlayed());
                    standingJson.put("won", standingImpl.getWon());
                    standingJson.put("drawn", standingImpl.getDrawn());
                    standingJson.put("lost", standingImpl.getLost());
                    standingJson.put("goalsFor", standingImpl.getGoalsFor());
                    standingJson.put("goalsAgainst", standingImpl.getGoalsAgainst());
                    standingJson.put("goalDifference", standingImpl.getGoalDifference());
                    standingJson.put("points", standingImpl.getPoints());
                    
                    standingsArray.add(standingJson);
                }
            }
        }
        
        return standingsArray;
    }
    
    /**
     * Creates a JSON representation of a team
     * 
     * @param team The team to convert to JSON
     * @return JSONObject containing team data
     */
    private JSONObject createTeamJson(ITeam team) {
        JSONObject teamJson = new JSONObject();
        
        // Team and club info
        IClub club = team.getClub();
        if (club != null) {
            teamJson.put("name", club.getName());
            teamJson.put("code", club.getCode());
            teamJson.put("stadium", club.getStadiumName());
        }
        
        // Add players
        JSONArray playersArray = createPlayersArray(team);
        teamJson.put("players", playersArray);
        
        // Add results from matches
        JSONArray resultsArray = new JSONArray();
        IMatch[] matches = season.getMatches();
        
        if (matches != null) {
            for (IMatch match : matches) {
                if (match != null && match.isPlayed() && 
                   (team.equals(match.getHomeTeam()) || team.equals(match.getAwayTeam()))) {
                    
                    JSONObject resultJson = new JSONObject();
                    resultJson.put("round", match.getRound());
                    
                    boolean isHomeTeam = team.equals(match.getHomeTeam());
                    String opponentName = isHomeTeam ? 
                        match.getAwayTeam().getClub().getName() : 
                        match.getHomeTeam().getClub().getName();
                    
                    resultJson.put("opponent", opponentName);
                    resultJson.put("isHomeGame", isHomeTeam);
                    
                    if (match instanceof MatchImpl) {
                        MatchImpl matchImpl = (MatchImpl) match;
                        int goalsFor = isHomeTeam ? matchImpl.getHomeGoals() : matchImpl.getAwayGoals();
                        int goalsAgainst = isHomeTeam ? matchImpl.getAwayGoals() : matchImpl.getHomeGoals();
                        
                        resultJson.put("goalsFor", goalsFor);
                        resultJson.put("goalsAgainst", goalsAgainst);
                        
                        String result = "Loss";
                        if (goalsFor > goalsAgainst) {
                            result = "Win";
                        } else if (goalsFor == goalsAgainst) {
                            result = "Draw";
                        }
                        resultJson.put("result", result);
                    }
                    
                    resultsArray.add(resultJson);
                }
            }
        }
        
        teamJson.put("results", resultsArray);
        
        // Add team statistics from standings
        IStanding[] standings = season.getLeagueStandings();
        if (standings != null) {
            for (IStanding standing : standings) {
                if (standing instanceof StandingImpl) {
                    StandingImpl standingImpl = (StandingImpl) standing;
                    if (standingImpl.getClub() != null && 
                        standingImpl.getClub().equals(club)) {
                        
                        JSONObject statsJson = new JSONObject();
                        statsJson.put("position", standingImpl.getPosition());
                        statsJson.put("played", standingImpl.getPlayed());
                        statsJson.put("won", standingImpl.getWon());
                        statsJson.put("drawn", standingImpl.getDrawn());
                        statsJson.put("lost", standingImpl.getLost());
                        statsJson.put("goalsFor", standingImpl.getGoalsFor());
                        statsJson.put("goalsAgainst", standingImpl.getGoalsAgainst());
                        statsJson.put("goalDifference", standingImpl.getGoalDifference());
                        statsJson.put("points", standingImpl.getPoints());
                        
                        teamJson.put("stats", statsJson);
                        break;
                    }
                }
            }
        }
        
        return teamJson;
    }
    
    /**
     * Creates a JSON array of player information
     * 
     * @param team The team containing players
     * @return JSONArray containing player data
     */
    private JSONArray createPlayersArray(ITeam team) {
        JSONArray playersArray = new JSONArray();
        
        IPlayer[] players = team.getPlayers();
        if (players != null) {
            for (IPlayer player : players) {
                if (player != null) {
                    JSONObject playerJson = new JSONObject();
                    playerJson.put("name", player.getName());
                    playerJson.put("number", player.getNumber());
                    
                    if (player.getPosition() != null) {
                        playerJson.put("position", player.getPosition().toString());
                    }
                    
                    playerJson.put("nationality", player.getNationality());
                    
                    if (player.getBirthDate() != null) {
                        playerJson.put("birthDate", player.getBirthDate().toString());
                        
                        // Calculate age based on birth date
                        int age = LocalDateTime.now().getYear() - player.getBirthDate().getYear();
                        playerJson.put("age", age);
                    }
                    
                    playersArray.add(playerJson);
                }
            }
        }
        
        return playersArray;
    }
    
    /**
     * Finds a team by its club
     * 
     * @param club The club to search for
     * @return The team associated with the club, or null if not found
     */
    private ITeam findTeamByClub(IClub club) {
        IMatch[] matches = season.getMatches();
        if (matches != null) {
            for (IMatch match : matches) {
                if (match != null) {
                    if (match.getHomeTeam() != null && match.getHomeTeam().getClub() != null && 
                        match.getHomeTeam().getClub().equals(club)) {
                        return match.getHomeTeam();
                    }
                    if (match.getAwayTeam() != null && match.getAwayTeam().getClub() != null && 
                        match.getAwayTeam().getClub().equals(club)) {
                        return match.getAwayTeam();
                    }
                }
            }
        }
        return null;
    }
}