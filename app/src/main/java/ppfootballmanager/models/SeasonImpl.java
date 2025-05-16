package ppfootballmanager.models;

import java.io.IOException;

import com.ppstudios.footballmanager.api.contracts.league.ISchedule;
import com.ppstudios.footballmanager.api.contracts.league.ISeason;
import com.ppstudios.footballmanager.api.contracts.league.IStanding;
import com.ppstudios.footballmanager.api.contracts.match.IMatch;
import com.ppstudios.footballmanager.api.contracts.simulation.MatchSimulatorStrategy;
import com.ppstudios.footballmanager.api.contracts.team.IClub;

public class SeasonImpl implements ISeason {

    @Override
    public void exportToJson() throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'exportToJson'");
    }

    @Override
    public boolean addClub(IClub arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addClub'");
    }

    @Override
    public String displayMatchResult(IMatch arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'displayMatchResult'");
    }

    @Override
    public void generateSchedule() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generateSchedule'");
    }

    @Override
    public IClub[] getCurrentClubs() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCurrentClubs'");
    }

    @Override
    public int getCurrentMatches() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCurrentMatches'");
    }

    @Override
    public int getCurrentRound() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCurrentRound'");
    }

    @Override
    public IStanding[] getLeagueStandings() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLeagueStandings'");
    }

    @Override
    public IMatch[] getMatches() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMatches'");
    }

    @Override
    public IMatch[] getMatches(int arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMatches'");
    }

    @Override
    public int getMaxRounds() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMaxRounds'");
    }

    @Override
    public int getMaxTeams() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMaxTeams'");
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }

    @Override
    public int getNumberOfCurrentTeams() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNumberOfCurrentTeams'");
    }

    @Override
    public int getPointsPerDraw() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPointsPerDraw'");
    }

    @Override
    public int getPointsPerLoss() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPointsPerLoss'");
    }

    @Override
    public int getPointsPerWin() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPointsPerWin'");
    }

    @Override
    public ISchedule getSchedule() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSchedule'");
    }

    @Override
    public int getYear() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getYear'");
    }

    @Override
    public boolean isSeasonComplete() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isSeasonComplete'");
    }

    @Override
    public boolean removeClub(IClub arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeClub'");
    }

    @Override
    public void resetSeason() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'resetSeason'");
    }

    @Override
    public void setMatchSimulator(MatchSimulatorStrategy arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setMatchSimulator'");
    }

    @Override
    public void simulateRound() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'simulateRound'");
    }

    @Override
    public void simulateSeason() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'simulateSeason'");
    }
    
}
