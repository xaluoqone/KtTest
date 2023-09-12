package com.xaluoqone.test

import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import okio.source
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

fun File.unzip() {
    try {
        FileInputStream(this).use { fis ->
            ZipInputStream(fis).use { zis ->
                var zipEntry: ZipEntry?
                while (zis.nextEntry.also { zipEntry = it } != null) {
                    val filePath = "${path.toPath().parent?.toFile()?.path}/${zipEntry?.name}"
                    if (filePath.contains("MACOSX")||filePath.contains("DS_Store")) {
                        continue
                    }
                    val file = filePath.toPath().toFile()
                    if (!file.exists()) {
                        if (zipEntry?.isDirectory == true) {
                            file.mkdirs()
                            continue
                        }
                        file.createNewFile()
                    }
                    println("解压的文件路径：$filePath")
                    FileSystem.SYSTEM.write(filePath.toPath()) {
                        write(zis.source().buffer().readByteArray())
                    }
                    zis.closeEntry()
                }
                zis.close()
            }
        }
    } catch (e: Exception) {
        println(e.toString())
    }
}