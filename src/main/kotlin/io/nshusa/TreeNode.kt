package io.nshusa

class TreeNode(private val name: String) {

   override fun toString() : String {
        return name
    }

    fun clone() : TreeNode {
        return TreeNode(name)
    }

}