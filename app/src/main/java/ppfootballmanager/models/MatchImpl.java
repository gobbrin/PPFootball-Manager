package ppfootballmanager.models;

import java.io.IOException;

import com.ppstudios.footballmanager.api.contracts.event.IEvent;
import com.ppstudios.footballmanager.api.contracts.match.IMatch;
import com.ppstudios.footballmanager.api.contracts.team.IClub;
import com.ppstudios.footballmanager.api.contracts.team.ITeam;

public class MatchImpl implements IMatch {

    @Override
    public void exportToJson() throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'exportToJson'");
    }

    @Override
    public void addEvent(IEvent arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addEvent'");
    }

    @Override
    public int getEventCount() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEventCount'");
    }

    @Override
    public IEvent[] getEvents() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEvents'");
    }

    @Override
    public IClub getAwayClub() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAwayClub'");
    }

    @Override
    public ITeam getAwayTeam() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAwayTeam'");
    }

    @Override
    public IClub getHomeClub() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHomeClub'");
    }

    @Override
    public ITeam getHomeTeam() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHomeTeam'");
    }

    @Override
    public int getRound() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRound'");
    }

    @Override
    public int getTotalByEvent(Class arg0, IClub arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTotalByEvent'");
    }

    @Override
    public ITeam getWinner() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getWinner'");
    }

    @Override
    public boolean isPlayed() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isPlayed'");
    }

    @Override
    public boolean isValid() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isValid'");
    }

    @Override
    public void setPlayed() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setPlayed'");
    }

    @Override
    public void setTeam(ITeam arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setTeam'");
    }
    
}
