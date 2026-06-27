package net.ax.manbou.module.impl.movement

import net.ax.manbou.event.EventTarget
import net.ax.manbou.event.MotionEvent
import net.ax.manbou.event.EventPhase
import net.ax.manbou.event.TickEvent
import net.ax.manbou.module.BooleanValue
import net.ax.manbou.module.Category
import net.ax.manbou.module.IntValue
import net.ax.manbou.module.ModuleBase
import net.ax.manbou.utils.MovementUtils
import net.minecraft.client.settings.GameSettings
import net.minecraft.item.ItemBlock

object Eagle: ModuleBase("Eagle", Category.MOVEMENT) {
    val threshold = IntValue("Threshold", 10, 0..100, "%")
    val onlyWhileClicking = BooleanValue("OnlyWhileClicking", false)
    val onlyBlock = BooleanValue("OnlyBlock", false)
    val onlyWhileBacking = BooleanValue("OnlyWhileBacking", false)
    val autoSwitch = BooleanValue("AutoSwitch", false)

    private var b = false
    private var i = 0

    @EventTarget
    fun onTick(event: TickEvent) {
        if(event.phase != EventPhase.PRE) return
        if (autoSwitch.value) {
            if ((b && mc.thePlayer.heldItem?.item !is ItemBlock) && (i == mc.thePlayer.inventory.currentItem)) {
                val blockSlot = findBlockSlot()
                if (blockSlot != -1) {
                    mc.thePlayer.inventory.currentItem = blockSlot
                }
            }
            b = mc.thePlayer.heldItem?.item is ItemBlock
            i = mc.thePlayer.inventory.currentItem
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (MovementUtils.isNearEdge(threshold.value / 100.0)
            && (!onlyWhileClicking.value || GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem))
            && (!onlyWhileBacking.value || mc.thePlayer.movementInput.moveForward < 0.0f)
            && (!onlyBlock.value || mc.thePlayer.heldItem?.item is ItemBlock)
            || GameSettings.isKeyDown(mc.gameSettings.keyBindSneak))
            mc.gameSettings.keyBindSneak.pressed = true
        else if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak))
            mc.gameSettings.keyBindSneak.pressed = false
    }

    private fun findBlockSlot(): Int {
        for (i in 0..8) {
            val stack = mc.thePlayer.inventory.mainInventory[i]
            if (stack != null && stack.item is ItemBlock && stack.stackSize > 0) {
                return i
            }
        }

        return -1
    }

    override fun onDisable() {
        mc.gameSettings.keyBindSneak.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)
    }
}