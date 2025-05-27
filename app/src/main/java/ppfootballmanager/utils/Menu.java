package ppfootballmanager.utils;

import java.util.Scanner;
import java.util.NoSuchElementException;
import java.util.Random;

import com.ppstudios.footballmanager.api.contracts.league.ILeague;
import com.ppstudios.footballmanager.api.contracts.league.ISeason;
import com.ppstudios.footballmanager.api.contracts.league.IStanding;
import com.ppstudios.footballmanager.api.contracts.match.IMatch;
import com.ppstudios.footballmanager.api.contracts.team.IClub;
import com.ppstudios.footballmanager.api.contracts.team.IPlayerSelector;
import com.ppstudios.footballmanager.api.contracts.team.ITeam;
import com.ppstudios.footballmanager.api.contracts.player.IPlayer;
import com.ppstudios.footballmanager.api.contracts.player.IPlayerPosition;

import ppfootballmanager.clubs.ClubImpl;
import ppfootballmanager.clubs.FormationImpl;
import ppfootballmanager.clubs.PlayerImpl;
import ppfootballmanager.clubs.PlayerPositionImpl;
import ppfootballmanager.clubs.PlayerSelectorImpl;
import ppfootballmanager.clubs.TeamImpl;
import ppfootballmanager.game.LeagueImpl;
import ppfootballmanager.game.MatchSimulatorStrategyImpl;
import ppfootballmanager.game.SeasonImpl;
import ppfootballmanager.game.StandingImpl;

/**
 * Handles the user interaction with the game
 */
public class Menu {
    private Scanner scanner;
    private ILeague currentLeague;
    private ISeason currentSeason;
    private ITeam userTeam;
    private static final int MAX_TEAMS = 20;

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

        // Load sample teams
        createSampleTeams();

        // Debug: print team counts
        ITeam[] teams = ((LeagueImpl) currentLeague).getTeams();
        System.out.println("Created " + teams.length + " teams for the league");

        // Select team to manage
        selectTeam();
        if (userTeam == null) {
            System.out.println("Failed to select a team. Returning to main menu.");
            return;
        }

        // Debug: print user team player count
        System.out.println("Your team has " + userTeam.getClub().getPlayerCount() + " players");

        // Create a season
        System.out.print("Enter season year: ");
        int year = getIntInput();
        String seasonName = year + "-" + (year + 1);

        // Create a new season with the teams from the league
        currentSeason = new SeasonImpl(year, seasonName, teams);

        if (currentSeason == null) {
            System.out.println("Failed to create season. Returning to main menu.");
            return;
        }

        // Set match simulator
        currentSeason.setMatchSimulator(new MatchSimulatorStrategyImpl());

        // Generate the schedule
        currentSeason.generateSchedule();
        System.out.println("Season created with " + teams.length + " teams");

        // Set default formation for user team
        userTeam.setFormation(FormationImpl.createClassic442());

        // Enter game loop
        gameLoop();
    }

    /**
     * Load a previously saved game
     */
    private void loadGame() {
        System.out.println("\n===== LOAD GAME =====");

        // Display available seasons
        String[] availableSeasons = getAvailableSeasons();
        if (availableSeasons.length == 0) {
            System.out.println("No seasons available to load. Returning to main menu.");
            return;
        }

        System.out.println("Available seasons:");
        for (int i = 0; i < availableSeasons.length; i++) {
            System.out.println((i + 1) + ". " + availableSeasons[i]);
        }
        System.out.println((availableSeasons.length + 1) + ". Return to Main Menu");

        System.out.print("Select a season: ");
        int choice = getIntInput();

        if (choice < 1 || choice > availableSeasons.length + 1) {
            System.out.println("Invalid selection. Returning to main menu.");
            return;
        }

        if (choice == availableSeasons.length + 1) {
            return; // Return to main menu
        }

        // Get selected season filename
        String seasonFile = availableSeasons[choice - 1] + ".json";

        // Use JsonLoader to load the season - THIS IS THE KEY PART
        System.out.println("Loading season from " + seasonFile + "...");
        SeasonImpl loadedSeason = JsonLoader.loadSeason(seasonFile);

        if (loadedSeason == null) {
            System.out.println("Failed to load season. Returning to main menu.");
            return;
        }

        // Successfully loaded season
        currentSeason = loadedSeason;
        System.out.println("Season loaded successfully: " + currentSeason.getName());

        // We also need to set currentLeague for the menu system to work
        // We can extract the league name from the season name
        String leagueName = currentSeason.getName().split(" ")[0];

        // Get the teams from the schedule to rebuild the league
        IMatch[] matches = currentSeason.getMatches();
        if (matches == null || matches.length == 0) {
            System.out.println("Warning: Season has no schedule. Creating a new league.");
            currentLeague = new LeagueImpl(leagueName);
        } else {
            // Create a new league
            currentLeague = new LeagueImpl(leagueName);

            // Extract unique teams from matches and add them to the league
            String[] addedTeamCodes = new String[MAX_TEAMS];
            int addedCount = 0;

            for (int i = 0; i < matches.length; i++) {
                if (matches[i] != null) {
                    // Process home team
                    ITeam homeTeam = matches[i].getHomeTeam();
                    if (homeTeam != null && homeTeam.getClub() != null) {
                        String teamCode = homeTeam.getClub().getCode();

                        // Check if already added
                        boolean alreadyAdded = false;
                        for (int j = 0; j < addedCount; j++) {
                            if (addedTeamCodes[j] != null && addedTeamCodes[j].equals(teamCode)) {
                                alreadyAdded = true;
                                break;
                            }
                        }

                        if (!alreadyAdded) {
                            ((LeagueImpl) currentLeague).addTeam(homeTeam);
                            addedTeamCodes[addedCount++] = teamCode;
                        }
                    }

                    // Process away team
                    ITeam awayTeam = matches[i].getAwayTeam();
                    if (awayTeam != null && awayTeam.getClub() != null) {
                        String teamCode = awayTeam.getClub().getCode();

                        // Check if already added
                        boolean alreadyAdded = false;
                        for (int j = 0; j < addedCount; j++) {
                            if (addedTeamCodes[j] != null && addedTeamCodes[j].equals(teamCode)) {
                                alreadyAdded = true;
                                break;
                            }
                        }

                        if (!alreadyAdded) {
                            ((LeagueImpl) currentLeague).addTeam(awayTeam);
                            addedTeamCodes[addedCount++] = teamCode;
                        }
                    }
                }
            }

            System.out.println("Extracted " + addedCount + " teams for the league.");
        }

        /**
         * TODO:
         * Fix for getMatchSimulator() Error
         * Since the ISeason interface doesn't have a getMatchSimulator() method, you
         * need to modify your code. Here are two approaches:
         * 
         * Option 1: Cast to SeasonImpl
         * // Ensure the season has a match simulator
         * if (currentSeason instanceof SeasonImpl) {
         * SeasonImpl seasonImpl = (SeasonImpl) currentSeason;
         * if (seasonImpl.getMatchSimulator() == null) {
         * seasonImpl.setMatchSimulator(new MatchSimulatorStrategyImpl());
         * }
         * }
         * Option 2: Always Set the Match Simulator
         * // Always set the match simulator since we can't check if it exists
         * currentSeason.setMatchSimulator(new MatchSimulatorStrategyImpl());
         */
        // Always set the match simulator since we can't check if it exists
        currentSeason.setMatchSimulator(new MatchSimulatorStrategyImpl());

        // Let user select a team
        selectTeam();

        // After selecting a team, check if it has players
        if (userTeam != null && userTeam.getClub().getPlayerCount() == 0) {
            System.out.println("No players found for " + userTeam.getClub().getName() + ", adding sample players...");
            addSamplePlayersToClub((ClubImpl) userTeam.getClub());
        }

        // Enter game loop
        gameLoop();
    }

    /**
     * Get a list of available season files
     * In a full implementation, this would scan a resources directory
     * TODO: Implement file scanning to get available seasons
     */
    private String[] getAvailableSeasons() {
        // Hardcoded for demonstration
        // In a real implementation, scan the resources directory
        return new String[] {
                "premier_league_2023",
                "la_liga_2024",
                "bundesliga_2024"
        };
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

        // After user selects a team:
        userTeam = teams[choice - 1];
        System.out.println("You are now managing " + userTeam.getClub().getName() + "!");

        // Ensure the team has the same players as its club
        syncTeamPlayersFromClub();
    }

    /**
     * Ensure team has the same players as its club
     */
    private void syncTeamPlayersFromClub() {
        if (userTeam != null && userTeam.getClub() != null) {
            IPlayer[] clubPlayers = userTeam.getClub().getPlayers();

            // Clear existing team players first
            if (userTeam instanceof TeamImpl) {
                ((TeamImpl) userTeam).clearPlayers();
            }

            // Add club players to team
            if (clubPlayers != null && clubPlayers.length > 0) {
                System.out.println("Synchronizing players from club to team...");

                for (IPlayer player : clubPlayers) {
                    if (player != null) {
                        userTeam.addPlayer(player);
                    }
                }

                System.out.println("Added " + clubPlayers.length + " players to team");
            } else {
                System.out.println("No players found in club to synchronize");
            }
        }
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
        System.out.println("2. View Team Statistics");
        System.out.println("3. View Fixtures");
        System.out.println("4. View League Table");
        System.out.println("5. Play Next Match");
        System.out.println("6. Simulate to Next Match");
        System.out.println("7. Simulate Rest of Season");
        System.out.println("8. Save Game");
        System.out.println("9. Exit to Main Menu");
        System.out.print("Select an option: ");

        int choice = getIntInput();

        switch (choice) {
            case 1:
                viewTeam();
                break;
            case 2:
                viewTeamStatistics();
                break;
            case 3:
                viewFixtures();
                break;
            case 4:
                viewLeagueTable();
                break;
            case 5:
                playNextMatch();
                break;
            case 6:
                simulateToNextMatch();
                break;
            case 7:
                simulateRestOfSeason();
                break;
            case 8:
                // saveGame();
                break;
            case 9:
                return; // Exit to main menu
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    /**
     * View details about the user's team with enhanced information
     */
    private void viewTeam() {
        System.out.println("\n===== TEAM: " + userTeam.getClub().getName() + " =====");

        // Display club information
        IClub club = userTeam.getClub();
        System.out.println("Stadium: " + club.getStadiumName());
        System.out.println("Founded: " + club.getFoundedYear());
        System.out.println("Country: " + club.getCountry());

        // Display team statistics
        System.out.println("Team Strength: " + userTeam.getTeamStrength() + "/100");

        // Display squad size
        System.out.println("Squad Size: " + club.getPlayerCount() + " players");

        // Display formation if available
        if (userTeam.getFormation() != null) {
            System.out.println("Formation: " + userTeam.getFormation().getDisplayName());
        } else {
            System.out.println("Formation: Not set");
        }

        // Team management submenu
        boolean exitTeamMenu = false;
        while (!exitTeamMenu) {
            System.out.println("\n----- Team Management -----");
            System.out.println("1. View Squad by Position");
            System.out.println("2. View Player Details");
            System.out.println("3. Change Formation");
            System.out.println("4. View Position Breakdown");
            System.out.println("5. Return to Game Menu");
            System.out.print("Select an option: ");

            int choice = getIntInput();
            switch (choice) {
                case 1:
                    viewSquad();
                    break;
                case 2:
                    viewPlayerDetails();
                    break;
                case 3:
                    changeFormation();
                    break;
                case 4:
                    viewPositionBreakdown();
                    break;
                case 5:
                    exitTeamMenu = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    /**
     * View position breakdown for the team
     */
    private void viewPositionBreakdown() {
        System.out.println("\n===== POSITION BREAKDOWN =====");

        // Define all the positions we want to check
        PlayerPositionImpl[] positions = new PlayerPositionImpl[] {
                new PlayerPositionImpl("GK"),
                new PlayerPositionImpl("CB"), new PlayerPositionImpl("RB"),
                new PlayerPositionImpl("LB"), new PlayerPositionImpl("WB"),
                new PlayerPositionImpl("CDM"), new PlayerPositionImpl("CM"),
                new PlayerPositionImpl("CAM"), new PlayerPositionImpl("RM"),
                new PlayerPositionImpl("LM"), new PlayerPositionImpl("RW"),
                new PlayerPositionImpl("LW"), new PlayerPositionImpl("CF"),
                new PlayerPositionImpl("ST")
        };

        // Count the players in each position
        System.out.println("Position distribution:");
        System.out.println("------------------------------------------");

        int totalPlayers = 0;

        // Count goalkeepers
        int goalkeepers = userTeam.getPositionCount(positions[0]);
        totalPlayers += goalkeepers;
        System.out.printf("%-20s: %d\n", "Goalkeepers", goalkeepers);

        // Count defenders
        int defenders = 0;
        for (int i = 1; i <= 4; i++) {
            defenders += userTeam.getPositionCount(positions[i]);
        }
        totalPlayers += defenders;
        System.out.printf("%-20s: %d\n", "Defenders", defenders);

        // Count midfielders
        int midfielders = 0;
        for (int i = 5; i <= 9; i++) {
            midfielders += userTeam.getPositionCount(positions[i]);
        }
        totalPlayers += midfielders;
        System.out.printf("%-20s: %d\n", "Midfielders", midfielders);

        // Count forwards
        int forwards = 0;
        for (int i = 10; i <= 13; i++) {
            forwards += userTeam.getPositionCount(positions[i]);
        }
        totalPlayers += forwards;
        System.out.printf("%-20s: %d\n", "Forwards", forwards);

        System.out.println("------------------------------------------");
        System.out.printf("%-20s: %d\n", "Total Players", totalPlayers);

        // Check formation compatibility
        if (userTeam.getFormation() != null) {
            FormationImpl formation = (FormationImpl) userTeam.getFormation();
            System.out.println("\nCurrent formation: " + formation.getName());
            System.out.println("Formation requires: " + formation.getDefenders() + " defenders, " +
                    formation.getMidfielders() + " midfielders, " +
                    formation.getForwards() + " forwards");

            // Check if we have enough players for each position
            boolean hasEnoughDefenders = defenders >= formation.getDefenders();
            boolean hasEnoughMidfielders = midfielders >= formation.getMidfielders();
            boolean hasEnoughForwards = forwards >= formation.getForwards();

            System.out.println("\nFormation compatibility:");
            System.out.println("Defenders: " + (hasEnoughDefenders ? "✓" : "✗") +
                    " (" + defenders + "/" + formation.getDefenders() + ")");
            System.out.println("Midfielders: " + (hasEnoughMidfielders ? "✓" : "✗") +
                    " (" + midfielders + "/" + formation.getMidfielders() + ")");
            System.out.println("Forwards: " + (hasEnoughForwards ? "✓" : "✗") +
                    " (" + forwards + "/" + formation.getForwards() + ")");

            if (hasEnoughDefenders && hasEnoughMidfielders && hasEnoughForwards) {
                System.out.println("\nYour squad is compatible with the current formation.");
            } else {
                System.out.println("\nWarning: Your squad may not be compatible with the current formation.");
            }
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * View the team's squad with more organized display
     */
    private void viewSquad() {
        // Ensure players are synchronized from club to team before showing squad
        syncTeamPlayersFromClub();

        IPlayer[] players = userTeam.getPlayers();

        // Add debug output
        System.out.println("\n----- DEBUG: PLAYER COUNT -----");
        System.out.println("Total players in team: " + (players != null ? players.length : 0));

        if (players != null) {
            System.out.println("\n----- DEBUG: PLAYER POSITIONS -----");
            for (IPlayer player : players) {
                if (player != null) {
                    String positionCode = "Unknown";
                    if (player.getPosition() != null) {
                        if (player.getPosition() instanceof PlayerPositionImpl) {
                            positionCode = ((PlayerPositionImpl) player.getPosition()).getCode();
                        } else {
                            positionCode = player.getPosition().getDescription();
                        }
                    }
                    System.out.println(player.getName() + " (#" + player.getNumber() + "): " + positionCode);
                }
            }
        }

        if (players == null || players.length == 0) {
            System.out.println("No players in squad.");
            return;
        }

        // Group players by position type
        System.out.println("\n===== SQUAD: " + userTeam.getClub().getName() + " =====");

        // Goalkeepers
        System.out.println("\n----- Goalkeepers -----");
        System.out.println("--------------------------------------------");
        System.out.printf("%-3s %-20s %-5s %-8s %-5s\n", "No.", "Name", "Age", "Position", "Rating");
        System.out.println("--------------------------------------------");
        displayPlayersByPosition(players, "GK");

        // Defenders
        System.out.println("\n----- Defenders -----");
        System.out.println("--------------------------------------------");
        System.out.printf("%-3s %-20s %-5s %-8s %-5s\n", "No.", "Name", "Age", "Position", "Rating");
        System.out.println("--------------------------------------------");
        displayPlayersByPosition(players, "CB");
        displayPlayersByPosition(players, "RB");
        displayPlayersByPosition(players, "LB");
        displayPlayersByPosition(players, "WB");

        // Midfielders
        System.out.println("\n----- Midfielders -----");
        System.out.println("--------------------------------------------");
        System.out.printf("%-3s %-20s %-5s %-8s %-5s\n", "No.", "Name", "Age", "Position", "Rating");
        System.out.println("--------------------------------------------");
        displayPlayersByPosition(players, "CM");
        displayPlayersByPosition(players, "CDM");
        displayPlayersByPosition(players, "CAM");
        displayPlayersByPosition(players, "RM");
        displayPlayersByPosition(players, "LM");

        // Forwards
        System.out.println("\n----- Forwards -----");
        System.out.println("--------------------------------------------");
        System.out.printf("%-3s %-20s %-5s %-8s %-5s\n", "No.", "Name", "Age", "Position", "Rating");
        System.out.println("--------------------------------------------");
        displayPlayersByPosition(players, "ST");
        displayPlayersByPosition(players, "CF");
        displayPlayersByPosition(players, "RW");
        displayPlayersByPosition(players, "LW");

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Display players with a specific position code
     */
    private void displayPlayersByPosition(IPlayer[] players, String positionCode) {
        for (IPlayer player : players) {
            if (player != null && player.getPosition() != null) {
                // Get position information safely
                String playerPositionCode = "";

                // Use the standard PlayerPositionImpl implementation if available
                if (player.getPosition() instanceof PlayerPositionImpl) {
                    playerPositionCode = ((PlayerPositionImpl) player.getPosition()).getCode();
                } else {
                    // Fallback to description if not our implementation
                    String desc = player.getPosition().getDescription();
                    // Try to extract code from description (e.g. "Goalkeeper" -> "GK")
                    playerPositionCode = extractPositionCode(desc);
                }

                // Only display if position matches
                if (playerPositionCode.equals(positionCode)) {
                    // Calculate age if birth date is available
                    int age = 25; // Default age
                    if (player instanceof PlayerImpl && ((PlayerImpl) player).getBirthDate() != null) {
                        java.time.LocalDate birthDate = ((PlayerImpl) player).getBirthDate();
                        java.time.Period period = java.time.Period.between(birthDate, java.time.LocalDate.now());
                        age = period.getYears();
                    }

                    // Calculate rating (average of all attributes)
                    int rating = calculatePlayerRating(player);

                    System.out.printf("%-3d %-20s %-5d %-8s %-5d\n",
                            player.getNumber(),
                            player.getName(),
                            age,
                            playerPositionCode,
                            rating);
                }
            }
        }
    }

    /**
     * Extract a position code from a position description
     */
    private String extractPositionCode(String description) {
        if (description == null) {
            return "CM"; // Default to central midfielder
        }

        // Handle common position names
        description = description.toUpperCase();

        if (description.contains("GOALKEEPER")) {
            return "GK";
        }
        if (description.contains("CENTER BACK") || description.contains("CENTRE BACK")) {
            return "CB";
        }
        if (description.contains("RIGHT BACK")) {
            return "RB";
        }
        if (description.contains("LEFT BACK")) {
            return "LB";
        }
        if (description.contains("WING BACK")) {
            return "WB";
        }
        if (description.contains("DEFENSIVE MID")) {
            return "CDM";
        }
        if (description.contains("CENTRAL MID")) {
            return "CM";
        }
        if (description.contains("ATTACKING MID")) {
            return "CAM";
        }
        if (description.contains("RIGHT MID")) {
            return "RM";
        }
        if (description.contains("LEFT MID")) {
            return "LM";
        }
        if (description.contains("RIGHT WING")) {
            return "RW";
        }
        if (description.contains("LEFT WING")) {
            return "LW";
        }
        if (description.contains("CENTER FORWARD") || description.contains("CENTRE FORWARD")) {
            return "CF";
        }
        if (description.contains("STRIKER")) {
            return "ST";
        }

        // If no match, return first two letters
        return description.length() > 1 ? description.substring(0, 2) : description;
    }

    /**
     * Calculate player rating based on all attributes
     */
    private int calculatePlayerRating(IPlayer player) {
        if (player == null)
            return 0;

        return (player.getShooting() + player.getPassing() +
                player.getSpeed() + player.getStamina()) / 4;
    }

    /**
     * View detailed information about a specific player
     */
    private void viewPlayerDetails() {

        syncTeamPlayersFromClub();
        // Show selection options
        System.out.println("\n----- Player Selection Method -----");
        System.out.println("1. Select from squad list");
        System.out.println("2. Select by position");
        System.out.println("3. Select by number");
        System.out.println("4. Return to Team Menu");
        System.out.print("Choose selection method: ");

        int selectionMethod = getIntInput();
        if (selectionMethod < 1 || selectionMethod > 4) {
            System.out.println("Invalid selection.");
            return;
        }

        if (selectionMethod == 4) {
            return;
        }

        IPlayer selectedPlayer = null;

        switch (selectionMethod) {
            case 1:
                selectedPlayer = selectPlayerFromList();
                break;
            case 2:
                selectedPlayer = selectPlayerByPosition();
                break;
            case 3:
                selectedPlayer = selectPlayerByNumber();
                break;
        }

        if (selectedPlayer == null) {
            return; // Selection was canceled or failed
        }

        // Display detailed player information
        displayPlayerDetails(selectedPlayer);

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Select a player from a list of all squad players
     */
    private IPlayer selectPlayerFromList() {
        // Display player list for selection
        IPlayer[] players = userTeam.getPlayers();
        if (players == null || players.length == 0) {
            System.out.println("No players in squad.");
            return null;
        }

        System.out.println("\n----- Select Player from Squad -----");
        int validPlayerCount = 0;
        for (IPlayer player : players) {
            if (player != null) {
                validPlayerCount++;

                // Get position display safely
                String positionDisplay = getPositionDisplay(player.getPosition());

                System.out.printf("%2d. %s (%s)\n", validPlayerCount, player.getName(), positionDisplay);
            }
        }

        if (validPlayerCount == 0) {
            System.out.println("No valid players found.");
            return null;
        }

        System.out.println(validPlayerCount + 1 + ". Cancel");
        System.out.print("Select a player (1-" + (validPlayerCount + 1) + "): ");

        int choice = getIntInput();
        if (choice < 1 || choice > validPlayerCount + 1) {
            System.out.println("Invalid selection.");
            return null;
        }

        if (choice == validPlayerCount + 1) {
            return null; // User canceled
        }

        // Find the selected player
        int currentIndex = 0;
        for (IPlayer player : players) {
            if (player != null) {
                currentIndex++;
                if (currentIndex == choice) {
                    return player;
                }
            }
        }

        System.out.println("Player selection error.");
        return null;
    }

    /**
     * Select a player by position using IPlayerSelector
     */
    private IPlayer selectPlayerByPosition() {
        System.out.println("\n----- Select Player by Position -----");

        // Define available positions
        String[] positionOptions = {
                "GK", "CB", "RB", "LB", "CDM", "CM", "CAM", "RM", "LM", "RW", "LW", "CF", "ST"
        };

        // Display position options
        for (int i = 0; i < positionOptions.length; i++) {
            System.out.println((i + 1) + ". " + positionOptions[i]);
        }

        System.out.println(positionOptions.length + 1 + ". Cancel");
        System.out.print("Select a position: ");

        int choice = getIntInput();
        if (choice < 1 || choice > positionOptions.length + 1) {
            System.out.println("Invalid selection.");
            return null;
        }

        if (choice == positionOptions.length + 1) {
            return null; // User canceled
        }

        // Create position object
        String selectedPositionCode = positionOptions[choice - 1];
        PlayerPositionImpl selectedPosition = new PlayerPositionImpl(selectedPositionCode);

        // Use a PlayerSelector to select a player
        try {
            IPlayerSelector selector = new PlayerSelectorImpl();
            IPlayer selectedPlayer = userTeam.getClub().selectPlayer(selector, selectedPosition);

            if (selectedPlayer == null) {
                System.out.println("No player found for position: " + selectedPositionCode);
                return null;
            }

            return selectedPlayer;
        } catch (Exception e) {
            System.out.println("Error selecting player: " + e.getMessage());
            return null;
        }
    }

    /**
     * Select a player by jersey number
     */
    private IPlayer selectPlayerByNumber() {
        // Ensure players are synchronized
        syncTeamPlayersFromClub();

        System.out.print("\nEnter player jersey number: ");
        int number = getIntInput();

        if (number < 1) {
            System.out.println("Invalid jersey number.");
            return null;
        }

        // Get player from team, not club - no change needed here since you're already
        // using userTeam.getPlayers()
        IPlayer[] players = userTeam.getPlayers();
        for (IPlayer player : players) {
            if (player != null && player.getNumber() == number) {
                return player;
            }
        }

        System.out.println("No player found with jersey number " + number);
        return null;
    }

    /**
     * Get a display string for a position
     */
    private String getPositionDisplay(IPlayerPosition position) {
        if (position == null)
            return "Unknown";

        if (position instanceof PlayerPositionImpl) {
            return ((PlayerPositionImpl) position).getCode();
        } else {
            return position.getDescription();
        }
    }

    /**
     * Display detailed information for a specific player
     */
    private void displayPlayerDetails(IPlayer player) {
        System.out.println("\n===== PLAYER DETAILS =====");

        // Basic info
        System.out.println("Name: " + player.getName());
        System.out.println("Number: " + player.getNumber());
        System.out.println("Position: " + player.getPosition().toString());

        // Biographical info if available
        if (player instanceof PlayerImpl) {
            PlayerImpl playerImpl = (PlayerImpl) player;

            if (playerImpl.getBirthDate() != null) {
                java.time.LocalDate birthDate = playerImpl.getBirthDate();
                java.time.Period period = java.time.Period.between(birthDate, java.time.LocalDate.now());
                int age = period.getYears();
                System.out.println("Age: " + age);
                System.out.println("Birth Date: " + birthDate);
            }

            if (playerImpl.getNationality() != null) {
                System.out.println("Nationality: " + playerImpl.getNationality());
            }

            System.out.printf("Height: %.2f m\n", playerImpl.getHeight());
            System.out.printf("Weight: %.1f kg\n", playerImpl.getWeight());

            if (playerImpl.getPreferredFoot() != null) {
                System.out.println("Preferred Foot: " + playerImpl.getPreferredFoot());
            }
        }

        // Attributes with visual display
        System.out.println("\nATTRIBUTES:");
        System.out.println("--------------------------------------------");
        displayAttributeBar("Shooting", player.getShooting());
        displayAttributeBar("Passing", player.getPassing());
        displayAttributeBar("Speed", player.getSpeed());
        displayAttributeBar("Stamina", player.getStamina());

        // Overall rating
        int overallRating = (player.getShooting() + player.getPassing() +
                player.getSpeed() + player.getStamina()) / 4;
        System.out.println("--------------------------------------------");
        displayAttributeBar("OVERALL", overallRating);
    }

    /**
     * Display an attribute as a visual bar
     */
    private void displayAttributeBar(String name, int value) {
        final int MAX_BAR_LENGTH = 20;
        int barLength = value * MAX_BAR_LENGTH / 100;

        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < MAX_BAR_LENGTH; i++) {
            if (i < barLength) {
                bar.append("■");
            } else {
                bar.append("□");
            }
        }
        bar.append("]");

        System.out.printf("%-10s: %-3d %s\n", name, value, bar.toString());
    }

    /**
     * Change the team formation
     */
    private void changeFormation() {
        System.out.println("\n===== CHANGE FORMATION =====");
        System.out.println("1. 4-4-2  (Classic, Balanced)");
        System.out.println("2. 4-3-3  (Attack-focused)");
        System.out.println("3. 3-5-2  (Midfield control)");
        System.out.println("4. 4-2-3-1 (Modern balanced)");
        System.out.println("5. 5-3-2  (Defensive, Counter-attack)");
        System.out.println("6. Return to Team Menu");
        System.out.print("Select a formation: ");

        int choice = getIntInput();

        // Return if invalid or cancel
        if (choice < 1 || choice > 6) {
            System.out.println("Invalid selection.");
            return;
        }

        if (choice == 6) {
            return;
        }

        // Create the appropriate FormationImpl object using factory methods
        FormationImpl formation;
        switch (choice) {
            case 1:
                formation = FormationImpl.createClassic442();
                break;
            case 2:
                formation = FormationImpl.createAttacking433();
                break;
            case 3:
                formation = new FormationImpl("3-5-2");
                break;
            case 4:
                formation = FormationImpl.createBalanced4231();
                break;
            case 5:
                formation = FormationImpl.createDefensive532();
                break;
            default:
                formation = FormationImpl.createClassic442(); // Default
        }

        // Set the formation using the interface method
        userTeam.setFormation(formation);
        System.out.println("Formation changed to " + formation.getDisplayName());

        // Show formation details
        System.out.println("\nFormation details:");
        System.out.println("Defenders: " + ((FormationImpl) formation).getDefenders());
        System.out.println("Midfielders: " + ((FormationImpl) formation).getMidfielders());
        System.out.println("Forwards: " + ((FormationImpl) formation).getForwards());

        // Check formation validity
        if (((FormationImpl) formation).isValid()) {
            System.out.println("\nThis is a valid formation (10 outfield players + goalkeeper).");
        } else {
            System.out.println("\nWarning: This formation doesn't have exactly 10 outfield players.");
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
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

        syncTeamPlayersFromClub();
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
        syncTeamPlayersFromClub();
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

    /**
     * View detailed team statistics
     */
    private void viewTeamStatistics() {
        syncTeamPlayersFromClub();
        System.out.println("\n===== TEAM STATISTICS =====");
        System.out.println("Team: " + userTeam.getClub().getName());

        // Overall team strength
        System.out.println("\n----- Overall Team -----");
        System.out.println("Team Strength: " + userTeam.getTeamStrength() + "/100");

        // Count players by position area
        int goalkeepers = 0;
        int defenders = 0;
        int midfielders = 0;
        int forwards = 0;

        // Get players and categorize them
        IPlayer[] players = userTeam.getPlayers();
        for (IPlayer player : players) {
            if (player != null && player.getPosition() != null) {
                if (player.getPosition() instanceof PlayerPositionImpl) {
                    PlayerPositionImpl.PositionArea area = ((PlayerPositionImpl) player.getPosition()).getArea();

                    switch (area) {
                        case GOALKEEPER:
                            goalkeepers++;
                            break;
                        case DEFENSE:
                            defenders++;
                            break;
                        case MIDFIELD:
                            midfielders++;
                            break;
                        case ATTACK:
                            forwards++;
                            break;
                    }
                } else {
                    // If not using our implementation, try to categorize by description
                    String desc = player.getPosition().getDescription().toUpperCase();

                    if (desc.contains("GOALKEEPER") || desc.contains("KEEPER") || desc.equals("GK")) {
                        goalkeepers++;
                    } else if (desc.contains("BACK") || desc.contains("DEFENDER") ||
                            desc.equals("CB") || desc.equals("RB") || desc.equals("LB")) {
                        defenders++;
                    } else if (desc.contains("MID") || desc.equals("CM") || desc.equals("CDM") ||
                            desc.equals("CAM") || desc.equals("RM") || desc.equals("LM")) {
                        midfielders++;
                    } else {
                        forwards++;
                    }
                }
            }
        }

        // Display team structure
        System.out.println("\n----- Squad Composition -----");
        System.out.println("Total Players: " + userTeam.getClub().getPlayerCount());
        System.out.printf("Goalkeepers: %d (%.1f%%)\n", goalkeepers,
                (100.0 * goalkeepers / userTeam.getClub().getPlayerCount()));
        System.out.printf("Defenders: %d (%.1f%%)\n", defenders,
                (100.0 * defenders / userTeam.getClub().getPlayerCount()));
        System.out.printf("Midfielders: %d (%.1f%%)\n", midfielders,
                (100.0 * midfielders / userTeam.getClub().getPlayerCount()));
        System.out.printf("Forwards: %d (%.1f%%)\n", forwards,
                (100.0 * forwards / userTeam.getClub().getPlayerCount()));

        // Display current formation
        if (userTeam.getFormation() != null) {
            System.out.println("\n----- Current Formation -----");
            System.out.println("Formation: " + userTeam.getFormation().getDisplayName());

            // Show tactical advantage against other teams if available
            System.out.println("\n----- Tactical Advantages -----");
            System.out.println("Tactical advantages against other teams:");

            // Get all teams from the league
            ITeam[] teams = ((LeagueImpl) currentLeague).getTeams();
            for (ITeam team : teams) {
                if (team != null && team != userTeam && team.getFormation() != null) {
                    int advantage = userTeam.getFormation().getTacticalAdvantage(team.getFormation());
                    String advantageText;

                    if (advantage > 1) {
                        advantageText = "Strong advantage";
                    } else if (advantage == 1) {
                        advantageText = "Slight advantage";
                    } else if (advantage == 0) {
                        advantageText = "Even";
                    } else if (advantage == -1) {
                        advantageText = "Slight disadvantage";
                    } else {
                        advantageText = "Strong disadvantage";
                    }

                    System.out.printf("vs %-20s: %s\n",
                            team.getClub().getName(), advantageText);
                }
            }
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
}