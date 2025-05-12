package ppfootballmanager.model;

import com.ppstudios.footballmanager.api.contracts.player.IPlayer;

import java.util.ArrayList;
import java.util.List;

public class Team {

    private String name;
    private List<IPlayer> players;

    public Team(String name) {
        this.name = name;
        this.players = new ArrayList<>();
    }

    // Getters
    public String getName() {
        return name;
    }

    public List<IPlayer> getPlayers() {
        return players;
    }

    // Adiciona jogador à equipa
    public void addPlayer(IPlayer player) {
        this.players.add(player);
    }

    // Remove jogador (se necessário)
    public boolean removePlayer(IPlayer player) {
        return players.remove(player);
    }

    // Encontra jogador por nome
    public IPlayer getPlayerByName(String playerName) {
        for (IPlayer p : players) {
            if (p.getName().equalsIgnoreCase(playerName)) {
                return p;
            }
        }
        return null;
    }

    // Filtra jogadores por posição
    public List<IPlayer> getPlayersByPosition(String position) {
        List<IPlayer> filtered = new ArrayList<>();
        for (IPlayer p : players) {
            if (p.getPosition().getDescription().equalsIgnoreCase(position)) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    @Override
    public String toString() {
        return "Team: " + name + " (" + players.size() + " players)";
    }
}
