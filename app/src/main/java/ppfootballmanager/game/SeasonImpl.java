package ppfootballmanager.game;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.ppstudios.footballmanager.api.contracts.league.ISchedule;
import com.ppstudios.footballmanager.api.contracts.league.ISeason;
import com.ppstudios.footballmanager.api.contracts.league.IStanding;
import com.ppstudios.footballmanager.api.contracts.match.IMatch;
import com.ppstudios.footballmanager.api.contracts.simulation.MatchSimulatorStrategy;
import com.ppstudios.footballmanager.api.contracts.team.IClub;
import com.ppstudios.footballmanager.api.contracts.team.ITeam;

public class SeasonImpl implements ISeason {
    private String name;
    private int year;
    private IClub[] clubs;
    private int clubCount;
    private IMatch[] matches;
    private int matchCount;
    private IStanding[] standings;
    private ISchedule schedule;
    private MatchSimulatorStrategy matchSimulator;
    private int currentRound;
    private boolean isComplete;

    // Constants for league rules
    private static final int MAX_CLUBS = 20;
    private static final int POINTS_PER_WIN = 3;
    private static final int POINTS_PER_DRAW = 1;
    private static final int POINTS_PER_LOSS = 0;

    /**
     * Constructor for SeasonImpl
     * 
     * @param year The year of the season
     * @param name The name of the season (e.g., "2022-2023")
     */
    public SeasonImpl(int year, String name) {
        this.year = year;
        this.name = name;
        this.clubs = new IClub[MAX_CLUBS];
        this.clubCount = 0;
        this.currentRound = 0;
        this.isComplete = false;
    }

    /**
     * Constructor for SeasonImpl with teams
     * 
     * @param year  The year of the season
     * @param name  The name of the season
     * @param teams The teams participating in the season
     */
    public SeasonImpl(int year, String name, ITeam[] teams) {
        this(year, name);

        if (teams != null) {
            for (ITeam team : teams) {
                if (team != null && team.getClub() != null) {
                    addClub(team.getClub());
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void exportToJson() throws IOException {
        JSONObject seasonJson = new JSONObject();
        seasonJson.put("name", this.name);
        seasonJson.put("year", this.year);

        // Export clubs
        JSONArray clubsArray = new JSONArray();
        for (int i = 0; i < clubCount; i++) {
            JSONObject clubObject = new JSONObject();
            clubObject.put("name", clubs[i].getName());
            clubObject.put("code", clubs[i].getCode());
            clubsArray.add(clubObject);
        }
        seasonJson.put("clubs", clubsArray);

        // Export standings
        if (standings != null) {
            JSONArray standingsArray = new JSONArray();
            for (IStanding standing : standings) {
                JSONObject standingObject = new JSONObject();

                // Use type casting since these methods aren't in the interface
                if (standing instanceof StandingImpl) {
                    StandingImpl standingImpl = (StandingImpl) standing;
                    standingObject.put("club", standingImpl.getClub().getName());
                    standingObject.put("position", standingImpl.getPosition());
                    standingObject.put("points", standing.getPoints());
                    standingObject.put("played", standingImpl.getPlayed());
                    standingObject.put("won", standingImpl.getWon());
                    standingObject.put("drawn", standingImpl.getDrawn());
                    standingObject.put("lost", standingImpl.getLost());
                    standingObject.put("goalsFor", standingImpl.getGoalsFor());
                    standingObject.put("goalsAgainst", standingImpl.getGoalsAgainst());
                } else {
                    // Fallback to interface methods if available
                    standingObject.put("points", standing.getPoints());

                    // These might require alternative access if available in interface
                    if (standing.getTeam() != null && standing.getTeam().getClub() != null) {
                        standingObject.put("club", standing.getTeam().getClub().getName());
                    }
                    // Other stats may need to be omitted if not accessible
                    standingObject.put("played", standing.getTotalMatches());
                }

                standingsArray.add(standingObject);
            }
            seasonJson.put("standings", standingsArray);
        }

        // Export matches
        if (matches != null && matchCount > 0) {
            JSONArray matchesArray = new JSONArray();
            for (int i = 0; i < matchCount; i++) {
                IMatch match = matches[i];
                JSONObject matchObject = new JSONObject();
                matchObject.put("round", match.getRound());
                matchObject.put("homeTeam", match.getHomeTeam().getClub().getName());
                matchObject.put("awayTeam", match.getAwayTeam().getClub().getName());

                // Use casting to access implementation-specific methods
                if (match instanceof MatchImpl) {
                    MatchImpl matchImpl = (MatchImpl) match;
                    matchObject.put("homeGoals", matchImpl.getHomeGoals());
                    matchObject.put("awayGoals", matchImpl.getAwayGoals());
                }

                matchObject.put("isPlayed", match.isPlayed());
                matchesArray.add(matchObject);
            }
            seasonJson.put("matches", matchesArray);
        }

        // Write to file
        String fileName = "season_" + this.year + ".json";
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(seasonJson.toJSONString());
            file.flush();
        }
    }

    @Override
    public boolean addClub(IClub club) {
        if (club == null || clubCount >= MAX_CLUBS) {
            return false;
        }

        // Check for duplicate clubs
        for (int i = 0; i < clubCount; i++) {
            if (clubs[i].getName().equals(club.getName())) {
                return false; // Club already exists
            }
        }

        clubs[clubCount++] = club;
        updateStandings(); // Recalculate standings when a club is added
        return true;
    }

    @Override
    public String displayMatchResult(IMatch match) {
        if (match == null) {
            return "Invalid match";
        }

        if (!match.isPlayed()) {
            return match.getHomeTeam().getClub().getName() + " vs " +
                    match.getAwayTeam().getClub().getName() + " (Not played yet)";
        }

        if (match instanceof MatchImpl) {
            MatchImpl matchImpl = (MatchImpl) match;
            return match.getHomeTeam().getClub().getName() + " " + matchImpl.getHomeGoals() + " - " +
                    matchImpl.getAwayGoals() + " " + match.getAwayTeam().getClub().getName();
        } else {
            // Fallback if not our implementation
            ITeam winner = match.getWinner();
            if (winner == null) {
                return match.getHomeTeam().getClub().getName() + " DRAW " + match.getAwayTeam().getClub().getName();
            } else if (winner == match.getHomeTeam()) {
                return match.getHomeTeam().getClub().getName() + " WON vs " + match.getAwayTeam().getClub().getName();
            } else {
                return match.getHomeTeam().getClub().getName() + " LOST to " + match.getAwayTeam().getClub().getName();
            }
        }
    }

    @Override
    public void generateSchedule() {
        if (clubCount < 2) {
            return; // Need at least 2 clubs for a schedule
        }

        // Create schedule logic based on the number of clubs
        // For a balanced schedule in a league format, each team plays every other team
        // twice
        // (once home, once away)
        int totalRounds = (clubCount - 1) * 2; // Double round-robin format
        int matchesPerRound = clubCount / 2;
        int totalMatches = totalRounds * matchesPerRound;

        matches = new IMatch[totalMatches];
        matchCount = 0;

        // Simple round-robin algorithm
        // First half of season - each club plays every other club once
        for (int round = 0; round < clubCount - 1; round++) {
            for (int match = 0; match < matchesPerRound; match++) {
                int home = (round + match) % (clubCount - 1);
                int away = (clubCount - 1 - match + round) % (clubCount - 1);

                // Last club stays in the same position and others rotate
                if (match == 0) {
                    away = clubCount - 1;
                }

                matches[matchCount++] = new MatchImpl(round + 1, clubs[home], clubs[away]);
            }
        }

        // Second half of season - reverse home/away teams
        for (int i = 0; i < (clubCount - 1) * matchesPerRound; i++) {
            IMatch firstHalfMatch = matches[i];
            matches[matchCount++] = new MatchImpl(
                    firstHalfMatch.getRound() + clubCount - 1,
                    firstHalfMatch.getAwayTeam().getClub(),
                    firstHalfMatch.getHomeTeam().getClub());
        }

        // Create schedule object
        schedule = new ScheduleImpl(matches, totalRounds);
        updateStandings(); // Initialize standings
    }

    @Override
    public IClub[] getCurrentClubs() {
        IClub[] result = new IClub[clubCount];
        System.arraycopy(clubs, 0, result, 0, clubCount);
        return result;
    }

    @Override
    public int getCurrentMatches() {
        return matchCount;
    }

    @Override
    public int getCurrentRound() {
        return currentRound;
    }

    @Override
    public IStanding[] getLeagueStandings() {
        if (standings == null) {
            updateStandings();
        }
        return standings;
    }

    @Override
    public IMatch[] getMatches() {
        IMatch[] result = new IMatch[matchCount];
        System.arraycopy(matches, 0, result, 0, matchCount);
        return result;
    }

    @Override
    public IMatch[] getMatches(int round) {
        if (matches == null) {
            return new IMatch[0];
        }

        // Count matches in the specified round
        int count = 0;
        for (int i = 0; i < matchCount; i++) {
            if (matches[i].getRound() == round) {
                count++;
            }
        }

        // Create array of matches for the round
        IMatch[] roundMatches = new IMatch[count];
        int index = 0;
        for (int i = 0; i < matchCount && index < count; i++) {
            if (matches[i].getRound() == round) {
                roundMatches[index++] = matches[i];
            }
        }

        return roundMatches;
    }

    @Override
    public int getMaxRounds() {
        return (clubCount - 1) * 2; // Double round-robin format
    }

    @Override
    public int getMaxTeams() {
        return MAX_CLUBS;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getNumberOfCurrentTeams() {
        return clubCount;
    }

    @Override
    public int getPointsPerDraw() {
        return POINTS_PER_DRAW;
    }

    @Override
    public int getPointsPerLoss() {
        return POINTS_PER_LOSS;
    }

    @Override
    public int getPointsPerWin() {
        return POINTS_PER_WIN;
    }

    @Override
    public ISchedule getSchedule() {
        return schedule;
    }

    @Override
    public int getYear() {
        return year;
    }

    @Override
    public boolean isSeasonComplete() {
        return isComplete;
    }

    @Override
    public boolean removeClub(IClub clubToRemove) {
        if (clubToRemove == null) {
            return false;
        }

        for (int i = 0; i < clubCount; i++) {
            if (clubs[i].getName().equals(clubToRemove.getName())) {
                // Shift all clubs after the removed club
                for (int j = i; j < clubCount - 1; j++) {
                    clubs[j] = clubs[j + 1];
                }

                clubs[--clubCount] = null;

                // Regenerate schedule and standings
                if (clubCount >= 2) {
                    generateSchedule();
                } else {
                    matches = null;
                    matchCount = 0;
                    schedule = null;
                }

                updateStandings();
                return true;
            }
        }

        return false;
    }

    @Override
public void resetSeason() {
    if (matches != null) {
        for (int i = 0; i < matchCount; i++) {
            if (matches[i] != null && matches[i] instanceof MatchImpl) {
                ((MatchImpl) matches[i]).reset(); // Cast to MatchImpl first
            }
        }
    }

    currentRound = 0;
    isComplete = false;
    updateStandings();
}

    @Override
    public void setMatchSimulator(MatchSimulatorStrategy matchSimulator) {
        this.matchSimulator = matchSimulator;
    }

    @Override
    public void simulateRound() {
        if (isComplete || matchSimulator == null) {
            return;
        }

        // Check if we have matches to simulate
        if (matches == null || currentRound >= getMaxRounds()) {
            isComplete = true;
            return;
        }

        // Get matches for current round
        IMatch[] roundMatches = getMatches(currentRound + 1);

        // Simulate each match
        for (IMatch match : roundMatches) {
            if (!match.isPlayed()) {
                matchSimulator.simulate(match);
            }
        }

        // Update standings after simulation
        updateStandings();

        // Move to next round
        currentRound++;

        // Check if season is complete
        if (currentRound >= getMaxRounds()) {
            isComplete = true;
        }
    }

    @Override
    public void simulateSeason() {
        if (isComplete) {
            return;
        }

        while (!isComplete) {
            simulateRound();
        }
    }

    /**
     * Updates the league standings based on match results
     */
    private void updateStandings() {
        // Initialize standings for all clubs
        standings = new IStanding[clubCount];
        for (int i = 0; i < clubCount; i++) {
            standings[i] = new StandingImpl(clubs[i]);
        }

        // If there are no matches, just return the initialized standings
        if (matches == null || matchCount == 0) {
            return;
        }

        // Update standings based on match results
        for (int i = 0; i < matchCount; i++) {
            IMatch match = matches[i];
            if (match.isPlayed()) {
                // We need to use our implementation to get scores
                if (match instanceof MatchImpl) {
                    MatchImpl matchImpl = (MatchImpl) match;

                    // Find home team standing
                    for (int j = 0; j < clubCount; j++) {
                        if (standings[j] instanceof StandingImpl &&
                                ((StandingImpl) standings[j]).getClub().getName()
                                        .equals(match.getHomeTeam().getClub().getName())) {
                            StandingImpl homeStanding = (StandingImpl) standings[j];
                            homeStanding.updateWithMatch(matchImpl.getHomeGoals(), matchImpl.getAwayGoals(), true);
                            break;
                        }
                    }

                    // Find away team standing
                    for (int j = 0; j < clubCount; j++) {
                        if (standings[j] instanceof StandingImpl &&
                                ((StandingImpl) standings[j]).getClub().getName()
                                        .equals(match.getAwayTeam().getClub().getName())) {
                            StandingImpl awayStanding = (StandingImpl) standings[j];
                            awayStanding.updateWithMatch(matchImpl.getAwayGoals(), matchImpl.getHomeGoals(), false);
                            break;
                        }
                    }
                }
            }
        }

        // Sort standings by points (descending), then goal difference
        sortStandings();
    }

    /**
     * Sort the standings array by points (and then goal difference)
     */
    private void sortStandings() {
        // Simple bubble sort for now (can be optimized later)
        for (int i = 0; i < clubCount - 1; i++) {
            for (int j = 0; j < clubCount - i - 1; j++) {
                IStanding s1 = standings[j];
                IStanding s2 = standings[j + 1];

                // Compare points first
                if (s1.getPoints() < s2.getPoints()) {
                    // Swap
                    IStanding temp = standings[j];
                    standings[j] = standings[j + 1];
                    standings[j + 1] = temp;
                }
                // If points are equal, compare goal difference
                else if (s1.getPoints() == s2.getPoints()) {
                    int goalDiff1, goalDiff2;

                    if (s1 instanceof StandingImpl && s2 instanceof StandingImpl) {
                        StandingImpl s1Impl = (StandingImpl) s1;
                        StandingImpl s2Impl = (StandingImpl) s2;
                        goalDiff1 = s1Impl.getGoalsFor() - s1Impl.getGoalsAgainst();
                        goalDiff2 = s2Impl.getGoalsFor() - s2Impl.getGoalsAgainst();
                    } else {
                        // Use the interface method if available
                        goalDiff1 = s1.getGoalDifference();
                        goalDiff2 = s2.getGoalDifference();
                    }

                    if (goalDiff1 < goalDiff2) {
                        // Swap
                        IStanding temp = standings[j];
                        standings[j] = standings[j + 1];
                        standings[j + 1] = temp;
                    }
                }
            }
        }

        // Update positions after sorting
        for (int i = 0; i < clubCount; i++) {
            ((StandingImpl) standings[i]).setPosition(i + 1);
        }
    }

    @Override
    public String toString() {
        return name + " (" + year + ")";
    }
}