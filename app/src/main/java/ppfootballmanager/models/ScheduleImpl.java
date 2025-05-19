package ppfootballmanager.models;

import java.io.IOException;

import com.ppstudios.footballmanager.api.contracts.league.ISchedule;
import com.ppstudios.footballmanager.api.contracts.match.IMatch;
import com.ppstudios.footballmanager.api.contracts.team.ITeam;

public class ScheduleImpl implements ISchedule{

    @Override
    public void exportToJson() throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'exportToJson'");
    }

    @Override
    public IMatch[] getAllMatches() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllMatches'");
    }

    @Override
    public IMatch[] getMatchesForRound(int arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMatchesForRound'");
    }

    @Override
    public IMatch[] getMatchesForTeam(ITeam arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMatchesForTeam'");
    }

    @Override
    public int getNumberOfRounds() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNumberOfRounds'");
    }

    @Override
    public void setTeam(ITeam arg0, int arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setTeam'");
    }
    
}
