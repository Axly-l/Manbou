package net.ax.manbou.module.impl.client

import net.ax.manbou.module.Category
import net.ax.manbou.module.ModuleBase
import net.ax.manbou.module.StringValue

object Command: ModuleBase("Command", Category.CLIENT, forceEnable = true, shouldPlayToggleSound = false) {
    val prefix = StringValue("Prefix", 2, ".")
}