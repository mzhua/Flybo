package me.hua.flybo.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.hua.flybo.model.Geo;
import me.hua.flybo.model.Status;
import me.hua.flybo.model.User;
import me.hua.flybo.ui.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class Tools {
	private static Tools instance = null;

	// 将构造器设为私有的，不允许从外界构造
	private Tools() {
	}

	// 外界通过这个public方法获取实例，用来调用下面的函数
	public static Tools getInstance() {
		if (instance == null) {
			instance = new Tools();
		}
		return instance;
	}

	/**
	 * 解析用戶信息json
	 * @param user_info_json 从新浪下载的用户信息json
	 * @return 解析好的Arraylist
	 */
	public User parseUser(String user_info_json){
		JSONObject user_info_obj = null;
		try {
			 user_info_obj = new JSONObject(user_info_json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		User user_info = new User();
		try {
			user_info.setName(user_info_obj.getString("name"));
			user_info.setProfileImageUrl(user_info_obj.getString("profile_image_url"));
			user_info.setLocation(user_info_obj.getString("location"));
			user_info.setFollowersCount(user_info_obj.getInt("followers_count"));
			user_info.setFriendsCount(user_info_obj.getInt("friends_count"));
			user_info.setStatusesCount(user_info_obj.getInt("statuses_count"));
			user_info.setDescription(user_info_obj.getString("description"));
			user_info.setUrl(user_info_obj.getString("url"));
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return user_info;
		
	}

	/**
	 * 解析微博信息的json
	 * 
	 * @param jsonStr
	 *            获取到的微博信息json
	 * @param refresh_type_0
	 *            触发刷新的方式，home或者底部“点击加载更多”
	 * @return
	 */
	public ArrayList<Status> parseStatuses(String jsonStr, int refresh_type_0) {

		JSONArray statusesArray = null;

		try {
			JSONObject statusesList = new JSONObject(jsonStr);
			statusesArray = statusesList.getJSONArray("statuses");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<Status> statuses_arraylist = new ArrayList<Status>();

		for (int i = 0; i < statusesArray.length(); i++) {
			JSONObject status_json_object;
			Status status = new Status();
			status.setUser(new User());
			status.setGeo(new Geo());
			try {
				// 对应一条微博
				status_json_object = statusesArray.getJSONObject(i);
				// user的json
				JSONObject user_json_object = status_json_object
						.getJSONObject("user");

				status.setId(status_json_object.getString("idstr")); // 微博id
				status.getUser().setId(user_json_object.getString("idstr")); // 用户Id
				status.getUser().setName(user_json_object.getString("name")); // 用户名
				status.getUser().setLocation(user_json_object.getString("location")); 
				status.getUser().setFollowersCount(user_json_object.getInt("followers_count")); 
				status.getUser().setFriendsCount(user_json_object.getInt("friends_count")); 
				status.getUser().setStatusesCount(user_json_object.getInt("statuses_count")); 
				status.getUser().setDescription(user_json_object.getString("description")); 
				status.getUser().setUrl(user_json_object.getString("url")); 
				status.setCreatedAt(status_json_object.getString("created_at")); // 微博创建时间
				status.setText(status_json_object.getString("text")); // 微博内容
				status.setRepostsCount(status_json_object
						.getInt("reposts_count")); // 转发数
				status.setCommentsCount(status_json_object
						.getInt("comments_count")); // 评论数

				// 位置
				System.out.println(">>>>>>>>>>>>>>>>>>>"
						+ status_json_object.get("geo"));
				if (status_json_object.has("geo")
						&& !(JSONObject.NULL).equals(status_json_object.get("geo"))  ) {
					JSONObject geo_json_object = status_json_object
							.getJSONObject("geo");
					if (geo_json_object.has("city")) {
						// 位置信息
						status.getGeo().setLongitude(
								geo_json_object.getString("longitude")); // 经度
						status.getGeo().setLatitude(
								geo_json_object.getString("latitude")); // 纬度
						status.getGeo().setCity(
								geo_json_object.getString("city")); // 城市代码
						status.getGeo().setProvince(
								geo_json_object.getString("province")); // 省份代码
						status.getGeo().setCity_name(
								geo_json_object.getString("city_name")); // 城市名称
						status.getGeo().setProvince_name(
								geo_json_object.getString("province_name")); // 省份名称
						status.getGeo().setAddress(
								geo_json_object.getString("address")); // 实际地址,可以为空

						System.out.println(">>>>>>>>>>>>>>>>>>>"
								+ geo_json_object.getString("address"));
					}

				}

				// 正则表达式，提取其中的source内容
				Pattern pattern = Pattern.compile("<.+?>", Pattern.DOTALL);
				Matcher matcher = pattern.matcher(status_json_object
						.getString("source"));
				String source = matcher.replaceAll("");
				status.setSource(source); // 发微博的来源

				status.getUser().setProfileImageUrl(
						user_json_object.getString("profile_image_url")); // 用户头像

				boolean hasImg = false;
				if (status_json_object.has("thumbnail_pic")) {
					status.setThumbnailPic(status_json_object
							.getString("thumbnail_pic")); // 微博正文缩略图
					hasImg = true;
				}
				status.setHasImage(hasImg);

				if (status_json_object.has("retweeted_status")) {// 转发的微博内容
					status.setRetweetedStatus(new Status());
					status.getRetweetedStatus().setUser(new User());
					status.setHasRetweetedStatus(true);// 设置，该条微博有转发
					JSONObject retweeted_status_json_object = status_json_object
							.getJSONObject("retweeted_status"); // 转发的微博内容
					JSONObject retweeted_user_json_object = retweeted_status_json_object
							.getJSONObject("user"); // 所转发微博的所属用户的名字
					status.getRetweetedStatus().setText(
							retweeted_status_json_object.getString("text"));
					status.getRetweetedStatus().setThumbnailPic(
							retweeted_status_json_object
									.getString("thumbnail_pic"));
					status.getRetweetedStatus()
							.getUser()
							.setName(
									retweeted_user_json_object
											.getString("name"));

					boolean hasImg_retweet = false;
					if (retweeted_status_json_object.has("thumbnail_pic")) {
						status.getRetweetedStatus().setThumbnailPic(
								retweeted_status_json_object
										.getString("thumbnail_pic")); // 转发微博正文缩略图
						hasImg_retweet = true;
					}
					status.getRetweetedStatus().setHasImage(hasImg_retweet);
				} else {
					status.setHasRetweetedStatus(false);// 设置，该条微博有转发
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			statuses_arraylist.add(status);

		}
		return statuses_arraylist;

	}

	/**
	 * 获取user下的具体信息
	 * 
	 * @param status
	 *            要获取的用户信息的那一条微博
	 * @param info
	 *            想要获取的用户信息(id,distr,name等等)
	 * @return
	 */
	public String getStatusUserInfo(JSONObject status, String info) {
		String userInfo = null;
		try {
			userInfo = status.getJSONObject("user").getString(info);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userInfo;

	}

	/**
	 * 获取since_id和max_id
	 * 
	 * @param status_list
	 *            解析好的微博列表信息
	 * @return 包含since_id,max_id的数组
	 */
	public long[] getWeiboListId(ArrayList<Status> status_list) {

		long[] weibo_id = new long[2];
		long since_id = 0; // 时间最晚的微博的id
		long max_id = Long.MAX_VALUE; // 最早
		for (int i = 0; i < status_list.size(); i++) {
			if (Long.parseLong(status_list.get(i).getId()) > since_id) {
				since_id = Long.parseLong(status_list.get(i).getId());
			}
			if (Long.parseLong(status_list.get(i).getId()) < max_id) {
				max_id = Long.parseLong(status_list.get(i).getId());
			}
		}
		weibo_id[0] = since_id;
		weibo_id[1] = max_id;
		return weibo_id;
	}

	/**
	 * 下载图片资源
	 * 
	 * @param url
	 *            //下载链接
	 * @return
	 */
	public static Drawable getDrawableFromUrl(String url) {
		try {
			URLConnection urls = new URL(url).openConnection();
			return Drawable.createFromStream(urls.getInputStream(), "image");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param path
	 *            文件夹路径
	 * @param fileList
	 *            注意的是并不是所有的文件夹都可以进行读取的，权限问题
	 */
	public static void getFileList(String path, HashMap<String, String> fileList) {

		// 返回文件夹中有的数据
		File dir = new File(path);
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (!files[i].isDirectory()) {
				fileList.put("pic_name", files[i].getName());
			}
		}

	}

	/**
	 * 保存图片到本地
	 * 
	 * @param drawable
	 *            drawable资源
	 * @param head_image_path
	 *            保存的文件夹路径
	 * @param file_name
	 *            文件名
	 * @param image_type
	 *            下载图片的类型(头像，内容图片等)
	 */
	public void savePicture(Drawable drawable, String head_image_path,
			String file_name, String image_type) {

		// 保存到本地,以user_id为文件名保存
		File head_image_dir = new File(head_image_path + "/" + image_type);
		if (!head_image_dir.exists()) {
			head_image_dir.mkdirs();
		}
		File head_image_file = new File(head_image_path + "/" + image_type
				+ "/" + file_name);
		// 图像不存在时才去创建
		if (!head_image_file.exists()) {
			BitmapDrawable drawable_bitmap = (BitmapDrawable) drawable;
			try {
				head_image_file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(head_image_file);
				drawable_bitmap.getBitmap().compress(CompressFormat.JPEG, 100,
						fos);
				fos.close();

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * 格式化时间
	 * 
	 * @param create_at
	 *            微博json中的create_at
	 * @return
	 */
	public String getFormatDate(String create_at) {
		String format_type = "yyyy-MM-dd HH:mm:ss";// 完整时间格式
		String format_type_time = "HH:mm";// 昨天的时间格式
		String format_type_day = "dd";// 天
		// String format_type_hour = "HH";//小时
		// String format_type_minute = "mm";//分钟
		// String format_type_second = "ss";//秒

		SimpleDateFormat formatter_type = new SimpleDateFormat(format_type);
		SimpleDateFormat formatter_type_time = new SimpleDateFormat(
				format_type_time);
		SimpleDateFormat formatter_type_day = new SimpleDateFormat(
				format_type_day);
		// SimpleDateFormat formatter_type_hour = new
		// SimpleDateFormat(format_type_hour);
		// SimpleDateFormat formatter_type_minute = new
		// SimpleDateFormat(format_type_minute);
		// SimpleDateFormat formatter_type_second = new
		// SimpleDateFormat(format_type_second);

		long create_date = Date.parse(create_at);// 微博创建时间
		long now_date = System.currentTimeMillis();// 当前刷新时间
		long interval_date = now_date - create_date;// 微博发布到现在的时间间隔

		long one_day = 1000 * 60 * 60 * 24;
		long days = (interval_date) / (one_day);
		long hours = (interval_date) / (1000 * 60 * 60);
		long minutes = (interval_date) / (1000 * 60);
		long seconds = (interval_date) / (1000);

		String time_str = "";
		if (days <= 1) {// 判断是否是今天（以0点为界，并不只是days在24个小时内）
			// 下面两个通过判断 （当前时间 - 时间间隔）得到的（天）和（当前时间）的到的（天）是否是同一天
			String create_date_str = formatter_type_day.format(create_date);
			String now_day_str = formatter_type_day.format(now_date);

			if (now_day_str.equals(create_date_str)) {// 是今天发布的（即今天0点以后）
				if (hours < 1) {// 一小时内发的，显示时间就精确到分钟
					String hour_interval = formatter_type_day.format(now_date
							- minutes);
					if (minutes < 1) {
						time_str = seconds + "秒前";
					} else if (minutes < 60) {
						time_str = minutes + "分钟前";
					}
				} else {
					time_str = formatter_type_time.format(create_date);
				}
			} else {// 虽然发布时间和当前时间的时间间隔在24小时内，但是昨天发布的
				time_str = "昨天 " + formatter_type_time.format(create_date);
			}

		} else if (days <= 2) {
			// long yes_date = interval_date - one_day;
			time_str = "昨天 " + formatter_type_time.format(create_date);
		} else if (days <= 3) {
			// long yes_date = interval_date - one_day*2;
			time_str = "昨天 " + formatter_type_time.format(create_date);
		} else {
			time_str = formatter_type.format(create_date);
		}
		return time_str;
	}

	public boolean isTodayHour() {
		return true;
	}

	public boolean isTodayMinute() {
		return true;
	}

	public boolean isTodaySecond() {
		return true;
	}
}
