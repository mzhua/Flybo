package me.hua.flybo.utils;

import me.hua.flybo.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class CheckNetwork {

	/**
	 * 检查网络连接状况
	 * @param context 
	 * @return 连接正常返回true
	 */
	public static boolean checkNet(Context context){
		ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivity != null){
			NetworkInfo info = connectivity.getActiveNetworkInfo();
			if(info != null && info.isConnected()){
				if(info.getState() == NetworkInfo.State.CONNECTED){
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * 登陆时网络错误是弹窗
	 * @param con
	 */
	public static void AlertNetError(final Context con)
	{
		Toast.makeText(con, "网络连接出错，请先检查网络", Toast.LENGTH_SHORT).show();
//		AlertDialog.Builder ab=new AlertDialog.Builder(con);
//		ab.setTitle(R.string.NoRouteToHostException);
//		ab.setMessage(R.string.NoSignalException);
//		ab.setNegativeButton(R.string.apn_is_wrong1_exit,
//		 new OnClickListener()
//		{
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				// TODO Auto-generated method stub
//				dialog.cancel();
//				
//
//			}
//			
//		});
//		ab.setPositiveButton(R.string.apn_is_wrong1_setnet, 
//				 new OnClickListener()
//		{
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				// TODO Auto-generated method stub
//                dialog.dismiss();
//				con.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));				
//			}
//		});
//		ab.create().show();
	}
}
