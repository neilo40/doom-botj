import java.lang.Math;

public class Vertex {
    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public Vertex(Double x, Double y) {
        this.x = x;
        this.y = y;
    }

    public void setY(Double y) {
        this.y = y;

    }

    private Double x;
    private Double y;

    public Vertex add(Vertex that) {
        return new Vertex(x + that.getX(), y + that.getY());
    }

    public Boolean isCloseTo(Vertex that, Double range) {
        Double distance = Math.sqrt(Math.pow(that.getY() - y, 2) + Math.pow(that.getX() - x, 2));
        return distance < range;
    }

    public Double distanceTo(Vertex that) {
        Double height = that.getY() - y;
        Double width = that.getX() - x;
        return Math.sqrt((height * height) + (width * width));
    }
}
