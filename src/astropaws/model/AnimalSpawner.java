package astropaws.model;

import java.util.Random;

public class AnimalSpawner {

    private static final int ACTIVE_DURATION_MS = 8000;

    private Cat currentCat;
    private AudioBeacon beacon;

    private long spawnTime;
    private boolean active = false;

    private final Random random = new Random();

    private final int screenWidth;
    private final int screenHeight;

    public AnimalSpawner(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void update() {
        long now = System.currentTimeMillis();

        if (!active) {
            spawnCat(now);
        } else if (now - spawnTime >= ACTIVE_DURATION_MS) {
            despawnCat();
        }
    }

    private void spawnCat(long now) {
        double x = random.nextInt(screenWidth - 64);
        double y = random.nextInt(screenHeight - 64);

        currentCat = new Cat(x, y);
        beacon = new AudioBeacon(x, y);

        spawnTime = now;
        active = true;
    }

    private void despawnCat() {
        currentCat = null;
        beacon = null;
        active = false;
    }

    public Cat getCat() {
        return currentCat;
    }

    public AudioBeacon getBeacon() {
        return beacon;
    }

    public boolean isActive() {
        return active;
    }
}
