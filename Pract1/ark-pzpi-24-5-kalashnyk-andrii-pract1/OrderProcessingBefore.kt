package bookstore.before

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

fun main() {
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
