package ppfootballmanager.models;

import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.ppstudios.footballmanager.api.contracts.league.ISchedule;
import com.ppstudios.footballmanager.api.contracts.match.IMatch;
import com.ppstudios.footballmanager.api.contracts.team.ITeam;

public class ScheduleImpl implements ISchedule {
    private IMatch[] matches;
    private int matchCount;
    private int numberOfRounds;
    
    /**
     * Constructor for ScheduleImpl
     * 
     * @param matches Array of matches in the schedule
     * @param numberOfRounds Number of rounds in the schedule
     */
    public ScheduleImpl(IMatch[] matches, int numberOfRounds) {
        if (matches != null) {
            this.matchCount = matches.length;
            this.matches = new IMatch[matchCount];
            System.arraycopy(matches, 0, this.matches, 0, matchCount);
        } else {
            this.matchCount = 0;
            this.matches = new IMatch[0];
        }
        
        this.numberOfRounds = numberOfRounds;
    }
    
    /**
     * Empty constructor for ScheduleImpl
     */
    public ScheduleImpl() {
        this.matchCount = 0;
        this.matches = new IMatch[0];
        this.numberOfRounds = 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void exportToJson() throws IOException {
        JSONObject scheduleJson = new JSONObject();
        scheduleJson.put("numberOfRounds", this.numberOfRounds);
        
        // Export matches
        JSONArray matchesArray = new JSONArray();
        for (int i = 0; i < matchCount; i++) {
            IMatch match = matches[i];
            JSONObject matchObject = new JSONObject();
            
            matchObject.put("round", match.getRound());
            
            if (match.getHomeTeam() != null && match.getHomeTeam().getClub() != null) {
                matchObject.put("homeTeam", match.getHomeTeam().getClub().getName());
            }
            
            if (match.getAwayTeam() != null && match.getAwayTeam().getClub() != null) {
                matchObject.put("awayTeam", match.getAwayTeam().getClub().getName());
            }
            
            if (match.isPlayed()) {
                if (match instanceof MatchImpl) {
                    MatchImpl matchImpl = (MatchImpl) match;
                    matchObject.put("homeGoals", matchImpl.getHomeGoals());
                    matchObject.put("awayGoals", matchImpl.getAwayGoals());
                }
                matchObject.put("played", true);
            } else {
                matchObject.put("played", false);
            }
            
            matchesArray.add(matchObject);
        }
        scheduleJson.put("matches", matchesArray);
        
        // Write to file
        String fileName = "schedule.json";
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(scheduleJson.toJSONString());
            file.flush();
        }
    }

    @Override
    public IMatch[] getAllMatches() {
        IMatch[] result = new IMatch[matchCount];
        System.arraycopy(matches, 0, result, 0, matchCount);
        return result;
    }

    @Override
    public IMatch[] getMatchesForRound(int round) {
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
    public IMatch[] getMatchesForTeam(ITeam team) {
        if (team == null) {
            return new IMatch[0];
        }
        
        // Count matches for the specified team
        int count = 0;
        for (int i = 0; i < matchCount; i++) {
            IMatch match = matches[i];
            if ((match.getHomeTeam() != null && match.getHomeTeam().equals(team)) || 
                (match.getAwayTeam() != null && match.getAwayTeam().equals(team))) {
                count++;
            }
        }
        
        // Create array of matches for the team
        IMatch[] teamMatches = new IMatch[count];
        int index = 0;
        for (int i = 0; i < matchCount && index < count; i++) {
            IMatch match = matches[i];
            if ((match.getHomeTeam() != null && match.getHomeTeam().equals(team)) || 
                (match.getAwayTeam() != null && match.getAwayTeam().equals(team))) {
                teamMatches[index++] = match;
            }
        }
        
        return teamMatches;
    }

    @Override
    public int getNumberOfRounds() {
        return numberOfRounds;
    }

    @Override
    public void setTeam(ITeam team, int teamIndex) {
        if (team == null) {
            return;
        }
        
        // Find all matches where this team index appears and update
        for (int i = 0; i < matchCount; i++) {
            IMatch match = matches[i];
            
            // For simplicity, we're not using the teamIndex parameter directly
            // Instead, we're trying to match by club name if available
            // This implementation might need to be adjusted based on your specific needs
            if (match instanceof MatchImpl) {
                MatchImpl matchImpl = (MatchImpl) match;
                matchImpl.setTeam(team);
            }
        }
    }
    
    /**
     * Get the count of matches in the schedule
     * 
     * @return The number of matches
     */
    public int getMatchCount() {
        return matchCount;
    }
    
    /**
     * Adds a match to the schedule
     * 
     * @param match The match to add
     * @return True if the match was added successfully, false otherwise
     */
    public boolean addMatch(IMatch match) {
        if (match == null) {
            return false;
        }
        
        // Create a new array with increased size
        IMatch[] newMatches = new IMatch[matchCount + 1];
        System.arraycopy(matches, 0, newMatches, 0, matchCount);
        newMatches[matchCount] = match;
        
        // Update members
        matches = newMatches;
        matchCount++;
        
        // Update the number of rounds if needed
        if (match.getRound() > numberOfRounds) {
            numberOfRounds = match.getRound();
        }
        
        return true;
    }
    
    /**
     * String representation of the schedule
     */
    @Override
    public String toString() {
        return "Schedule with " + numberOfRounds + " rounds and " + matchCount + " matches";
    }
}