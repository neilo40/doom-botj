public class Sector {

    private Integer sectorType;
    private Integer tag;
    private Integer floorHeight;
    private Integer ceilingHeight;

    public Integer getSectorType() {
        return sectorType;
    }

    public void setSectorType(Integer sectorType) {
        this.sectorType = sectorType;
    }

    public Integer getTag() {
        return tag;
    }

    public void setTag(Integer tag) {
        this.tag = tag;
    }

    public Integer getFloorHeight() {
        return floorHeight;
    }

    public void setFloorHeight(Integer floorHeight) {
        this.floorHeight = floorHeight;
    }

    public Integer getCeilingHeight() {
        return ceilingHeight;
    }

    public void setCeilingHeight(Integer ceilingHeight) {
        this.ceilingHeight = ceilingHeight;
    }

    public Sector(Integer sectorType, Integer tag, Integer floorHeight, Integer ceilingHeight) {

        this.sectorType = sectorType;
        this.tag = tag;
        this.floorHeight = floorHeight;
        this.ceilingHeight = ceilingHeight;
    }
}
