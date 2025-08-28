// IBinderBusListener.aidl
package com.pop.binderbus;

oneway interface IBinderBusListener {
    void onBinderRegistered(String key);
    void onBinderUnregistered(String key);
}
