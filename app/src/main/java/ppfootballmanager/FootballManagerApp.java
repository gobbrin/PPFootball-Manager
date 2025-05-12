package ppfootballmanager;

import ppfootballmanager.io.JsonLoader;
import ppfootballmanager.model.League;
import ppfootballmanager.ui.Menu;

public class FootballManagerApp {
    public static void main(String[] args) {
        // Carrega a liga a partir do ficheiro clubs.json
        League league = JsonLoader.loadLeague("Liga Portugal", "clubs.json");

        // Cria o menu e passa a liga para ele
        Menu menu = new Menu(league); 
        menu.run();             
    }
}
