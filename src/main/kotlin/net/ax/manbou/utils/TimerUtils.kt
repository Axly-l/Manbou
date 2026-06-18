package net.ax.manbou.utils

import net.ax.manbou.Main

object TimerUtils: IMinecraft {
    fun randomCPS(min: Int, max: Int): Int {
        if (min >= max) return max
        return min + Main.random.nextInt(max - min)
    }
}