package com.pop.binder_bus_proxy

import android.content.ComponentName
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.IInterface
import android.util.Log
import com.pop.binderbus.IBinderBus
import com.pop.binderbus.IBinderBusListener
import kotlin.apply
import kotlin.collections.forEach
import kotlin.let

object BinderBus {

    private const val TAG = "BinderBusService"

    private var busService: IBinderBus? = null
    private val pendingTasks = mutableListOf<(IBinderBus) -> Unit>()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            busService = IBinderBus.Stub.asInterface(service)
            pendingTasks.forEach { it(busService!!) }
            pendingTasks.clear()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            busService = null
        }
    }

    /**
     * 初始化BinderBus
     * @param context Context
     */
    fun init(context: Context) {

        val intent = Intent().apply {
            component = ComponentName(
                "com.pop.binderbus",
                "com.pop.binderbus.BinderBusService"
            )
        }

        context.bindService(intent,connection,BIND_AUTO_CREATE)
    }

    /**
     * 注册Binder对象
     * @param key String
     * @param binder IBinder
     */
    fun registerBinder(key: String, binder: IBinder) {
        runWhenConnected { it.registerBinder(key, binder) }
    }

    /**
     * 注销Binder对象
     * @param key String
     */
    fun unregisterBinder(key: String) {
        runWhenConnected { it.unregisterBinder(key) }
    }

    /**
     * 监听Binder的注册、注销
     * @param listener IBinderBusListener
     */
    fun registerListener(listener: IBinderBusListener) {
        runWhenConnected { it.addListener(listener) }
    }

    /**
     * 停止监听Binder注册
     * @param listener IBinderBusListener
     */
    fun unregisterListener(listener: IBinderBusListener) {
        runWhenConnected { it.removeListener(listener) }
    }

    /**
     * 获取Binder对象，需要在IBinderBusListener监听到对应的Binder注册后才能获取到
     * @param key String
     * @param asInterface (IBinder) -> T Binder对应的asInterface犯法
     */
    fun <T: IInterface> get(key: String, asInterface: (IBinder) -> T): T? {
        Log.e(TAG, "get: $key")
        return busService?.queryBinder(key)?.let { asInterface(it) }
    }

    /**
     * 等待BinderBusService连接成功后执行任务
     */
    private fun runWhenConnected(task: (IBinderBus) -> Unit) {
        if (busService != null) {
            task(busService!!)
        } else {
            pendingTasks.add(task)
        }
    }
}