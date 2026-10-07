package com.freeshop.billing.domain

import kotlin.math.abs

object Money {
    fun format(amount: Double): String {
        val rupees = amount.toLong()
        val paise  = Math.round((abs(amount) - abs(rupees.toDouble())) * 100).toInt()
        return if (paise == 0) "Rs %.0f".format(amount) else "Rs %.2f".format(amount)
    }

    fun amountInWords(amount: Double): String {
        val rupees = amount.toLong()
        val paise  = Math.round((amount - rupees.toDouble()) * 100).toInt()
        val rupeesWords = toIndianWords(rupees)
        return if (paise == 0) {
            "Rupees $rupeesWords Only"
        } else {
            val paiseWords = toIndianWords(paise.toLong())
            "Rupees $rupeesWords And $paiseWords Paise Only"
        }
    }

    private val ones = listOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen"
    )
    private val tens = listOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")

    private fun twoDigits(n: Long): String {
        return when {
            n == 0L  -> ""
            n < 20L  -> ones[n.toInt()]
            else     -> tens[(n / 10).toInt()] + if (n % 10 != 0L) " ${ones[(n % 10).toInt()]}" else ""
        }
    }

    private fun threeDigits(n: Long): String {
        val h = n / 100
        val rem = n % 100
        val hundredPart = if (h > 0) "${ones[h.toInt()]} Hundred" else ""
        val remPart = twoDigits(rem)
        return listOf(hundredPart, remPart).filter { it.isNotEmpty() }.joinToString(" ")
    }

    private fun toIndianWords(n: Long): String {
        if (n == 0L) return "Zero"
        var num = n
        val crore   = num / 10_000_000L; num %= 10_000_000L
        val lakh    = num / 100_000L;    num %= 100_000L
        val thousand= num / 1_000L;      num %= 1_000L
        val parts = mutableListOf<String>()
        if (crore    > 0) parts += "${twoDigits(crore)} Crore"
        if (lakh     > 0) parts += "${twoDigits(lakh)} Lakh"
        if (thousand > 0) parts += "${twoDigits(thousand)} Thousand"
        if (num      > 0) parts += threeDigits(num)
        return parts.joinToString(" ")
    }
}
