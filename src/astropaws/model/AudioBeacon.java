package astropaws.model;

public class AudioBeacon {
    private Vector2D position;

    public AudioBeacon(double x, double y) {
        this.position = new Vector2D(x, y);
    }

    public Vector2D getPosition() {
        return position;
    }
}
