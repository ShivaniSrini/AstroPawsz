package astropaws;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        // Create the game controller
        GameController controller = new GameController();

        // Create the window
        JFrame frame = new JFrame("AstroPaws");

        // Set up the window
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(controller.getGamePanel());
        frame.pack();
        frame.setLocationRelativeTo(null); // center the window
        frame.setVisible(true);

        // Start the game loop
        controller.start();
    }
}
