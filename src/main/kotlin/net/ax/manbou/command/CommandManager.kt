package net.ax.manbou.command

import net.ax.manbou.event.SendChatEvent
import net.ax.manbou.event.EventListener
import net.ax.manbou.event.EventManager
import net.ax.manbou.event.EventTarget
import net.ax.manbou.module.impl.client.Command
import net.ax.manbou.utils.DisplayUtils
import net.ax.manbou.utils.IMinecraft
import org.reflections.Reflections

object CommandManager: EventListener, IMinecraft {
    val commands = arrayListOf<ICommand>()

    fun init() {
        commands.clear()
        EventManager.register(this)
        val refl = Reflections("net.ax.manbou.command.impl")
        val classes = refl.getSubTypesOf(ICommand::class.java)

        for (clazz in classes) {
            val c = try {
                clazz.kotlin.objectInstance
            } catch (_: NoSuchFieldException) { null } ?: continue

            commands.add(c)
        }
    }

    fun getByName(name: String): ICommand? {
        return commands.find({it.aliases.contains(name.lowercase())})
    }

    @EventTarget
    fun onSendChat(event: SendChatEvent) {
        var msg = event.msg
        val prefix = Command.prefix.value
        try{
            if(msg.substring(0, prefix.length) == prefix){
                msg = msg.substring(prefix.length)
                val split = msg.split(' ')
                val command = getByName(split[0])
                if(command != null) {
                    command.execute(split.drop(1).toTypedArray())
                } else {
                    DisplayUtils.addClientMessage("§c§oCommand '${split[0]}' was not found.")
                }
                event.addToChat = true
                event.cancel()
            }
        } catch (_: Exception){}
    }
}