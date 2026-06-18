package net.ax.manbou.utils

import net.ax.manbou.event.StrafeEvent
import net.minecraft.util.AxisAlignedBB
import kotlin.math.*


object MovementUtils: IMinecraft {
    val isMoving: Boolean
        get() = (mc.gameSettings.keyBindForward.isKeyDown != mc.gameSettings.keyBindBack.isKeyDown) ||
                (mc.gameSettings.keyBindLeft.isKeyDown != mc.gameSettings.keyBindRight.isKeyDown)
    val speed: Double
        get() = hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ)

    fun strafe(event: StrafeEvent) {
        strafe(event, speed)
    }

    fun strafe(event: StrafeEvent, speed: Double) {
        if(event.forward == 0.0f && event.strafe == 0.0f) { return }

        val yaw = RotationUtils.getAdjustYaw(event.yaw, event.forward, event.strafe)
        mc.thePlayer.motionX = -sin(yaw) * speed
        mc.thePlayer.motionZ = cos(yaw) * speed
    }

    fun isNearEdge(threshold: Double): Boolean {
        val player = mc.thePlayer ?: return false
        if (!player.onGround) return false

        val playerBox = player.entityBoundingBox

        val sensorBox = AxisAlignedBB(
            playerBox.minX - threshold, playerBox.minY - 0.5, playerBox.minZ - threshold,
            playerBox.maxX + threshold, playerBox.maxY, playerBox.maxZ + threshold
        )

        val collidingBoxes = mc.theWorld.getCollidingBoundingBoxes(player, sensorBox)

        if (collidingBoxes.isEmpty()) return true

        var minX = Double.MAX_VALUE
        var minZ = Double.MAX_VALUE
        var maxX = -Double.MAX_VALUE
        var maxZ = -Double.MAX_VALUE

        for (box in collidingBoxes) {
            if (box.minX < minX) minX = box.minX
            if (box.minZ < minZ) minZ = box.minZ
            if (box.maxX > maxX) maxX = box.maxX
            if (box.maxZ > maxZ) maxZ = box.maxZ
        }

        val x = (playerBox.minX + playerBox.maxX) / 2.0
        val z = (playerBox.minZ + playerBox.maxZ) / 2.0

        return x - threshold < minX ||
                z - threshold < minZ ||
                x + threshold > maxX ||
                z + threshold > maxZ
    }

    fun fixStrafe(event: StrafeEvent, cameraYaw: Float) {
        val rotationYaw = event.yaw
        val forward = event.forward
        val strafe = event.strafe

        if (forward == 0f && strafe == 0f) return

        val inputYaw = RotationUtils.getAdjustYaw(cameraYaw, forward, strafe)

        val rotationYawRad = rotationYaw * TO_RADS
        val diffYaw = rotationYawRad - inputYaw

        val p4 = StrictMath.PI / 4.0
        val snappedDiff = diffYaw.round(p4)

        val maxInput = max(abs(forward), abs(strafe)).toDouble()

        val rawCos = cos(snappedDiff)
        val rawSin = sin(snappedDiff)

        val calcForward = if (abs(rawCos) < 0.001) 0.0 else sign(rawCos) * maxInput
        val calcStrafe  = if (abs(rawSin) < 0.001) 0.0 else sign(rawSin) * maxInput

        event.forward = calcForward.round(0.001).toFloat()
        event.strafe = calcStrafe.round(0.001).toFloat()
    }

    fun Double.round(other: Double): Double {
        return StrictMath.round(this / other) * other
    }
}