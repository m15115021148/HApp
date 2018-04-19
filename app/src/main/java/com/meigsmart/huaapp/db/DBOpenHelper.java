package com.meigsmart.huaapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBOpenHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "MeigHuaApp.db"; //数据库名称
    private static final int DB_VERSION = 1;//数据库版本,大于0

    //蓝牙
    private static final String CREATE_BLUETOOTH = "create table "+BluetoothDao.TABLE+" ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "blueName TEXT, "
            + "password TEXT, "
            + "serialNum TEXT, "
            +"isFirstSetPsw TEXT)";//0 第一次  1 否

    public DBOpenHelper(Context context) {
        super(context, DB_NAME, null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BLUETOOTH);
        Log.e("result","---------create_table_sql-------------------");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
