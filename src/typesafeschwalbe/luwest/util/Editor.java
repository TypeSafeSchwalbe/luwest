
package typesafeschwalbe.luwest.util;

import java.awt.event.KeyEvent;

import typesafeschwalbe.luwest.engine.*;
import typesafeschwalbe.luwest.math.Vec2;

public final class Editor {

    private Editor() {}


    public static final double CAMERA_SPEED = 10.0;

    public static void moveCamera(Scene scene) {
        for(Entity camera: scene.allWith(
            Camera.Configuration.class, Velocity.class
        )) {
            Vec2 vel = new Vec2();
            if(Engine.window().keyPressed(KeyEvent.VK_W)) {
                vel.y -= 1.0;
            }
            if(Engine.window().keyPressed(KeyEvent.VK_A)) {
                vel.x -= 1.0;
            }
            if(Engine.window().keyPressed(KeyEvent.VK_D)) {
                vel.x += 1.0;
            }
            if(Engine.window().keyPressed(KeyEvent.VK_S)) {
                vel.y += 1.0;
            }
            Velocity velocity = camera.get(Velocity.class);
            velocity.value = vel.normalize().mul(CAMERA_SPEED);
        }
    }


    public static Scene createScene(String staticScenePath) {
        StaticScene scene = new StaticScene(staticScenePath, Resource.EXTERNAL);
        return new Scene()
            .with(
                Camera.create(20.0)
                    .with(Sectors.Observer.class, new Sectors.Observer(scene))
                    .with(Velocity.class, new Velocity())
            )
            .with(
                Sectors::manageAll,
                Editor::moveCamera,
                Velocity::applyAll,

                Camera::computeOffsets,

                SpriteRenderer::renderReflections,
                Camera::renderReflections,

                scene::renderBackground,
                SpriteRenderer::renderAll,
                Camera::renderAll,
                
                Camera::showBuffers
            );
    }

}
