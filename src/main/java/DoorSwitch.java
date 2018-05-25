public class DoorSwitch {
    private Vertex a;
    private Vertex b;
    private Boolean switched;

    public static DoorSwitch fromWadLine(Linedef wadLine) {
        return new DoorSwitch(wadLine.getA(), wadLine.getB(), Boolean.FALSE);
    }

    public Vertex getA() {
        return a;
    }

    public void setA(Vertex a) {
        this.a = a;
    }

    public Vertex getB() {
        return b;
    }

    public void setB(Vertex b) {
        this.b = b;
    }

    public Boolean getSwitched() {
        return switched;
    }

    public void setSwitched(Boolean switched) {
        this.switched = switched;
    }

    public DoorSwitch(Vertex a, Vertex b, Boolean switched) {

        this.a = a;
        this.b = b;
        this.switched = switched;
    }
}
