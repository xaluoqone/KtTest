package com.xaluoqone.test.queue

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

class DeviceAddQueue {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val channel = Channel<String>(Channel.BUFFERED)
    var currentTask = ""
        private set

    fun dequeue(doTask: suspend (String) -> Unit) {
        scope.launch {
            while (true) {
                currentTask = channel.receive()
                doTask(currentTask)
            }
        }
    }

    fun enqueue(task: String) {
        scope.launch {
            channel.send(task)
        }
    }

    fun close() {
        channel.close()
    }
}