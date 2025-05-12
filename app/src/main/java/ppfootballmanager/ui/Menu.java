package ppfootballmanager.ui;

import ppfootballmanager.model.League;

public class Menu {

    private League league;

    public Menu(League league) {
        this.league = league;
    }

    public void run() {
        System.out.println("Bem-vindo ao PPFootball Manager!");
        System.out.println("Liga carregada: " + league.getName());
        // aqui podes mostrar equipas, jornadas, etc.
    }
}
