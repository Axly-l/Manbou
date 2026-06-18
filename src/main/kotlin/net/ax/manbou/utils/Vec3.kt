package net.ax.manbou.utils

import kotlin.math.pow

/**
 * The type Vec 3.
 */
class Vec3
/**
 * Instantiates a new Vec 3.
 *
 * @param x  the x
 * @param y2 the y 2
 * @param z  the z
 */(
    /**
     * Gets x.
     *
     * @return the x
     */
    val x: Double,
    /**
     * Gets y.
     *
     * @return the y
     */
    val y: Double,
    /**
     * Gets z.
     *
     * @return the z
     */
    val z: Double
) {
    /**
     * Add vector vec 3.
     *
     * @param x  the x
     * @param y2 the y 2
     * @param z  the z
     * @return the vec 3
     */
    fun addVector(x: Double, y2: Double, z: Double): Vec3 {
        return Vec3(this.x + x, this.y + y2, this.z + z)
    }

    /**
     * Floor vec 3.
     *
     * @return the vec 3
     */
    fun floor(): Vec3 {
        return Vec3(kotlin.math.floor(this.x), kotlin.math.floor(this.y), kotlin.math.floor(this.z))
    }

    /**
     * Square distance to double.
     *
     * @param v the v
     * @return the double
     */
    fun squareDistanceTo(v: Vec3): Double {
        return (v.x - this.x).pow(2.0) + (v.y - this.y).pow(2.0) + (v.z - this.z).pow(2.0)
    }

    /**
     * Add vec 3.
     *
     * @param v the v
     * @return the vec 3
     */
    fun add(v: Vec3): Vec3 {
        return this.addVector(v.x, v.y, v.z)
    }

    /**
     * Mc net . minecraft . util . vec 3.
     *
     * @return the net . minecraft . util . vec 3
     */
    fun mc(): net.minecraft.util.Vec3 {
        return net.minecraft.util.Vec3(this.x, this.y, this.z)
    }

    override fun toString(): String {
        return "[" + this.x + ";" + this.y + ";" + this.z + "]"
    }
}