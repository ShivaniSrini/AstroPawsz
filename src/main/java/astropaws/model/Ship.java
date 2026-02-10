package astropaws.model;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class Ship {
    private Vector2D position;
    private Vector2D velocity;
    private double angle; // in radians
    private double rotationSpeed = 0.03;
    private double acceleration = 0.3;
    private double maxSpeed = 12.0;
    private double friction = 0.3;

    private BufferedImage shipImage;
    private static final int SHIP_WIDTH = 70;
    private static final int SHIP_HEIGHT = 70;

    // Ship shape points (triangle pointing up) - fallback if image doesn't load
    private int[] xPoints = {0, -15, 15};
    private int[] yPoints = {-20, 20, 20};

    public Ship(double x, double y) {
        this.position = new Vector2D(x, y);
        this.velocity = new Vector2D(0, 0);
        this.angle = -Math.PI; // pointing up

        // Load ship image
        try {
            shipImage = ImageIO.read(
                    getClass().getResourceAsStream("/images/ship.png")
            );
            System.out.println("Ship image loaded successfully!");
        } catch (IOException e) {
            System.err.println("Could not load ship image: " + e.getMessage());
            shipImage = null;
        }
    }

    public Vector2D getForwardVector() {
        double visualAngle = angle + Math.PI / 2.0; // MUST match render()
        Vector2D v = new Vector2D(Math.cos(visualAngle), Math.sin(visualAngle));
        v.normalize();
        return v;
    }

    public void rotateLeft() {
        angle -= rotationSpeed;
    }

    public void rotateRight() {
        angle += rotationSpeed;
    }

    public void thrust() {
        Vector2D fwd = getForwardVector();
        velocity.x = fwd.x * maxSpeed;
        velocity.y = fwd.y * maxSpeed;
    }

    public void update(int screenWidth, int screenHeight) {
        // Apply friction
        velocity.multiply(friction);

        // Stop completely if velocity is tiny
        if (velocity.magnitude() < 0.05) {
            velocity.x = 0;
            velocity.y = 0;
        }

        // Update position
        position.add(velocity);

        // Wrap around screen edges
        if (position.x < 0) position.x = screenWidth;
        if (position.x > screenWidth) position.x = 0;
        if (position.y < 0) position.y = screenHeight;
        if (position.y > screenHeight) position.y = 0;
    }

    public void render(Graphics2D g) {
        AffineTransform old = g.getTransform();

        // Translate to ship position and rotate
        g.translate(position.x, position.y);
        g.rotate(angle + Math.PI / 2); // +90 degrees to point ship correctly

        if (shipImage != null) {
            // Draw ship image
            g.drawImage(shipImage,
                    -SHIP_WIDTH / 2, -SHIP_HEIGHT / 2,
                    SHIP_WIDTH, SHIP_HEIGHT, null);
        } else {
            // Draw fallback triangle if image doesn't load
            g.setColor(Color.WHITE);
            g.fillPolygon(xPoints, yPoints, 3);
            g.setColor(new Color(100, 149, 237));
            g.setStroke(new BasicStroke(2));
            g.drawPolygon(xPoints, yPoints, 3);
        }

        g.setTransform(old);
    }

    // Getters
    public Vector2D getPosition() {
        return position;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public double getAngle() {
        return angle;
    }
}