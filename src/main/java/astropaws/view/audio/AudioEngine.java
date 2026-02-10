package astropaws.view.audio;

import org.lwjgl.openal.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.alSpeedOfSound;
import static org.lwjgl.openal.ALC10.*;

public class AudioEngine {

    private long device;
    private long context;

    private final Map<String, Integer> buffers = new HashMap<>();
    private final Map<String, Integer> sources = new HashMap<>();

    // ---------- INIT ----------

    public void init() {
        device = alcOpenDevice((CharSequence) null);
        if (device == MemoryUtil.NULL) {
            throw new IllegalStateException("Failed to open OpenAL device");
        }

        context = alcCreateContext(device, (int[]) null);
        alcMakeContextCurrent(context);

        AL.createCapabilities(ALC.createCapabilities(device));

        // Distance model (slides 20–22)
        alDistanceModel(AL_NONE);

        // Doppler (slide 25)
        alDopplerFactor(1.0f);
        alSpeedOfSound(343.3f);

        // Listener defaults
        setListenerPosition(0f, 0f, 0f);
        setListenerOrientation(0, 1, 0, 0, 0, -1);

        System.out.println("OpenAL initialized successfully");
    }

    // ---------- LISTENER ----------

    public void setListenerPosition(float x, float y, float z) {
        alListener3f(AL_POSITION, x, y, z);
    }

    public void setListenerOrientation(
            float atX, float atY, float atZ,
            float upX, float upY, float upZ
    ) {
        float[] orientation = new float[] {
                atX, atY, atZ,
                upX, upY, upZ
        };
        alListenerfv(AL_ORIENTATION, orientation);
    }

    // ---------- BUFFERS ----------

    public void loadSound(String bufferId, ShortBuffer pcmData, int sampleRate) {
        int buffer = alGenBuffers();
        alBufferData(buffer, AL_FORMAT_MONO16, pcmData, sampleRate);
        buffers.put(bufferId, buffer);
    }

    // ---------- SOURCES ----------

    public void createSource(String sourceId, String bufferId, boolean looping, float gain) {
        int source = alGenSources();

        alSourcei(source, AL_BUFFER, buffers.get(bufferId));
        alSourcei(source, AL_LOOPING, looping ? AL_TRUE : AL_FALSE);
        alSourcef(source, AL_GAIN, gain);

        // Attenuation tuning (slides 19–23)
        alSourcef(source, AL_REFERENCE_DISTANCE, 1.0f);
        alSourcef(source, AL_ROLLOFF_FACTOR, 0.0f);
        alSourcef(source, AL_MAX_DISTANCE, 50.0f);

        alSource3f(source, AL_POSITION, 0f, 0f, 0f);
        alSource3f(source, AL_VELOCITY, 0f, 0f, 0f);

        sources.put(sourceId, source);
    }

    public void play(String sourceId) {
        Integer source = sources.get(sourceId);
        if (source == null) return;

        int state = alGetSourcei(source, AL_SOURCE_STATE);
        if (state != AL_PLAYING) {
            alSourcePlay(source);
        }
    }

    public void setSourcePosition(String sourceId, float x, float y, float z) {
        Integer source = sources.get(sourceId);
        if (source != null) {
            alSource3f(source, AL_POSITION, x, y, z);
        }
    }

    // ---------- CLEANUP ----------

    public void cleanup() {
        for (int source : sources.values()) {
            alDeleteSources(source);
        }
        for (int buffer : buffers.values()) {
            alDeleteBuffers(buffer);
        }

        alcDestroyContext(context);
        alcCloseDevice(device);
    }

    public void setLooping(String sourceId, boolean loop) {
        Integer source = sources.get(sourceId);
        if (source != null) {
            alSourcei(source, AL_LOOPING, loop ? AL_TRUE : AL_FALSE);
        }
    }

}
