package net.ax.manbou.module.impl.movement

import net.ax.manbou.event.EventTarget
import net.ax.manbou.event.MotionEvent
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

    @EventTarget
    fun onMotion(event: MotionEvent) {
        // --- autoSwitch 機能の追加 ---
        if (autoSwitch.value) {
            // 今手にブロックを持っていない場合のみ切り替え処理を走らせる
            if (mc.thePlayer.heldItem?.item !is ItemBlock) {
                val blockSlot = findBlockSlot()
                if (blockSlot != -1) {
                    if (blockSlot < 9) {
                        // 1. ホットバー（0〜8）で見つかった場合は、スロット選択を直接変えるだけ
                        mc.thePlayer.inventory.currentItem = blockSlot
                    } else {
                        // 2. インベントリ（9〜35）で見つかった場合は、現在のホットバーの枠とスワップする
                        // PlayerControllerのwindowClick（JavaだとwindowClick）を使って、一瞬でアイテムを入れ替える
                        // 引数: (windowId, slotId, button, mode, mc.thePlayer)
                        // mode 2 は「ホットバーとのクイックスワップ」、button に現在のcurrentItemを指定する
                        mc.playerController.windowClick(
                            mc.thePlayer.inventoryContainer.windowId,
                            blockSlot,
                            mc.thePlayer.inventory.currentItem,
                            2,
                            mc.thePlayer
                        )
                    }
                }
            }
        }

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
        // まずはホットバー（0〜8）を優先して探す（切り替えラグが一番少ないため）
        for (i in 0..8) {
            val stack = mc.thePlayer.inventory.mainInventory[i]
            if (stack != null && stack.item is ItemBlock && stack.stackSize > 0) {
                return i
            }
        }

        // ホットバーになければ、インベントリのメイン枠（9〜35）を探す
        // マイクラのメインインベントリ配列は、内部的にホットバー(0-8)の後に通常枠(9-35)が続く構造
        for (i in 9..35) {
            val stack = mc.thePlayer.inventory.mainInventory[i]
            if (stack != null && stack.item is ItemBlock && stack.stackSize > 0) {
                // 通常枠のインベントリスロットIDは、Container内での位置に合わせる必要がある
                // ContainerPlayer内では、メインインベントリ(9-35)は 9番目からスタートする
                return i
            }
        }

        return -1
    }

    override fun onDisable() {
        mc.gameSettings.keyBindSneak.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)
    }
}