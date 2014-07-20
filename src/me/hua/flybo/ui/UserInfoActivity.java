package me.hua.flybo.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import me.hua.flybo.R;
import me.hua.flybo.baidumap.GeoCoderActivity;
import me.hua.flybo.db.AccessTokenKeeper;
import me.hua.flybo.model.User;
import me.hua.flybo.utils.AsyncImageLoader;
import me.hua.flybo.utils.CheckNetwork;
import me.hua.flybo.utils.AsyncImageLoader.ImageCallback;
import me.hua.flybo.utils.Tools;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.legacy.UsersAPI;

/**
 * Created by Hua on 14-1-16.
 */
public class UserInfoActivity extends Activity {
	private Context context;

	private String user_name; //传递参数用户名

	private ImageView user_head;
	private TextView text_user_name;
	private TextView text_user_location;
	private TextView text_user_followers_count;
	private TextView text_user_friends_count;
	private TextView text_user_status_count;
	private TextView text_user_descriptione;
	private TextView text_user_url;

	private Handler handler;

	// 新浪API
	private UsersAPI usersAPI;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_userinfo);
		context = UserInfoActivity.this;
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				setUserInfoView((String)msg.obj);
			}

		};
		initView();

	}

	/**
	 * 初始化组件，获取用户信息，绑定监听器
	 */
	private void initView() {

		user_head = (ImageView) findViewById(R.id.user_info_head_image);
		text_user_name = (TextView) findViewById(R.id.user_info_name);
		text_user_location = (TextView) findViewById(R.id.user_info_location);
		text_user_followers_count = (TextView) findViewById(R.id.user_info_followers_count);
		text_user_friends_count = (TextView) findViewById(R.id.user_info_friends_count);
		text_user_status_count = (TextView) findViewById(R.id.user_info_status_count);
		text_user_descriptione = (TextView) findViewById(R.id.user_info_description);
		text_user_url = (TextView) findViewById(R.id.user_info_url);

		Intent intent = this.getIntent();
		Bundle user_info_bundle = intent.getExtras();
		user_name = user_info_bundle.getString("user_name");

		getUserInfo(user_name); //从新浪下载数据

	}

	/**
	 * 根据传递的用户id参数，获取其余的用户信息
	 * 
	 * @param user_id_str
	 *            用户id
	 */
	private void getUserInfo(String user_name_str) {

		usersAPI = new UsersAPI(AccessTokenKeeper.readAccessToken(context));
		usersAPI.show(user_name_str, new RequestListener() {

			@Override
			public void onComplete(String response) {
				// TODO Auto-generated method stub

				Message msg = new Message();
				msg.obj = response;
				handler.sendMessage(msg);
			}

			@Override
			public void onComplete4binary(ByteArrayOutputStream responseOS) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onIOException(IOException e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onError(WeiboException e) {
				// TODO Auto-generated method stub

			}

		});
	}

	/**
	 * 设置用户界面上的信息
	 * 
	 * @param user_info_json
	 *            从新浪下载的用户信息json
	 */
	private void setUserInfoView(String user_info_json) {
		//解析用戶信息json
		User user_info = Tools.getInstance().parseUser(user_info_json);
		

		this.setTitle(user_info.getName());

		//头像
		Drawable user_head_image = AsyncImageLoader.loadDrawable(user_info.getProfileImageUrl(),user_head , "image_head", new ImageCallback(){

			@Override
			public void imageSet(Drawable drawable, ImageView iv) {
				// TODO Auto-generated method stub
				iv.setImageDrawable(drawable);
			}
			
		});
		if(user_head_image != null){
			user_head.setImageDrawable(user_head_image);
		}
		text_user_name.setText(user_info.getName());
		text_user_location.setText(user_info.getLocation());
		text_user_followers_count.setText(user_info.getFollowersCount() + "");
		text_user_friends_count.setText(user_info.getFriendsCount() + "");
		text_user_status_count.setText(user_info.getStatusesCount() + "");
		if(!"".equals(user_info.getDescription())){
			text_user_descriptione.setVisibility(View.VISIBLE);
			text_user_descriptione.setText("简介：" + user_info.getDescription());
		}

		if(!"".equals(user_info.getUrl())){
			text_user_url.setVisibility(View.VISIBLE);
			text_user_url.setText("博客：" + user_info.getUrl());
		}

		

		text_user_location.setOnClickListener(new myOnClickListener()); // 位置点击监听
	}

	public class myOnClickListener implements OnClickListener {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub

			switch(view.getId()){
			case R.id.user_info_location:
				if(CheckNetwork.checkNet(context)){
					TextView view_user_head = (TextView)view;
					Intent intent = new Intent(UserInfoActivity.this,GeoCoderActivity.class);
					Bundle location_data = new Bundle();
					location_data.putString("location", view_user_head.getText().toString());
					location_data.putString("user_name", text_user_name.getText().toString());
					intent.putExtras(location_data);
					context.startActivity(intent);
				}
				else{
            		CheckNetwork.AlertNetError(context);
            	}
				break;
			}
		}

	}
	
	@Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                this.finish();
                break;
        }
        return super.onKeyUp(keyCode, event);
    }
}