package aquarium

import java.io.ByteArrayOutputStream
import java.io.PrintStream


// 1 - Encapsulate Field

// До
object EncapsulateFieldBefore {
    // поля public, тому їх можна змінити з будь-якого місця
    class Aquarium(val name: String, val volumeLiters: Int) {
        var temperature: Double = 0.0
        var ph: Double = 7.0
        var readingsCount: Int = 0
    }

    // лічильник збільшуємо вручну, і так у кожній функції
    fun applyTemperature(aquarium: Aquarium, value: Double) {
        aquarium.temperature = value
        aquarium.readingsCount = aquarium.readingsCount + 1
    }

    fun applyPh(aquarium: Aquarium, value: Double) {
        aquarium.ph = value
        aquarium.readingsCount = aquarium.readingsCount + 1
    }

    fun run() {
        val tank = Aquarium("Рифовий акваріум", volumeLiters = 120)
        applyTemperature(tank, 25.4)
        applyPh(tank, 8.1)
        println("${tank.name}, ${tank.volumeLiters} л")
        println("Температура: ${tank.temperature} °C")
        println("pH: ${tank.ph}")
        println("Оброблено показів: ${tank.readingsCount}")
    }
}

// Після
object EncapsulateFieldAfter {
    // set закрили (private), змінити поле можна тільки через методи
    class Aquarium(val name: String, val volumeLiters: Int) {
        var temperature: Double = 0.0
            private set
        var ph: Double = 7.0
            private set
        var readingsCount: Int = 0
            private set

        // лічильник тепер збільшується тільки тут
        fun updateTemperature(celsius: Double) {
            temperature = celsius
            readingsCount++
        }

        fun updatePh(newPh: Double) {
            ph = newPh
            readingsCount++
        }
    }

    fun run() {
        val tank = Aquarium("Рифовий акваріум", volumeLiters = 120)
        tank.updateTemperature(25.4)
        tank.updatePh(8.1)
        println("${tank.name}, ${tank.volumeLiters} л")
        println("Температура: ${tank.temperature} °C")
        println("pH: ${tank.ph}")
        println("Оброблено показів: ${tank.readingsCount}")
    }
}


// 2 - Replace Type Code with Class

// До
object TypeCodeBefore {
    // тип датчика - просто числа, можна передати будь-яке
    const val SENSOR_TEMPERATURE = 1
    const val SENSOR_PH = 2
    const val SENSOR_WATER_LEVEL = 3

    class SensorReading(val sensorType: Int, val value: Double)

    // назва і одиниця в окремих функціях, при новому датчику міняти обидві
    fun sensorTitle(type: Int): String = when (type) {
        SENSOR_TEMPERATURE -> "Температура"
        SENSOR_PH -> "Кислотність"
        SENSOR_WATER_LEVEL -> "Рівень води"
        else -> "Невідомий датчик"
    }

    fun sensorUnit(type: Int): String = when (type) {
        SENSOR_TEMPERATURE -> "°C"
        SENSOR_PH -> "pH"
        SENSOR_WATER_LEVEL -> "см"
        else -> ""
    }

    fun run() {
        val readings = listOf(
            SensorReading(SENSOR_TEMPERATURE, 25.4),
            SensorReading(SENSOR_PH, 8.1),
            SensorReading(SENSOR_WATER_LEVEL, 42.0),
        )
        for (reading in readings) {
            val title = sensorTitle(reading.sensorType)
            val unit = sensorUnit(reading.sensorType)
            println("$title: ${reading.value} $unit")
        }
    }
}

// Після
object TypeCodeAfter {
    // тип датчика став enum, назва і одиниця лежать разом з ним
    enum class SensorType(val title: String, val unit: String) {
        TEMPERATURE("Температура", "°C"),
        PH("Кислотність", "pH"),
        WATER_LEVEL("Рівень води", "см"),
    }

    class SensorReading(val sensorType: SensorType, val value: Double)

    fun run() {
        val readings = listOf(
            SensorReading(SensorType.TEMPERATURE, 25.4),
            SensorReading(SensorType.PH, 8.1),
            SensorReading(SensorType.WATER_LEVEL, 42.0),
        )
        for (reading in readings) {
            val type = reading.sensorType
            println("${type.title}: ${reading.value} ${type.unit}")
        }
    }
}


// 3 - Replace Data Value with Object

// До
object DataValueBefore {
    // pH - звичайний Double, діапазон 0..14 перевіряємо вручну
    fun phStatus(ph: Double): String = when {
        ph < 0.0 || ph > 14.0 -> "помилка"
        ph < 6.5 -> "кисла"
        ph > 8.5 -> "лужна"
        else -> "норма"
    }

    fun showPh(value: Double) {
        if (value < 0.0 || value > 14.0) {
            println("Некоректне значення pH: $value")
        } else {
            println("pH: $value (${phStatus(value)})")
        }
    }

    fun run() {
        showPh(8.1)
        showPh(15.2)
    }
}

// Після
object DataValueAfter {
    // pH став окремим класом, перевірка і статус тепер всередині
    @JvmInline
    value class Ph(val value: Double) {
        init {
            require(value in MIN..MAX) { "Некоректне значення pH: $value" }
        }

        val status: String
            get() = when {
                value < 6.5 -> "кисла"
                value > 8.5 -> "лужна"
                else -> "норма"
            }

        override fun toString(): String = value.toString()

        companion object {
            const val MIN = 0.0
            const val MAX = 14.0

            fun ofOrNull(value: Double): Ph? =
                if (value in MIN..MAX) Ph(value) else null
        }
    }

    fun showPh(value: Double) {
        val ph = Ph.ofOrNull(value)
        if (ph == null) {
            println("Некоректне значення pH: $value")
        } else {
            println("pH: $ph (${ph.status})")
        }
    }

    fun run() {
        showPh(8.1)
        showPh(15.2)
    }
}


// Перевірка: чи однаковий вивід до і після

/** Запускає [block] і повертає все, що він вивів у консоль. */
fun captureOutput(block: () -> Unit): String {
    val buffer = ByteArrayOutputStream()
    val original = System.out
    System.setOut(PrintStream(buffer, true, "UTF-8"))
    try {
        block()
    } finally {
        System.setOut(original)
    }
    return buffer.toString("UTF-8")
}

fun check(name: String, before: () -> Unit, after: () -> Unit) {
    val outBefore = captureOutput(before)
    val outAfter = captureOutput(after)
    println("=== $name: до ===")
    print(outBefore)
    println("=== $name: після ===")
    print(outAfter)
    println("Вивід однаковий: ${outBefore == outAfter}")
    println()
}

fun main() {
    check("1 - Encapsulate Field", EncapsulateFieldBefore::run, EncapsulateFieldAfter::run)
    check("2 - Replace Type Code with Class", TypeCodeBefore::run, TypeCodeAfter::run)
    check("3 - Replace Data Value with Object", DataValueBefore::run, DataValueAfter::run)
}
