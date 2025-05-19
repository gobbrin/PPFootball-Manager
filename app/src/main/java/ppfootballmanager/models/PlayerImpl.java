package ppfootballmanager.models;

import java.io.IOException; // JSON
import java.io.FileWriter; // JSON
import java.time.LocalDate; // Date
import java.time.Period; // Date

import org.json.simple.JSONObject;

import com.ppstudios.footballmanager.api.contracts.player.IPlayer;
import com.ppstudios.footballmanager.api.contracts.player.IPlayerPosition;
import com.ppstudios.footballmanager.api.contracts.player.PreferredFoot;

public class PlayerImpl implements IPlayer {

    // All the attributes of the player
    private String name;
    private int number;
    private LocalDate birthDate;
    private String nationality;
    private float height;
    private float weight;
    private IPlayerPosition position;
    private PreferredFoot preferredFoot;
    private String photo;
    private int shooting;
    private int passing;
    private int speed;
    private int stamina;

    /**
     * Constructor for PlayerImpl
     * 
     * @param name          Player name
     * @param number        Player jersey number
     * @param birthDate     Player birth date
     * @param nationality   Player nationality
     * @param height        Player height in meters
     * @param weight        Player weight in kilograms
     * @param position      Player position on the field
     * @param preferredFoot Player preferred foot
     * @param photo         Path to player's photo
     * @param shooting      Shooting skill (0-100)
     * @param passing       Passing skill (0-100)
     * @param speed         Speed skill (0-100)
     * @param stamina       Stamina skill (0-100)
     */
    public PlayerImpl(String name, int number, LocalDate birthDate, String nationality,
            float height, float weight, IPlayerPosition position,
            PreferredFoot preferredFoot, String photo, int shooting,
            int passing, int speed, int stamina) {
        this.name = name;
        this.number = number;
        this.birthDate = birthDate;
        this.nationality = nationality;
        this.height = height;
        this.weight = weight;
        this.position = position;
        this.preferredFoot = preferredFoot;
        this.photo = photo;
        this.shooting = validateStat(shooting);
        this.passing = validateStat(passing);
        this.speed = validateStat(speed);
        this.stamina = validateStat(stamina);
    }

    /**
     * Validates a stat value to ensure it's between 0 and 100
     * 
     * @param value The stat value to validate
     * @return The validated stat value
     */
    private int validateStat(int value) {
        return Math.max(0, Math.min(100, value));
    }

    @Override
    public void exportToJson() throws IOException {
        JSONObject playerJson = new JSONObject();
        playerJson.put("name", this.name);
        playerJson.put("number", this.number);
        playerJson.put("birthDate", this.birthDate.toString());
        playerJson.put("nationality", this.nationality);
        playerJson.put("height", this.height);
        playerJson.put("weight", this.weight);
        playerJson.put("position", this.position.getDescription());
        playerJson.put("preferredFoot", this.preferredFoot.toString());
        playerJson.put("photo", this.photo);
        playerJson.put("shooting", this.shooting);
        playerJson.put("passing", this.passing);
        playerJson.put("speed", this.speed);
        playerJson.put("stamina", this.stamina);
        
        try (FileWriter file = new FileWriter("player_" + this.name.replace(" ", "_") + ".json")) {
            file.write(playerJson.toJSONString());
            file.flush();
        }
    }

    @Override
    public int getAge() {
        if (birthDate == null) {
            return 0;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    @Override
    public LocalDate getBirthDate() {
        return birthDate;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNationality() {
        return nationality;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public int getPassing() {
        return passing;
    }

    @Override
    public String getPhoto() {
        return photo;
    }

    @Override
    public IPlayerPosition getPosition() {
        return position;
    }

    @Override
    public PreferredFoot getPreferredFoot() {
        return preferredFoot;
    }

    @Override
    public int getShooting() {
        return shooting;
    }

    @Override
    public int getSpeed() {
        return speed;
    }

    @Override
    public int getStamina() {
        return stamina;
    }

    @Override
    public float getWeight() {
        return weight;
    }

    @Override
    public void setPosition(IPlayerPosition newPosition) {
        this.position = newPosition;
    }
    
    /**
     * Sets the player's jersey number
     * 
     * @param number The new jersey number
     */
    public void setNumber(int number) {
        this.number = number;
    }
    
    /**
     * Returns the overall rating of the player based on their attributes
     * 
     * @return The player's overall rating (0-100)
     */
    public int getOverallRating() {
        return (shooting + passing + speed + stamina) / 4;
    }
    
    @Override
    public String toString() {
        return "#" + number + " " + name + " (" + position.getDescription() + ")";
    }
}
