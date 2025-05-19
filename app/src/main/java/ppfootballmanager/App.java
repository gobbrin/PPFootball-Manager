package ppfootballmanager;

// import ppfootballmanager.utils.JsonLoader;    // json
// import ppfootballmanager.utils.Menu;          // menu
import ppfootballmanager.models.LeagueImpl;      // league


public class App {
    public String getGreeting() {
        return "Welcome to PP Football Manager!";
    }

     public static void main(String[] args) {
        System.out.println(new App().getGreeting());
        
        // Load the league data
        // LeagueImpl portugueseLeague = JsonLoader.loadLeague("Portuguese League", "clubs.json");
        
        // Initialize the game menu
        // Menu gameMenu = new Menu(portugueseLeague);
        // gameMenu.start();
    }
}
