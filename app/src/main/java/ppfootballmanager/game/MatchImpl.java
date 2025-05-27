package ppfootballmanager.game;

import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.ppstudios.footballmanager.api.contracts.event.IEvent;
import com.ppstudios.footballmanager.api.contracts.match.IMatch;
import com.ppstudios.footballmanager.api.contracts.team.IClub;
import com.ppstudios.footballmanager.api.contracts.team.ITeam;

import ppfootballmanager.clubs.TeamImpl;
import ppfootballmanager.events.BaseEvent;
import ppfootballmanager.events.EventImpl;
import ppfootballmanager.events.EventType;
import ppfootballmanager.events.GoalEventImpl;

public class MatchImpl implements IMatch {
    private int round;
    private ITeam homeTeam;
    private ITeam awayTeam;
    private IEvent[] events;
    private int eventCount;
    private boolean played;
    private int homeGoals;
    private int awayGoals;

    private static final int MAX_EVENTS = 100;

    /**
     * Constructor for MatchImpl with teams
     * 
     * @param round    Match round number
     * @param homeTeam Home team
     * @param awayTeam Away team
     */
    public MatchImpl(int round, ITeam homeTeam, ITeam awayTeam) {
        this.round = round;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.events = new IEvent[MAX_EVENTS];
        this.eventCount = 0;
        this.played = false;
        this.homeGoals = 0;
        this.awayGoals = 0;
    }

    /**
     * Constructor for MatchImpl with clubs (creates teams from clubs)
     * 
     * @param round    Match round number
     * @param homeClub Home club
     * @param awayClub Away club
     */
    public MatchImpl(int round, IClub homeClub, IClub awayClub) {
        this(round, new TeamImpl(homeClub), new TeamImpl(awayClub));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void exportToJson() throws IOException {
        JSONArray eventsArray = new JSONArray();
        for (int i = 0; i < eventCount; i++) {
            JSONObject eventObject = new JSONObject();
            IEvent event = events[i];

            // Add common properties from IEvent interface
            eventObject.put("minute", event.getMinute());
            eventObject.put("description", event.getDescription());

            // Handle our event implementations
            if (event instanceof EventImpl) {
                EventImpl ourEvent = (EventImpl) event;
                eventObject.put("type", ourEvent.getType().toString());

                // Add team information if available
                if (ourEvent.getTeam() != null && ourEvent.getTeam().getClub() != null) {
                    eventObject.put("team", ourEvent.getTeam().getClub().getName());
                }

                // Generic approach for player information using reflection
                try {
                    // Try to get player using reflection - works for any class with getPlayer()
                    java.lang.reflect.Method getPlayerMethod = event.getClass().getMethod("getPlayer");
                    Object player = getPlayerMethod.invoke(event);

                    if (player != null
                            && player instanceof com.ppstudios.footballmanager.api.contracts.player.IPlayer) {
                        com.ppstudios.footballmanager.api.contracts.player.IPlayer iPlayer = (com.ppstudios.footballmanager.api.contracts.player.IPlayer) player;
                        eventObject.put("player", iPlayer.getName());
                        eventObject.put("playerNumber", iPlayer.getNumber());
                    }
                } catch (Exception e) {
                    // No getPlayer method or it failed - that's fine, just skip player info
                }

                // Try to add additional goal information for goal events
                if (ourEvent.getType() == EventType.GOAL && event instanceof GoalEventImpl) {
                    GoalEventImpl goalEvent = (GoalEventImpl) event;

                    try {
                        // Try to get assist information if available
                        java.lang.reflect.Method getAssistMethod = goalEvent.getClass().getMethod("getAssist");
                        Object assist = getAssistMethod.invoke(goalEvent);

                        if (assist != null
                                && assist instanceof com.ppstudios.footballmanager.api.contracts.player.IPlayer) {
                            com.ppstudios.footballmanager.api.contracts.player.IPlayer assistPlayer = (com.ppstudios.footballmanager.api.contracts.player.IPlayer) assist;
                            eventObject.put("assist", assistPlayer.getName());
                        }
                    } catch (Exception e) {
                        // No getAssist method or it failed - that's fine
                    }
                }
            } else {
                // For non-EventImpl events, just use what we have from the interface
                eventObject.put("type", "UNKNOWN");
            }

            eventsArray.add(eventObject);
        }
    }

    @Override
    public void addEvent(IEvent event) {
        if (event == null || eventCount >= MAX_EVENTS) {
            return;
        }

        // Add the event
        events[eventCount++] = event;

        // Update goals if this is a goal event - check our implementation
        if (event instanceof EventImpl) {
            EventImpl ourEvent = (EventImpl) event;
            if (ourEvent.getType() == EventType.GOAL) {
                if (ourEvent.getTeam() == homeTeam) {
                    homeGoals++;
                } else if (ourEvent.getTeam() == awayTeam) {
                    awayGoals++;
                }
            }
        }
    }

    @Override
    public int getEventCount() {
        return eventCount;
    }

    @Override
    public IEvent[] getEvents() {
        IEvent[] result = new IEvent[eventCount];
        System.arraycopy(events, 0, result, 0, eventCount);
        return result;
    }

    @Override
    public IClub getAwayClub() {
        return awayTeam != null ? awayTeam.getClub() : null;
    }

    @Override
    public ITeam getAwayTeam() {
        return awayTeam;
    }

    @Override
    public IClub getHomeClub() {
        return homeTeam != null ? homeTeam.getClub() : null;
    }

    @Override
    public ITeam getHomeTeam() {
        return homeTeam;
    }

    @Override
    public int getRound() {
        return round;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public int getTotalByEvent(Class eventClass, IClub club) {
        if (eventClass == null || club == null) {
            return 0;
        }

        int total = 0;
        for (int i = 0; i < eventCount; i++) {
            IEvent event = events[i];
            if (eventClass.isInstance(event)) {
                // We need to check if this is our implementation
                if (event instanceof EventImpl) {
                    EventImpl ourEvent = (EventImpl) event;
                    if (ourEvent.getTeam() != null &&
                            ourEvent.getTeam().getClub() != null &&
                            ourEvent.getTeam().getClub().getName().equals(club.getName())) {
                        total++;
                    }
                }
            }
        }

        return total;
    }

    @Override
    public ITeam getWinner() {
        if (!played) {
            return null;
        }

        if (homeGoals > awayGoals) {
            return homeTeam;
        } else if (awayGoals > homeGoals) {
            return awayTeam;
        } else {
            return null; // Draw
        }
    }

    @Override
    public boolean isPlayed() {
        return played;
    }

    @Override
    public boolean isValid() {
        return homeTeam != null && homeTeam.getClub() != null &&
                awayTeam != null && awayTeam.getClub() != null &&
                !homeTeam.getClub().getName().equals(awayTeam.getClub().getName());
    }

    @Override
    public void setPlayed() {
        this.played = true;
    }

    @Override
    public void setTeam(ITeam team) {
        if (team == null) {
            return;
        }

        // Determine if this is a home or away team update
        if (homeTeam == null) {
            homeTeam = team;
        } else if (awayTeam == null) {
            awayTeam = team;
        } else {
            // Both teams already set, update based on club name matching
            IClub club = team.getClub();
            if (club != null) {
                if (homeTeam.getClub() != null &&
                        homeTeam.getClub().getName().equals(club.getName())) {
                    homeTeam = team;
                } else if (awayTeam.getClub() != null &&
                        awayTeam.getClub().getName().equals(club.getName())) {
                    awayTeam = team;
                }
            }
        }
    }

    /**
     * Resets the match state to unplayed
     */
    public void reset() {
        played = false;
        homeGoals = 0;
        awayGoals = 0;

        // Clear events
        for (int i = 0; i < eventCount; i++) {
            events[i] = null;
        }
        eventCount = 0;
    }

    /**
     * Gets number of goals scored by the home team
     * 
     * @return Home team goals
     */
    public int getHomeGoals() {
        return homeGoals;
    }

    /**
     * Gets number of goals scored by the away team
     * 
     * @return Away team goals
     */
    public int getAwayGoals() {
        return awayGoals;
    }

    /**
     * Sets the score for this match (used during simulation)
     * 
     * @param homeGoals Home team goals
     * @param awayGoals Away team goals
     */
    public void setScore(int homeGoals, int awayGoals) {
        this.homeGoals = Math.max(0, homeGoals);
        this.awayGoals = Math.max(0, awayGoals);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Round ").append(round).append(": ");

        if (homeTeam != null && homeTeam.getClub() != null) {
            sb.append(homeTeam.getClub().getName());
        } else {
            sb.append("Unknown Home");
        }

        if (played) {
            sb.append(" ").append(homeGoals).append(" - ").append(awayGoals).append(" ");
        } else {
            sb.append(" vs ");
        }

        if (awayTeam != null && awayTeam.getClub() != null) {
            sb.append(awayTeam.getClub().getName());
        } else {
            sb.append("Unknown Away");
        }

        return sb.toString();
    }
}