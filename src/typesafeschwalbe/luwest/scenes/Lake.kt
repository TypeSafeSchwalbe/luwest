
package typesafeschwalbe.luwest.scenes;

import java.awt.Color
import java.awt.AlphaComposite
import java.util.Optional

import com.google.gson.JsonObject;

import typesafeschwalbe.luwest.math.Vec2
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
            val sPos = conv.posOnScreen(position.value.clone())
            val sSize = conv.sizeOnScreen(renderer.size.clone())
            buffer.world.add(sPos.y) { g ->
                g.setColor(Color(126, 196, 193))
                g.fillRect(
                    sPos.x.toInt(), sPos.y.toInt(),
                    sSize.x.toInt(), sSize.y.toInt()
                )
                val ogComposite = g.getComposite()
                g.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, 0.5f
                ))
                g.drawImage(
                    buffer.reflectBuff,
                    sPos.x.toInt(), sPos.y.toInt(),
                    (sPos.x + sSize.x).toInt(), (sPos.y + sSize.y).toInt(),
                    sPos.x.toInt(), sPos.y.toInt(),
                    (sPos.x + sSize.x).toInt(), (sPos.y + sSize.y).toInt(),
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