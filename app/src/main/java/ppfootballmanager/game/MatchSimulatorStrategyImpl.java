package ppfootballmanager.game;

import java.util.Random;

import com.ppstudios.footballmanager.api.contracts.event.IEvent;
import com.ppstudios.footballmanager.api.contracts.match.IMatch;
import com.ppstudios.footballmanager.api.contracts.player.IPlayer;
import com.ppstudios.footballmanager.api.contracts.simulation.MatchSimulatorStrategy;
import com.ppstudios.footballmanager.api.contracts.team.ITeam;

import ppfootballmanager.clubs.PlayerImpl;
import ppfootballmanager.events.EventImpl;
import ppfootballmanager.events.EventType;
import ppfootballmanager.events.GoalEventImpl;
import ppfootballmanager.events.BaseEvent;

/**
 * Implementation of the match simulator strategy
 */
public class MatchSimulatorStrategyImpl implements MatchSimulatorStrategy {
    
    private static final Random random = new Random();
    
    /**
     * Simulates a match between two teams.
     */
    @Override
    public void simulate(IMatch match) {
        if (match == null || match.isPlayed() || !(match instanceof MatchImpl)) {
            return;
        }
        
        MatchImpl matchImpl = (MatchImpl) match;
        ITeam homeTeam = match.getHomeTeam();
        ITeam awayTeam = match.getAwayTeam();
        
        if (homeTeam == null || awayTeam == null) {
            return;
        }
        
        // Calculate team strengths based on player ratings
        int homeStrength = calculateTeamStrength(homeTeam);
        int awayStrength = calculateTeamStrength(awayTeam);
        
        // Add home advantage
        homeStrength += 100;
        
        // Simulate goals based on team strengths
        int homeGoals = simulateGoals(homeStrength);
        int awayGoals = simulateGoals(awayStrength);
        
        // Set the score
        matchImpl.setScore(homeGoals, awayGoals);
        
        // Generate events for the goals
        generateGoalEvents(matchImpl, homeTeam, homeGoals, awayTeam, awayGoals);
        
        // Mark the match as played
        matchImpl.setPlayed();
    }
    
    /**
     * Calculate a team's strength based on player ratings
     */
    /**
 * Calculate a team's strength based on player ratings
 */
private int calculateTeamStrength(ITeam team) {
    int totalRating = 0;
    int playerCount = 0;
    
    IPlayer[] players = team.getPlayers();
    for (IPlayer player : players) {
        if (player != null) {
            // Check if we're using our implementation
            if (player instanceof PlayerImpl) {
                totalRating += ((PlayerImpl) player).getOverallRating();
            } else {
                // Fallback for players that aren't our implementation
                // Calculate a basic rating from available stats
                int rating = (player.getShooting() + player.getPassing() + 
                             player.getSpeed() + player.getStamina()) / 4;
                totalRating += rating;
            }
            playerCount++;
        }
    }
    
    return playerCount > 0 ? totalRating / playerCount : 70; // Default to 70 if no players
}
    
    /**
     * Simulate the number of goals based on team strength
     */
    private int simulateGoals(int teamStrength) {
        // Base goal probability
        double goalProbability = teamStrength / 1000.0;
        
        // Simulate goals for 90 minutes
        int goals = 0;
        for (int minute = 1; minute <= 90; minute++) {
            if (random.nextDouble() < goalProbability) {
                goals++;
            }
        }
        
        return goals;
    }
    
    /**
     * Generate goal events for the match
     */
    private void generateGoalEvents(MatchImpl match, ITeam homeTeam, int homeGoals, ITeam awayTeam, int awayGoals) {
        // Generate home team goals
        for (int i = 0; i < homeGoals; i++) {
            int minute = random.nextInt(90) + 1;
            IPlayer scorer = getRandomPlayer(homeTeam);
            
            if (scorer != null) {
                // Determine goal type
                GoalEventImpl.GoalType goalType = randomGoalType();
                
                // Create goal event
                IEvent goalEvent = new GoalEventImpl(minute, scorer, homeTeam, goalType);
                match.addEvent(goalEvent);
            }
        }
        
        // Generate away team goals
        for (int i = 0; i < awayGoals; i++) {
            int minute = random.nextInt(90) + 1;
            IPlayer scorer = getRandomPlayer(awayTeam);
            
            if (scorer != null) {
                // Determine goal type
                GoalEventImpl.GoalType goalType = randomGoalType();
                
                // Create goal event
                IEvent goalEvent = new GoalEventImpl(minute, scorer, awayTeam, goalType);
                match.addEvent(goalEvent);
            }
        }
        
        // Generate some other events like cards, substitutions
        generateOtherEvents(match, homeTeam, awayTeam);
    }
    
    /**
     * Get a random player from a team
     */
    private IPlayer getRandomPlayer(ITeam team) {
        IPlayer[] players = team.getPlayers();
        if (players == null || players.length == 0) {
            return null;
        }
        
        // Try to get a valid player
        for (int attempts = 0; attempts < 10; attempts++) {
            int index = random.nextInt(players.length);
            if (players[index] != null) {
                return players[index];
            }
        }
        
        return null;
    }
    
    /**
 * Generate random non-goal events
 */
private void generateOtherEvents(MatchImpl match, ITeam homeTeam, ITeam awayTeam) {
    // Generate yellow cards
    int yellowCards = random.nextInt(6);
    for (int i = 0; i < yellowCards; i++) {
        int minute = random.nextInt(90) + 1;
        ITeam team = random.nextBoolean() ? homeTeam : awayTeam;
        IPlayer player = getRandomPlayer(team);
        
        if (player != null) {
            // Use BaseEventImpl instead of EventImpl
            IEvent cardEvent = new BaseEvent(EventType.YELLOW_CARD, minute, player, team);
            match.addEvent(cardEvent);
        }
    }
    
    // Generate red cards (fewer)
    if (random.nextInt(5) == 0) { // 20% chance
        int minute = random.nextInt(90) + 1;
        ITeam team = random.nextBoolean() ? homeTeam : awayTeam;
        IPlayer player = getRandomPlayer(team);
        
        if (player != null) {
            // Use BaseEventImpl instead of EventImpl
            IEvent cardEvent = new BaseEvent(EventType.RED_CARD, minute, player, team);
            match.addEvent(cardEvent);
        }
    }
}
    
    /**
     * Get a random goal type
     */
    private GoalEventImpl.GoalType randomGoalType() {
        GoalEventImpl.GoalType[] types = GoalEventImpl.GoalType.values();
        int randomIndex = random.nextInt(types.length);
        return types[randomIndex];
    }
}