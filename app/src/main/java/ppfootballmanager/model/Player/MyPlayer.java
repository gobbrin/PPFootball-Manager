package ppfootballmanager.model.Player;

import com.ppstudios.footballmanager.api.contracts.player.IPlayer;
import com.ppstudios.footballmanager.api.contracts.player.IPlayerPosition;
import com.ppstudios.footballmanager.api.contracts.player.PreferredFoot;

import java.io.IOException;
import java.time.LocalDate;

public class MyPlayer implements IPlayer {

    private String name;
    private LocalDate birthDate;
    private String nationality;
    private String photo;
    private int number;
    private int shooting;
    private int passing;
    private int stamina;
    private int speed;
    private float height;
    private float weight;
    private PreferredFoot preferredFoot;
    private IPlayerPosition position;

    public MyPlayer(String name, LocalDate birthDate, String nationality, String photo, int number,
                    int shooting, int passing, int stamina, int speed,
                    float height, float weight, PreferredFoot preferredFoot, IPlayerPosition position) {
        this.name = name;
        this.birthDate = birthDate;
        this.nationality = nationality;
        this.photo = photo;
        this.number = number;
        this.shooting = shooting;
        this.passing = passing;
        this.stamina = stamina;
        this.speed = speed;
        this.height = height;
        this.weight = weight;
        this.preferredFoot = preferredFoot;
        this.position = position;
    }

    // Getters obrigatórios da interface
    @Override public String getName() { return name; }
    @Override public LocalDate getBirthDate() { return birthDate; }
    @Override public int getAge() {
        return LocalDate.now().getYear() - birthDate.getYear();
    }
    @Override public String getNationality() { return nationality; }
    @Override public String getPhoto() { return photo; }
    @Override public int getNumber() { return number; }
    @Override public int getShooting() { return shooting; }
    @Override public int getPassing() { return passing; }
    @Override public int getStamina() { return stamina; }
    @Override public int getSpeed() { return speed; }
    @Override public IPlayerPosition getPosition() { return position; }
    @Override public float getHeight() { return height; }
    @Override public float getWeight() { return weight; }
    @Override public PreferredFoot getPreferredFoot() { return preferredFoot; }

    // Setter obrigatório
    @Override public void setPosition(IPlayerPosition position) {
        this.position = position;
    }

    // Método do IExporter
    @Override
    public void exportToJson() throws IOException {
        // Implementação simples: só imprimir ao terminal por agora
        System.out.println("{");
        System.out.println("  \"name\": \"" + name + "\",");
        System.out.println("  \"position\": \"" + position + "\",");
        System.out.println("  \"nationality\": \"" + nationality + "\"");
        System.out.println("  // outros atributos omitidos por brevidade");
        System.out.println("}");
    }

    @Override
    public String toString() {
        return name + " (#" + number + ") - " + position + " - SHO:" + shooting + " PAS:" + passing;
    }
}
