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

    // Scale pixels → OpenAL world units (meters)
    // 800px ≈ 8 meters
    private static final float WORLD_SCALE = 0.01f;

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

        // Stronger distance model (better for blind-first gameplay)
        alDistanceModel(AL_INVERSE_DISTANCE_CLAMPED);

        // Doppler configuration
        alDopplerFactor(1.0f);
        alSpeedOfSound(343.3f);

        // Listener defaults
        setListenerPosition(0f, 0f, 0f);
        setListenerOrientation(0, 0, -1, 0, 1, 0);

        System.out.println("OpenAL initialized successfully");
    }

    // ---------- LISTENER ----------

    public void setListenerPosition(float x, float y, float z) {
        alListener3f(AL_POSITION, x * WORLD_SCALE, y * WORLD_SCALE, z * WORLD_SCALE);
    }

    public void setListenerVelocity(float x, float y, float z) {
        alListener3f(AL_VELOCITY, x * WORLD_SCALE, y * WORLD_SCALE, z * WORLD_SCALE);
    }

    public void setListenerOrientation(
            float atX, float atY, float atZ,
            float upX, float upY, float upZ
    ) {
        float[] orientation = new float[]{
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

        // Distance tuning (scaled to pixel world)
        alSourcef(source, AL_REFERENCE_DISTANCE, 1.0f);  // 1 meter (~100px)
        alSourcef(source, AL_ROLLOFF_FACTOR, 2.5f);      // stronger falloff
        alSourcef(source, AL_MAX_DISTANCE, 20.0f);       // 20 meters (~2000px)

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

    public void playFromStart(String sourceId) {
        Integer source = sources.get(sourceId);
        if (source != null) {
            alSourceStop(source);
            alSourceRewind(source);
            alSourcePlay(source);
        }
    }

    public void stop(String sourceId) {
        Integer source = sources.get(sourceId);
        if (source != null) {
            alSourceStop(source);
        }
    }

    public boolean isPlaying(String sourceId) {
        Integer source = sources.get(sourceId);
        if (source == null) return false;
        return alGetSourcei(source, AL_SOURCE_STATE) == AL_PLAYING;
    }

    public void setSourcePosition(String sourceId, float x, float y, float z) {
        Integer source = sources.get(sourceId);
        if (source != null) {
            alSource3f(source,
                    AL_POSITION,
                    x * WORLD_SCALE,
                    y * WORLD_SCALE,
                    z * WORLD_SCALE
            );
        }
    }

    public void setSourceVelocity(String sourceId, float x, float y, float z) {
        Integer source = sources.get(sourceId);
        if (source != null) {
            alSource3f(source,
                    AL_VELOCITY,
                    x * WORLD_SCALE,
                    y * WORLD_SCALE,
                    z * WORLD_SCALE
            );
        }
    }

    public void setSourcePitch(String sourceId, float pitch) {
        Integer source = sources.get(sourceId);
        if (source != null) {
            alSourcef(source, AL_PITCH, pitch);
        }
    }

    public void setSourceGain(String sourceId, float gain) {
        Integer source = sources.get(sourceId);
        if (source != null) {
            alSourcef(source, AL_GAIN, gain);
        }
    }

    public void setLooping(String sourceId, boolean loop) {
        Integer source = sources.get(sourceId);
        if (source != null) {
            alSourcei(source, AL_LOOPING, loop ? AL_TRUE : AL_FALSE);
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
}
