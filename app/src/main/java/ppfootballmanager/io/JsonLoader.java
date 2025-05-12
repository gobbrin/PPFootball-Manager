package ppfootballmanager.io;


import com.ppstudios.footballmanager.api.contracts.player.IPlayer;
import com.ppstudios.footballmanager.api.contracts.player.IPlayerPosition;
import com.ppstudios.footballmanager.api.contracts.player.PreferredFoot;
import ppfootballmanager.model.Player.MyPlayer;
import ppfootballmanager.model.Player.MyPlayerPosition;


public class JsonLoader {

    public static List<IPlayer> loadPlayers(String playersFile) {
        List<IPlayer> players = new ArrayList<>();

        try {
            InputStream is = JsonLoader.class.getClassLoader().getResourceAsStream(playersFile);
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

                // Valores padrão
                int shooting = 60;
                int passing = 65;
                int stamina = 70;
                int speed = 68;
                float height = 1.75f;
                float weight = 70.0f;
                PreferredFoot foot = PreferredFoot.RIGHT;

                IPlayerPosition position = new MyPlayerPosition(positionStr);

                MyPlayer player = new MyPlayer(
                    name, birthDate, nationality, photo, number,
                    shooting, passing, stamina, speed,
                    height, weight, foot, position
                );

                players.add(player);
            }

        } catch (Exception e) {
            System.out.println("Erro ao carregar jogadores de " + playersFile + ": " + e.getMessage());
            e.printStackTrace();
        }

        return players;
    }
}