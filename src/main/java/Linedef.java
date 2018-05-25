public class Linedef {
    private Vertex a;
    private Vertex b;
    private Boolean nonTraversable;
    private Integer sectorTag;
    private Integer lineType;
    private Sidedef rightSideDef;
    private Sidedef leftSideDef;

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

    public Boolean getNonTraversable() {
        return nonTraversable;
    }

    public void setNonTraversable(Boolean nonTraversable) {
        this.nonTraversable = nonTraversable;
    }

    public Integer getSectorTag() {
        return sectorTag;
    }

    public void setSectorTag(Integer sectorTag) {
        this.sectorTag = sectorTag;
    }

    public Integer getLineType() {
        return lineType;
    }

    public void setLineType(Integer lineType) {
        this.lineType = lineType;
    }

    public Sidedef getRightSideDef() {
        return rightSideDef;
    }

    public void setRightSideDef(Sidedef rightSideDef) {
        this.rightSideDef = rightSideDef;
    }

    public Sidedef getLeftSideDef() {
        return leftSideDef;
    }

    public void setLeftSideDef(Sidedef leftSideDef) {
        this.leftSideDef = leftSideDef;
    }

    public Linedef(Vertex a, Vertex b, Boolean nonTraversable, Integer sectorTag, Integer lineType, Sidedef rightSideDef, Sidedef leftSideDef) {

        this.a = a;
        this.b = b;
        this.nonTraversable = nonTraversable;
        this.sectorTag = sectorTag;
        this.lineType = lineType;
        this.rightSideDef = rightSideDef;
        this.leftSideDef = leftSideDef;
    }

    public Vertex midpoint() {
        Double midX = a.getX() + (b.getX() - a.getX()) / 2;
        Double midY = a.getY() + (b.getY() - a.getY()) / 2;
        return new Vertex(midX, midY);
    }
}
