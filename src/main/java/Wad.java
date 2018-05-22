import java.util.ArrayList;

public class Wad {

    public String getWadType() {
        return wadType;
    }

    public void setWadType(String wadType) {
        this.wadType = wadType;
    }

    private String wadType;
    private ArrayList<Level> levels;

    public ArrayList<Level> getLevels() {
        return levels;
    }

    public void setLevels(ArrayList<Level> levels) {
        this.levels = levels;
    }

    public Wad(String wadType, ArrayList<Level> levels) {
        this.wadType = wadType;
        this.levels = levels;
    }
}
