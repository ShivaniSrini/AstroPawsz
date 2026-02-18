package astropaws;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        GameController controller = new GameController();

        JFrame frame = new JFrame("AstroPaws");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(controller.getGamePanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Important: ensure key events (SPACE) are captured
        controller.getGamePanel().requestFocusInWindow();

        controller.start();
    }
}
