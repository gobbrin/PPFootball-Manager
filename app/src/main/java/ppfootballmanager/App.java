package ppfootballmanager;

import ppfootballmanager.utils.JsonLoader;
import ppfootballmanager.utils.Menu;
import ppfootballmanager.models.SeasonImpl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class App {
    // Flag to determine if app is running in a Gradle environment
    private static final boolean IS_GRADLE_ENV = System.getProperty("gradle.running") != null;
    
    public String getGreeting() {
        return "Welcome to PP Football Manager!";
    }

    public static void main(String[] args) {
        System.out.println(new App().getGreeting());
        
        // Create data directory if it doesn't exist
        createDataDirectories();
        
        if (IS_GRADLE_ENV) {
            System.out.println("Running in Gradle environment - using demo mode");
            demoRun();
        } else {
            // Start the menu system normally
            Menu menu = new Menu();
            menu.showMainMenu();
        }
    }
    
    /**
     * Demo run with simulated inputs for Gradle execution
     */
    private static void demoRun() {
        System.out.println("\nDEMO MODE ACTIVATED");
        System.out.println("This is a demonstration of the football manager app.");
        System.out.println("In a real run, you would interact with the menu system.");
        System.out.println("\nSample Features:");
        System.out.println("1. Create and manage football teams");
        System.out.println("2. Run season simulations");
        System.out.println("3. View match results and statistics");
        System.out.println("\nTo play the full game, run the app directly using:");
        System.out.println("java -cp build/classes/java/main ppfootballmanager.App");
        System.out.println("java -cp \"app/build/classes/java/main;app/libs/apicontracts-1.0-SNAPSHOT.jar;app/build/resources/main\" ppfootballmanager.App");
    }
    
    /**
     * Create necessary data directories
     */
    private static void createDataDirectories() {
        String[] paths = {
            "app/src/main/resources/data",
            "app/src/main/resources/data/clubs",
            "app/src/main/resources/data/players",
            "app/src/main/resources/data/leagues"
        };
        
        for (String path : paths) {
            java.io.File directory = new java.io.File(path);
            if (!directory.exists()) {
                directory.mkdirs();
            }
        }
    }
}