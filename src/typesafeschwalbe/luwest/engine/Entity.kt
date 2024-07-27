
package typesafeschwalbe.luwest.engine

inline fun <reified T> Entity.with(component: T) 
    = this.with(T::class.java, component!!)

inline fun <reified T> Entity.has()
    = this.has(T::class.java)

inline fun <reified T> Entity.get(): T
    = this.get(T::class.java)