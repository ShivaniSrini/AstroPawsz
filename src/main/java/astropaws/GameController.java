package astropaws;

import astropaws.controller.InputHandler;
import astropaws.model.AnimalSpawner;
import astropaws.model.AudioBeacon;
import astropaws.model.Cat;
import astropaws.model.Ship;
import astropaws.model.Vector2D;
import astropaws.view.GamePanel;
import astropaws.view.audio.AudioEngine;
import astropaws.view.audio.WavLoader;

public class GameController {

    private final GamePanel gamePanel;
    private final Ship ship;
    private final InputHandler inputHandler;
    private final GameLoop gameLoop;

    private double lastAngle = 0.0;
    private double rotationAccumulator = 0.0;

    private final AudioEngine audioEngine;
    private final AnimalSpawner spawner;

    public GameController() {
        audioEngine = new AudioEngine();
        audioEngine.init();

        WavLoader.WavData meow = WavLoader.load("Audio/Meow.wav");
        audioEngine.loadSound("meow", meow.pcm, meow.sampleRate);

        audioEngine.createSource("cat", "meow", false, 1.0f);
        audioEngine.setLooping("cat", false);

        gamePanel = new GamePanel();

        ship = new Ship(GamePanel.WIDTH / 2.0, GamePanel.HEIGHT / 2.0);
        gamePanel.setShip(ship);

        spawner = new AnimalSpawner(GamePanel.WIDTH, GamePanel.HEIGHT);

        inputHandler = new InputHandler();
        gamePanel.addKeyListener(inputHandler);

        gameLoop = new GameLoop();
    }

    public void start() {
        gameLoop.start();
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }

    private class GameLoop extends Thread {

        private static final int FPS = 60;
        private static final long FRAME_TIME_MS = 1000L / FPS;

        @Override
        public void run() {
            long lastTime = System.currentTimeMillis();

            while (true) {
                long now = System.currentTimeMillis();

                if (now - lastTime >= FRAME_TIME_MS) {
                    update();
                    gamePanel.repaint();
                    lastTime = now;
                }

                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {
                }
            }
        }

        private void update() {
            if (inputHandler.isLeftPressed()) ship.rotateLeft();
            if (inputHandler.isRightPressed()) ship.rotateRight();
            if (inputHandler.isThrustPressed()) ship.thrust();

            ship.update(GamePanel.WIDTH, GamePanel.HEIGHT);

            double currentAngle = ship.getAngle();
            double delta = Math.abs(currentAngle - lastAngle);
            if (delta > Math.PI) delta = (2.0 * Math.PI) - delta;
            rotationAccumulator += delta;
            lastAngle = currentAngle;

            spawner.update();
            Cat cat = spawner.getCat();
            AudioBeacon beacon = spawner.getBeacon();

            gamePanel.setCat(cat);
            if (cat == null || beacon == null) return;

            Vector2D forward = ship.getForwardVector();

            audioEngine.setListenerPosition(
                    (float) ship.getPosition().x, 0f, (float) ship.getPosition().y
            );

            audioEngine.setListenerOrientation(
                    (float) forward.x, 0f, (float) forward.y,
                    0f, 1f, 0f
            );

            audioEngine.setSourcePosition(
                    "cat",
                    (float) beacon.getPosition().x, 0f, (float) beacon.getPosition().y
            );

            double toCatX = beacon.getPosition().x - ship.getPosition().x;
            double toCatY = beacon.getPosition().y - ship.getPosition().y;

            double dist = Math.sqrt(toCatX * toCatX + toCatY * toCatY);
            if (dist > 0.0) {
                toCatX /= dist;
                toCatY /= dist;
            }

            double dot = forward.x * toCatX + forward.y * toCatY;

            gamePanel.setDebugData(
                    beacon.getPosition().x,
                    beacon.getPosition().y,
                    dot
            );

            if (rotationAccumulator > Math.toRadians(10.0) && dot > 0.995) {
                audioEngine.play("cat");
                rotationAccumulator = 0.0;
            }
        }
    }
}
