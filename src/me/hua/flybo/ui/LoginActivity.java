package me.hua.flybo.ui;

import me.hua.flybo.R;
import me.hua.flybo.constants.Weibo;
import me.hua.flybo.db.AccessTokenKeeper;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;

public class LoginActivity extends Activity {

	private ImageView image_login;

	//授权
	private WeiboAuth mWeiboAuth;
	private Oauth2AccessToken mAccessToken;
	
	/** 注意：SsoHandler 仅当 SDK 支持 SSO 时有效 */
    private SsoHandler mSsoHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logo);
		image_login = (ImageView)findViewById(R.id.image_login);
		image_login.setOnClickListener(new myOnClickListener());
		// 创建微博授权类对象
		mWeiboAuth = new WeiboAuth(this, Weibo.APP_KEY,
				Weibo.REDIRECT_URL, Weibo.SCOPE);

		//判断是否已经授权
		mAccessToken = AccessTokenKeeper.readAccessToken(this);
		if (mAccessToken.isSessionValid()) {
			//授权成功进入主页
			goToMainActivity();
		}
		
	}

	public class myOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.image_login:
				// 调用微博授权接口
//				mSsoHandler = new SsoHandler(LoginActivity.this, mWeiboAuth);
//				mSsoHandler.authorize(new myAuthListener());
				//上面是sso授权登陆
				mWeiboAuth.anthorize(new myAuthListener());
				break;

			default:
				break;
			}
		}

	}
	//授权监听
	class myAuthListener implements WeiboAuthListener {

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onComplete(Bundle values) {
			// TODO Auto-generated method stub
			mAccessToken = Oauth2AccessToken.parseAccessToken(values);
			if (mAccessToken.isSessionValid()) {
				AccessTokenKeeper.writeAccessToken(LoginActivity.this, mAccessToken);
				goToMainActivity();
			}
		}

		@Override
		public void onWeiboException(WeiboException arg0) {
			// TODO Auto-generated method stub

		}

	}

	/**
	 * 当 SSO 授权 Activity 退出时，该函数被调用。
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		// SSO 授权回调
        // 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResult
		if(mSsoHandler != null){
			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
			goToMainActivity();
		}
	}

	//授权成功进入微博信息列表主页
	private void goToMainActivity(){
		//授权成功进入主页
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
		LoginActivity.this.startActivity(intent);

		this.finish();
	}


}
