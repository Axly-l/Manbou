package net.ax.manbou.module

import net.ax.manbou.command.CommandManager
import net.ax.manbou.command.ICommand
import net.ax.manbou.event.*
import net.ax.manbou.utils.*
import org.lwjgl.input.Keyboard
import org.reflections.Reflections
import java.util.EnumMap
import java.util.Locale

enum class Category(val displayName: String) {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    WORLD("World"),
    VISUAL("Visual"),
    TARGET("Target"),
    CLIENT("Client"),
    NONE("None")
}

interface IModule: EventListener, IMinecraft {
    val name: String
    val category: Category
    var state: Boolean
    val forceEnable: Boolean
    val shouldPlayToggleSound: Boolean
    var keyBind: Int
    val defaultHidden: Boolean
    var hidden: BooleanValue?
    val values: ArrayList<Value<*>>

    fun getDisplayValues(): List<Value<*>>
    fun toggle(): Boolean
    fun setState(state: Boolean) = if(state != this.state) toggle() else false
    fun onEnable() {}
    fun onDisable() {}
    fun registryAllValues()

    override fun isListenable(): Boolean {
        return state
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class OMMC

abstract class ModuleBase(
    override val name: String = "",
    override val category: Category = Category.NONE,
    defaultState: Boolean = false,
    override val forceEnable: Boolean = false,
    override val shouldPlayToggleSound: Boolean = true,
    override var keyBind: Int = 0,
    override val defaultHidden: Boolean = false
) : IModule, ICommand {
    final override var state = defaultState
    override var hidden: BooleanValue? = BooleanValue("HideArray", defaultHidden)
    override val values: ArrayList<Value<*>> = arrayListOf()
    override val aliases: Array<String> = arrayOf(this.name.lowercase())

    init {
        if(forceEnable) state = true
    }

    override fun getDisplayValues(): List<Value<*>> = buildList {
        hidden?.let(::add)
        addAll(values)
    }

    override fun registryAllValues() {
        values.clear()

        val collectedValues = linkedSetOf<Value<*>>()
        for (field in this::class.java.declaredFields) {
            if (field.isSynthetic || field.name == "hidden") continue

            field.isAccessible = true
            try {
                when (val entry = field.get(this)) {
                    is Value<*> -> {
                        entry.parent = this
                        if (entry !== hidden) collectedValues.add(entry)
                    }
                    is ValueProvider -> entry.collectValues()
                        .filterNot { it === hidden }
                        .forEach(collectedValues::add)
                }
            } catch (e: Exception) {
                LogUtil.error("Lmao", e)
            }
        }

        values.addAll(collectedValues)
    }

    override fun toggle(): Boolean {
        if (forceEnable) return false
        state = !state

        if (shouldPlayToggleSound && mc.theWorld != null) {
            mc.thePlayer.playSound("random.click", 0.4f, if(state) 1.0f else 0.8f)
        }

        if (state) onEnable() else onDisable()
        return state
    }

    override fun onEnable() {}
    override fun onDisable() {}
    final override fun execute(args: Array<String>) {
        if(args.isEmpty()) {
            DisplayUtils.addClientMessage("${this.name}::${if(this.state) "Enabled" else "Disabled"} [${Keyboard.getKeyName(this.keyBind)}]")
            for(value in this.values) {
                when (value) {
                    is MultiValue -> {
                        DisplayUtils.addClientMessage("${value.name}: ${value.selected.joinToString(", ")}")
                    }

                    is ListValue -> {
                        DisplayUtils.addClientMessage("${value.name}: ${value.stringValue}")
                    }

                    else -> {
                        DisplayUtils.addClientMessage("${value.name}: ${value.value}")
                    }
                }
            }
            return
        }

        if(args[0] == "state") {
            if(args.size != 2) return
            this.setState(args[1].toBooleanStrictOrNull() ?: this.state)
            return
        }

        val vName = args[0]
        val value = values.find { it.name.equals(vName, ignoreCase = true) }

        if(value == null) {
            DisplayUtils.addClientMessage("invalid args: ${args[0]}")
        }

        when (value) {
            is MultiValue -> {
                if (args.size == 1) {
                    DisplayUtils.addClientMessage("${this.name}.${value.name}: ${value.selected.joinToString { ", " }}")
                    return
                }

                var i = 0
                for (str in args.drop(1)) {
                    val index = value.options.indexOf(str)
                    i = i or index
                }
                value.value = i
            }

            is ListValue -> {
                if (args.size == 1) {
                    DisplayUtils.addClientMessage("${this.name}.${value.name}: ${value.stringValue}")
                    return
                }

                value.value = value.options.indexOf(args[1])
            }

            is FloatValue -> {
                if (args.size == 1) {
                    DisplayUtils.addClientMessage("${this.name}.${value.name}: ${value.value}")
                    return
                }
                value.value = args[1].toFloat()
            }

            is DoubleValue -> {
                if (args.size == 1) {
                    DisplayUtils.addClientMessage("${this.name}.${value.name}: ${value.value}")
                    return
                }
                value.value = args[1].toDouble()
            }

            is IntValue -> {
                if (args.size == 1) {
                    DisplayUtils.addClientMessage("${this.name}.${value.name}: ${value.value}")
                    return
                }
                value.value = args[1].toInt()
            }

            is BooleanValue -> {
                if (args.size == 1) {
                    DisplayUtils.addClientMessage("${this.name}.${value.name}: ${value.value}")
                    return
                }
                val b = args[1].toBooleanStrictOrNull()
                if (b != null) {
                    value.value = b
                } else {
                    DisplayUtils.addClientMessage("invalid value: ${args[1]}")
                }
            }

            is StringValue -> {
                if (args.size == 1) {
                    DisplayUtils.addClientMessage("${this.name}.${value.name}: ${value.value}")
                    return
                }
                value.value = args.drop(1).joinToString(" ")
            }
        }
    }
}

object ModuleManager: EventListener, IMinecraft{
    val modules = arrayListOf<IModule>()
    private val modulesByCategory = EnumMap<Category, MutableList<IModule>>(Category::class.java).apply {
        Category.entries.forEach { put(it, arrayListOf()) }
    }
    private val modulesByName = linkedMapOf<String, IModule>()

    fun init() {
        modules.clear()
        modulesByName.clear()
        modulesByCategory.values.forEach { it.clear() }
        EventManager.register(this)

        val refl = Reflections("net.ax.manbou.module.impl")
        val classes = refl.getSubTypesOf(IModule::class.java)

        for (clazz in classes) {
            try {
                if (clazz.isAnnotationPresent(OMMC::class.java)) continue

                val module = try {
                    clazz.kotlin.objectInstance
                } catch (_: NoSuchFieldException) { null } ?: continue

                val normalizedName = module.name.lowercase(Locale.ROOT)
                if (modulesByName.containsKey(normalizedName)) {
                    LogUtil.warn("Skipping duplicate module registration: ${module.name}")
                    continue
                }

                modules.add(module)
                modulesByName[normalizedName] = module
                modulesByCategory[module.category]?.add(module)
                CommandManager.commands.add(module as ModuleBase)
            } catch (e: Exception) {
                LogUtil.error("Lmao", e)
                e.printStackTrace()
            }
        }

        modules.sortBy { it.name.lowercase(Locale.ROOT) }
        modulesByCategory.values.forEach { categoryModules ->
            categoryModules.sortBy { it.name.lowercase(Locale.ROOT) }
        }

        for (module in modules) {
            module.registryAllValues()
                EventManager.register(module)
        }
    }

    fun getByCategory(category: Category): List<IModule> = modulesByCategory[category].orEmpty()

    fun getByName(name: String): IModule? = modulesByName[name.lowercase(Locale.ROOT)]

    @EventTarget
    fun onKey(event: KeyboardEvent) {
        if (mc.thePlayer == null || mc.currentScreen != null) return

        val key = event.key
        val isPress: Boolean = event.isPress

        if (!isPress || key == Keyboard.KEY_NONE) return

        for(module in modules) {
            if (key == module.keyBind) {
                module.toggle()
            }
        }
    }
}
