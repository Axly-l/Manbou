package net.ax.manbou.command.impl

import net.ax.manbou.command.ICommand
import net.ax.manbou.module.ModuleManager
import net.ax.manbou.utils.DisplayUtils
import net.ax.manbou.utils.LogUtil
import org.lwjgl.input.Keyboard

object BindCommand: ICommand {
    override val aliases: Array<String> = arrayOf("bind", "b")

    override fun execute(args: Array<String>) {
        if(args.size != 2) return
        val module = ModuleManager.getByName(args[0])
        if(module == null) {
            DisplayUtils.addClientMessage("Module ${args[0]} was not found.")
            return
        }

        if(args[1] == "None") {
            module.keyBind = 0
            return
        }

        val key = Keyboard.getKeyIndex(args[1])
        if(key == 0) {
            DisplayUtils.addClientMessage("invalid arg: ${args[1]}")
            return
        }
        module.keyBind = key
    }
}