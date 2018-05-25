import java.util.List;

public class Wad {

    public String getWadType() {
        return wadType;
    }

    public void setWadType(String wadType) {
        this.wadType = wadType;
    }

    private String wadType;
    private List<Level> levels;

    public List<Level> getLevels() {
        return levels;
    }

    public void setLevels(List<Level> levels) {
        this.levels = levels;
    }

    public Wad(String wadType, List<Level> levels) {
        this.wadType = wadType;
        this.levels = levels;
    }
}
