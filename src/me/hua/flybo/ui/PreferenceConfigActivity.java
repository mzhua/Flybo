package me.hua.flybo.ui;

import me.hua.flybo.R;
import me.hua.flybo.db.AccessTokenKeeper;
import me.hua.flybo.db.DbOperater;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.support.v4.app.NavUtils;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

public class PreferenceConfigActivity extends PreferenceActivity {

	/**
	 * 如果要使用actionbar左上角的返回，则必须有下面几个设定的地方 1、onCreate時
	 * getActionBar().setDisplayHomeAsUpEnabled(true);
	 * getActionBar().setDisplayShowHomeEnabled(true);
	 * getActionBar().setHomeButtonEnabled(true);
	 * 
	 * 2、清单文件中设定本activity的ParentActivity： <activity
	 * android:name="me.hua.flybo.ui.PreferenceConfigActivity"
	 * android:label="@string/title_activity_preference_config"
	 * android:parentActivityName="me.hua.flybo.ui.MainActivity" > <meta-data
	 * android:name="android.support.PARENT_ACTIVITY"
	 * android:value="me.hua.flybo.ui.MainActivity" /> </activity>
	 * 
	 * 3、onOptionsItemSelected方法中： 
	 * switch (item.getItemId()) { // Respond to the
	 * action bar's Up/Home button case android.R.id.home:
	 * NavUtils.navigateUpFromSameTask(this); return true; }
	 */
	Context context;
	private EditTextPreference pre_key_save_counts;
	private Preference delete_cache;
	private Preference logout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference_config);
		context = PreferenceConfigActivity.this;
		initView();

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
	}

	private void initView(){
		pre_key_save_counts = (EditTextPreference)findPreference("pre_key_save_counts");
		delete_cache = (Preference)findPreference("delete_cache");
		logout = (Preference)findPreference("logout");
		
		//设定EditText的显示方式和默认输入
		pre_key_save_counts.getEditText().setGravity(Gravity.CENTER);
		pre_key_save_counts.getEditText().setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
		pre_key_save_counts.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		pre_key_save_counts.getEditText().setHint("最大可输入三位的数字");
		
		delete_cache.setOnPreferenceClickListener(new myOnPreferenceClickListener());
		logout.setOnPreferenceClickListener(new myOnPreferenceClickListener());
	}
	
	private class myOnPreferenceClickListener implements OnPreferenceClickListener{

		@Override
		public boolean onPreferenceClick(Preference pref) {
			// TODO Auto-generated method stub
			if("delete_cache".equals(pref.getKey())){//清除账号token
				DbOperater.getInstance().truncTable(context);
			}
			else if("logout".equals(pref.getKey())){
				AccessTokenKeeper.clear(context);
				Intent intent = new Intent(context,LoginActivity.class);
				startActivity(intent);
//				MainActivity.class.finish();
				PreferenceConfigActivity.this.finish();
				
			}
			
			return false;
		}
		
	}
	
	@Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
            	Intent intent = new Intent(PreferenceConfigActivity.this,MainActivity.class);
            	context.startActivity(intent);
                this.finish();
                break;
        }
        return super.onKeyUp(keyCode, event);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
//			NavUtils.navigateUpFromSameTask(this);
			Intent intent = new Intent(PreferenceConfigActivity.this,MainActivity.class);
        	context.startActivity(intent);
			this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.preference_config, menu);
		return true;
	}

}
