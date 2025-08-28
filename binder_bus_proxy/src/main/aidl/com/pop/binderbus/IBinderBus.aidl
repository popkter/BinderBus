// IBinderBus.aidl
package com.pop.binderbus;
import android.os.IBinder;
import com.pop.binderbus.IBinderBusListener;

interface IBinderBus {
    void registerBinder(String key, IBinder binder);
    IBinder queryBinder(String key);
    void unregisterBinder(String key);

    void addListener(IBinderBusListener listener);
    void removeListener(IBinderBusListener listener);
}