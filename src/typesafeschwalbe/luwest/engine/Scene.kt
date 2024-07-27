
package typesafeschwalbe.luwest.engine

import kotlin.reflect.KClass

data class OneComponent<A>(
    val entity: Entity, val a: A
)
inline fun <reified A: Any> Scene.allWith(
    a: KClass<A>
): Sequence<OneComponent<A>> = sequence {
    for(e in allWith(a.java)) {
        yield(OneComponent(e, e.get()))
    }
}

data class TwoComponent<A, B>(val entity: Entity, val a: A, val b: B)
inline fun <reified A: Any, reified B: Any> Scene.allWith(
    a: KClass<A>, b: KClass<B>
): Sequence<TwoComponent<A, B>> = sequence {
    for(e in allWith(a.java, b.java)) {
        yield(TwoComponent(e, e.get(), e.get()))
    }
}

data class ThreeComponent<A, B, C>(
    val entity: Entity, val a: A, val b: B, val c: C
)
inline fun <reified A: Any, reified B: Any, reified C: Any> Scene.allWith(
    a: KClass<A>, b: KClass<B>, c: KClass<C>
): Sequence<ThreeComponent<A, B, C>> = sequence {
    for(e in allWith(a.java, b.java, c.java)) {
        yield(ThreeComponent(e, e.get(), e.get(), e.get()))
    }
}

data class FourComponent<A, B, C, D>(
    val entity: Entity, val a: A, val b: B, val c: C, val d: D
)
inline fun <reified A: Any, reified B: Any, reified C: Any, reified D: Any> 
Scene.allWith(
    a: KClass<A>, b: KClass<B>, c: KClass<C>, d: KClass<D>
): Sequence<FourComponent<A, B, C, D>> = sequence {
    for(e in allWith(a.java, b.java, c.java, d.java)) {
        yield(FourComponent(e, e.get(), e.get(), e.get(), e.get()))
    }
}