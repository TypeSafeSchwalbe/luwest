
package typesafeschwalbe.luwest.engine;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class Resource<T> {

    @FunctionalInterface
    public interface Decoder<T> {
        T decode(InputStream is) throws Exception;
    }

    public static final Decoder<String> STRING_DECODER 
        = is -> new String(is.readAllBytes(), StandardCharsets.UTF_8);

    public static final Decoder<BufferedImage> IMAGE_DECODER 
        = ImageIO::read;

    public static final Decoder<JsonElement> JSON_DECODER = is -> {
        try(
            InputStreamReader ir = new InputStreamReader(is)
        ) {
            return JsonParser.parseReader(ir);
        }
    };

    public static final Decoder<Font> FONT_DECODER 
        = is -> Font.createFont(Font.TRUETYPE_FONT, is);


    @FunctionalInterface
    public interface Origin {
        InputStream read(String path);
    }

    public static final Origin EMBEDDED = path -> {
        InputStream is = ClassLoader.getSystemClassLoader()
            .getResourceAsStream(path);
        if(is == null) {
            throw new RuntimeException(
                "Unable to locate embedded file '" + path + "'"
            );
        }
        return is;
    };

    public static final Origin EXTERNAL = path -> {
        try {
            return new FileInputStream(path);
        } catch(FileNotFoundException e) {
            throw new RuntimeException(
                "Unable to load file '" + path + "': " + e.getMessage(), e
            );
        }
    };


    private static final ExecutorService READER_POOL
        = Executors.newFixedThreadPool(8);

    public final String path;
    public final Origin origin;
    public final Decoder<T> decoder;
    private Optional<Future<T>> decoded = Optional.empty();
    private Optional<T> value = Optional.empty();

    public Resource(String path, Origin origin, Decoder<T> decoder) {
        this.path = path;
        this.origin = origin;
        this.decoder = decoder;
        this.reload();
    }

    public void reload() {
        this.decoded = Optional.of(Resource.READER_POOL.submit(() -> {
            try(InputStream is = this.origin.read(this.path)) {
                return this.decoder.decode(is);
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }));
    }

    public T get() {
        boolean getDecoded = this.value.isEmpty()
            || (this.decoded.isPresent() && this.decoded.get().isDone());
        if(getDecoded) {
            try {
                this.value = Optional.of(this.decoded.get().get());
                this.decoded = Optional.empty();
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            } catch(ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return this.value.get();
    }


    private static class CacheEntry {
        final WeakReference<Resource<?>> resource;
        long lastModified;

        CacheEntry(final Resource<?> resource, long lastModified) {
            this.resource = new WeakReference<>(resource);
            this.lastModified = lastModified;
        }
    }

    private static final HashMap<String, CacheEntry> CACHE = new HashMap<>();

    private static long fileLastModified(String path) {
        try {
            Path file = Paths.get(path);
            BasicFileAttributes attrs = Files
                .readAttributes(file, BasicFileAttributes.class);
            return attrs.lastModifiedTime().toMillis();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static long lastHotReload = System.currentTimeMillis();
    private static final long HOT_RELOAD_TIMER = 500;

    public static void hotReloadExternals(Scene scene) {
        long now = System.currentTimeMillis();
        if(lastHotReload + HOT_RELOAD_TIMER > now) { return; }
        lastHotReload = now;
        LinkedList<String> removedPaths = new LinkedList<>();
        for(String path: Resource.CACHE.keySet()) {
            CacheEntry entry = Resource.CACHE.get(path);
            Resource<?> resource = entry.resource.get();
            if(resource == null) {
                removedPaths.add(path);
                continue;
            }
            if(resource.origin != Resource.EXTERNAL) { continue; }
            long fileUpdateTime = Resource.fileLastModified(path);
            if(fileUpdateTime == entry.lastModified) { continue; }
            resource.reload();
            entry.lastModified = fileUpdateTime;
        }
        for(String removedPath: removedPaths) {
            Resource.CACHE.remove(removedPath);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Resource<T> getCachedOrRead(
        String path, Origin origin, Decoder<T> decoder
    ) {
        CacheEntry cached = Resource.CACHE.get(path);
        if(cached != null) {
            Resource<?> resource = cached.resource.get();
            if(resource != null) {
                return (Resource<T>) resource;
            }
        }
        Resource<T> resource = new Resource<>(path, origin, decoder);
        long lastModified = origin == Resource.EXTERNAL
            ? Resource.fileLastModified(path)
            : 0;
        Resource.CACHE.put(path, new CacheEntry(resource, lastModified));
        return resource;
    }

    public static Resource<String> string(String path, Origin origin) {
        return Resource.getCachedOrRead(path, origin, Resource.STRING_DECODER);
    }

    public static Resource<BufferedImage> image(String path, Origin origin) {
        return Resource.getCachedOrRead(path, origin, Resource.IMAGE_DECODER);
    }

    public static Resource<JsonElement> json(String path, Origin origin) {
        return Resource.getCachedOrRead(path, origin, Resource.JSON_DECODER);
    }

    public static Resource<Font> ttfFont(String path, Origin origin) {
        return Resource.getCachedOrRead(path, origin, Resource.FONT_DECODER);
    }

}
