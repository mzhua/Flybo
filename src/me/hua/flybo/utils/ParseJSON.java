package me.hua.flybo.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ParseJSON {

	/**
	 * 测试函数
	 * 解析微博信息的json
	 * @param jsonStr 获取到的微博信息json
	 * @return	
	 */
	public static String parseStatuses(String jsonStr){
		final StringBuffer sb = new StringBuffer();
		JSONArray statusesArray = null;
		try {
			JSONObject statusesList = new JSONObject(jsonStr);
			statusesArray = statusesList.getJSONArray("statuses");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i = 0 ; i < statusesArray.length() ; i++){
			JSONObject status;
			try {
				status = statusesArray.getJSONObject(i);
				sb.append(getStatusUserInfo(status,"name"));
				sb.append(":");
				sb.append(status.getString("text")+"\n");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return sb.toString();
		
	}
	
	/**
	 * 获取user下的具体信息
	 * @param status 要获取的用户信息的那一条微博
	 * @param info	想要获取的用户信息(id,distr,name等等)
	 * @return
	 */
	public static String getStatusUserInfo(JSONObject status,String info){
		String userInfo = null;
		try {
			userInfo = status.getJSONObject("user").getString(info);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userInfo;
		
	}
}
