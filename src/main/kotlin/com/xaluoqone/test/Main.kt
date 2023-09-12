package com.xaluoqone.test

import com.xaluoqone.test.queue.DeviceAddQueue
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.appendingSink
import okio.buffer

fun main() = runBlocking {
    // ebf4d85b00a4887d87apnn
    // val res = tuyaCloudAPI.getStatisticType("ebf4d85b00a4887d87apnn")
    // println(res)
    // println(180.processOf(0, 359))
    // println(.5f.valueOf(0, 359))
    // val a = async { test(this) }
    // val b = async { test1() }

    // a.await()
    // b.await()
    // readFile()
    // writeFile()
    // unZipFile()
    // readFile2()
    // queue()
    // flowAsync()
    println("main finish!")
}

suspend fun test(scope: CoroutineScope) {
    val t = System.currentTimeMillis()

    listOf(1500L, 2000L, 1000L, 3000L, 2500L)
        .map { scope.async { delay(it) } }
        .forEach { it.await() }

    println("test:${System.currentTimeMillis() - t}")
}

suspend fun test1() = withContext(Dispatchers.IO) {
    val t = System.currentTimeMillis()
    val t1 = async { delay(1000) }
    val t2 = async { testChild() }

    t1.await()
    t2.await()
    println("test1:${System.currentTimeMillis() - t}")
}

suspend fun testChild() = withContext(Dispatchers.IO) {
    val t = System.currentTimeMillis()

    val a = async { delay(1000) }
    val a2 = async { delay(1500) }

    a.await()
    a2.await()

    println("testChild:${System.currentTimeMillis() - t}")
}

suspend fun readFile() = withContext(Dispatchers.IO) {
    val fileContent = FileSystem.SYSTEM.read("./src/main/resources/config_modules.tuya".toPath()) {
        readByteArray()
    }
    println(fileContent.contentToString())
}

suspend fun readFile2() = withContext(Dispatchers.IO) {
    val contentList = FileSystem.SYSTEM.read("./src/main/resources/test.txt".toPath()) {
        val contentList = mutableListOf<String>()
        var content = readLine()
        while (content.isNotEmpty()) {
            contentList.add(content)
            content = readLine()
        }
        contentList
    }
    println(contentList)
}

suspend fun writeFile() = withContext(Dispatchers.IO) {
    val file = "./src/main/resources/test.txt".toPath().toFile()
    file.appendingSink().buffer().use {
        it.writeUtf8("hahahahha\n")
    }
}

suspend fun unZipFile() = withContext(Dispatchers.IO) {
    "./src/main/resources/rn-cell-fan-lamp.zip".toPath().toFile().unzip()
}

suspend fun queue() = withContext(Dispatchers.IO) {
    val deviceAddQueue = DeviceAddQueue()
    launch {
        deviceAddQueue.dequeue {
            println("开始处理任务 ==== $it")
            delay(1000)
            println("结束处理任务 ==== $it")
        }
    }

    launch {
        repeat(10) {
            deviceAddQueue.enqueue("task --- $it")
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun flowAsync() = withContext(Dispatchers.IO) {
    val listFlow = listOf(1000 to "1", 2000 to "2", 1500 to "3")
        .asFlow()
        .onEach {
            delay(it.first.toLong())
        }
    val listFlow2 = listOf(1000 to "4", 2000 to "5", 1500 to "6")
        .asFlow()
        .flatMapMerge { value ->
            flow {
                delay(value.first.toLong())
                emit(value)
            }
        }

    launch {
        val data = flowOf(listFlow, listFlow2)
            .flattenMerge()
            .map { it.second }
            .toList()
        println("All items: $data")
    }

    /*launch {
        listFlow.collect {
            println("flow 1 ===> ${System.currentTimeMillis()} --> ${it.second}")
        }
    }

    launch {
        listFlow2.collect {
            println("flow 2 ===> ${System.currentTimeMillis()} --> ${it.second}")
        }
    }*/
}