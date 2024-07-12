
package typesafeschwalbe.luwest.engine;

import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public abstract class Resource<T> {

    private Resource() {}

    private T value = null;

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
        this.value = this.read();
    }

    public void unload() {
        this.value = null;
    }


    private static HashMap<String, WeakReference<BufferedImage>> IMAGE_CACHE 
        = new HashMap<>();

    public static Resource<BufferedImage> embeddedImage(String path) {
        return new Resource<BufferedImage>() {
            @Override BufferedImage read() {
                if(Resource.IMAGE_CACHE.containsKey(path)) {
                    BufferedImage cached = Resource.IMAGE_CACHE.get(path).get();
                    if(cached != null) { return cached; }
                }
                BufferedImage read = Resources.readEmbImage(path);
                Resource.IMAGE_CACHE.put(path, new WeakReference<>(read));
                return read;
            }
        };
    }


    private static HashMap<String, WeakReference<String>> STRING_CACHE 
        = new HashMap<>();

    public static Resource<String> embeddedString(String path) {
        return new Resource<String>() {
            @Override String read() {
                if(Resource.STRING_CACHE.containsKey(path)) {
                    String cached = Resource.STRING_CACHE.get(path).get();
                    if(cached != null) { return cached; }
                }
                String read = Resources.readEmbString(path);
                Resource.STRING_CACHE.put(path, new WeakReference<>(read));
                return read;
            }
        };
    }

}
