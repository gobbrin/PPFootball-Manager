package ppfootballmanager.models;

import com.ppstudios.footballmanager.api.contracts.player.IPlayer;
import com.ppstudios.footballmanager.api.contracts.player.IPlayerPosition;
import com.ppstudios.footballmanager.api.contracts.team.IClub;
import com.ppstudios.footballmanager.api.contracts.team.IPlayerSelector;

// Interface for selecting a player based on the club and position. This interface follows the Dependency Inversion Principle by allowing different implementations of player selection strategies.
public class PlayerSelectorImpl implements IPlayerSelector {

    @Override
    public IPlayer selectPlayer(IClub club, IPlayerPosition position) {
        // Validate parameters
        if (club == null) {
            throw new IllegalArgumentException("Club cannot be null");
        }
        if (position == null) {
            throw new IllegalArgumentException("Position cannot be null");
        }
        
        // Get all players from the club
        IPlayer[] players = club.getPlayers();
        
        if (players.length == 0) {
            throw new IllegalStateException("The team is empty");
        }
        
        // Find the first player that matches the requested position
        for (IPlayer player : players) {
            if (player.getPosition().getDescription().equalsIgnoreCase(position.getDescription())) {
                return player;
            }
        }
        
        // No player found for that position
        throw new IllegalStateException("No player found for position: " + position.getDescription());
    }
}