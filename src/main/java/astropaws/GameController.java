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

    private Vector2D lastShipPos = null;
    private long lastVelTimeMs = 0;

    private final AudioEngine audioEngine;
    private final AnimalSpawner spawner;

    public GameController() {
        audioEngine = new AudioEngine();
        audioEngine.init();

        WavLoader.WavData meow = WavLoader.load("Audio/meow.wav");
        audioEngine.loadSound("meow", meow.pcm, meow.sampleRate);

        WavLoader.WavData beacon = WavLoader.load("Audio/beacon.wav");
        audioEngine.loadSound("beacon", beacon.pcm, beacon.sampleRate);

        audioEngine.createSource("cat", "meow", false, 1.0f);
        audioEngine.setLooping("cat", false);

        audioEngine.createSource("beacon", "beacon", true, 0.08f);
        audioEngine.setLooping("beacon", true);

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

            if (cat == null || beacon == null) {
                if (audioEngine.isPlaying("beacon")) {
                    audioEngine.stop("beacon");
                }
                // Reset velocity tracking so next spawn doesn't produce a huge jump
                lastShipPos = null;
                lastVelTimeMs = 0;
                return;
            } else {
                if (!audioEngine.isPlaying("beacon")) {
                    audioEngine.play("beacon");
                }
            }

            // Step 3: Doppler requires listener velocity
            long nowMs = System.currentTimeMillis();
            if (lastShipPos == null) {
                lastShipPos = ship.getPosition().copy();
                lastVelTimeMs = nowMs;
                audioEngine.setListenerVelocity(0f, 0f, 0f);
            } else {
                long dtMs = nowMs - lastVelTimeMs;
                if (dtMs <= 0) dtMs = 1;

                double dt = dtMs / 1000.0;
                double vx = (ship.getPosition().x - lastShipPos.x) / dt;
                double vy = (ship.getPosition().y - lastShipPos.y) / dt;

                // 2D (x,y) -> OpenAL (x,z)
                audioEngine.setListenerVelocity((float) vx, 0f, (float) vy);

                lastShipPos.x = ship.getPosition().x;
                lastShipPos.y = ship.getPosition().y;
                lastVelTimeMs = nowMs;
            }

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

            audioEngine.setSourcePosition(
                    "beacon",
                    (float) beacon.getPosition().x, 0f, (float) beacon.getPosition().y
            );

            // Step 3: source velocity (cat/beacon are stationary for now)
            audioEngine.setSourceVelocity("cat", 0f, 0f, 0f);
            audioEngine.setSourceVelocity("beacon", 0f, 0f, 0f);

            double toCatX = beacon.getPosition().x - ship.getPosition().x;
            double toCatY = beacon.getPosition().y - ship.getPosition().y;

            double dist = Math.sqrt(toCatX * toCatX + toCatY * toCatY);
            if (dist > 0.0) {
                toCatX /= dist;
                toCatY /= dist;
            }

            double dot = forward.x * toCatX + forward.y * toCatY;

            double clamped = Math.max(-1.0, Math.min(1.0, dot));
            double t = (clamped + 1.0) / 2.0;

            float pitch = (float) (0.6 + 1.0 * t);   // 0.6 .. 1.6
            float gain = (float) (0.04 + 0.10 * t);  // 0.04 .. 0.14

            audioEngine.setSourcePitch("beacon", pitch);
            audioEngine.setSourceGain("beacon", gain);

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
