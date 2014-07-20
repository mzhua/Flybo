package me.hua.flybo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    public static  String DATABASE_NAME = "flybo.db";
    public static  String TABLE_NAME = "statusLists";
    public static  int DATABASE_VERSION = 1;

    public MySQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        //status_has_image为0表示没有图片，status_has_image为1表示有图片
        db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_NAME +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " user_token TEXT, " +   //用户标识
                " status_create_at TEXT, " +
                " status_id TEXT, " +
                " status_text TEXT," +
                " status_reposts_count INTEGER," +
                " status_comments_count INTEGER," +
                " status_source TEXT," +
                " status_thumbnail_pic TEXT," +
//	               " status_bmiddle_pic TEXT," + 
//	               " status_original_pic TEXT," + 
                " status_user_id TEXT," +
                " status_user_name TEXT," +
//	               " status_user_location TEXT," + 
//	               " status_user_description TEXT," + 
                " status_user_profile_image_url TEXT," +

                " status_retweeted_user_name TEXT," +
                " status_retweeted_status_text TEXT," +
                " status_retweeted_status_thumbnail_pic TEXT" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }

}
