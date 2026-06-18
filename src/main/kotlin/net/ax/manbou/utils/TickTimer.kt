package net.ax.manbou.utils

class TickTimer {
    var tick = 0

    fun onTick() {
        ++tick
    }

    fun reset() {
        tick = 0
    }

    fun hasTimePassed(required: Int): Boolean = this.tick >= required
}