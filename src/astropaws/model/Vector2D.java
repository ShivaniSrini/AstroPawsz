package astropaws.model;

public class Vector2D {
    public double x;
    public double y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D() {
        this(0, 0);
    }

    public void add(Vector2D other) {
        this.x += other.x;
        this.y += other.y;
    }

    public void multiply(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public void limit(double max) {
        if (magnitude() > max) {
            normalize();
            multiply(max);
        }
    }

    public void normalize() {
        double mag = magnitude();
        if (mag > 0) {
            x /= mag;
            y /= mag;
        }
    }

    public Vector2D normalized() {
        double mag = magnitude();
        if (mag == 0) return new Vector2D(0, 0);
        return new Vector2D(x / mag, y / mag);
    }


    public Vector2D copy() {
        return new Vector2D(x, y);
    }

}