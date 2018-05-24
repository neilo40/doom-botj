public class Thing {

    private Vertex position;
    private Integer facing;
    private Integer doomId;

    public Vertex getPosition() {
        return position;
    }

    public void setPosition(Vertex position) {
        this.position = position;
    }

    public Integer getFacing() {
        return facing;
    }

    public void setFacing(Integer facing) {
        this.facing = facing;
    }

    public Integer getDoomId() {
        return doomId;
    }

    public void setDoomId(Integer doomId) {
        this.doomId = doomId;
    }

    public Thing(Vertex position, Integer facing, Integer doomId) {

        this.position = position;
        this.facing = facing;
        this.doomId = doomId;
    }
}
