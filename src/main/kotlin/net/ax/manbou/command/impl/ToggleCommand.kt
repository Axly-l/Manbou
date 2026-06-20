package net.ax.manbou.command.impl

import net.ax.manbou.command.ICommand
import net.ax.manbou.module.ModuleManager
import net.ax.manbou.utils.DisplayUtils

object ToggleCommand: ICommand {
    override val aliases: Array<String>
        get() = arrayOf("toggle", "t")

    override fun execute(args: Array<String>) {
        if(args.isEmpty()) return
        for(str in args) {
            val m = ModuleManager.getByName(str)
            if(m != null) {
                m.toggle()
            } else {
                DisplayUtils.addClientMessage("Module $str was not found.")
            }
        }
    }
}