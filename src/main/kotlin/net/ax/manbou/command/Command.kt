package net.ax.manbou.command

import net.ax.manbou.utils.IMinecraft

interface ICommand: IMinecraft {
    val aliases: Array<String>
    fun execute(args: Array<String>)
}