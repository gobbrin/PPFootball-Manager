package ppfootballmanager.models;

import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.ppstudios.footballmanager.api.contracts.league.ILeague;
import com.ppstudios.footballmanager.api.contracts.simulation.MatchSimulatorStrategy;
import com.ppstudios.footballmanager.api.contracts.league.ISeason;
import com.ppstudios.footballmanager.api.contracts.team.ITeam;

public class LeagueImpl implements ILeague {
    
    private String name;
    private ISeason[] seasons;
    private int seasonCount;
    private ITeam[] teams;
    private int teamCount;
    private MatchSimulatorStrategy matchSimulator;
    
    private static final int MAX_SEASONS = 10;
    private static final int MAX_TEAMS = 20;
    
    /**
     * Constructor for LeagueImpl
     * 
     * @param name The name of the league
     */
    public LeagueImpl(String name) {
        this.name = name;
        this.seasons = new ISeason[MAX_SEASONS];
        this.seasonCount = 0;
        this.teams = new ITeam[MAX_TEAMS];
        this.teamCount = 0;
    }
    
    /**
     * Adds a team to the league
     * 
     * @param team The team to add
     * @return true if the team was added successfully, false otherwise
     */
    public boolean addTeam(ITeam team) {
        if (team == null || teamCount >= MAX_TEAMS) {
            return false;
        }
        
        // Check for duplicate teams
        for (int i = 0; i < teamCount; i++) {
            if (teams[i].getClub().getName().equals(team.getClub().getName())) {
                return false; // Team already exists
            }
        }
        
        teams[teamCount++] = team;
        return true;
    }
    
    /**
     * Gets all teams in the league
     * 
     * @return Array of teams in the league
     */
    public ITeam[] getTeams() {
        ITeam[] result = new ITeam[teamCount];
        System.arraycopy(teams, 0, result, 0, teamCount);
        return result;
    }
    
    /**
     * Sets the match simulator strategy for this league
     * 
     * @param matchSimulator The match simulator strategy to use
     */
    public void setMatchSimulator(MatchSimulatorStrategy matchSimulator) {
        this.matchSimulator = matchSimulator;
    }
    
    /**
     * Gets the current match simulator strategy
     * 
     * @return The current match simulator strategy
     */
    public MatchSimulatorStrategy getMatchSimulator() {
        return matchSimulator;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void exportToJson() throws IOException {
        JSONObject leagueJson = new JSONObject();
        leagueJson.put("name", this.name);
        
        // Add teams
        JSONArray teamsArray = new JSONArray();
        for (int i = 0; i < teamCount; i++) {
            JSONObject teamObj = new JSONObject();
            teamObj.put("name", teams[i].getClub().getName());
            teamObj.put("code", teams[i].getClub().getCode());
            teamsArray.add(teamObj);
        }
        leagueJson.put("teams", teamsArray);
        
        // Add seasons
        JSONArray seasonsArray = new JSONArray();
        for (int i = 0; i < seasonCount; i++) {
            JSONObject seasonObj = new JSONObject();
            seasonObj.put("year", seasons[i].getYear());
            seasonObj.put("name", seasons[i].getName());
            seasonsArray.add(seasonObj);
        }
        leagueJson.put("seasons", seasonsArray);
        
        // Write to file
        String fileName = "league_" + this.name.replace(" ", "_") + ".json";
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(leagueJson.toJSONString());
            file.flush();
        }
    }

    @Override
    public boolean createSeason(ISeason season) {
        if (season == null || seasonCount >= MAX_SEASONS) {
            return false;
        }
        
        // Check for duplicate seasons
        for (int i = 0; i < seasonCount; i++) {
            if (seasons[i].getYear() == season.getYear()) {
                return false; // Season for this year already exists
            }
        }
        
        seasons[seasonCount++] = season;
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ISeason getSeason(int year) {
        for (int i = 0; i < seasonCount; i++) {
            if (seasons[i].getYear() == year) {
                return seasons[i];
            }
        }
        return null; // No season found for the specified year
    }

    @Override
    public ISeason[] getSeasons() {
        ISeason[] result = new ISeason[seasonCount];
        System.arraycopy(seasons, 0, result, 0, seasonCount);
        return result;
    }

    @Override
    public ISeason removeSeason(int year) {
        for (int i = 0; i < seasonCount; i++) {
            if (seasons[i].getYear() == year) {
                ISeason removed = seasons[i];
                
                // Shift all elements after the removed season
                for (int j = i; j < seasonCount - 1; j++) {
                    seasons[j] = seasons[j + 1];
                }
                
                seasons[--seasonCount] = null;
                return removed;
            }
        }
        return null; // No season found to remove
    }
    
    /**
     * Creates a new season for the specified year
     * 
     * @param year The year of the season
     * @return The newly created season, or null if creation failed
     */
    public ISeason createNewSeason(int year) {
        if (teamCount < 2) {
            return null; // Need at least 2 teams for a meaningful season
        }
        
        // Create a season name with the year range (e.g., "2022-2023")
        String seasonName = year + "-" + (year + 1);
        
        // Create a copy of the teams array for the new season
        ITeam[] seasonTeams = new ITeam[teamCount];
        System.arraycopy(teams, 0, seasonTeams, 0, teamCount);
        
        // Create the season
        ISeason newSeason = new SeasonImpl(year, seasonName, seasonTeams);
        
        // Add it to the league
        if (createSeason(newSeason)) {
            return newSeason;
        }
        
        return null;
    }
    
    /**
     * String representation of the league
     */
    @Override
    public String toString() {
        return name + " (" + teamCount + " teams, " + seasonCount + " seasons)";
    }
}