import java.util.Optional;

public class Sidedef {
    private Integer sectorTag;

    public Integer getSectorTag() {
        return sectorTag;
    }

    public void setSectorTag(Integer sectorTag) {
        this.sectorTag = sectorTag;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public Sidedef(Integer sectorTag, Sector sector) {

        this.sectorTag = sectorTag;
        this.sector = sector;
    }

    private Sector sector;
}
