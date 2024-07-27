
package typesafeschwalbe.luwest.scenes

import typesafeschwalbe.luwest.engine.*
import typesafeschwalbe.luwest.util.*

fun overworld(): Scene {
    val scene = StaticScene("res/scenes/overworld.json", Resource.EMBEDDED)
    return Scene()
        .with(Camera.create(20.0).with(Sectors.Observer(scene, 4)))
        .with(
            Sectors::manageAll,
            Velocity::handleAll,
            Collision::handleAll,
            Camera::computeOffsets,

            SpriteRenderer::renderReflections,
            Camera::renderReflections,

            scene::renderBackground,
            SpriteRenderer::renderAll,
            ::renderLakes,
            Camera::renderAll,
            
            Camera::showBuffers
        )
}