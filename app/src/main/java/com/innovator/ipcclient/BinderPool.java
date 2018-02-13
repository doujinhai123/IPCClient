package com.innovator.ipcclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.concurrent.CountDownLatch;

/**
 * 封装好用来访问服务端的工具
 * Created by innovator on 2018/2/13.
 */

public class BinderPool {

    private static final String TAG = "BinderPool";

    private static final int BINDER_NONE = -1;

    public static final int BINDER_SECURITY_CENTER = 3;

    public static final int BINDER_COMPUTE = 4;

    private Context mContext;
    private IBinderPool mBinderPool;
    private static volatile BinderPool sInatance;
    private CountDownLatch mCountDownLatch;

    private BinderPool(Context context){
        this.mContext = context;
        connectBinderPoolService();
    }

    public static BinderPool getsInatance(Context context){
        if(sInatance == null){
            synchronized (BinderPool.class){
                if(sInatance == null){
                    sInatance = new BinderPool(context);
                }
            }
        }

        return sInatance;
    }

    private ServiceConnection mBinderPoolConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinderPool = IBinderPool.Stub.asInterface(service);
            try{
                mBinderPool.asBinder().linkToDeath(mDeathRecipient,0);
            }catch (RemoteException e){
                Log.i(TAG,"连接 BinderPool 出错："+e.getMessage());
            }

            Log.i(TAG,"mCountDownLatch.countDown: "+Thread.currentThread().getName());
            // Binder 回调在主线程
            mCountDownLatch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.i(TAG,"binder dead");
            mBinderPool.asBinder().unlinkToDeath(mDeathRecipient,0);
            mBinderPool = null;
            connectBinderPoolService();
        }
    };

    /**
     * 连接 BinderPool 服务端
     */
    private synchronized void connectBinderPoolService() {
        mCountDownLatch = new CountDownLatch(1);
        Intent i = new Intent();
        i.setAction("com.innovator.binderpool");
        i.setPackage("com.innovator.ipcserver");
        mContext.bindService(i,mBinderPoolConnection, Context.BIND_AUTO_CREATE);
        Log.i(TAG,"bindService");
        try {
            Log.i(TAG,"mCountDownLatch.await: "+Thread.currentThread().getName());
            mCountDownLatch.await();
        }catch (Exception e){
            Log.i(TAG,"mCountDownLatch.await 出错："+e.getMessage());
        }
    }


    /**
     * 客户端根据业务需求传递不同的 Code 获取对应的 Binder 对象
     * @param code
     * @return
     */
    public IBinder getServerBinderByCode(int code){
        IBinder binder = null;
        try {
            if(mBinderPool != null){
                binder = mBinderPool.queryBinder(code);
            }
        }catch (RemoteException e){
            Log.i(TAG,"mBinderPool.queryBinder 出错："+e.getMessage());
        }

        return binder;
    }
}
