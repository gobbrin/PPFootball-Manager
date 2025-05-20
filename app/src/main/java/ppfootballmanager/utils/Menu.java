package ppfootballmanager.utils;

import java.util.Scanner;
import java.util.NoSuchElementException;
import java.util.Random;

import com.ppstudios.footballmanager.api.contracts.league.ILeague;
import com.ppstudios.footballmanager.api.contracts.league.ISeason;
import com.ppstudios.footballmanager.api.contracts.league.IStanding;
import com.ppstudios.footballmanager.api.contracts.team.IClub;
import com.ppstudios.footballmanager.api.contracts.team.ITeam;
import com.ppstudios.footballmanager.api.contracts.player.IPlayer;
import com.ppstudios.footballmanager.api.contracts.player.IPlayerPosition;

import ppfootballmanager.models.LeagueImpl;
import ppfootballmanager.models.StandingImpl;
import ppfootballmanager.models.TeamImpl;
import ppfootballmanager.models.ClubImpl;
import ppfootballmanager.models.PlayerImpl;
import ppfootballmanager.models.PlayerPositionImpl;
import ppfootballmanager.models.SeasonImpl;
import ppfootballmanager.services.MatchSimulatorStrategyImpl;

/**
 * Handles the user interaction with the game
 */
public class Menu {
    private Scanner scanner;
    private ILeague currentLeague;
    private ISeason currentSeason;
    private ITeam userTeam;

    public Menu() {
        scanner = new Scanner(System.in);
    }

    /**
     * Display the main menu and process user selection
     */
    public void showMainMenu() {
        boolean exit = false;

        while (!exit) {
            System.out.println("\n===== PP FOOTBALL MANAGER =====");
            System.out.println("1. Start New Game");
            System.out.println("2. Load Game");
            System.out.println("3. Exit");
            System.out.print("Select an option: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    startNewGame();
                    break;
                case 2:
                    loadGame();
                    break;
                case 3:
                    exit = true;
                    System.out.println("Thank you for playing PP Football Manager!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        scanner.close();
    }

    /**
     * Start a new game by creating a league and selecting a team
     */
    private void startNewGame() {
        System.out.println("\n===== START NEW GAME =====");

        // Create or load a league
        System.out.print("Enter league name: ");
        String leagueName = scanner.nextLine();
        currentLeague = new LeagueImpl(leagueName);

        // Load sample teams from sample creation method
        createSampleTeams();

        // Select team to manage
        selectTeam();

        // Create a season
        System.out.print("Enter season year: ");
        int year = getIntInput();
        String seasonName = year + "-" + (year + 1);

        // Create a new season with the teams from the league
        ITeam[] teams = ((LeagueImpl) currentLeague).getTeams();
        currentSeason = new SeasonImpl(year, seasonName, teams);

        if (currentSeason == null) {
            System.out.println("Failed to create season. Returning to main menu.");
            return;
        }

        // Set match simulator
        currentSeason.setMatchSimulator(new MatchSimulatorStrategyImpl());

        // Generate the schedule
        currentSeason.generateSchedule();

        // Enter game loop
        gameLoop();
    }

    /**
     * Load a previously saved game
     */
    private void loadGame() {
        System.out.println("\n===== LOAD GAME =====");
        System.out.println("Loading not implemented yet. Returning to main menu.");
        // Future enhancement: Implement game loading functionality
    }

    /**
     * Let the user select a team to manage
     */
    private void selectTeam() {
        System.out.println("\n===== SELECT YOUR TEAM =====");

        ITeam[] teams = ((LeagueImpl) currentLeague).getTeams();
        if (teams == null || teams.length == 0) {
            System.out.println("No teams available. Please create teams first.");
            return;
        }

        for (int i = 0; i < teams.length; i++) {
            if (teams[i] != null && teams[i].getClub() != null) {
                System.out.println((i + 1) + ". " + teams[i].getClub().getName());
            }
        }

        System.out.print("Select a team to manage: ");
        int choice = getIntInput();

        if (choice < 1 || choice > teams.length || teams[choice - 1] == null) {
            System.out.println("Invalid choice. Please try again.");
            selectTeam();
            return;
        }

        userTeam = teams[choice - 1];
        System.out.println("You are now managing " + userTeam.getClub().getName() + "!");
    }

    /**
     * Main game loop for playing through a season
     */
    private void gameLoop() {
        boolean seasonComplete = false;

        while (!seasonComplete) {
            System.out.println("\n===== " + currentSeason.getName() + " =====");
            System.out.println("Team: " + userTeam.getClub().getName());
            System.out.println("Current Round: " + (currentSeason.getCurrentRound() + 1));

            showGameMenu();

            // Check if season is complete
            seasonComplete = currentSeason.isSeasonComplete();

            if (seasonComplete) {
                showSeasonSummary();
            }
        }
    }

    /**
     * Show the in-game menu during season play
     */
    private void showGameMenu() {
        System.out.println("\n===== GAME MENU =====");
        System.out.println("1. View Team");
        System.out.println("2. View Fixtures");
        System.out.println("3. View League Table");
        System.out.println("4. Play Next Match");
        System.out.println("5. Simulate to Next Match");
        System.out.println("6. Simulate Rest of Season");
        System.out.println("7. Exit to Main Menu");
        System.out.print("Select an option: ");

        int choice = getIntInput();

        switch (choice) {
            case 1:
                viewTeam();
                break;
            case 2:
                viewFixtures();
                break;
            case 3:
                viewLeagueTable();
                break;
            case 4:
                playNextMatch();
                break;
            case 5:
                simulateToNextMatch();
                break;
            case 6:
                simulateRestOfSeason();
                break;
            case 7:
                return; // Exit to main menu
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    /**
     * View details about the user's team
     */
    private void viewTeam() {
        System.out.println("\n===== TEAM: " + userTeam.getClub().getName() + " =====");

        // Display formation if available
        if (userTeam.getFormation() != null) {
            System.out.println("Formation: " + userTeam.getFormation().toString());
        }

        // Display players
        System.out.println("\nPlayers:");
        System.out.println("--------------------------------------------");
        System.out.printf("%-3s %-20s %-10s %-5s\n", "No.", "Name", "Position", "Rating");
        System.out.println("--------------------------------------------");

        // Get all players in the team
        var players = userTeam.getPlayers();
        for (var player : players) {
            if (player != null) {
                int rating = 0;
                String positionName = "Unknown";

                // Check if we can access position
                if (player.getPosition() != null) {
                    positionName = player.getPosition().toString();
                }

                // Calculate rating (simple average of available attributes)
                rating = (player.getShooting() + player.getPassing() +
                        player.getSpeed() + player.getStamina()) / 4;

                System.out.printf("%-3d %-20s %-10s %-5d\n",
                        player.getNumber(),
                        player.getName(),
                        positionName,
                        rating);
            }
        }
    }

    /**
     * View upcoming fixtures
     */
    private void viewFixtures() {
        System.out.println("\n===== FIXTURES =====");

        var matches = currentSeason.getMatches();
        System.out.println("--------------------------------------------");
        System.out.printf("%-5s %-20s %-5s %-20s\n", "Round", "Home", "vs", "Away");
        System.out.println("--------------------------------------------");

        for (var match : matches) {
            if (match != null) {
                // Highlight user's team matches
                boolean isUserMatch = match.getHomeTeam().getClub().getName().equals(userTeam.getClub().getName()) ||
                        match.getAwayTeam().getClub().getName().equals(userTeam.getClub().getName());

                if (isUserMatch) {
                    System.out.print("* ");
                } else {
                    System.out.print("  ");
                }

                System.out.printf("%-5d %-20s %-5s %-20s %s\n",
                        match.getRound(),
                        match.getHomeTeam().getClub().getName(),
                        "vs",
                        match.getAwayTeam().getClub().getName(),
                        match.isPlayed() ? currentSeason.displayMatchResult(match) : "");
            }
        }
    }

    /**
     * View the current league table
     */
    private void viewLeagueTable() {
        System.out.println("\n===== LEAGUE TABLE =====");

        var standings = currentSeason.getLeagueStandings();
        System.out.println("--------------------------------------------------------------------");
        System.out.printf("%-3s %-20s %-3s %-3s %-3s %-3s %-7s %-5s\n",
                "Pos", "Team", "P", "W", "D", "L", "GF-GA", "Pts");
        System.out.println("--------------------------------------------------------------------");

        for (var standing : standings) {
            if (standing != null) {
                int position = 0;
                String teamName = "";
                int played = 0, won = 0, drawn = 0, lost = 0;
                int goalsFor = 0, goalsAgainst = 0;

                // Access our implementation when possible
                if (standing instanceof StandingImpl) {
                    StandingImpl standingImpl = (StandingImpl) standing;
                    position = standingImpl.getPosition();
                    teamName = standingImpl.getClub().getName();
                    played = standingImpl.getPlayed();
                    won = standingImpl.getWon();
                    drawn = standingImpl.getDrawn();
                    lost = standingImpl.getLost();
                    goalsFor = standingImpl.getGoalsFor();
                    goalsAgainst = standingImpl.getGoalsAgainst();
                } else {
                    // Fallback to interface methods
                    position = 0; // Position might not be available through interface
                    teamName = standing.getTeam().getClub().getName();
                    played = standing.getTotalMatches();
                    won = standing.getWins();
                    drawn = standing.getDraws();
                    lost = standing.getLosses();
                    goalsFor = standing.getGoalScored();
                    goalsAgainst = standing.getGoalsConceded();
                }

                boolean isUserTeam = teamName.equals(userTeam.getClub().getName());

                if (isUserTeam) {
                    System.out.print("* ");
                } else {
                    System.out.print("  ");
                }

                System.out.printf("%-3d %-20s %-3d %-3d %-3d %-3d %-3d-%-3d %-5d\n",
                        position,
                        teamName,
                        played,
                        won,
                        drawn,
                        lost,
                        goalsFor,
                        goalsAgainst,
                        standing.getPoints());
            }
        }
    }

    /**
     * Play the next match with user interaction
     */
    private void playNextMatch() {
        // Find the next match for the user's team
        var matches = currentSeason.getMatches();
        for (var match : matches) {
            if (match != null && !match.isPlayed() &&
                    (match.getHomeTeam().getClub().getName().equals(userTeam.getClub().getName()) ||
                            match.getAwayTeam().getClub().getName().equals(userTeam.getClub().getName()))) {

                // Simulate matches until we reach this match
                while (currentSeason.getCurrentRound() + 1 < match.getRound()) {
                    currentSeason.simulateRound();
                }

                System.out.println("\n===== MATCH DAY =====");
                System.out.println(match.getHomeTeam().getClub().getName() + " vs " +
                        match.getAwayTeam().getClub().getName());
                System.out.println("Press Enter to play this match...");
                scanner.nextLine();

                // Simulate the current round (which includes this match)
                currentSeason.simulateRound();

                System.out.println("\n===== MATCH RESULT =====");
                System.out.println(currentSeason.displayMatchResult(match));
                System.out.println("Press Enter to continue...");
                scanner.nextLine();
                return;
            }
        }

        System.out.println("No more matches for your team this season.");
    }

    /**
     * Simulate until the user's next match
     */
    private void simulateToNextMatch() {
        // Find the next match for the user's team
        var matches = currentSeason.getMatches();
        for (var match : matches) {
            if (match != null && !match.isPlayed() &&
                    (match.getHomeTeam().getClub().getName().equals(userTeam.getClub().getName()) ||
                            match.getAwayTeam().getClub().getName().equals(userTeam.getClub().getName()))) {

                // Simulate matches until we reach this match
                while (currentSeason.getCurrentRound() + 1 < match.getRound()) {
                    currentSeason.simulateRound();
                    System.out.println("Simulating round " + currentSeason.getCurrentRound() + "...");
                }

                System.out.println("Reached your next match!");
                return;
            }
        }

        System.out.println("No more matches for your team this season.");
    }

    /**
     * Simulate the rest of the season
     */
    private void simulateRestOfSeason() {
        System.out.println("Simulating the rest of the season...");
        currentSeason.simulateSeason();
        System.out.println("Season complete!");
    }

    /**
     * Show a summary of the completed season
     */
    private void showSeasonSummary() {
        System.out.println("\n===== SEASON SUMMARY =====");
        System.out.println("Season: " + currentSeason.getName());

        // Show final league table
        viewLeagueTable();

        // Show user team's performance
        var standings = currentSeason.getLeagueStandings();
        for (var standing : standings) {
            String teamName = "";
            int position = 0;

            if (standing instanceof StandingImpl) {
                StandingImpl standingImpl = (StandingImpl) standing;
                teamName = standingImpl.getClub().getName();
                position = standingImpl.getPosition();
            } else {
                teamName = standing.getTeam().getClub().getName();
                // Position might not be available through the interface
            }

            if (standing != null && teamName.equals(userTeam.getClub().getName())) {
                System.out.println("\nYour team finished in position: " + position);

                // Determine achievement based on position
                if (position == 1) {
                    System.out.println("Congratulations! You are the league champion!");
                } else if (position <= 4) {
                    System.out.println("Well done! You qualified for the Champions League!");
                } else if (position <= 6) {
                    System.out.println("Good job! You qualified for the Europa League!");
                } else if (position >= standings.length - 3) {
                    System.out.println("Oh no! Your team has been relegated!");
                } else {
                    System.out.println("You finished in mid-table. Try to improve next season!");
                }

                break;
            }
        }

        System.out.println("\nPress Enter to return to main menu...");
        scanner.nextLine();
    }

    /**
     * Create sample teams if loading from JSON fails
     */
    private void createSampleTeams() {
        // Create some sample teams with basic data
        String[] teamNames = {
                "Arsenal", "Chelsea", "Liverpool", "Manchester City",
                "Manchester United", "Tottenham", "Everton", "Leicester"
        };

        for (String name : teamNames) {
            // Create a club with some basic data
            // Using the full constructor since the shorter one isn't available
            IClub club = new ClubImpl(name, name.substring(0, 3).toUpperCase(), "England", 1900, name + " Stadium");

            // Add some random players to the club
            addSamplePlayersToClub((ClubImpl) club);

            // Create a team based on the club
            ITeam team = new TeamImpl(club);

            // Add the team to the league
            ((LeagueImpl) currentLeague).addTeam(team);
        }

        System.out.println("Created " + teamNames.length + " sample teams.");
    }

    /**
     * Add sample players to a club
     */
    private void addSamplePlayersToClub(ClubImpl club) {
        // Create sample players with different positions
        String[] firstNames = { "John", "David", "Michael", "James", "Robert", "William", "Thomas", "Daniel" };
        String[] lastNames = { "Smith", "Johnson", "Williams", "Jones", "Brown", "Davis", "Miller", "Wilson" };

        Random random = new Random();

        // Define player positions
        String[] positions = {
                "GOALKEEPER", "CENTER_BACK", "RIGHT_BACK", "LEFT_BACK",
                "DEFENSIVE_MIDFIELDER", "CENTRAL_MIDFIELDER", "ATTACKING_MIDFIELDER",
                "LEFT_WINGER", "RIGHT_WINGER", "STRIKER"
        };

        // Add goalkeepers
        for (int i = 0; i < 2; i++) {
            String name = firstNames[random.nextInt(firstNames.length)] + " "
                    + lastNames[random.nextInt(lastNames.length)];
            int number = 1 + i;
            IPlayerPosition position = new PlayerPositionImpl("GOALKEEPER");

            PlayerImpl player = new PlayerImpl(name, number, position);
            // Set some random attributes instead of using setOverallRating
            player.setPassing(50 + random.nextInt(30));
            player.setShooting(40 + random.nextInt(20));
            player.setSpeed(50 + random.nextInt(30));
            player.setStamina(60 + random.nextInt(30));
            club.addPlayer(player);
        }

        // Add defenders
        for (int i = 0; i < 6; i++) {
            String name = firstNames[random.nextInt(firstNames.length)] + " "
                    + lastNames[random.nextInt(lastNames.length)];
            int number = 2 + i;

            String[] defPositions = { "CENTER_BACK", "RIGHT_BACK", "LEFT_BACK" };
            IPlayerPosition position = new PlayerPositionImpl(defPositions[random.nextInt(defPositions.length)]);

            PlayerImpl player = new PlayerImpl(name, number, position);
            // Set some random attributes
            player.setPassing(60 + random.nextInt(30));
            player.setShooting(40 + random.nextInt(30));
            player.setSpeed(60 + random.nextInt(30));
            player.setStamina(70 + random.nextInt(25));
            club.addPlayer(player);
        }

        // Add midfielders
        for (int i = 0; i < 6; i++) {
            String name = firstNames[random.nextInt(firstNames.length)] + " "
                    + lastNames[random.nextInt(lastNames.length)];
            int number = 8 + i;

            String[] midPositions = { "DEFENSIVE_MIDFIELDER", "CENTRAL_MIDFIELDER", "ATTACKING_MIDFIELDER" };
            IPlayerPosition position = new PlayerPositionImpl(midPositions[random.nextInt(midPositions.length)]);

            PlayerImpl player = new PlayerImpl(name, number, position);
            // Set some random attributes
            player.setPassing(70 + random.nextInt(30));
            player.setShooting(60 + random.nextInt(30));
            player.setSpeed(60 + random.nextInt(30));
            player.setStamina(70 + random.nextInt(25));
            club.addPlayer(player);
        }

        // Add forwards
        for (int i = 0; i < 4; i++) {
            String name = firstNames[random.nextInt(firstNames.length)] + " "
                    + lastNames[random.nextInt(lastNames.length)];
            int number = 14 + i;

            String[] fwdPositions = { "LEFT_WINGER", "RIGHT_WINGER", "STRIKER" };
            IPlayerPosition position = new PlayerPositionImpl(fwdPositions[random.nextInt(fwdPositions.length)]);

            PlayerImpl player = new PlayerImpl(name, number, position);
            // Set some random attributes
            player.setPassing(60 + random.nextInt(30));
            player.setShooting(75 + random.nextInt(25));
            player.setSpeed(70 + random.nextInt(30));
            player.setStamina(65 + random.nextInt(25));
            club.addPlayer(player);
        }
    }

    /**
     * Helper method to get integer input
     */
    private int getIntInput() {
        try {
            if (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                return Integer.parseInt(input);
            } else {
                // Default value if no input is available
                System.out.println("No input available, using default value");
                return 1; // Default to the first option
            }
        } catch (NumberFormatException e) {
            return -1; // Invalid input
        } catch (NoSuchElementException e) {
            System.out.println("Input stream closed, using default value");
            return 1; // Default to the first option
        }
    }
}