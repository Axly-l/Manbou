package net.ax.manbou.module.impl.combat

import net.ax.manbou.Main
import net.ax.manbou.module.Category
import net.ax.manbou.module.DoubleValue
import net.ax.manbou.module.FloatValue
import net.ax.manbou.module.ModuleBase

object Reach: ModuleBase("Reach", Category.COMBAT) {
    val minValue: FloatValue = FloatValue("Min", 3.0f, 3.0f..5.0f, "m", 0.01f) {
        if(it > maxValue.value) maxValue.value = it
    }
    val maxValue: FloatValue = FloatValue("Max", 3.0f, 3.0f..5.0f, "m", 0.01f) {
        if(it < minValue.value) minValue.value = it
    }

    val reach: Float
        get() {
            return Main.random.nextFloat() * (maxValue.value - minValue.value) + minValue.value
        }
}