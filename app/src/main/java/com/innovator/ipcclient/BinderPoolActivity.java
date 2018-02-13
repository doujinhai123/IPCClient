package com.innovator.ipcclient;

import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import static com.innovator.ipcclient.BinderPool.BINDER_COMPUTE;
import static com.innovator.ipcclient.BinderPool.BINDER_SECURITY_CENTER;

public class BinderPoolActivity extends AppCompatActivity {

    private static final String TAG = "BinderPool";

    private ISecurityCenter securityCenter;
    private ICompute compute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binder_pool);
    }


    public void doMyWork(View view){

        //因为 CountDownLatch 是阻塞的，而且连接 Binder 是耗时的，所以放在子线程去做
        new Thread(new Runnable() {

            @Override
            public void run() {
                doWork();
            }
        }).start();
    }

    public void doWork(){
        BinderPool binderPool = BinderPool.getsInatance(BinderPoolActivity.this);
        IBinder securityBinder = binderPool.getServerBinderByCode(BINDER_SECURITY_CENTER);
        securityCenter = ISecurityCenter.Stub.asInterface(securityBinder);
        Log.i(TAG,"visit ISecurityCenter："+Thread.currentThread().getName());

        String msg = "大爱安卓";
        try{
            String password = securityCenter.encrypt(msg);
            Log.i(TAG,"encrypt: "+ password);
            password = securityCenter.decript(password);
            Log.i(TAG,"decript: "+ password);
        }catch (Exception e){
            Log.i(TAG,"msecurityCenter 加解密出错："+e.getMessage());
        }

        IBinder computeBinder = binderPool.getServerBinderByCode(BINDER_COMPUTE);
        compute = ICompute.Stub.asInterface(computeBinder);
        try{
            Log.i(TAG,"compute 计算 4+6: "+ compute.add(4,6));
        }catch (Exception e){
            Log.i(TAG,"compute 计算出错："+e.getMessage());
        }
    }
}
