package io.nshusa

class Sprite(val id: Int, var data: ByteArray?): Comparable<Sprite> {

    override fun compareTo(other: Sprite):Int {
            return if (id > other.id) 1 else -1
    }

    override fun toString() : String {
        return id.toString()
    }

}