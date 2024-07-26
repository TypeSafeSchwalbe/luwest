
package typesafeschwalbe.luwest.scenes

import typesafeschwalbe.luwest.engine.*
import typesafeschwalbe.luwest.util.*

fun overworld(): Scene {
    Serialization.define("lake", Lake.LakeSerializer())
    val scene = StaticScene("res/scenes/overworld.json", Resource.EMBEDDED)
    return Scene()
        .with(Camera.create(20.0).with(
            Sectors.Observer::class.java, Sectors.Observer(scene, 4)
        ))
        .with(
            Sectors::manageAll,
            Velocity::handleAll,
            Collision::handleAll,
            Camera::computeOffsets,

            SpriteRenderer::renderReflections,
            Camera::renderReflections,

            scene::renderBackground,
            SpriteRenderer::renderAll,
            Lake::renderAll,
            Camera::renderAll,
            
            Camera::showBuffers
        )
}