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

    // Capture tuning
    private static final double CAPTURE_DISTANCE_PX = 200.0;
    private static final long CAPTURE_COOLDOWN_MS = 400;
    private static final long KACHING_DELAY_MS = 200;

    // Sticky precision rotation
    private static final double STICKY_ROTATION_MULTIPLIER = 0.25;

    // Distance-adaptive alignment thresholds:
    // Far away = strict, close = forgiving
    private static final double ALIGN_DOT_FAR = 0.995;
    private static final double ALIGN_DOT_NEAR = 0.970;
    private static final double ALIGN_NEAR_DIST = 120.0;
    private static final double ALIGN_FAR_DIST = 600.0;

    // Range feedback tuning
    private static final float RANGE_BOOST_PITCH = 1.12f;
    private static final float RANGE_BOOST_GAIN = 1.35f;
    private static final long RANGE_PULSE_PERIOD_MS = 650;
    private static final float RANGE_PULSE_DEPTH = 0.35f;

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

    private boolean prevShootPressed = false;
    private long lastCaptureAttemptMs = 0;
    private long pendingKachingAtMs = -1;

    private int score = 0;

    // Range state for one-time cue
    private boolean wasInRangeLastFrame = false;

    public GameController() {
        audioEngine = new AudioEngine();
        audioEngine.init();

        WavLoader.WavData meow = WavLoader.load("Audio/meow.wav");
        audioEngine.loadSound("meow", meow.pcm, meow.sampleRate);

        WavLoader.WavData beacon = WavLoader.load("Audio/beacon.wav");
        audioEngine.loadSound("beacon", beacon.pcm, beacon.sampleRate);

        WavLoader.WavData whoosh = WavLoader.load("Audio/whoosh.wav");
        audioEngine.loadSound("whoosh", whoosh.pcm, whoosh.sampleRate);

        WavLoader.WavData kaching = WavLoader.load("Audio/kaching.wav");
        audioEngine.loadSound("kaching", kaching.pcm, kaching.sampleRate);

        audioEngine.createSource("cat", "meow", false, 0.08f);
        audioEngine.setLooping("cat", false);

        audioEngine.createSource("beacon", "beacon", true, 0.08f);
        audioEngine.setLooping("beacon", true);

        audioEngine.createSource("whoosh", "whoosh", false, 1.0f);
        audioEngine.setLooping("whoosh", false);

        audioEngine.createSource("kaching", "kaching", false, 1.0f);
        audioEngine.setLooping("kaching", false);

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

    private static double dynamicAlignThreshold(double distancePx) {
        if (distancePx <= ALIGN_NEAR_DIST) return ALIGN_DOT_NEAR;
        if (distancePx >= ALIGN_FAR_DIST) return ALIGN_DOT_FAR;

        double t = (distancePx - ALIGN_NEAR_DIST) / (ALIGN_FAR_DIST - ALIGN_NEAR_DIST); // 0..1
        return ALIGN_DOT_NEAR + (ALIGN_DOT_FAR - ALIGN_DOT_NEAR) * t;
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
                } catch (InterruptedException ignored) {}
            }
        }

        private void update() {
            long nowMs = System.currentTimeMillis();

            // Play delayed kaching if scheduled
            if (pendingKachingAtMs > 0 && nowMs >= pendingKachingAtMs) {
                audioEngine.playFromStart("kaching");
                pendingKachingAtMs = -1;
            }

            // Spawn/update target
            spawner.update();
            Cat cat = spawner.getCat();
            AudioBeacon beacon = spawner.getBeacon();
            gamePanel.setCat(cat);

            // No target: stop beacon and reset tracking + range state
            if (cat == null || beacon == null) {
                if (audioEngine.isPlaying("beacon")) {
                    audioEngine.stop("beacon");
                }
                lastShipPos = null;
                lastVelTimeMs = 0;
                prevShootPressed = inputHandler.isShootPressed();
                wasInRangeLastFrame = false;
                return;
            }

            // Ensure beacon is playing
            if (!audioEngine.isPlaying("beacon")) {
                audioEngine.play("beacon");
            }

            // Pre-dot to decide sticky rotation BEFORE changing angle
            double dx0 = beacon.getPosition().x - ship.getPosition().x;
            double dy0 = beacon.getPosition().y - ship.getPosition().y;
            double dist0 = Math.sqrt(dx0 * dx0 + dy0 * dy0);

            double toCatX0 = dx0;
            double toCatY0 = dy0;
            if (dist0 > 0.0) {
                toCatX0 /= dist0;
                toCatY0 /= dist0;
            }

            Vector2D forward0 = ship.getForwardVector();
            double dot0 = forward0.x * toCatX0 + forward0.y * toCatY0;

            double alignThreshold0 = dynamicAlignThreshold(dist0);
            boolean alignedForSticky = dot0 > alignThreshold0;

            // Input: rotation (sticky if aligned), thrust
            boolean leftPressed = inputHandler.isLeftPressed();
            boolean rightPressed = inputHandler.isRightPressed();

            if (alignedForSticky) {
                if (leftPressed) ship.rotateLeft(STICKY_ROTATION_MULTIPLIER);
                if (rightPressed) ship.rotateRight(STICKY_ROTATION_MULTIPLIER);
            } else {
                if (leftPressed) ship.rotateLeft();
                if (rightPressed) ship.rotateRight();
            }

            if (inputHandler.isThrustPressed()) {
                ship.thrust();
            }

            // Movement update AFTER inputs
            ship.update(GamePanel.WIDTH, GamePanel.HEIGHT);

            // Track rotation amount (for alignment meow trigger)
            double currentAngle = ship.getAngle();
            double delta = Math.abs(currentAngle - lastAngle);
            if (delta > Math.PI) delta = (2.0 * Math.PI) - delta;
            rotationAccumulator += delta;
            lastAngle = currentAngle;

            // Listener velocity (Doppler)
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

                audioEngine.setListenerVelocity((float) vx, 0f, (float) vy);

                lastShipPos.x = ship.getPosition().x;
                lastShipPos.y = ship.getPosition().y;
                lastVelTimeMs = nowMs;
            }

            // Recompute forward/dot/distance AFTER rotation + movement
            Vector2D forward = ship.getForwardVector();

            double dx = beacon.getPosition().x - ship.getPosition().x;
            double dy = beacon.getPosition().y - ship.getPosition().y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            double toCatX = dx;
            double toCatY = dy;
            if (distance > 0.0) {
                toCatX /= distance;
                toCatY /= distance;
            }

            double dot = forward.x * toCatX + forward.y * toCatY;

            double alignThreshold = dynamicAlignThreshold(distance);
            boolean alignedNow = dot > alignThreshold;

            boolean inRangeNow = distance <= CAPTURE_DISTANCE_PX;

            // One-time range entry cue (uses existing meow for now)
            if (inRangeNow && !wasInRangeLastFrame) {
                audioEngine.playFromStart("cat");
            }
            wasInRangeLastFrame = inRangeNow;

            // Listener + orientation
            audioEngine.setListenerPosition(
                    (float) ship.getPosition().x, 0f, (float) ship.getPosition().y
            );

            audioEngine.setListenerOrientation(
                    (float) forward.x, 0f, (float) forward.y,
                    0f, 1f, 0f
            );

            // Source position
            audioEngine.setSourcePosition(
                    "cat",
                    (float) beacon.getPosition().x, 0f, (float) beacon.getPosition().y
            );

            audioEngine.setSourcePosition(
                    "beacon",
                    (float) beacon.getPosition().x, 0f, (float) beacon.getPosition().y
            );

            // Static sources for now
            audioEngine.setSourceVelocity("cat", 0f, 0f, 0f);
            audioEngine.setSourceVelocity("beacon", 0f, 0f, 0f);

            // Beacon guidance modulation by alignment
            double clampedDot = Math.max(-1.0, Math.min(1.0, dot));
            double t = (clampedDot + 1.0) / 2.0; // [-1..1] -> [0..1]

            float basePitch = (float) (0.6 + 1.0 * t);   // 0.6 .. 1.6
            float baseGain = (float) (0.04 + 0.10 * t);  // 0.04 .. 0.14

            float pitch = basePitch;
            float gain = baseGain;

            // In-range cue: boost pitch/gain and pulse gain smoothly
            if (inRangeNow) {
                pitch = pitch * RANGE_BOOST_PITCH;

                double phase = (nowMs % RANGE_PULSE_PERIOD_MS) / (double) RANGE_PULSE_PERIOD_MS; // 0..1
                double pulse01 = 0.5 - 0.5 * Math.cos(2.0 * Math.PI * phase); // 0..1
                float pulseFactor = (float) ((1.0 - RANGE_PULSE_DEPTH) + RANGE_PULSE_DEPTH * pulse01);

                gain = gain * RANGE_BOOST_GAIN * pulseFactor;
            }

            audioEngine.setSourcePitch("beacon", pitch);
            audioEngine.setSourceGain("beacon", gain);

            // Debug visuals
            gamePanel.setDebugData(
                    beacon.getPosition().x,
                    beacon.getPosition().y,
                    dot
            );

            // Alignment meow trigger (rotate + aligned using dynamic threshold)
            if (rotationAccumulator > Math.toRadians(10.0) && alignedNow) {
                audioEngine.playFromStart("cat");
                rotationAccumulator = 0.0;
            }

            // Capture on SPACE (edge-triggered + cooldown)
            boolean shootPressed = inputHandler.isShootPressed();
            boolean shootJustPressed = shootPressed && !prevShootPressed;
            prevShootPressed = shootPressed;

            if (shootJustPressed && (nowMs - lastCaptureAttemptMs) >= CAPTURE_COOLDOWN_MS) {
                lastCaptureAttemptMs = nowMs;

                boolean closeEnough = distance <= CAPTURE_DISTANCE_PX;

                // Always play whoosh so SPACE always has feedback
                audioEngine.playFromStart("whoosh");

                if (alignedNow && closeEnough) {
                    pendingKachingAtMs = nowMs + KACHING_DELAY_MS;

                    score++;
                    System.out.println("Captured! Score = " + score);

                    spawner.despawnNow();
                } else {
                    System.out.println(
                            "Missed capture. aligned=" + alignedNow +
                                    " close=" + closeEnough +
                                    " dot=" + dot +
                                    " dist=" + distance +
                                    " threshold=" + alignThreshold
                    );
                }
            }
        }
    }
}
