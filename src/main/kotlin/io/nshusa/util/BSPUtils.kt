package io.nshusa.util

import java.io.File
import java.util.*
import java.text.DecimalFormat



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

    fun readableFileSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "kB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

}