package net.ax.manbou.event

import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.EnumPacketDirection
import net.minecraft.network.Packet

class TickEvent(phase: EventPhase): IEvent by EventBase(phase)
class MotionEvent(var posX: Double, var posY: Double, var posZ: Double, var yaw: Float, var pitch: Float, var onGround: Boolean): IEvent by EventBase(EventPhase.PRE)
class PreSprintEvent: IEvent by EventBase(EventPhase.PRE)
class PacketEvent(var packet: Packet<*>, val direction: EnumPacketDirection): IEvent by EventBase(EventPhase.PRE, true)
class KeyboardEvent(val key: Int, val isPress: Boolean): IEvent by EventBase(EventPhase.POST)
class Render2DEvent(val partialTicks: Float): IEvent by EventBase(EventPhase.NONE)
class Render3DEvent(val partialTicks: Float): IEvent by EventBase(EventPhase.NONE)
class StrafeEvent(var yaw: Float, var strafe: Float, var forward: Float, var friction: Float): IEvent by EventBase(EventPhase.PRE, true)
class JumpEvent(var upwardsMotion: Double, var yaw: Float): IEvent by EventBase(EventPhase.PRE, true)
class AttackEvent(val playerIn: EntityPlayer, val targetIn: Entity): IEvent by EventBase(EventPhase.PRE, true)
class LivingUpdateEvent: IEvent by EventBase(EventPhase.PRE)
class SectionEvent(val name: String): IEvent by EventBase(EventPhase.PRE)
class SignalEvent(val name: String): IEvent by EventBase(EventPhase.PRE)
class CameraEvent(val partialTicks: Float): IEvent by EventBase(EventPhase.POST)
class ActionEvent(var sprinting: Boolean, var sneaking: Boolean): IEvent by EventBase(EventPhase.PRE)
class SendChatEvent(var msg: String, var addToChat: Boolean): IEvent by EventBase(EventPhase.PRE, true)