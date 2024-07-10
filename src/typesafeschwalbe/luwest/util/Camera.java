
package typesafeschwalbe.luwest.util;

import java.awt.image.BufferedImage;

import typesafeschwalbe.luwest.engine.*;
import typesafeschwalbe.luwest.math.Vec2;

public class Camera {

    public static class Configuration {
        public double distance = 10.0;
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
        public BufferedImage world = null;
        public BufferedImage reflection = null;
    }

    public static Entity create() {
        return new Entity()
            .with(Position.class, new Position())
            .with(Configuration.class, new Configuration())
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
            int vmin = Math.min(
                Engine.window().width(), Engine.window().height()
            );
            conv.unitSize = vmin / conf.distance;
            conv.offset.x = -(pos.value.x * conv.unitSize) 
                + (Engine.window().width() / 2.0);
            conv.offset.y = -(pos.value.y * conv.unitSize)
                + (Engine.window().height() / 2.0);
        }
    }

    public static void resizeBuffers(Scene scene) {
        for(Entity camera: scene.allWith(Buffer.class)) {
            Buffer buffer = camera.get(Buffer.class);
            boolean buffersInvalid = buffer.world == null
                || buffer.world.getWidth() != Engine.window().width()
                || buffer.world.getHeight() != Engine.window().height();
            if(buffersInvalid) {
                buffer.world = new BufferedImage(
                    Engine.window().width(),
                    Engine.window().height(), 
                    BufferedImage.TYPE_INT_ARGB
                );
                buffer.reflection = new BufferedImage(
                    Engine.window().width(),
                    Engine.window().height(), 
                    BufferedImage.TYPE_INT_ARGB
                );
            }
        }
    }

    public static void showBuffers(Scene scene) {
        for(Entity camera: scene.allWith(Buffer.class)) {
            Buffer buffer = camera.get(Buffer.class);
            Engine.window().gfx().drawImage(buffer.world, 0, 0, null);
        }
    }

}
