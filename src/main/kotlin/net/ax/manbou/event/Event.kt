package net.ax.manbou.event

import net.ax.manbou.utils.LogUtil
import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.Collections
import java.util.IdentityHashMap
import java.util.function.Consumer

interface EventListener {
    fun isListenable() = true
}

enum class Phase {
    PRE,
    POST,
    NONE
}

interface IEvent {
    val phase: Phase
    var isCancelled: Boolean
    fun cancel()
}

open class EventBase(
    override val phase: Phase,
    private val cancellable: Boolean = false
) : IEvent {

    override var isCancelled = false

    override fun cancel() {
        if (cancellable) {
            isCancelled = true
        }
    }
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventTarget(val priority: Int = 0)

object EventManager {

    private class Handler(
        val priority: Int,
        val invoker: Consumer<IEvent>,
        val listener: EventListener
    )

    private val registry =
        IdentityHashMap<Class<out IEvent>, ArrayList<Handler>>()

    private val lookup = MethodHandles.lookup()

    private val registeredListeners =
        Collections.newSetFromMap(IdentityHashMap<EventListener, Boolean>())

    @Suppress("UNCHECKED_CAST")
    fun register(listener: EventListener) {
        if (!registeredListeners.add(listener)) return

        listener.javaClass.declaredMethods.forEach { method ->

            val annotation = method.getAnnotation(EventTarget::class.java)
                ?: return@forEach

            if (method.parameterCount != 1) return@forEach

            val eventClass = method.parameterTypes[0]

            if (!IEvent::class.java.isAssignableFrom(eventClass))
                return@forEach

            try {
                method.isAccessible = true

                val handle = lookup.unreflect(method)

                val site = LambdaMetafactory.metafactory(
                    lookup,
                    "accept",
                    MethodType.methodType(
                        Consumer::class.java,
                        listener.javaClass
                    ),
                    MethodType.methodType(Void.TYPE, Any::class.java),
                    handle,
                    MethodType.methodType(Void.TYPE, eventClass)
                )

                val invoker = site.target.invoke(listener) as Consumer<IEvent>

                registry
                    .computeIfAbsent(eventClass as Class<out IEvent>) {
                        arrayListOf()
                    }
                    .apply {
                        add(Handler(annotation.priority, invoker, listener))
                        sortBy { it.priority }
                    }

            } catch (e: Throwable) {
                LogUtil.error("Lmao\n${e.message}", e)
            }
        }
    }

    fun call(event: IEvent): Boolean {

        val handlers = registry[event.javaClass] ?: return false

        for (i in handlers.indices) {
            val handler = handlers[i]

            if (handler.listener.isListenable()) {
                handler.invoker.accept(event)
            }
        }

        return event.isCancelled
    }
}