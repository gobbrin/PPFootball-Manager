package ppfootballmanager.clubs;

import com.ppstudios.footballmanager.api.contracts.player.IPlayer;
import com.ppstudios.footballmanager.api.contracts.player.IPlayerPosition;
import com.ppstudios.footballmanager.api.contracts.team.IClub;
import com.ppstudios.footballmanager.api.contracts.team.IPlayerSelector;

// Interface for selecting a player based on the club and position. This interface follows the Dependency Inversion Principle by allowing different implementations of player selection strategies.
public class PlayerSelectorImpl implements IPlayerSelector {

    @Override
    public IPlayer selectPlayer(IClub club, IPlayerPosition position) {
        if (club == null || position == null) {
            throw new IllegalArgumentException("Club and position cannot be null");
        }

        IPlayer[] players = club.getPlayers();
        if (players.length == 0) {
            throw new IllegalStateException("The club has no players");
        }

        String targetPositionCode = position instanceof PlayerPositionImpl ? ((PlayerPositionImpl) position).getCode()
                : position.getDescription();

        // Find players matching the position
        for (IPlayer player : players) {
            if (player != null && player.getPosition() != null) {
                String playerPositionCode = player.getPosition() instanceof PlayerPositionImpl
                        ? ((PlayerPositionImpl) player.getPosition()).getCode()
                        : player.getPosition().getDescription();

                if (playerPositionCode.equals(targetPositionCode)) {
                    return player; // Return the first matching player
                }
            }
        }

        // No exact match, try to find a player with a compatible position
        for (IPlayer player : players) {
            if (player != null && player.getPosition() != null) {
                // For now just return any player rather than null
                return player;
            }
        }

        return null; // No player found
    }
}