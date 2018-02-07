package com.innovator.ipcclient;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

/**
 * 客户端的 ContentResolver
 */
public class ProviderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider);

        //这个 Uri 是由 ContentProvider 声明的 AUTHORITY 参数决定的，在末尾加上要访问的表名就可以了
        Uri uri = Uri.parse("content://com.innovator.ipcserver.provider/book");

        //插入 book 数据
        ContentValues values = new ContentValues();
        values.put("_id",6);
        values.put("name","程序艺术设计");
        getContentResolver().insert(uri,values);

        Cursor bookCursor = getContentResolver().query(uri,new String[]{"_id","name"},null,null,null);
        while (bookCursor != null && bookCursor.moveToNext()){
            Book book = new Book();
            book.setPrice(bookCursor.getInt(0));
            book.setName(bookCursor.getString(1));
            Log.i("TAG","获取到的 Book："+book.toString());
        }
        bookCursor.close();


        Uri userUri = Uri.parse("content://com.innovator.ipcserver.provider/user");

        Cursor userCursor = getContentResolver().query(userUri,new String[]{"_id","name","sex"},null,null,null);
        while (userCursor != null && userCursor.moveToNext()){
            User user = new User();
            user.setId(userCursor.getInt(0));
            user.setName(userCursor.getString(1));
            user.setSex(userCursor.getInt(2));
            Log.i("TAG","获取到的 User："+user.toString());
        }
        userCursor  .close();

    }
}
