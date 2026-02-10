package astropaws.view.audio;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;

public class OpenALTest {
    public static void main(String[] args) {
        long device = ALC10.alcOpenDevice((java.nio.ByteBuffer) null);
        long context = ALC10.alcCreateContext(device, (int[]) null);

        ALC10.alcMakeContextCurrent(context);
        AL.createCapabilities(ALC.createCapabilities(device));

        System.out.println("OpenAL initialized successfully!");

        ALC10.alcDestroyContext(context);
        ALC10.alcCloseDevice(device);
    }
}
