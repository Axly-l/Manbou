package net.ax.manbou.module.impl.target

import net.ax.manbou.module.BooleanValue
import net.ax.manbou.module.Category
import net.ax.manbou.module.ModuleBase
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer

object AntiBot: ModuleBase("AntiBot", Category.TARGET) {
    val tabList = BooleanValue("TabList", true)
    val entityID = BooleanValue("EntityID", true)
    val illegalName = BooleanValue("IllegalName", true)
    val spectator = BooleanValue("Spectator", true)
    val armorStand = BooleanValue("standFuck", true)

    fun isBot(entity: EntityLivingBase): Boolean {
        if (!this.state) return false

        if(entity is EntityArmorStand && armorStand.value) return true

        if (entity !is EntityPlayer) return false

        // TabList check
        if (tabList.value) {

            val networkManager = mc.netHandler ?: return false
            networkManager.getPlayerInfo(entity.uniqueID) ?: return true
        }

        // EntityID check
        if (entityID.value) {
            if (entity.entityId >= 1000000000 || entity.entityId <= -1) return true
        }
        
        // IllegalName check
        if (illegalName.value) {
            val name = entity.gameProfile.name
            if (!name.matches(Regex("^[a-zA-Z0-9_]{3,16}$"))) return true
        }

        // Spectator check
        if (spectator.value) {
            if (entity.isSpectator) return true
        }
        return false
    }
}