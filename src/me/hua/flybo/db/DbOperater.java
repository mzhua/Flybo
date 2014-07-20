package me.hua.flybo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public class DbOperater {

	private static DbOperater instance = null;
	
	private DbOperater(){
		
	}
	
	public static DbOperater getInstance(){
		if(instance == null)
			instance = new DbOperater();
		return instance;
		
	}
	/**
	 * 清空表数据
	 */
	public  void truncTable(Context context) {
		SQLiteDatabase db = new MySQLiteOpenHelper(context)
				.getWritableDatabase();

		String table_name = MySQLiteOpenHelper.TABLE_NAME;

		db.delete(table_name, "user_token = ?", new String[]{AccessTokenKeeper.readAccessToken(context).toString()});
		
		db.close();
		Toast.makeText(context, "数据缓存已清除", Toast.LENGTH_SHORT).show();

	}
}
