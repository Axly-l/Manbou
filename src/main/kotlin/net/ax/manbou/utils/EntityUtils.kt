package net.ax.manbou.utils

import net.ax.manbou.module.impl.target.Animal
import net.ax.manbou.module.impl.target.AntiBot
import net.ax.manbou.module.impl.target.Corpse
import net.ax.manbou.module.impl.target.Invisible
import net.ax.manbou.module.impl.target.Mob
import net.ax.manbou.module.impl.target.Other
import net.ax.manbou.module.impl.target.Player
import net.ax.manbou.module.impl.target.Teams
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.player.EntityPlayer
import kotlin.math.abs

object EntityUtils: IMinecraft {
    fun findTargets(distance: Float, angle: Float, wallCheck: Boolean): List<EntityLivingBase> {
        val entities = mc.theWorld?.loadedEntityList?: return emptyList<EntityLivingBase>()
        val targets = entities
            .filterIsInstance<EntityLivingBase>()
            .filter {
                it != mc.thePlayer
                        && (!AntiBot.state || !AntiBot.isBot(it))
                        && (it !is EntityPlayer || !Teams.state || !Teams.isTeam(it))
                        && mc.thePlayer.getDistanceToEntity(it) < distance
                        && isTarget(it)
                        && abs(RotationUtils.getAngleDifference(RotationManager.cameraYaw, RotationUtils.getRotationsTo(mc.thePlayer.positionVector, it.positionVector).yaw)) <= angle
                        && (!wallCheck || mc.thePlayer.canEntityBeSeen(it))
            }.sortedBy { it.getDistanceToEntity(mc.thePlayer) }

        return targets
    }

    fun isTarget(target: EntityLivingBase): Boolean {
        if(!Invisible.state && target.isInvisible) return false
        if(!Corpse.state && !target.isEntityAlive) return false

        val isPlayer = target is EntityPlayer
        val isMob = target is EntityMob
        val isAnimal = target is EntityAnimal

        if(Player.state && isPlayer) return true
        if(Mob.state && isMob) return true
        if(Animal.state && isAnimal) return true
        return Other.state && !isPlayer && !isMob && !isAnimal
    }
}