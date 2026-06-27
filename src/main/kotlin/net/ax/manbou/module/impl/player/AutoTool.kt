package net.ax.manbou.module.impl.player

import net.ax.manbou.event.EventTarget
import net.ax.manbou.event.TickEvent
import net.ax.manbou.event.EventPhase
import net.ax.manbou.module.BooleanValue
import net.ax.manbou.module.Category
import net.ax.manbou.module.IntValue
import net.ax.manbou.module.ModuleBase
import net.minecraft.util.MovingObjectPosition

object AutoTool : ModuleBase("AutoTool", Category.PLAYER) {

    private enum class Phase {
        IDLE,
        MINING,
        RESTORING
    }

    private data class Swap(
        val inventorySlot: Int,
        val hotbarSlot: Int
    )

    val inventorySwap = BooleanValue("InvSwap", false)
    val restoreDelay = IntValue("RestoreDelay", 2, 0..4, "ticks")
    val swapDelay = IntValue("SwapDelay", 1, 0..4, "ticks")

    private val range: IntRange
        get() = if (inventorySwap.value) 0..35 else 0..8

    private val swaps = ArrayDeque<Swap>()

    private var phase = Phase.IDLE
    private var previousSlot = 0
    private var restoreTimer = 0
    private var swapTimer = 0

    @EventTarget
    fun onTick(event: TickEvent) {
        if (event.phase != EventPhase.PRE)
            return

        if (mc.currentScreen != null && phase != Phase.RESTORING)
            return

        when (phase) {
            Phase.IDLE -> updateIdle()
            Phase.MINING -> updateMining()
            Phase.RESTORING -> updateRestoring()
        }
    }

    private fun updateIdle() {
        if (isMining()) {
            phase = Phase.MINING
            return
        }

        previousSlot = mc.thePlayer.inventory.currentItem
    }

    private fun updateMining() {
        if(swapTimer-- > 0) return
        swapTimer = swapDelay.value

        if (!isMining()) {
            phase = Phase.RESTORING
            restoreTimer = restoreDelay.value
            swapTimer = swapDelay.value
            return
        }

        equipBestTool()
    }

    private fun updateRestoring() {
        if (isMining()) {
            phase = Phase.MINING
            return
        }

        if (swaps.isNotEmpty() || mc.thePlayer.inventory.currentItem != previousSlot) {
            if(swaps.isNotEmpty()) restoreInventory()
            if(mc.thePlayer.inventory.currentItem != previousSlot) restoreSwap()
        } else {
            phase = Phase.IDLE
        }
    }

    fun restoreSwap() {
        if(swapTimer-- > 0)
            return
        swapTimer = swapDelay.value

        mc.thePlayer.inventory.currentItem = previousSlot
    }

    fun restoreInventory() {
        if (restoreTimer-- > 0)
            return
        restoreTimer = restoreDelay.value

        val swap = swaps.removeLast()
        performSwap(swap.inventorySlot, swap.hotbarSlot)
    }

    private fun equipBestTool() {
        val bestSlot = findBestSlot()
        if (bestSlot == -1)
            return

        val current = mc.thePlayer.inventory.currentItem
        if (bestSlot == current)
            return

        if (bestSlot < 9) {
            mc.thePlayer.inventory.currentItem = bestSlot
            return
        }

        if (swaps.lastOrNull()?.inventorySlot == bestSlot)
            return

        swaps += Swap(bestSlot, current)
        performSwap(bestSlot, current)
    }

    private fun performSwap(inventorySlot: Int, hotbarSlot: Int) {
        mc.playerController.windowClick(
            mc.thePlayer.inventoryContainer.windowId,
            inventorySlot,
            hotbarSlot,
            2,
            mc.thePlayer
        )
    }

    private fun findBestSlot(): Int {
        val hit = mc.objectMouseOver
        if (hit?.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
            return -1

        val block = mc.theWorld.getBlockState(hit.blockPos).block

        var bestSlot = -1
        var bestSpeed = 1.0f

        for (slot in range) {
            val stack = mc.thePlayer.inventory.mainInventory[slot] ?: continue
            val speed = stack.getStrVsBlock(block)

            if (speed > bestSpeed) {
                bestSpeed = speed
                bestSlot = slot
            }
        }

        return bestSlot
    }

    private fun isMining(): Boolean =
        mc.gameSettings.keyBindAttack.isKeyDown

    override fun onDisable() {
        phase = Phase.IDLE
        restoreTimer = 0
        swaps.clear()
    }
}