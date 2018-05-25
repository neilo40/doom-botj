import java.util.List;

public class Level {

    private String name;
    private List<Linedef> linedefs;
    private Vertex start;
    private Vertex exit;
    private List<DoorSwitch> doorSwitches;

    public List<Linedef> getLinedefs() {
        return linedefs;
    }

    public void setLinedefs(List<Linedef> linedefs) {
        this.linedefs = linedefs;
    }

    public Vertex getStart() {
        return start;
    }

    public void setStart(Vertex start) {
        this.start = start;
    }

    public Vertex getExit() {
        return exit;
    }

    public void setExit(Vertex exit) {
        this.exit = exit;
    }

    public List<DoorSwitch> getDoorSwitches() {
        return doorSwitches;
    }

    public void setDoorSwitches(List<DoorSwitch> doorSwitches) {
        this.doorSwitches = doorSwitches;
    }

    public Level(String name, List<Linedef> linedefs, Vertex start, Vertex exit, List<DoorSwitch> doorSwitches) {
        this.name = name;
        this.linedefs = linedefs;
        this.start = start;
        this.exit = exit;
        this.doorSwitches = doorSwitches;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
