package net.ax.manbou.module.impl.target

import net.ax.manbou.module.BooleanValue
import net.ax.manbou.module.Category
import net.ax.manbou.module.ModuleBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemArmor
import net.minecraft.util.EnumChatFormatting


object Teams: ModuleBase("Teams", Category.TARGET) {
    val scoreboard = BooleanValue("Scoreboard", true)
    val nameColor = BooleanValue("NameColor", true)
    val armorColor = BooleanValue("ArmorColor", true)

    fun isTeam(target: EntityPlayer): Boolean {
        val player = mc.thePlayer?: return false
        
        // Scoreboard Team check
        if (scoreboard.value && player.team?.isSameTeam(target.team) == true) {
            return true
        }

        // Name color check
        if (nameColor.value) {
            val targetColor = getPlayerNameColor(target)
            val playerColor = getPlayerNameColor(player)
            if(targetColor == playerColor && targetColor != EnumChatFormatting.WHITE)
                return true
        }

        // Armor color check
        if (armorColor.value) {
            val targetArmor = target.inventory.armorInventory[2] // Chestplate
            val playerArmor = player.inventory.armorInventory[2]
            val targetItem = targetArmor?.item
            val playerItem = playerArmor?.item

            if (targetArmor != null && playerArmor != null
                && targetItem is ItemArmor && playerItem is ItemArmor
                && targetItem.armorMaterial == ItemArmor.ArmorMaterial.LEATHER && playerItem.armorMaterial == ItemArmor.ArmorMaterial.LEATHER) {


                val targetColor = targetItem.getColor(targetArmor)
                val playerColor = playerItem.getColor(playerArmor)

                if (targetColor != -1 && targetColor == playerColor) {
                    return true
                }
            }
        }

        return false
    }

    fun getPlayerNameColor(player: EntityPlayer?): EnumChatFormatting {
        if (player != null) {
            val displayName = player.getDisplayName()

            if (displayName != null) {
                val color = displayName.getChatStyle().color

                return color ?: EnumChatFormatting.WHITE
            }
        }
        return EnumChatFormatting.WHITE
    }
}