// IBinderPool.aidl
package com.innovator.ipcclient;

// 对外提供给客户端，并且提供选择 Binder 的方法

interface IBinderPool {

    IBinder queryBinder(int binderCode);
}
