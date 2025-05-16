package ppfootballmanager.utils;

import ppfootballmanager.models.*;
import com.ppstudios.footballmanager.api.contracts.player.*;
import com.ppstudios.footballmanager.api.contracts.player.IPlayerPosition;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JsonLoader {

    public static LeagueImpl loadLeague(String leagueName, String clubsFile) {
        LeagueImpl league = new LeagueImpl(leagueName);

        try {
            InputStream is = JsonLoader.class.getClassLoader().getResourceAsStream("players/" + clubsFile);
            if (is == null) {
                System.out.println("Ficheiro clubs.json não encontrado!");
                return league;
            }

            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            JSONParser parser = new JSONParser();
            JSONArray clubsArray = (JSONArray) parser.parse(reader);

            for (Object obj : clubsArray) {
                JSONObject clubJson = (JSONObject) obj;
                String clubName = (String) clubJson.get("name");
                String playersFile = (String) clubJson.get("playersFile");

                ClubImpl club = new ClubImpl(clubName);
                TeamImpl team = new TeamImpl(club);

                List<IPlayer> players = loadPlayers(playersFile);
                for (IPlayer player : players) {
                    team.addPlayer(player);
                }

                league.addTeam(team);
            }

            // Opcional: gerar calendário
            // league.generateSchedule();

        } catch (Exception e) {
            System.out.println("Erro ao carregar a liga: " + e.getMessage());
            e.printStackTrace();
        }

        return league;
    }

    public static List<IPlayer> loadPlayers(String playersFile) {
        List<IPlayer> players = new ArrayList<>();

        try {
            InputStream is = JsonLoader.class.getClassLoader().getResourceAsStream("players/" + playersFile);
            if (is == null) {
                System.out.println("Ficheiro de jogadores não encontrado: " + playersFile);
                return players;
            }

            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            JSONParser parser = new JSONParser();
            JSONArray array = (JSONArray) parser.parse(reader);

            for (Object obj : array) {
                JSONObject jsonPlayer = (JSONObject) obj;

                String name = (String) jsonPlayer.get("name");
                String birthDateStr = (String) jsonPlayer.get("birthDate");
                LocalDate birthDate = LocalDate.parse(birthDateStr);
                String nationality = (String) jsonPlayer.get("nationality");
                String positionStr = (String) jsonPlayer.get("basePosition");
                String photo = (String) jsonPlayer.get("photo");
                int number = ((Long) jsonPlayer.get("number")).intValue();

                // Valores simulados ou default
                int shooting = 60;
                int passing = 65;
                int stamina = 70;
                int speed = 68;
                float height = 1.75f;
                float weight = 70.0f;
                PreferredFoot foot = PreferredFoot.Right;

                IPlayerPosition position = new PlayerPositionImpl(positionStr);

                PlayerImpl player = new PlayerImpl(name, number, birthDate, nationality,
                        height, weight, position, foot, photo,
                        shooting, passing, speed, stamina);

                players.add(player);
            }

        } catch (Exception e) {
            System.out.println("Erro ao carregar jogadores de " + playersFile + ": " + e.getMessage());
            e.printStackTrace();
        }

        return players;
    }
}
