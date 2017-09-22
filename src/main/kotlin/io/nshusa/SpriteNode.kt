package io.nshusa

class SpriteNode(val id: Int, val sprite: Sprite): Comparable<SpriteNode> {

    override fun compareTo(other:SpriteNode):Int {
            return if (id > other.id) 1 else -1
    }

    override fun toString() : String {
        return id.toString()
    }

}