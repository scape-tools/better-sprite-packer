package io.nshusa.codec

import io.nshusa.Sprite
import javafx.collections.ObservableList

interface SpriteEncoder {

    fun encode(list: ObservableList<Sprite>) : Array<Byte>

}