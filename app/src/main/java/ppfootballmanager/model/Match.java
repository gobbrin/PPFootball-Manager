package ppfootballmanager.model;

public class Match {
    private Team homeTeam;
    private Team awayTeam;
    private int homeGoals;
    private int awayGoals;
    private boolean played;

    public Match(Team home, Team away) {
        this.homeTeam = home;
        this.awayTeam = away;
        this.homeGoals = 0;
        this.awayGoals = 0;
        this.played = false;
    }

    // Getters
    public Team getHomeTeam() {
        return homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public int getHomeGoals() {
        return homeGoals;
    }

    public int getAwayGoals() {
        return awayGoals;
    }

    public boolean isPlayed() {
        return played;
    }

    // Set resultado (para ser usado por simulador)
    public void setResult(int homeGoals, int awayGoals) {
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
        this.played = true;
    }

    @Override
    public String toString() {
        if (played) {
            return homeTeam.getName() + " " + homeGoals + " - " + awayGoals + " " + awayTeam.getName();
        } else {
            return homeTeam.getName() + " vs " + awayTeam.getName() + " (not played)";
        }
    }
}
