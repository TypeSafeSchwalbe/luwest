
package typesafeschwalbe.luwest.util;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Consumer;

public class RenderQueue {

    private static record Item(double depth, Consumer<Graphics2D> render) {}

    private ArrayList<Item> queue = new ArrayList<>();

    public RenderQueue() {}

    public void add(double depth, Consumer<Graphics2D> render) {
        this.queue.add(new Item(depth, render));
    }

    public void renderAll(Graphics2D g) {
        this.queue.sort(Comparator.comparingDouble(Item::depth));
        for(Item item: this.queue) {
            item.render.accept(g);
        }
        this.queue.clear();
    }

}
