package ppfootballmanager.utils;

import ppfootballmanager.models.LeagueImpl;
import ppfootballmanager.services.MatchSimulatorStrategyImpl;

import java.util.Scanner;

/**
 * Handles the user interaction with the game
 */
public class Menu {
    private LeagueImpl league;
    private Scanner scanner;
    private boolean running;

    /**
     * Constructor for the Menu class
     * 
     * @param league The league to manage
     */
    public Menu(LeagueImpl league) {
        this.league = league;
        this.scanner = new Scanner(System.in);
        this.running = true;
        
        // Set the match simulator strategy
        // league.setMatchSimulator(new MatchSimulatorStrategyImpl());
        /**
         * TODO: Uncomment the above line when MatchSimulatorStrategyImpl is implemented
         * and the league class has a setMatchSimulator method.
         */
    }

    /**
     * Starts the menu system
     */
    public void start() {
        while (running) {
            displayMainMenu();
            int choice = getUserChoice();
            handleMainMenuChoice(choice);
        }
    }

    /**
     * Displays the main menu options
     */
    private void displayMainMenu() {
        System.out.println("\n=== PP Football Manager ===");
        System.out.println("1. View Team Information");
        System.out.println("2. View League Schedule");
        System.out.println("3. View League Standings");
        System.out.println("4. Simulate Next Round");
        System.out.println("5. Simulate Full Season");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }

    /**
     * Gets the user's menu choice
     * 
     * @return The selected menu option
     */
    private int getUserChoice() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1; // Invalid choice
        }
    }

    /**
     * Handles the selected main menu option
     * 
     * @param choice The selected menu option
     */
    private void handleMainMenuChoice(int choice) {
        switch (choice) {
            case 1:
                viewTeamInformation();
                break;
            case 2:
                viewLeagueSchedule();
                break;
            case 3:
                viewLeagueStandings();
                break;
            case 4:
                simulateNextRound();
                break;
            case 5:
                simulateFullSeason();
                break;
            case 0:
                running = false;
                System.out.println("Thank you for playing PP Football Manager!");
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    /**
     * Displays information about the teams
     */
    private void viewTeamInformation() {
        System.out.println("\n=== Team Information ===");
        // Display teams and allow user to select one
        
        System.out.println("Press Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Displays the league schedule
     */
    private void viewLeagueSchedule() {
        System.out.println("\n=== League Schedule ===");
        // Display league schedule
        
        System.out.println("Press Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Displays the current league standings
     */
    private void viewLeagueStandings() {
        System.out.println("\n=== League Standings ===");
        // Display league standings
        
        System.out.println("Press Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Simulates the next round of matches
     */
    private void simulateNextRound() {
        System.out.println("\n=== Simulating Next Round ===");
        // Simulate next round
        
        System.out.println("Press Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Simulates the entire season
     */
    private void simulateFullSeason() {
        System.out.println("\n=== Simulating Full Season ===");
        // Simulate full season
        
        System.out.println("Press Enter to continue...");
        scanner.nextLine();
    }
}