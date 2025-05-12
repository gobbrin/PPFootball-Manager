package ppfootballmanager.model;

import java.util.ArrayList;
import java.util.List;

public class League {
    private String name;
    private List<Team> teams;
    private List<Match> schedule;

    public League(String name) {
        this.name = name;
        this.teams = new ArrayList<>();
        this.schedule = new ArrayList<>();
    }

    // Getters
    public String getName() {
        return name;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public List<Match> getSchedule() {
        return schedule;
    }

    // Adicionar equipa
    public void addTeam(Team team) {
        teams.add(team);
    }

    // Adicionar jogo (caso faças manualmente algum)
    public void addMatch(Match match) {
        schedule.add(match);
    }

    // Gera o calendário automaticamente (jogos ida e volta)
    public void generateSchedule() {
        schedule.clear();
        for (int i = 0; i < teams.size(); i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                Team home = teams.get(i);
                Team away = teams.get(j);
                schedule.add(new Match(home, away)); // ida
                schedule.add(new Match(away, home)); // volta
            }
        }
    }

    // Obter todos os jogos de uma jornada (por blocos de N/2 jogos)
    public List<Match> getMatchday(int day) {
        int matchesPerRound = teams.size() / 2;
        int start = (day - 1) * matchesPerRound;
        int end = Math.min(start + matchesPerRound, schedule.size());

        if (start >= schedule.size()) return new ArrayList<>();

        return schedule.subList(start, end);
    }

    @Override
    public String toString() {
        return "League: " + name + " with " + teams.size() + " teams and " + schedule.size() + " matches.";
    }
}
