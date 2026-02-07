package astropaws;

import astropaws.model.*;
import astropaws.view.audio.AudioEngine;
import astropaws.controller.InputHandler;
import astropaws.view.GamePanel;
import astropaws.view.audio.WavLoader;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.*;

public class GameController {

    private GamePanel gamePanel;
    private Ship ship;
    private InputHandler inputHandler;
    private GameLoop gameLoop;

    private double lastAngle = 0;
    private double rotationAccumulator = 0;

    private AudioEngine audioEngine;
    private AnimalSpawner spawner;

    public GameController() {

        audioEngine = new AudioEngine();
        audioEngine.init();

        var meow = WavLoader.load("Audio/Meow.wav");
        audioEngine.loadSound("meow", meow.pcm, meow.sampleRate);

        audioEngine.createSource("cat", "meow", false, 1.0f);
        audioEngine.setLooping("cat", false);

        alDopplerFactor(1.0f);
        alSpeedOfSound(343.3f);

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
        private static final long FRAME_TIME = 1000 / FPS;

        @Override
        public void run() {
            long lastTime = System.currentTimeMillis();

            while (true) {
                long now = System.currentTimeMillis();

                if (now - lastTime >= FRAME_TIME) {
                    update();
                    gamePanel.repaint();
                    lastTime = now;
                }

                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {}
            }
        }

        private void update() {

            if (inputHandler.isLeftPressed()) ship.rotateLeft();
            if (inputHandler.isRightPressed()) ship.rotateRight();
            if (inputHandler.isThrustPressed()) ship.thrust();

            ship.update(GamePanel.WIDTH, GamePanel.HEIGHT);

            double currentAngle = ship.getAngle();
            double delta = Math.abs(currentAngle - lastAngle);
            if (delta > Math.PI) delta = (2 * Math.PI) - delta;
            rotationAccumulator += delta;
            lastAngle = currentAngle;

            spawner.update();
            Cat cat = spawner.getCat();
            AudioBeacon beacon = spawner.getBeacon();

            gamePanel.setCat(cat);

            if (cat == null || beacon == null) return;

            // Listener
            alListener3f(
                    AL_POSITION,
                    (float) ship.getPosition().x,
                    0f,
                    (float) ship.getPosition().y
            );

            // Source
            audioEngine.setSourcePosition(
                    "cat",
                    (float) beacon.getPosition().x,
                    0f,
                    (float) beacon.getPosition().y
            );

            // Facing math
            Vector2D forward = ship.getForwardVector();
            double shipFx = Math.cos(ship.getAngle());
            double shipFy = -Math.sin(ship.getAngle());


            double toCatX = beacon.getPosition().x - ship.getPosition().x;
            double toCatY = beacon.getPosition().y - ship.getPosition().y;

            double distance = Math.sqrt(toCatX * toCatX + toCatY * toCatY);
            if (distance > 0) {
                toCatX /= distance;
                toCatY /= distance;
            }

            double dot = shipFx * toCatX + shipFy * toCatY;


            //PASS DEBUG INFO TO PANEL
            gamePanel.setDebugData(
                    beacon.getPosition().x,
                    beacon.getPosition().y,
                    dot
            );

            if (rotationAccumulator > Math.toRadians(10) && dot > 0.3) {
                audioEngine.play("cat");
                rotationAccumulator = 0;
            }
        }
    }
}
