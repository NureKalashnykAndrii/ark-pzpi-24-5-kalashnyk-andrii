package bookstore.after

/** макс. знижка в % */
const val MAX_DISCOUNT_PERCENT = 30

/**
 * Книга з каталогу.
 *
 * @property title назва.
 * @property author автор.
 * @property price ціна в грн.
 */
data class Book(
    val title: String,
    val author: String,
    val price: Int,
)

/** позиція в замовленні - книга і кількість штук */
data class OrderItem(val book: Book, val quantity: Int) {
    init {
        require(quantity > 0) { "Кількість має бути більшою за 0" }
    }

    val cost: Int get() = book.price * quantity
}

data class Customer(val name: String, val email: String?)

/** результат оплати (і причина). */
sealed interface PaymentResult {
    data class Success(val amount: Int) : PaymentResult
    data class Failure(val reason: String) : PaymentResult
}

fun Int.toMoney(): String = "$this грн"

class OrderService {

    /**
     * Рахує суму замовлення з урахуванням знижки.
     *
     * @param items товари.
     * @param discountPercent знижка в %, від 0 до [MAX_DISCOUNT_PERCENT].
     * @return сума до оплати, грн.
     */
    fun calculateTotal(
        items: List<OrderItem>,
        discountPercent: Int = 0,
    ): Int {
        require(discountPercent in 0..MAX_DISCOUNT_PERCENT) {
            "Знижка має бути від 0 до $MAX_DISCOUNT_PERCENT %"
        }
        val subtotal = items.sumOf { it.cost }
        return subtotal * (100 - discountPercent) / 100
    }

    fun pay(total: Int, balance: Int): PaymentResult =
        if (balance >= total) {
            PaymentResult.Success(total)
        } else {
            PaymentResult.Failure("Недостатньо коштів")
        }
}

fun contactOf(customer: Customer): String =
    customer.email ?: "email не вказано"

fun parseQuantity(input: String): Int? = input.toIntOrNull()

fun describe(result: PaymentResult): String = when (result) {
    is PaymentResult.Success -> "Оплачено ${result.amount.toMoney()}"
    is PaymentResult.Failure -> "Помилка оплати: ${result.reason}"
}

fun main() {
    val books = listOf(
        Book("1984", "Джордж Орвелл", 450),
        Book("Капітал", "Карл Маркс", 520),
    )
    val items = listOf(
        OrderItem(books[0], quantity = 2),
        OrderItem(books[1], quantity = 1),
    )
    val customer = Customer("Олег Шевченко", email = "oleg.shevchenko92@gmail.com")
    val service = OrderService()

    val total = service.calculateTotal(items, discountPercent = 10)
    println("Покупець: ${customer.name}, ${contactOf(customer)}")
    println("Замовлення (${items.size} позиції):")
    items.forEach {
        println(
            "  ${it.book.title} (${it.book.author}): " +
                "${it.quantity} × ${it.book.price.toMoney()} = ${it.cost.toMoney()}",
        )
    }
    println("До оплати зі знижкою 10%: ${total.toMoney()}")
    println("Баланс 1500 грн → ${describe(service.pay(total, balance = 1500))}")
    println("Баланс 500 грн → ${describe(service.pay(total, balance = 500))}")

    val quantity = parseQuantity("abc") ?: 1
    println("Кількість з введення \"abc\": $quantity (це не число, тому взято 1 за замовчуванням)")
}
