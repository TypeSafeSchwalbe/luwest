
package typesafeschwalbe.luwest.math

operator fun Vec2.unaryMinus() = this.negate()

operator fun Vec2.plus(other: Vec2) = this.add(other)
operator fun Vec2.plus(other: Double) = this.add(other)

operator fun Vec2.minus(other: Vec2) = this.sub(other)
operator fun Vec2.minus(other: Double) = this.sub(other)

operator fun Vec2.times(other: Vec2) = this.mul(other)
operator fun Vec2.times(other: Double) = this.mul(other)

operator fun Vec2.div(other: Vec2) = this.div(other)
operator fun Vec2.div(other: Double) = this.div(other)

operator fun Vec2.rem(other: Vec2): Vec2 {
    this.x %= other.x
    this.y %= other.y
    return this
}
operator fun Vec2.rem(other: Double): Vec2 {
    this.x %= other
    this.y %= other
    return this
}