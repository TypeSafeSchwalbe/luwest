
package typesafeschwalbe.luwest.scenes;

import java.awt.Color
import java.awt.AlphaComposite
import java.util.Optional

import com.google.gson.JsonObject;

import typesafeschwalbe.luwest.math.*
import typesafeschwalbe.luwest.engine.*
import typesafeschwalbe.luwest.util.*
import typesafeschwalbe.luwest.scenes.WaterRenderer
import typesafeschwalbe.luwest.scenes.renderLakes

data class WaterRenderer(val size: Vec2)

fun renderLakes(scene: Scene) {
    for((_, buffer, conv) in scene.allWith(
        Camera.Buffer::class, Camera.Conversion::class
    )) {
        for((_, renderer, position) in scene.allWith(
            WaterRenderer::class, Position::class
        )) {
            val sTopLeft = conv.posOnScreen(position.value.clone())
            val sBottomRight = conv.posOnScreen(
                position.value.clone() + renderer.size
            )
            val sWidth = Math.ceil(sBottomRight.x - sTopLeft.x).toInt()
            val sHeight = Math.ceil(sBottomRight.y - sTopLeft.y).toInt()
            buffer.world.add(sTopLeft.y) { g ->
                g.setColor(Color(126, 196, 193))
                g.fillRect(
                    sTopLeft.x.toInt(), sTopLeft.y.toInt(), 
                    sWidth, sHeight
                )
                val ogComposite = g.getComposite()
                g.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, 0.5f
                ))
                g.drawImage(
                    buffer.reflectBuff,
                    sTopLeft.x.toInt(), sTopLeft.y.toInt(),
                    sBottomRight.x.toInt(), sBottomRight.y.toInt(),
                    sTopLeft.x.toInt(), sTopLeft.y.toInt(),
                    sBottomRight.x.toInt(), sBottomRight.y.toInt(),
                    null
                )
                g.setComposite(ogComposite)
            }
        }
    }
}


private val WATER_RENDERERS: MutableMap<JsonObject, WaterRenderer> 
    = mutableMapOf()

class LakeSerializer: Serialization.Serializer {

    override fun deserialize(
        instance: Optional<JsonObject>, type: JsonObject, 
        origin: Resource.Origin
    ): Entity {
        val at = if(instance.isPresent()) {
            Vec2(instance.get().get("at").getAsJsonArray())
        } else {
            Vec2()
        }
        val size = Vec2(type.get("size").getAsJsonArray())
        val renderer = WATER_RENDERERS[type] ?: WaterRenderer(size)
        WATER_RENDERERS[type] = renderer
        return Entity()
            .with(Position(at))
            .with(renderer)
    }

    override fun serialize(entity: Entity): JsonObject {
        val position: Position = entity.get()
        val instance = JsonObject()
        instance.add("at", position.value.asJsonArray())
        return instance
    }

}