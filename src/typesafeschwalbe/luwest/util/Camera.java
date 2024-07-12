
package typesafeschwalbe.luwest.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import typesafeschwalbe.luwest.engine.*;
import typesafeschwalbe.luwest.math.Vec2;

public class Camera {

    public static class Configuration {
        public double distance;

        public Configuration(double distance) {
            this.distance = distance;
        }
    }

    public static class Conversion {
        private double unitSize = 0.0;
        private Vec2 offset = new Vec2(0.0, 0.0);

        public Vec2 sizeOnScreen(Vec2 size) { 
            return size.mul(this.unitSize); 
        }
        public Vec2 sizeInWorld(Vec2 size) { 
            return size.div(this.unitSize);
        }

        public Vec2 posOnScreen(Vec2 pos) {
            return pos.mul(this.unitSize).add(this.offset);
        }
        public Vec2 posInWorld(Vec2 pos) {
            return pos.sub(this.offset).div(this.unitSize);
        }
    }

    public static class Buffer {
        public BufferedImage reflectBuff = null;
        public RenderQueue reflect = new RenderQueue();
        public BufferedImage worldBuff = null;
        public RenderQueue world = new RenderQueue();

        private void resize() {
            boolean buffersInvalid = this.worldBuff == null
                || this.worldBuff.getWidth() != Engine.window().width()
                || this.worldBuff.getHeight() != Engine.window().height();
            if(!buffersInvalid) { return; }
            this.worldBuff = new BufferedImage(
                Engine.window().width(),
                Engine.window().height(), 
                BufferedImage.TYPE_INT_ARGB
            );
            this.reflectBuff = new BufferedImage(
                Engine.window().width(),
                Engine.window().height(), 
                BufferedImage.TYPE_INT_ARGB
            );
        }
    }

    public static Entity create(double distance) {
        return new Entity()
            .with(Position.class, new Position())
            .with(Configuration.class, new Configuration(distance))
            .with(Conversion.class, new Conversion())
            .with(Buffer.class, new Buffer());
    }

    public static void computeOffsets(Scene scene) {
        for(Entity camera: scene.allWith(
            Position.class, Configuration.class, Conversion.class
        )) {
            Position pos = camera.get(Position.class);
            Configuration conf = camera.get(Configuration.class);
            Conversion conv = camera.get(Conversion.class);
            int vmax = Math.max(
                Engine.window().width(), Engine.window().height()
            );
            conv.unitSize = vmax / conf.distance;
            conv.offset.x = -(pos.value.x * conv.unitSize) 
                + (Engine.window().width() / 2.0);
            conv.offset.y = -(pos.value.y * conv.unitSize)
                + (Engine.window().height() / 2.0);
        }
    }

    public static void renderReflections(Scene scene) {
        for(Entity camera: scene.allWith(Buffer.class)) {
            Buffer buffer = camera.get(Buffer.class);
            buffer.resize();
            Graphics2D g = buffer.reflectBuff.createGraphics();
            g.setBackground(new Color(1f, 1f, 1f, 0f));
            g.clearRect(
                0, 0,
                buffer.reflectBuff.getWidth(), buffer.reflectBuff.getHeight()
            );
            buffer.reflect.renderAll(g);
            g.dispose();
        }
    }

    public static void renderAll(Scene scene) {
        for(Entity camera: scene.allWith(Buffer.class)) {
            Buffer buffer = camera.get(Buffer.class);
            buffer.resize();
            Graphics2D g = buffer.worldBuff.createGraphics();
            g.setBackground(new Color(1f, 1f, 1f, 0f));
            g.clearRect(
                0, 0, buffer.worldBuff.getWidth(), buffer.worldBuff.getHeight()
            );
            buffer.world.renderAll(g);
            g.dispose();
        }
    }

    public static void showBuffers(Scene scene) {
        for(Entity camera: scene.allWith(Buffer.class)) {
            Buffer buffer = camera.get(Buffer.class);
            Engine.window().gfx().setColor(Color.BLACK);
            Engine.window().gfx().fillRect(
                0, 0, Engine.window().width(), Engine.window().height()
            );
            Engine.window().gfx().drawImage(buffer.worldBuff, 0, 0, null);
        }
    }

}
