package com.innovator.ipcclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import static com.innovator.ipcclient.MyConstants.MSG_REPLY_TO_CLIENT;

public class MessengerActivity extends AppCompatActivity {

    private static final String TAG = "MessengerActivity";

    //将从服务器获取到的 Binder 对象包装成自己的 Messenger 对象，从而给服务端发送消息
    private Messenger mMessenger = null;
    private static final int SAY_HELLO = 0;
    private boolean mBound = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("TAG","通过 Messenger 连接服务器成功");
            mBound = true;
            //根据服务器返回的 Binder 对象创建 Messenger 对象，并使用这个对象向服务器发送数据
            mMessenger = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("TAG","断开与服务器的连接");
            mBound = false;
        }
    };

    //构造用于服务器回复客户端的Handler
    private static class ReplyMessengerHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_REPLY_TO_CLIENT:
                    Log.i("TAG","客户端接收到了服务端通过 Messenger 发送过来的消息："+msg.getData().getString("msg"));
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    //构造用于服务器回复客户端的 Messeenger
    private Messenger mReplyMessenger = new Messenger(new ReplyMessengerHandler());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);
        attempToBind();

    }

    @Override
    protected void onStart() {
        super.onStart();
        attempToBind();
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }

    private void attempToBind(){
        if(!mBound){
            Intent i = new Intent();
            i.setAction("com.innovator.messenger");
            i.setPackage("com.innovator.ipcserver");
            bindService(i,connection, Context.BIND_AUTO_CREATE);
        }
    }

    public void sayHello(View v){
        if(mMessenger == null){
            attempToBind();
            return;
        }

        if(mMessenger != null){
            Message message = Message.obtain(null,SAY_HELLO);
            Bundle data = new Bundle();
            data.putString("msg","你好，这是在客户端发送给你的消息");
            message.setData(data);

            //将可以回复自身（客户端）的工具 mReplyMessenger 传给 服务端
            message.replyTo = mReplyMessenger;

            try {
                mMessenger.send(message);
            }catch (RemoteException e){
                Log.i("TAG","客户端发送信息异常："+e.getMessage());
            }
        }
    }
}
