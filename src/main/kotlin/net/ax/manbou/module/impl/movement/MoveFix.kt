package net.ax.manbou.module.impl.movement

import net.ax.manbou.module.BooleanValue
import net.ax.manbou.module.Category
import net.ax.manbou.module.ListValue
import net.ax.manbou.module.ModuleBase

object MoveFix: ModuleBase("MoveFix", Category.VISUAL) {
    val mode = ListValue("Mode", listOf("None", "Silent"), 0)
    val sprintIntegrity = BooleanValue("SprintIntegrity", false)
}