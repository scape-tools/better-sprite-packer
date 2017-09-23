package io.nshusa

object Test {

    @JvmStatic
    fun main(args: Array<String>) {

        val a = 50
        val r = 100
        val g = 200
        val b = 255

        val result = (a shl 24) + (r shl 16) + (g shl 8) + b

        println(result)

        println((result shr 24) and 0xFF)
        println((result shr 16) and 0xFF)
        println((result shr 8) and 0xFF)
        println((result) and 0xFF)

    }

}