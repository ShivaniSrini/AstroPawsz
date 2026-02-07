package astropaws.model;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class Cat {
    private Vector2D position;
    private BufferedImage image;

    public Cat(double x, double y) {
        this.position = new Vector2D(x, y);

        try {
            image = ImageIO.read(
                    getClass().getResourceAsStream("/images/Cat.png")
            );
        } catch (IOException e) {
            System.err.println("Failed to load Cat image");
        }
    }

    public Vector2D getPosition() {
        return position;
    }

    public void render(Graphics2D g) {
        if (image != null) {
            g.drawImage(image,
                    (int) position.x,
                    (int) position.y,
                    64, 64,
                    null);
        }
    }
}
