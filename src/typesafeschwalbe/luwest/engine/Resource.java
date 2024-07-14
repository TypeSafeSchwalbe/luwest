
package typesafeschwalbe.luwest.engine;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Resource<T> {

    private Optional<T> value = Optional.empty();
    private final Future<T> loaded;

    private Resource(Future<T> loaded) {
        this.loaded = loaded;
    }

    public T get() {
        if(!this.value.isPresent()) {
            try {
                this.value = Optional.of(this.loaded.get());
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            } catch(ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return this.value.get();
    }


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


    private static final HashMap<String, WeakReference<Resource<?>>> CACHE
        = new HashMap<>();
    private static final ExecutorService READER_POOL
        = Executors.newFixedThreadPool(8);

    @SuppressWarnings("unchecked")
    private static <T> Resource<T> getCachedOrRead(
        String path, Callable<T> reader
    ) {
        WeakReference<Resource<?>> cachedRef = Resource.CACHE.get(path);
        if(cachedRef != null) {
            Resource<?> cached = cachedRef.get();
            if(cached != null) {
                return (Resource<T>) cached;
            }
        }
        Future<T> read = READER_POOL.submit(reader);
        Resource<T> resource = new Resource<>(read);
        Resource.CACHE.put(path, new WeakReference<>(resource));
        return resource;
    }

    public static Resource<String> string(String path, Origin origin) {
        return Resource.getCachedOrRead(path, () -> {
            try(InputStream is = origin.read(path)) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static Resource<BufferedImage> image(String path, Origin origin) {
        return Resource.getCachedOrRead(path, () -> {
            try(InputStream is = origin.read(path)) {
                return ImageIO.read(is);
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static Resource<JsonObject> json(String path, Origin origin) {
        return Resource.getCachedOrRead(path, () -> {
            try(
                InputStream is = origin.read(path);
                InputStreamReader ir = new InputStreamReader(is)
            ) {
                return JsonParser.parseReader(ir).getAsJsonObject();
            }
        });
    }



}
