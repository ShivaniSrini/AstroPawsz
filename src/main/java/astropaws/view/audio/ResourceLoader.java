package astropaws.view.audio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryUtil.*;

public class ResourceLoader {

    public static ByteBuffer load(String path) {
        try (InputStream is = ResourceLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("Resource not found: " + path);
            }

            byte[] bytes = is.readAllBytes();
            ByteBuffer buffer = memAlloc(bytes.length);
            buffer.put(bytes).flip();
            return buffer;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
