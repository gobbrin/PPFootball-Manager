package ppfootballmanager.models;

import com.ppstudios.footballmanager.api.contracts.league.IStanding;
import com.ppstudios.footballmanager.api.contracts.team.IClub;
import com.ppstudios.footballmanager.api.contracts.team.ITeam;

/**
 * Implementation of IStanding interface to track team standings in a league
 */
public class StandingImpl implements IStanding {
    private IClub club;
    private int position;
    private int played;
    private int won;
    private int drawn;
    private int lost;
    private int goalsFor;
    private int goalsAgainst;
    private int points;
    
    /**
     * Constructor for StandingImpl with club
     * 
     * @param club The club this standing belongs to
     */
    public StandingImpl(IClub club) {
        this.club = club;
        this.position = 0;
        this.played = 0;
        this.won = 0;
        this.drawn = 0;
        this.lost = 0;
        this.goalsFor = 0;
        this.goalsAgainst = 0;
        this.points = 0;
    }

    @Override
    public void addDraw(int pointsForDraw) {
        this.drawn++;
        this.played++;
        this.points += pointsForDraw;
    }

    @Override
    public void addLoss(int pointsForLoss) {
        this.lost++;
        this.played++;
        this.points += pointsForLoss; // Usually 0
    }

    @Override
    public void addPoints(int additionalPoints) {
        this.points += additionalPoints;
    }

    @Override
    public void addWin(int pointsForWin) {
        this.won++;
        this.played++;
        this.points += pointsForWin;
    }

    @Override
    public int getDraws() {
        return drawn;
    }

    // This is mapped to match the interface method with a different name
    public int getDrawn() {
        return drawn;
    }

    @Override
    public int getGoalDifference() {
        return goalsFor - goalsAgainst;
    }

    @Override
    public int getGoalScored() {
        return goalsFor;
    }

    // This is mapped to match the interface method with a different name
    public int getGoalsFor() {
        return goalsFor;
    }

    @Override
    public int getGoalsConceded() {
        return goalsAgainst;
    }

    // This is mapped to match the interface method with a different name
    public int getGoalsAgainst() {
        return goalsAgainst;
    }

    @Override
    public int getLosses() {
        return lost;
    }

    // This is mapped to match the interface method with a different name
    public int getLost() {
        return lost;
    }

    public int getPlayed() {
        return played;
    }

    @Override
    public int getPoints() {
        return points;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public ITeam getTeam() {
        // Convert IClub to ITeam - if needed by the interface
        return new TeamImpl(club);
    }

    public IClub getClub() {
        return club;
    }

    @Override
    public int getTotalMatches() {
        return played;
    }

    @Override
    public int getWins() {
        return won;
    }

    // This is mapped to match the interface method with a different name
    public int getWon() {
        return won;
    }

    /**
     * Updates the position of this team in the standings
     * 
     * @param position The new position
     */
    public void setPosition(int position) {
        this.position = position;
    }
    
    /**
     * Updates the standing with match result
     * 
     * @param goalsScored Goals scored by this team
     * @param goalsConceded Goals conceded by this team
     * @param isHomeTeam True if this was the home team
     */
    public void updateWithMatch(int goalsScored, int goalsConceded, boolean isHomeTeam) {
        // Update goals
        this.goalsFor += goalsScored;
        this.goalsAgainst += goalsConceded;
        
        // Update match result
        if (goalsScored > goalsConceded) {
            // Win
            this.won++;
            this.points += 3; // Standard points for a win
        } else if (goalsScored == goalsConceded) {
            // Draw
            this.drawn++;
            this.points += 1; // Standard point for a draw
        } else {
            // Loss
            this.lost++;
            // No points for a loss
        }
        
        // Increment played games
        this.played++;
    }
    
    @Override
    public String toString() {
        return String.format("%2d. %-20s %3d %2d %2d %2d %2d:%2d %3d", 
                position, 
                club.getName(), 
                played, 
                won, 
                drawn, 
                lost, 
                goalsFor, 
                goalsAgainst, 
                points);
    }
}