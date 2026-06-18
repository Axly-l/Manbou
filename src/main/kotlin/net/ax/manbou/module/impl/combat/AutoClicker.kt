package net.ax.manbou.module.impl.combat

import net.ax.manbou.event.EventTarget
import net.ax.manbou.event.SignalEvent
import net.ax.manbou.module.BooleanValue
import net.ax.manbou.module.Category
import net.ax.manbou.module.IntValue
import net.ax.manbou.module.ModuleBase
import net.ax.manbou.utils.TickTimer
import net.ax.manbou.utils.TimerUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import kotlin.math.roundToInt

object AutoClicker: ModuleBase("AutoClicker", Category.COMBAT) {
    val left = BooleanValue("Left", false)
    val leftMaxCPS: IntValue = IntValue("LeftMaxCPS", 0, 0..20, "CPS", 1, { left.value }) {
        if(it < leftMinCPS.value) leftMinCPS.value = it
    }
    val leftMinCPS: IntValue = IntValue("LeftMinCPS", 0, 0..20, "CPS", 1, { left.value }) {
        if(it > leftMaxCPS.value) leftMaxCPS.value = it
    }
    var leftTicks = 0.0

    val right = BooleanValue("Right", false)
    val rightMaxCPS: IntValue = IntValue("RightMaxCPS", 0, 0..20, "CPS", 1, { right.value }) {
        if(it < rightMinCPS.value) rightMinCPS.value = it
    }
    val rightMinCPS: IntValue = IntValue("RightMinCPS", 0, 0..20, "CPS", 1, { right.value }) {
        if(it > rightMaxCPS.value) rightMaxCPS.value = it
    }
    var rightTicks = 0.0


    @EventTarget
    fun onSignal(event: SignalEvent) {
        if(event.name != "clicking") return

        if (left.value && mc.gameSettings.keyBindAttack.isKeyDown) {
            mc.leftClickCounter = 0

            leftTicks += TimerUtils.randomCPS(leftMinCPS.value, leftMaxCPS.value) / 20.0
            val clicks = leftTicks.toInt()
            repeat(clicks) {
                mc.gameSettings.keyBindAttack.onTick()
            }
            leftTicks -= clicks
        } else {
            leftTicks = 0.0
        }


        if (right.value && mc.gameSettings.keyBindUseItem.isKeyDown) {
            rightTicks += TimerUtils.randomCPS(rightMinCPS.value, rightMaxCPS.value) / 20.0
            val clicks = rightTicks.toInt()
            repeat(clicks) {
                mc.gameSettings.keyBindUseItem.onTick()
            }
            rightTicks -= clicks
        } else {
            rightTicks = 0.0
        }
    }
}