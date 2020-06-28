package com.amaze.smartnotif.utils

import kotlinx.coroutines.sync.Mutex

class RWMutex(){
    var rmutex=Mutex()
    var wmutex= Mutex()
    var readCount=0
    suspend fun readLock(){
        rmutex.lock()
        readCount++
        if (readCount==1)
            wmutex.lock()
        rmutex.unlock()
    }
    suspend fun readUnlock(){
        rmutex.lock()
        readCount--
        if (readCount==0)
            wmutex.unlock()
        rmutex.unlock()
    }
    suspend fun writeLock(){
        wmutex.lock()
    }
    suspend fun writeUnlock(){
        wmutex.unlock()
    }
}