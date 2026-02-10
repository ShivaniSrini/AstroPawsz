package astropaws.controller;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public class InputHandler extends KeyAdapter {
    private Set<Integer> pressedKeys = new HashSet<>();

    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }

    public boolean isKeyPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }

    public boolean isLeftPressed() {
        return isKeyPressed(KeyEvent.VK_LEFT) || isKeyPressed(KeyEvent.VK_A);
    }

    public boolean isRightPressed() {
        return isKeyPressed(KeyEvent.VK_RIGHT) || isKeyPressed(KeyEvent.VK_D);
    }

    public boolean isThrustPressed() {
        return isKeyPressed(KeyEvent.VK_UP) || isKeyPressed(KeyEvent.VK_W);
    }

    public boolean isShootPressed() {
        return isKeyPressed(KeyEvent.VK_SPACE);
    }
}