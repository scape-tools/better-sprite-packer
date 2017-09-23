package io.nshusa.util

import java.io.File
import java.util.*

object BSPUtils {

    fun getFilePrefix(file: File) : String {
        val name = file.name
        return if (name.lastIndexOf(".") != -1) name.substring(0, name.lastIndexOf(".")) else name
    }

    fun sortFiles(files: Array<File>) {
        Arrays.sort(files, { first, second->
            val fid = Integer.parseInt(first.name.substring(0, first.name.lastIndexOf(".")))
            val sid = Integer.parseInt(second.name.substring(0, second.name.lastIndexOf(".")))
            Integer.compare(fid, sid)
        })
    }

}