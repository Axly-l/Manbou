package net.ax.manbou.utils

import net.ax.manbou.Main
import net.ax.manbou.event.*
import net.ax.manbou.module.impl.movement.MoveFix
import net.ax.manbou.module.impl.visual.SilentRotation
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt


const val TO_RADS = StrictMath.PI / 180.0
const val TO_DEGS = 180.0 / StrictMath.PI

val gcd: Float
    get() {
        val f = Minecraft.getMinecraft().gameSettings.mouseSensitivity * 0.6f + 0.2f
        return f * f * f * 8.0f
    }

data class Rotation(val yaw: Float, val pitch: Float)

object RotationManager : EventListener, IMinecraft {

    var cameraYaw = 0.0f
    var cameraPitch = 0.0f
    var prevCameraYaw = 0.0f
    var prevCameraPitch = 0.0f
    var headYaw = 0.0f
    var headPitch = 0.0f
    var prevHeadYaw = 0.0f
    var prevHeadPitch = 0.0f

    fun getInterpolatedCameraYaw(partialTicks: Float): Float = prevCameraYaw + (cameraYaw - prevCameraYaw) * partialTicks
    fun getInterpolatedCameraPitch(partialTicks: Float): Float = prevCameraPitch + (cameraPitch - prevCameraPitch) * partialTicks

    private var tick: Int = 0
    var rotating = false
    var returning = false

    @EventTarget
    fun onTick(event: TickEvent) {
        mc.thePlayer ?: return
        mc.theWorld ?: return

        if(rotating) {
            ++tick
            if(tick > 5) {
                tick = 0
                rotating = false
                returning = true
            }
        }

        if(returning) {
            handleReturning()
        }
    }

    @EventTarget(-1000)
    fun onStrafe(event: StrafeEvent) {
        if ((rotating || returning) && MoveFix.state) {
            when(MoveFix.mode.stringValue) {
                "None" -> {
                    event.yaw = cameraYaw
                }
                "Silent" -> {
                    MovementUtils.fixStrafe(event, cameraYaw)
                }
            }
        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if ((rotating || returning) && MoveFix.state && MoveFix.mode.stringValue == "None") event.yaw = cameraYaw
    }

    @EventTarget(-100)
    fun onPreCamera(e: CameraEvent) {
        if(!rotating && !returning) {
            headYaw = cameraYaw
            headPitch = cameraPitch
        }
    }

    @EventTarget(100)
    fun onPostCamera(e: CameraEvent) {
        headYaw = MathHelper.wrapAngleTo180_float(headYaw)
        headPitch = headPitch.coerceIn(-90.0f, 90.0f)
        cameraYaw = MathHelper.wrapAngleTo180_float(cameraYaw)
        cameraPitch = cameraPitch.coerceIn(-90.0f, 90.0f)

        while (this.headYaw - this.prevHeadYaw < -180.0F)
        {
            this.prevHeadYaw -= 360.0F;
        }

        while (this.headYaw - this.prevHeadYaw >= 180.0F)
        {
            this.prevHeadYaw += 360.0F;
        }

        while (this.cameraYaw - this.prevCameraYaw < -180.0F)
        {
            this.prevCameraYaw -= 360.0F;
        }

        while (this.cameraYaw - this.prevCameraYaw >= 180.0F)
        {
            this.prevCameraYaw += 360.0F;
        }

        while (this.headPitch - this.prevHeadPitch < -180.0F)
        {
            this.prevHeadPitch -= 360.0F;
        }

        while (this.headPitch - this.prevHeadPitch >= 180.0F)
        {
            this.prevHeadPitch += 360.0F;
        }

        while (this.cameraPitch - this.prevCameraPitch < -180.0F)
        {
            this.prevCameraPitch -= 360.0F;
        }

        while (this.cameraPitch - this.prevCameraPitch >= 180.0F)
        {
            this.prevCameraPitch += 360.0F;
        }

        mc.thePlayer.rotationYaw = headYaw
        mc.thePlayer.rotationPitch = headPitch
        mc.thePlayer.prevRotationYaw = prevHeadYaw
        mc.thePlayer.prevRotationPitch = prevHeadPitch
        mc.thePlayer.renderArmYaw = headYaw
        mc.thePlayer.renderArmPitch = headPitch
        mc.thePlayer.prevRenderArmYaw = prevHeadYaw
        mc.thePlayer.prevRenderArmPitch = prevHeadPitch
    }

    @EventTarget()
    fun onPacket(e: PacketEvent) {
        val packet = e.packet
        if(packet is S08PacketPlayerPosLook) {
            this.headYaw = packet.yaw
            this.cameraYaw = packet.yaw
            this.headPitch = packet.pitch
            this.cameraPitch = packet.pitch
        }
    }

    fun setAngles(deltaYaw: Float, deltaPitch: Float) {
        updatePrevRotations()

        cameraYaw += deltaYaw * 0.15f
        if (mc.gameSettings.invertMouse)
            cameraPitch += deltaPitch * 0.15f
        else cameraPitch -= deltaPitch * 0.15f
        cameraPitch = cameraPitch.coerceIn(-90.0f, 90.0f)
    }

    fun canSprint(): Boolean {
        val flag = mc.thePlayer.movementInput.moveForward >= 0.8f

        if(!this.rotating && !this.returning) return flag
        if(!MoveFix.state) return flag
        if(!MoveFix.sprintIntegrity.value) return flag
        return MovementUtils.isMoving
                && abs(RotationUtils.getAngleDifference(mc.thePlayer.rotationYaw
                , RotationUtils.getAdjustYaw_Degs(cameraYaw, mc.thePlayer.movementInput.moveForward, mc.thePlayer.movementInput.moveStrafe))) <= 45.0f
                && flag
    }

    fun updatePrevRotations() {
        prevHeadYaw = headYaw
        prevHeadPitch = headPitch
        prevCameraYaw = cameraYaw
        prevCameraPitch = cameraPitch
    }

    private var y = 0.0f
    private var p = 0.0f

    fun handleReturning() {
        rotateTo(SilentRotation.returnSpeed.value, this.cameraYaw, this.cameraPitch, 1.0f, true, true)

        val yawDiff = MathHelper.wrapAngleTo180_float(this.cameraYaw - this.headYaw)
        val pitchDiff = this.cameraPitch - this.headPitch

        if(abs(yawDiff) < gcd && abs(pitchDiff) < gcd) {
            this.returning = false
            this.cameraYaw = this.headYaw
            this.cameraPitch = this.headPitch
        }
    }

    fun rotateTo(speed: Float, yaw: Float, pitch: Float, deltaPT: Float, silent: Boolean = true, r: Boolean = false) {
        headYaw += ((Main.random.nextFloat() * 4.0f).roundToInt() - 2) * gcd
        headPitch += ((Main.random.nextFloat() * 2.0f).roundToInt() - 1) * gcd

        val yawDiff = MathHelper.wrapAngleTo180_float(yaw - headYaw)
        val pitchDiff = pitch - headPitch

        val dist = hypot(yawDiff, pitchDiff)
        val step = speed * deltaPT
        val ratio = step / dist

        if(abs(yawDiff) > step) {
            y += yawDiff * ratio

            repeat(abs(y).toInt()) {
                val f = abs(y) / y
                y -= f
                headYaw += f * gcd
            }
        } else {
            headYaw += RotationUtils.fixGCD(yawDiff)
        }

        if(abs(pitchDiff) > step) {
            p += pitchDiff * ratio

            repeat(abs(p).toInt()) {
                val f = abs(p) / p
                p -= f
                headPitch += f * gcd
            }
        } else {
            headPitch += RotationUtils.fixGCD(pitchDiff)
        }

        headPitch = headPitch.coerceIn(-90.0f, 90.0f)

        if(r) else if (silent) {
            rotating = true
            returning = false
            tick = 0
        } else {
            cameraYaw = headYaw
            cameraPitch = headPitch
        }
    }

    fun rotateEntity(speed: Float, targetIn: Entity, partialTicks: Float, lastPartialTicks: Float, silent: Boolean = true) {
        val playerEyePos = mc.thePlayer.getPositionEyes(partialTicks)
        val targetBB = targetIn.entityBoundingBox.expandMultiplier(0.6, 0.4, 0.6).offset(0.0, 0.1, 0.0)
        val targetRot = RotationUtils.getRotationsTo(playerEyePos, targetBB.getClosestPoint(playerEyePos))
        var deltaPT = partialTicks - lastPartialTicks
        if(deltaPT < 0) ++deltaPT
        rotateTo(speed, targetRot.yaw, targetRot.pitch, deltaPT, silent)
    }
}

object RotationUtils {
    fun getRotationsTo(current: Vec3, target: Vec3): Rotation {
        val xDiff = target.xCoord - current.xCoord
        val yDiff = target.yCoord - current.yCoord
        val zDiff = target.zCoord - current.zCoord

        val dist = hypot(xDiff, zDiff)
        val yaw = atan2(-xDiff, zDiff) * TO_DEGS
        val pitch = atan2(-yDiff, dist) * TO_DEGS

        return Rotation(yaw.toFloat(), pitch.toFloat())
    }
    fun getAdjustYaw(yaw: Float, forward: Float, strafe: Float): Double {
        if(forward == 0.0f && strafe == 0.0f) return yaw * TO_RADS
        val inputYaw = atan2(-strafe.toDouble(), forward.toDouble())
        return yaw * TO_RADS + inputYaw
    }
    fun getAdjustYaw_Degs(yaw: Float, forward: Float, strafe: Float): Float {
        if(forward == 0.0f && strafe == 0.0f) return yaw
        val inputYaw = atan2(-strafe.toDouble(), forward.toDouble())
        return yaw + (inputYaw * TO_DEGS).toFloat()
    }
    fun fixGCD(delta: Float): Float = (delta / gcd).toInt() * gcd
    fun getAngleDifference(a: Float, b: Float) = ((a - b) % 360f + 540f) % 360f - 180f
}