package ppfootballmanager.model.Player;

import com.ppstudios.footballmanager.api.contracts.player.IPlayerPosition;

public class MyPlayerPosition implements IPlayerPosition {

    private String description;

    public MyPlayerPosition(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
