
package typesafeschwalbe.luwest.engine;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

public final class Resources {

    private Resources() {}

    private static InputStream external(String path) {
        try {
            return new FileInputStream(path);
        } catch(FileNotFoundException e) {
            throw new RuntimeException(
                "Unable to load file '" + path + "': " + e.getMessage(), e
            );
        }
    }

    private static InputStream embedded(String path) {
        InputStream is = ClassLoader.getSystemClassLoader()
            .getResourceAsStream(path);
        if(is == null) {
            throw new RuntimeException(
                "Unable to locate embedded file '" + path + "'"
            );
        }
        return is;
    }

    private static String readString(InputStream is) {
        try(is) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static BufferedImage readImage(InputStream is) {
        try(is) {
            return ImageIO.read(is);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readExtString(String path) {
        return Resources.readString(Resources.external(path));
    }

    public static BufferedImage readExtImage(String path) {
        return Resources.readImage(Resources.external(path));
    }

    public static String readEmbString(String path) {
        return Resources.readString(Resources.embedded(path));
    }

    public static BufferedImage readEmbImage(String path) {
        return Resources.readImage(Resources.embedded(path));
    }

}