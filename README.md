> BinderBus是一个基于Binder的进程通讯服务，统一并简化了服务链接流程，通过单一服务提高了进程通讯效率。
## Design
<img width="1404" height="654" alt="image" src="https://github.com/user-attachments/assets/d0b0c980-d1eb-4d10-a243-7ab1b4ab7793" />

----

## Instruction
### 运行BinderBus
此应用无界面，常规设备需要在应用设置中设置 允许自启动，且不限制电量消耗，保证后台服务活跃。
### 调用方配置
`Android 11 +`的平台，由于安全设置，需要调用BinderBus的在AndroidManifest.xml设置包可见性:
```xml
    <queries>
        <package android:name="com.pop.binderbus" />
        <intent>
            <action android:name="com.pop.binderbus.BIND" />
        </intent>
    </queries>
```
#### 注册Binder
调用方需要初始化BinderBus，然后传入对应AIDL文件的实现（也即是Binder对象），如**应用A**定义了一个aidl文件：
```aidl
    package com.qianli.binderbus;
    
    interface IFunctionCall {
        int add(int a, int b);
        int min(int a, int b);
    }
```
然后在Activity中，初始化BinderBus并传入Binder对象：
```kotlin
    class MainActivityA : AppCompatActivity() {
    
        ...
    
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            ...
            BinderBus.init(this)
    
            val functionCall = object : IFunctionCall.Stub() {
                override fun add(a: Int, b: Int): Int {
                    return a + b
                }
    
                override fun min(a: Int, b: Int): Int {
                    return a - b
                }
            }
    
            BinderBus.registerBinder("FunctionCall",functionCall)
            ...
        }
    
        ...
    }
```
至此即完成Binder的注册，通过`BinderBus.unregisterBinder(key: String)`完成注销。
#### 获取Binder
调用方想要使用指定的Binder，需要传入注册时对应的Key。但调用方getBinder时，目标Binder可能并没有完成注册，因此需要注册监听，当目标Binder注册后获取。
如**应用B**想要使用应用A的注册Binder，**将AIDL文件拷贝到项目内的目录下**，进行如下编码：
```kotlin
    class MainActivityB : ComponentActivity() {
    
        //期待的Binder对象
        private var functionCall: IFunctionCall? = null
    
        //注册Binder状态监听
        private val onModelLoaded = object : IBinderBusListener.Stub() {
            //首次注册后，会受到全部的已经注册的Binder的Key
            override fun onBinderRegistered(p0: String?) {
                if (p0 == "FunctionCall") {
                    
                    //监听到Binder已经注册，则调用Get方法
                    functionCall = BinderBus.get("FunctionCall", IFunctionCall.Stub::asInterface)
                }
            }
    
            override fun onBinderUnregistered(p0: String?) {
                if (p0 == "FunctionCall") {
                    functionCall = null
                }
            }
    
        }
    
    
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
                ...
            BinderBus.init(this)
            BinderBus.registerListener(onModelLoaded)
                ...
        }
        
        ...
    
    }
```
