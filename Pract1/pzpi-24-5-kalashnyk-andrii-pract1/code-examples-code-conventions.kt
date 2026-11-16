package bookstore

// Приклади до доповіді "Основні рекомендації написання коду мовою Kotlin".
// Before - код без рекомендацій, After - той самий код за Kotlin Coding Conventions.

// До
object Before {
  class book(t: String, a: String, p: Int) {
    var title: String = t
    var author: String = a
    var price: Int = p
  }

  class item(b: book, q: Int) {
    var b: book = b
    var q: Int = q
  }

  class Customer(var name: String, var email: String?)

  class OrderService
  {
    fun calc(l: ArrayList<item>, d: Int): Int
    {
      var s = 0
      for (i in 0..l.size - 1) {
        s = s + l[i].b.price * l[i].q
      }
      if (d > 30) return -1
      return s * (100 - d) / 100
    }

    fun pay(t: Int, bal: Int): String
    {
      if (bal >= t) {
        return "OK"
      }
      else {
        return "ERR"
      }
    }
  }

  fun getContact(c: Customer): String {
    var e: String = ""
    if (c.email != null) e = c.email!!
    else e = "email не вказано"
    return e
  }

  fun parse(s: String): Int {
    try {
      return Integer.parseInt(s)
    } catch (e: Exception) {
      return 1
    }
  }

  fun run() {
    var books = ArrayList<book>()
    books.add(book("1984", "Джордж Орвелл", 450))
    books.add(book("Капітал", "Карл Маркс", 520))
    var items = ArrayList<item>()
    items.add(item(books[0], 2))
    items.add(item(books[1], 1))
    var c = Customer("Олег Шевченко", "oleg.shevchenko92@gmail.com")
    var s = OrderService()
    var t = s.calc(items, 10)
    println("Покупець: " + c.name + ", " + getContact(c))
    println("Замовлення (" + items.size + " позиції):")
    for (i in items) println("  " + i.b.title + " (" + i.b.author + "): " + i.q + " × " + i.b.price + " грн = " + i.q * i.b.price + " грн")
    println("До оплати зі знижкою 10%: " + t + " грн")
    var r = s.pay(t, 1500)
    if (r == "OK") println("Баланс 1500 грн → Оплачено " + t + " грн")
    else println("Баланс 1500 грн → Помилка оплати: Недостатньо коштів")
    r = s.pay(t, 500)
    if (r == "OK") println("Баланс 500 грн → Оплачено " + t + " грн")
    else println("Баланс 500 грн → Помилка оплати: Недостатньо коштів")
    println("Кількість з введення \"abc\": " + parse("abc") + " (це не число, тому взято 1 за замовчуванням)")
  }
}

// Після
object After {
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

    fun run() {
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
        println(
            "Кількість з введення \"abc\": $quantity " +
                "(це не число, тому взято 1 за замовчуванням)",
        )
    }
}

// обидві версії виводять однаковий результат
fun main() {
    println("=== До ===")
    Before.run()
    println("=== Після ===")
    After.run()
}
