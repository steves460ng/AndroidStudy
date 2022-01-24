package com.example.webviewtest;

// SQLite import
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

// 디버그 로그뷰어 (하단 logcat에서 확인)
import android.util.Log;

/* ******************************************

    데이터베이스 처리 백엔드

******************************************* */

public class DBHelper extends SQLiteOpenHelper
{
    private SQLiteDatabase db;
    private static final int DB_VERSION = 1;
    private static final String TBL_NAME = "mytable";

    public DBHelper(Context context, String name)
    {
        super(context, name, null, DB_VERSION);

        // 내부적으로 db 생성
        db = getWritableDatabase();
    }

    // 데이터 베이스 테이블 초기화, 없으면 생성
    public void DBInit()
    {
        // 사용할 테이블이 없으면 생성 (최초 테이블 인스톨)
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TBL_NAME +"(ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT)");
    }

    @Override
    public void onCreate(SQLiteDatabase db) { }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

    //===========================================================================
    // Test select query
    public void select_db()//SQLiteDatabase db)
    {
        Cursor c = db.rawQuery("select * from mytable;", null);
        while(c.moveToNext())
        {
            Log.d("DB:", c.getString(c.getColumnIndex("NAME")));
        }
    }

    // Test data insert
    public void insert_data(String txt)//SQLiteDatabase db, String txt)
    {
        db.execSQL("insert into mytable values(null, '"+ txt + "')");
        select_db();
    }
}
