package com.pop.binderbus

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteCallbackList
import android.os.RemoteException
import android.util.Log
import java.util.concurrent.ConcurrentHashMap

class BinderBusService : Service() {

    companion object {
        private const val TAG = "BinderBusService"
        private const val ALL = "all"
    }

    private val mBinderPool = ConcurrentHashMap<String, IBinder>()

    private val deathRecipients = ConcurrentHashMap<String, IBinder.DeathRecipient>()

    private val mListeners = RemoteCallbackList<IBinderBusListener>()

    /**
     * BinderBus的Stub
     */
    private val mBusBinder = object : IBinderBus.Stub() {
        override fun registerBinder(key: String, binder: IBinder) {
            // 先清理旧的
            deathRecipients[key]?.let { oldRecipient ->
                mBinderPool[key]?.unlinkToDeath(oldRecipient, 0)
            }
            Log.w(TAG, "mBinderPool Size Before ${mBinderPool.size}")

            mBinderPool[key] = binder
            Log.w(TAG, "mBinderPool Size After ${mBinderPool.map { it.key to it.value }}")
            // 绑定死亡代理
            val recipient = IBinder.DeathRecipient {
                Log.w(TAG, "Binder for [$key] died, removing...")
                unregisterBinder(key)
            }
            try {
                binder.linkToDeath(recipient, 0)
                deathRecipients[key] = recipient
            } catch (e: RemoteException) {
                Log.e(TAG, "linkToDeath failed for $key", e)
                unregisterBinder(key)
            }

            notifyRegistered(key)
        }

        override fun queryBinder(key: String): IBinder? {
            return mBinderPool[key]?.apply {
                Log.w(TAG, "queryBinder $key $this")
            } ?: run {
                Log.w(TAG, "queryBinder $key null")
                null
            }
        }

        override fun unregisterBinder(key: String) {
            mBinderPool.remove(key)?.let { binder ->
                deathRecipients.remove(key)?.let { binder.unlinkToDeath(it, 0) }
            }
            notifyUnregistered(key)
        }

        override fun addListener(listener: IBinderBusListener) {
            mListeners.register(listener)
            mBinderPool.keys.toList().forEach { listener.onBinderRegistered(it) }
        }

        override fun removeListener(listener: IBinderBusListener) {
            mListeners.unregister(listener)
        }
    }

    private fun notifyRegistered(key: String) {
        with(mListeners) {
            val count = beginBroadcast()
            for (index in 0 until count) {
                getBroadcastItem(index).onBinderRegistered(key)
            }
            finishBroadcast()
        }
    }

    private fun notifyUnregistered(key: String) {
        with(mListeners) {
            val count = beginBroadcast()
            for (index in 0 until count) {
                getBroadcastItem(index).onBinderUnregistered(key)
            }
            finishBroadcast()
        }
    }


    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return START_STICKY
    }


    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind")
        return mBusBinder
    }


    override fun onDestroy() {
        super.onDestroy()
        with(mListeners) {
            val count = beginBroadcast()
            for (index in 0 until count) {
                getBroadcastItem(index).onBinderUnregistered(ALL)
            }
            finishBroadcast()
            kill()
        }
        deathRecipients.forEach { (k, r) -> mBinderPool[k]?.unlinkToDeath(r, 0) }
        deathRecipients.clear()
        mBinderPool.clear()
    }
}