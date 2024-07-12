
package typesafeschwalbe.luwest.engine;

import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;

public abstract class Resource<T> {

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
