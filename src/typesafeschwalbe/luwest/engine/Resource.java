
package typesafeschwalbe.luwest.engine;

import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public abstract class Resource<T> {
    
    static void loadAll(Stream<Resource<?>> resources) {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        resources
            .map(r -> executor.submit(r::load))
            .forEach(f -> {
                try {
                    f.get();
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                } catch(ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
    }


    private Resource() {}

    private T value = null;
    private WeakReference<T> existingValue = new WeakReference<>(null);

    public T get() {
        if(this.value == null) {
            throw new RuntimeException("Resource has not yet been loaded!");
        }
        return this.value;
    }

    public boolean loaded() {
        return this.value != null;
    }

    abstract T read();

    public void load() {
        this.value = this.existingValue.get();
        if(this.value != null) {
            return;
        }
        this.value = this.read();
        this.existingValue = new WeakReference<>(this.value);
    }

    public void unload() {
        this.value = null;
    }


    public static Resource<BufferedImage> embeddedImage(String path) {
        return new Resource<BufferedImage>() {
            @Override BufferedImage read() {
                return Resources.readEmbImage(path);
            }
        };
    }

    public static Resource<String> embeddedString(String path) {
        return new Resource<String>() {
            @Override String read() {
                return Resources.readEmbString(path);
            }
        };
    }

}
