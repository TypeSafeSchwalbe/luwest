
package typesafeschwalbe.luwest.util;

import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;

import typesafeschwalbe.luwest.engine.*;
import typesafeschwalbe.luwest.math.Vec2;

public class Editor {

    public final String editedPath;
    private final StaticScene staticScene;
    private final Sectors.Observer observer;
    public final Scene scene;

    public Editor(String editedPath) {
        this.editedPath = editedPath;
        this.staticScene = new StaticScene(editedPath, Resource.EXTERNAL);
        this.observer = new Sectors.Observer(this.staticScene, 4);
        this.scene = new Scene()
            .with(
                Camera.create(20.0)
                    .with(Sectors.Observer.class, observer)
                    .with(Velocity.class, new Velocity())
            )
            .with(
                Sectors::manageAll,
                Editor::moveCamera,
                Velocity::applyAll,

                Camera::computeOffsets,

                SpriteRenderer::renderReflections,
                Camera::renderReflections,

                staticScene::renderBackground,
                SpriteRenderer::renderAll,
                Camera::renderAll,
                
                Camera::showBuffers
            );
    }

    public String serializeScene() {
        HashSet<Sectors.Sector> reset = new HashSet<>();
        for(Entity entity: this.scene.allWith(
            Position.class, Sectors.Owned.class
        )) {
            Position position = entity.get(Position.class);
            Sectors.Sector sector = this.observer.asSector(position.value);
            if(reset.contains(sector)) { continue; }
            this.staticScene.serializeSector(sector.x, sector.y, this.observer, this.scene);
            reset.add(sector);
        }
        return this.staticScene.serialize();
    }

    // /!\ IMPORTANT /!\
    // THIS SHOULD RUN EVERY TIME SOMETHING IS MODIFED :)
    public void saveScene() {
        try(PrintWriter pw = new PrintWriter(this.editedPath)) {
            pw.println(this.serializeScene());
        } catch(FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


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

}
