
package typesafeschwalbe.luwest.math;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

public class Vec2 implements Cloneable {
    
    public double x;
    public double y;

    public Vec2() {
        this.x = 0.0;
        this.y = 0.0;
    }

    public Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2(JsonArray jsonArr) {
        this.x = jsonArr.get(0).getAsDouble();
        this.y = jsonArr.get(1).getAsDouble();
    }


    @Override
    public Vec2 clone() {
        return new Vec2(this.x, this.y);
    }

    public JsonArray asJsonArray() {
        JsonArray arr = new JsonArray();
        arr.add(new JsonPrimitive(this.x));
        arr.add(new JsonPrimitive(this.y));
        return arr;
    }


    public Vec2 add(Vec2 other) { return this.add(other.x, other.y); }
    public Vec2 add(double f) { return this.add(f, f); }
    public Vec2 add(double x, double y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vec2 sub(Vec2 other) { return this.sub(other.x, other.y); }
    public Vec2 sub(double f) { return this.sub(f, f); }
    public Vec2 sub(double x, double y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Vec2 mul(Vec2 other) { return this.mul(other.x, other.y); }
    public Vec2 mul(double f) { return this.mul(f, f); }
    public Vec2 mul(double x, double y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    public Vec2 div(Vec2 other) { return this.div(other.x, other.y); }
    public Vec2 div(double f) { return this.div(f, f); }
    public Vec2 div(double x, double y) {
        this.x /= x;
        this.y /= y;
        return this;
    }

    public double len() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }

}
