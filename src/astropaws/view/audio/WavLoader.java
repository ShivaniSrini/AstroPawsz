package astropaws.view.audio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;

public class WavLoader {

    public static class WavData {
        public final ShortBuffer pcm;
        public final int sampleRate;

        public WavData(ShortBuffer pcm, int sampleRate) {
            this.pcm = pcm;
            this.sampleRate = sampleRate;
        }
    }

    public static WavData load(String path) {
        try (InputStream is = WavLoader.class
                .getClassLoader()
                .getResourceAsStream(path)) {

            if (is == null) {
                throw new RuntimeException("Audio file not found: " + path);
            }

            BufferedInputStream bis = new BufferedInputStream(is);

            byte[] header = bis.readNBytes(44);
            ByteBuffer headerBuf = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);

            // WAV header checks
            if (headerBuf.getInt(0) != 0x46464952) { // "RIFF"
                throw new RuntimeException("Not a valid WAV file");
            }

            int channels = headerBuf.getShort(22);
            int sampleRate = headerBuf.getInt(24);
            int bitsPerSample = headerBuf.getShort(34);

            if (channels != 1 || bitsPerSample != 16) {
                throw new RuntimeException(
                        "Only 16-bit MONO WAV supported. Found: "
                                + channels + " channels, "
                                + bitsPerSample + " bits");
            }

            byte[] audioBytes = bis.readAllBytes();
            ByteBuffer audioBuffer = BufferUtils.createByteBuffer(audioBytes.length);
            audioBuffer.put(audioBytes).flip();

            ShortBuffer pcm = audioBuffer
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .asShortBuffer();

            return new WavData(pcm, sampleRate);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load audio: " + path, e);
        }
    }
}
