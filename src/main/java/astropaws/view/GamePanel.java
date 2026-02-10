package astropaws.view;

import astropaws.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class GamePanel extends JPanel {

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    private Ship ship;
    private Cat cat;
    private BufferedImage backgroundImage;

    // DEBUG DATA FROM CONTROLLER
    private double beaconX;
    private double beaconY;
    private double dotProduct;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);

        try {
            backgroundImage = ImageIO.read(
                    getClass().getResourceAsStream("/images/Space.png")
            );
        } catch (IOException e) {
            backgroundImage = null;
        }
    }

    public void setShip(Ship ship) {
        this.ship = ship;
    }

    public void setCat(Cat cat) {
        this.cat = cat;
    }

    // CALLED EVERY FRAME
    public void setDebugData(double bx, double by, double dot) {
        this.beaconX = bx;
        this.beaconY = by;
        this.dotProduct = dot;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT, null);
        }

        if (cat != null) cat.render(g2d);
        if (ship != null) ship.render(g2d);

        if (ship != null && cat != null) {
            drawDebugLines(g2d);
        }
    }

    private void drawDebugLines(Graphics2D g2d) {

        float shipX = (float) ship.getPosition().x;
        float shipY = (float) ship.getPosition().y;

        float catX = (float) beaconX;
        float catY = (float) beaconY;

        // Ship → Cat line
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawLine(
                (int) shipX, (int) shipY,
                (int) catX, (int) catY
        );

        // Ship forward vector
        Vector2D fwd = ship.getForwardVector();
        float fx = (float) fwd.x;
        float fy = (float) fwd.y;

        int len = 50;
        int endX = (int) (shipX + fx * len);
        int endY = (int) (shipY + fy * len);


        g2d.setColor(dotProduct > 0.995 ? Color.GREEN : Color.RED);
        g2d.drawLine(
                (int) shipX, (int) shipY,
                endX, endY
        );

        g2d.setStroke(new BasicStroke(2));

    }
}
