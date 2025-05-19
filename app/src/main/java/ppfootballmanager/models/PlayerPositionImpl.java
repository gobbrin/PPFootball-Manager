package ppfootballmanager.models;

import com.ppstudios.footballmanager.api.contracts.player.IPlayerPosition;

public class PlayerPositionImpl implements IPlayerPosition {
    private String position;
    
    /**
     * TODO: Develop a more sophisticated way to handle player positions´
     * For now, we will just use a string to represent the position
     * This is a placeholder for the actual implementation
     * @param position
     */

    public PlayerPositionImpl(String position) {
        this.position = position;
    }

    @Override
    public String getDescription() {
        return position;
    }

}
