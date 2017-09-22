package io.nshusa.codec

import io.nshusa.SpriteNode
import javafx.collections.ObservableList
import javafx.scene.control.TreeItem

interface SpriteEncoder {

    fun encode(list: ObservableList<SpriteNode>) : Array<Byte>

}