package com.innovator.ipcclient;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.List;

/**
 *
 * IPC 客户端的 MainActivity.java
 */
public class MainActivity extends Activity {

    //由AIDL文件生成的Java类
    private BookManager mBookManager;

    //标志当前与服务端连接状况的布尔值，false为未连接，true为连接中
    private boolean mBound;

    //包含Book对象的list
    private List<Book> mBooks;

    private Book book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("TAG","连接上了服务端");
            mBookManager = BookManager.Stub.asInterface(service);
            mBound = true;

            //获取代理对象，调用服务端的方法
            if(mBookManager != null){
                try {
                    mBooks = mBookManager.getBooks();
                    Log.i("TAG","获取到了服务端数据："+mBooks.toString());
                }catch (RemoteException e){
                    e.printStackTrace();
                    Log.i("TAG","调用服务端方法出现异常："+e.getMessage());
                }

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
            Log.i("TAG","断开服务端连接");
        }
    };

    /**
     * 按钮的点击事件，点击之后调用服务端的addBookIn方法
     *
     * @param v
     */
    public void addBook(View v){
        if(!mBound){
            attemptBindService();
            Toast.makeText(this, "当前与服务端处于未连接状态，正在尝试重连，请稍后再试", Toast.LENGTH_SHORT).show();
            return;
        }

        if(mBookManager == null){
            return;
        }

        book = new Book();
        book.setName("认知突围");
        book.setPrice(88);
        //调用服务器端的方法，传入一个Book对象
        try{
            mBookManager.addBook(book);
            Log.i("TAG","调用服务端方法传入一个Book");
        }catch (RemoteException e){
            e.printStackTrace();
            Log.i("TAG","调用服务端方法出现异常："+e.getMessage());
        }
    }

    /**
     * 点击了 addBook 之后看看客户端的数据有没有同步被改
     * 验证 inout tag 的功能
     *
     * 确实是同步更改了 inout 传入的那个对象的值
     * @param v
     */
    public void getBooks(View v) {

        Log.i("TAG","打印 addBooks 之后的数据 ："+book.toString());
    }

    public void goTo(View v) {

        Intent i = new Intent(this,MessengerActivity.class);
        startActivity(i);
    }

    public void goTo1(View v) {

        Intent i = new Intent(this,ProviderActivity.class);
        startActivity(i);
    }

    public void goTo2(View v) {

        Intent i = new Intent(this,SocketActivity.class);
        startActivity(i);
    }

    /**
     * 尝试连接上服务器
     */
    private void attemptBindService(){
        Intent i = new Intent();
        i.setAction("com.innovator.aidl");
        i.setPackage("com.innovator.ipcserver");
        bindService(i,mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!mBound){
            attemptBindService();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBound){
            unbindService(mServiceConnection);
            mBound = false;
        }
    }
}
