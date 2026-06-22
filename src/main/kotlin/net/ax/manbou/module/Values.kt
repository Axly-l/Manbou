package net.ax.manbou.module

import kotlin.math.floor
import kotlin.math.roundToInt

fun interface ValueProvider {
    fun collectValues(): Iterable<Value<*>>
}

abstract class Value<T>(val name: String, defaultValue: T, val visibility: () -> Boolean = {true}, val observable: (T) -> Unit = {}) {
    open var value: T = defaultValue
        set(value) {
            if (value == field) return
            observable(value)
            field = value
        }

    lateinit var parent: IModule
}

class BooleanValue(name: String, defaultValue: Boolean, visibility: () -> Boolean = {true}, observable: (Boolean) -> Unit = {}) : Value<Boolean>(name, defaultValue, visibility,
    observable
) {
    override var value: Boolean
        get() = super.value
        set(value) {
            super.value = value
        }
    fun toggle() {
        this.value = !this.value
    }
}

abstract class NumberValue<T>(name: String, defaultValue: T, val valueRange: ClosedRange<T>, val suffix: String = "", val step: T, visibility: () -> Boolean = {true},
                     observable: (T) -> Unit = {}
)
    : Value<T>(name, defaultValue, visibility, observable) where T : Number, T : Comparable<T> {

    override var value: T
        get() = super.value
        set(value) {
            super.value = value.coerceIn(valueRange.start, valueRange.endInclusive)
        }

    abstract fun parse(value: String)
}

class IntValue(name: String, defaultValue: Int, valueRange: ClosedRange<Int>, suffix: String = "", step: Int = 1, visibility: () -> Boolean = {true}, observable: (Int) -> Unit = {})
    : NumberValue<Int>(name, defaultValue, valueRange, suffix, step, visibility, observable) {
    override var value: Int
        get() = super.value
        set(value) {
            super.value = value.coerceIn(valueRange.start, valueRange.endInclusive)
        }

    override fun parse(value: String) {
        this.value = value.toDouble().toInt()
    }
}

class FloatValue(name: String, defaultValue: Float, valueRange: ClosedRange<Float>, suffix: String = "", step: Float = 0.1f, visibility: () -> Boolean = {true}, observable: (Float) -> Unit = {})
    : NumberValue<Float>(name, defaultValue, valueRange, suffix, step, visibility, observable) {
    override var value: Float
        get() = super.value
        set(value) {
            val v = value.coerceIn(valueRange.start, valueRange.endInclusive)
            super.value= floor((((v/ this.step).roundToInt() * this.step) + 1E-6f) * 1E5f) / 1E5f
        }
    override fun parse(value: String) {
        this.value = value.toFloat()
    }
}

class DoubleValue(name: String, defaultValue: Double, valueRange: ClosedRange<Double>, suffix: String = "", step: Double = 0.1, visibility: () -> Boolean = {true}, observable: (Double) -> Unit = {})
    : NumberValue<Double>(name, defaultValue, valueRange, suffix, step, visibility, observable) {
    override var value: Double
        get() = super.value
        set(value) {
            val v = value.coerceIn(valueRange.start, valueRange.endInclusive)
            super.value = floor((((v / this.step).roundToInt() * this.step) + 1E-6) * 1E5) / 1E5
        }
    override fun parse(value: String) {
        this.value = value.toDouble()
    }
}

class ListValue(
    name: String,
    val options: List<String>,
    defaultIndex: Int,
    visibility: () -> Boolean = {true},
    observable: (Int) -> Unit = {}
) : Value<Int>(name, defaultIndex, visibility, observable) {

    var expanded: Boolean = false

    val stringValue: String
        get() = options[value.coerceIn(0, options.lastIndex)]

    override var value: Int
        get() = super.value
        set(v) {
            if (v in options.indices) super.value = v
        }

    fun next() {
        value = (value + 1) % options.size
    }
}

class MultiValue(
    name: String,
    val options: List<String>,
    defaultMask: Int,
    visibility: () -> Boolean = {true},
    observable: (Int) -> Unit = {}
) : Value<Int>(name, defaultMask, visibility, observable) {
    var expanded: Boolean = false
    val selectedCount: Int
        get() = Integer.bitCount(value)
    val selected: List<String>
        get() = options.filterIndexed { index, _ -> isSelected(index) }
    private val optionIndex =
        options.withIndex().associate { it.value to it.index }

    fun toggle(index: Int) {
        if (index !in options.indices) return
        value = value xor (1 shl index)
    }

    fun toggle(optionName: String)
        = optionIndex[optionName]?.let(::toggle) ?: Unit

    fun isSelected(index: Int): Boolean {
        return (value and (1 shl index)) != 0
    }

    fun isSelected(optionName: String): Boolean =
        optionIndex[optionName]?.let(::isSelected) ?: false
}

class StringValue(
    name: String,
    val maxLength: Int,
    defaultValue: String,
    val ignoreEmpty: Boolean = true,
    visibility: () -> Boolean = { true },
    observable: (String) -> Unit = {}
): Value<String>(name, defaultValue, visibility, observable) {
    override var value: String
        get() = super.value
        set(value) {
            if(ignoreEmpty && value.isEmpty()) return
            if(value.length > maxLength) return
            super.value = value
        }
}