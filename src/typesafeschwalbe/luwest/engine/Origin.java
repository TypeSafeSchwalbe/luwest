
package typesafeschwalbe.luwest.engine;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

public enum Origin {

    WORKING_DIRECTORY(
        path -> new String(
            Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8
        ),
        path -> ImageIO.read(new File(path))
    ),
    CLASSPATH(
        path -> {
            try(var br = new BufferedReader(new InputStreamReader(
                ClassLoader.getSystemClassLoader().getResourceAsStream(path)
            ))) {
                StringBuilder result = new StringBuilder();
                for(;;) {
                    String line = br.readLine();
                    if(line == null) { break; }
                    result.append(line);
                }
                return result.toString();
            }
        },
        path -> ImageIO.read(
            ClassLoader.getSystemClassLoader().getResourceAsStream(path)
        )
    );

    @FunctionalInterface
    public interface Reader<T> {
        T readChecked(String path) throws Exception;

        default T read(String path) {
            try {
                return this.readChecked(path);
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public final Reader<String> string;
    public final Reader<BufferedImage> image;

    private Origin(Reader<String> string, Reader<BufferedImage> image) {
        this.string = string;
        this.image = image;
    }
}