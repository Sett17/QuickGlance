package dev.dikka.quickglance

interface Destination {
    val route: String
}

object Input : Destination {
    override val route: String = "input"
}

object Read : Destination {
    override val route: String = "read"
}

var screens = listOf(Input, Read)