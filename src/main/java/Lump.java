import java.util.List;

public class Lump {
    private String name;
    private List<Byte> data;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Byte> getData() {
        return data;
    }

    public void setData(List<Byte> data) {
        this.data = data;
    }

    public Lump(String name, List<Byte> data) {

        this.name = name;
        this.data = data;
    }
}
