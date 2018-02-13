package com.innovator.ipcclient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Socket 客户端
 */
public class SocketActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int MESSAGE_RECEIVE_NEW_MSG = 1;
    private static final int MESSAGE_SOCKET_CONNECTED = 2;

    private Button mSendButton;
    private TextView mMessagetextView;
    private TextView mMessagetEditText;

    private PrintWriter mPrintWriter;
    private Socket mClientSocket;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_RECEIVE_NEW_MSG:
                    //显示服务器发送的消息
                    mMessagetextView.setText(mMessagetextView.getText()+(String)msg.obj);
                    break;
                case MESSAGE_SOCKET_CONNECTED:
                    mSendButton.setEnabled(true);
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);
        mMessagetextView = findViewById(R.id.msg_container);
        mSendButton = findViewById(R.id.send);
        mSendButton.setOnClickListener(this);
        mMessagetEditText = findViewById(R.id.msg);
        Intent i  = new Intent();
        i.setAction("com.innovator.socket");
        i.setPackage("com.innovator.ipcserver");
        startService(i);
        new Thread(){
            @Override
            public void run() {
                //连接服务器
                connectTCPServer();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        if(mClientSocket != null){
            //关闭 Socket
            try {
              mClientSocket.shutdownInput();
              mClientSocket.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if(v == mSendButton){
            String msg = mMessagetEditText.getText().toString();
            if(!TextUtils.isEmpty(msg) && mPrintWriter != null){
                Log.i("TCP","sending message to the server");
                mPrintWriter.println(msg);
                mMessagetEditText.setText("");
                String time = formateDateTime(System.currentTimeMillis());
                String showMsg = "self"+time+":"+msg+"\n";
                //显示发送的消息
                mMessagetextView.setText(mMessagetextView.getText() + showMsg);
            }
        }
    }

    private String formateDateTime(long time){
        return new SimpleDateFormat("(HH:mm:ss)").format(new Date(time));
    }

    /**
     * 连接服务端的 Socket
     */
    private void connectTCPServer(){
        Socket socket = null;
        while (null == socket){
            try{
                socket = new Socket("localhost",8688);
                mClientSocket = socket;
                mPrintWriter = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())),true);
                mHandler.sendEmptyMessage(MESSAGE_SOCKET_CONNECTED);
                mPrintWriter.println("我是客户端");
                Log.i("TCP","连接到了服务端的Socket");
            }catch (IOException i){
                SystemClock.sleep(1000);
                i.printStackTrace();
                Log.i("TCP","连接服务端的Socket失败，正在重连..."+i.getMessage());
            }
        }

            try{
                //接收服务端发送过来的信息
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                while (!SocketActivity.this.isFinishing()){
                    String msg = br.readLine();
                    if(msg != null){
                        Log.i("TCP","服务端发送的消息："+msg);
                        String time = formateDateTime(System.currentTimeMillis());
                        String showMsg = "server"+time+":"+msg+"\n";
                        //显示接收的消息
                        mHandler.obtainMessage(MESSAGE_RECEIVE_NEW_MSG,showMsg).sendToTarget();
                    }
                }

                Log.i("TCP","quit...");

                try {
                    if (null != mPrintWriter) {
                        mPrintWriter.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if (null != br) {
                        br.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                socket.close();
            }catch (Exception e){
                e.printStackTrace();
            }

    }
}
