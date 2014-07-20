package me.hua.flybo.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import me.hua.flybo.R;
import me.hua.flybo.adapter.MainAdapter;
import me.hua.flybo.baidumap.GeoCoderActivity;
import me.hua.flybo.constants.DrawerMenus;
import me.hua.flybo.db.AccessTokenKeeper;
import me.hua.flybo.db.MySQLiteOpenHelper;
import me.hua.flybo.model.Status;
import me.hua.flybo.model.User;
import me.hua.flybo.utils.CheckNetwork;
import me.hua.flybo.utils.Tools;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.sina.weibo.sdk.openapi.legacy.WeiboAPI;

/**
 * 程序设计流程和思想：
 * status_list_all这个全局变量很重要,在点击加载更多后，这个里面保存的就是之前已经获取的信息，最后将加载更多获取的拼接上去显示
 * 1、在initView()里面进行界面组件的初始化和对组件的监听器绑定 2、在initDrawerLayout()中加载drawerlayout抽屉菜单
 * 3、在initData("home", 0, 0)中从新浪获取微博数据，获取完成后调用myPublicTimeLineListener监听器
 * 需要注意的是，因为这个监听器是新浪微博timeline回调接口中的回调函数，而timeline的执行是在线程中的，所以对于在
 * 监听器myPublicTimeLineListener中获取到的response数据（微博数据json数据），要通过handler来处理
 */
public class MainActivity extends Activity {

    private Context mContext;
    private int REFRESH_HOME = 1;// 点击HOME触发的刷新
    private int REFRESH_LOAD_MORE = 2;// 点击加载更多的刷新
    private int refresh_type = 1;

    // drawerlayout抽屉菜单
    private String[] mPlanetTitles = null;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;

    // 适配器
    private MainAdapter adapter;
    private int pagesize = 20;
    private int pageNumber = 0;
    private ArrayList<Status> status_list_all = new ArrayList<Status>();// 获取到的信息全部拼接到这里，后面就不用重新请求之前已经获取过的信息了

    // 微博信息列表主界面
    private ListView main_lv;// 微博列表list
    private TextView text_user_group;
    // footer
    private LinearLayout list_footer;// 加载更多
    private LinearLayout loading;
    private TextView tv_msg;

    private Oauth2AccessToken mAccessToken;

    // 获取列表
    private StatusesAPI statuses;// 记得后面创建实例
    private Handler handler;

    //数据，配置
    private SQLiteDatabase db;
    private SharedPreferences pref ;
    private SharedPreferences.Editor pref_editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new MySQLiteOpenHelper(this).getWritableDatabase();
        pref = this.getSharedPreferences(AccessTokenKeeper.PREFERENCES_NAME, Context.MODE_APPEND);
        pref_editor = pref.edit(); 
        mContext = MainActivity.this;
        mAccessToken = AccessTokenKeeper.readAccessToken(this);// 从preference中获取accesstoken，获取timeline时需要这个

//        getActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.actionbar_bg_blue_fuubo));
        initView();
        initDrawerLayout();
        preLoadStatus();// 如果数据库之前缓存过信息，则从数据库加载，否则，重新下载
        if(!CheckNetwork.checkNet(mContext)){
        	CheckNetwork.AlertNetError(mContext);
        }
        Log.d("trace", ">>>>>>>>>>>>>>>>>>>>onCreate");
    }

    /**
     * onCreate时从数据库读取数据，加载到Adapter上
     */
    private void preLoadStatus() {
        Cursor cursor = db.rawQuery(
                "SELECT * FROM statusLists where user_token = '"+mAccessToken.getUid()+"' order by status_id desc",
                new String[]{});
        ArrayList<Status> status_list = new ArrayList<Status>();
        long[] properties = AccessTokenKeeper.readPositionProperties(mContext);

//		Toast.makeText(mContext, "tttt------since_id:"+since_id+"   max_id:"+max_id, 2000).show();
        int read_position = (int) properties[2];
        if (cursor.getCount() <= 0)
            getNewStatus("home", 0, 0, REFRESH_HOME);
        else {

            while (cursor.moveToNext()) {
                Status status = new Status();
                status.setUser(new User());
                status.setCreatedAt(cursor.getString(cursor
                        .getColumnIndex("status_create_at")));
                status.setId(cursor.getString(cursor
                        .getColumnIndex("status_id")));
                status.setText(cursor.getString(cursor
                        .getColumnIndex("status_text")));
                status.setRepostsCount(cursor.getInt(cursor
                        .getColumnIndex("status_reposts_count")));
                status.setCommentsCount(cursor.getInt(cursor
                        .getColumnIndex("status_comments_count")));

                status.setSource(cursor.getString(cursor
                        .getColumnIndex("status_source")));
                status.setThumbnailPic(cursor.getString(cursor
                        .getColumnIndex("status_thumbnail_pic")));

                status.getUser().setId(
                        cursor.getString(cursor
                                .getColumnIndex("status_user_id")));
                status.getUser().setName(
                        cursor.getString(cursor
                                .getColumnIndex("status_user_name")));
                status.getUser()
                        .setProfileImageUrl(
                                cursor.getString(cursor
                                        .getColumnIndex("status_user_profile_image_url")));
                if (cursor.getString(cursor
                        .getColumnIndex("status_thumbnail_pic")) != null
                        && !"".equals(cursor.getString(cursor
                        .getColumnIndex("status_thumbnail_pic")))) {
                    status.setHasImage(true);
                } else {
                    status.setHasImage(false);
                }

                //转发的微博内容
                status.setRetweetedStatus(new Status());//创建,重要
                status.getRetweetedStatus().setUser(new User());

                status.getRetweetedStatus().getUser().setName(cursor.getString(cursor
                        .getColumnIndex("status_retweeted_user_name")));
                status.getRetweetedStatus().setText(cursor.getString(cursor
                        .getColumnIndex("status_retweeted_status_text")));
                status.getRetweetedStatus().setThumbnailPic(cursor.getString(cursor
                        .getColumnIndex("status_retweeted_status_thumbnail_pic")));
                if (cursor.getString(cursor
                        .getColumnIndex("status_retweeted_status_thumbnail_pic")) != null
                        && !"".equals(cursor.getString(cursor
                        .getColumnIndex("status_retweeted_status_thumbnail_pic")))){
                    status.getRetweetedStatus().setHasImage(true);
                }
                else{
                    status.getRetweetedStatus().setHasImage(false);
                }
                if( !"".equals(cursor.getString(cursor
                        .getColumnIndex("status_retweeted_user_name")))){
                    status.setHasRetweetedStatus(true);
                }
                else{
                    status.setHasRetweetedStatus(false);
                }

                status_list.add(status);
            }
        }
        cursor.close();
        status_list_all.clear();
        status_list_all = status_list;
        adapter = new MainAdapter(mContext, status_list_all);
        main_lv.setAdapter(adapter);
        main_lv.setSelectionFromTop(read_position, 0);// 返回顶部
        showOffFooterView("show");
    }

    /**
     * 初始化界面组件，绑定监听器
     */
    private void initView() {
        main_lv = (ListView) findViewById(R.id.main_lv);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mPlanetTitles = getResources().getStringArray(R.array.left_drawer);
        text_user_group = (TextView) findViewById(R.id.text_user_group);
        setFooterView();// 设置底部的加载更多
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mPlanetTitles));
        // DrawerLayout点击触发监听
        mDrawerList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                // TODO Auto-generated method stub
                // Highlight the selected item, update the title, and close the
                // drawer

                mDrawerList.setItemChecked(position, true);
                // setTitle(mPlanetTitles[position]);
                // drawer_text.setText(mPlanetTitles[position]);
                mDrawerLayout.closeDrawer(mDrawerList);//关闭drawer_layout

                switch (position) {
                    case DrawerMenus.DRAWER_MENU_SETTING:
                        Intent intent = new Intent(mContext,
                                PreferenceConfigActivity.class);
                        mContext.startActivity(intent);
                        MainActivity.this.finish();
                        break;
                    case DrawerMenus.DRAWER_MENU_EXIT:
                    	MainActivity.this.finish();
                    	
                    	break;
                }

            }

        });

        text_user_group.setOnClickListener(new myOnClickListener());// actionbar左边下面的分组

        handler = new Handler() { // 更新列表

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub super.handleMessage(msg);
                setStatusListAdapter((String) msg.obj);
            }

        };

    }

    /**
     * 将微博的json字符串解析成ArrayList,然后 将解析好的ArrayList设置到MainAdapter上显示
     *
     * @param str_status_json 微博信息列表jspn
     */

    private void setStatusListAdapter(String str_status_json) {
        ArrayList<Status> status_list =  Tools.getInstance().parseStatuses(
                str_status_json, refresh_type);


        //把所有微博都拼接到status_list_all作为显示
        if (refresh_type == REFRESH_LOAD_MORE) {//获取以前的微博（点击加载更多）
            for (int i = 0; i < status_list.size(); i++) {
                status_list_all.add(status_list.get(i));
            }

        } else if (refresh_type == REFRESH_HOME) {//刷新最新的微博（home）
            if(status_list.size() <= pagesize){
                if (!status_list.isEmpty()) {
                    for (int i = 0; i < status_list.size(); i++) {
                        status_list_all.add(i, status_list.get(i));
                    }
                }
            }
            else{
                status_list_all = status_list;
            }


        }

        //上面已经获取，解析，拼接好了微博list，下面可以求出其中的since_id和max_id，并且写入到preference中
        long[] weibo_id = Tools.getInstance().getWeiboListId(status_list_all);
        pref_editor.putLong(AccessTokenKeeper.SINCE_ID,weibo_id[0]);
        pref_editor.putLong(AccessTokenKeeper.MAX_ID,weibo_id[1]);
        pref_editor.commit();

        if (status_list.size() > 0) {
            adapter = new MainAdapter(mContext, status_list_all);
            main_lv.setAdapter(adapter);
            int read_position = 0;
            if (refresh_type == REFRESH_HOME) {
                read_position = 0;
            } else {
                read_position = status_list_all.size() - status_list.size();// 滚动到的位置
                pageNumber++;
            }
            main_lv.setSelectionFromTop(read_position > 0 ? (read_position - 1)
                    : read_position, 0);
            Toast.makeText(mContext, "加载了" + status_list.size() + "条微博",
                    Toast.LENGTH_SHORT).show();
            showOffFooterView("show");

        } else {
            //虽然没有新微博了，但是点了刷新就要重新设置一遍以更新显示时间
            adapter = new MainAdapter(mContext, status_list_all);
            main_lv.setAdapter(adapter);
//            main_lv.setSelectionFromTop(0, 0);
            showOffFooterView("show");
            Toast.makeText(mContext, "没有新微博了", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 获取列表信息 Parameters: since_id
     * 若指定此参数，则返回ID比since_id大的微博（即比since_id时间晚的微博），默认为0。 max_id
     * 若指定此参数，则返回ID小于或等于max_id的微博，默认为0。 count 单页返回的记录条数，默认为50。 page
     * 返回结果的页码，默认为1。 base_app 是否只获取当前应用的数据。false为否（所有数据），true为是（仅当前应用），默认为false。
     * feature 过滤类型ID，0：全部、1：原创、2：图片、3：视频、4：音乐，默认为0。 trim_user
     * 返回值中user字段开关，false：返回完整user字段、true：user字段仅返回user_id，默认为false。 listener
     *
     * @param timeline_type  要获取的timeline类型(比如，home，friend,public等等)
     * @param since_id_tmp
     * @param max_id_tmp
     * @param refresh_type_0 刷新的类型，确定是由那个组件触发的刷新 refresh_HOME:将新加载的信息添加到前面
     *                       refresh_LOAD_MORE：将加载的信息添加到后面
     */
    private void getNewStatus(String timeline_type, long since_id_tmp, long max_id_tmp,
                              int refresh_type_0) {
    	if(CheckNetwork.checkNet(mContext)){ //网络正常
    		refresh_type = refresh_type_0;

            statuses = new StatusesAPI(mAccessToken);
            if ("home".equals(timeline_type)) {
                statuses.homeTimeline(since_id_tmp, max_id_tmp, pagesize, 1, false,
                        WeiboAPI.FEATURE.ALL, false, new RequestListener(){
                	@Override
                    public void onComplete(final String response) {
                        // TODO Auto-generated method stub
                        Message msg = handler.obtainMessage();
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
    	}
    	else{//网络异常
    		CheckNetwork.AlertNetError(mContext);
    	}
        

    }

    private class myOnClickListener implements OnClickListener {

        @Override
        public void onClick(View view) {
            // TODO Auto-generated method stub
            switch (view.getId()) {
                case R.id.text_user_group:
                    onPopupTextClick(text_user_group);
                    break;
                case R.id.tv_msg:
                    if (tv_msg.getVisibility() == View.VISIBLE) {
                        showOffFooterView("off");
                        long max_id = pref.getLong(AccessTokenKeeper.MAX_ID,0);
                        getNewStatus("home", 0, max_id > 0 ? (max_id - 1) : max_id, REFRESH_LOAD_MORE); // 此处的max_id要减一，因为max_id是上一次刷新的微博的最后一条，如果不减一，则下次点击“加载更多”刷新时最后一条会重复
                    }
                    break;
            }
        }

    }

    /**
     * 设置底部的加载更多
     */
    private void setFooterView() {
        list_footer = (LinearLayout) LayoutInflater.from(mContext).inflate(
                R.layout.main_status_list_footer, null);
        list_footer.setTag("footer");
        tv_msg = (TextView) list_footer.findViewById(R.id.tv_msg);
        loading = (LinearLayout) list_footer.findViewById(R.id.loading);
        tv_msg.setOnClickListener(new myOnClickListener());
        list_footer.setOnClickListener(new myOnClickListener());
        showOffFooterView("show");
        main_lv.addFooterView(list_footer);// 这儿是关键中的关键呀，利用FooterVIew分页动态加载
    }

    /**
     * 底部“点击加载更多”的显示控制
     *
     * @param showOff “show”：表示显示点击加载更多这几个字 其他：表示正在读取数据，显示“读取中”
     */
    private void showOffFooterView(String showOff) {
        if ("show".equals(showOff)) {// 显示“点击加载更多”
            tv_msg.setVisibility(View.VISIBLE);
            loading.setVisibility(View.GONE);
        } else {// 显示"读取中"
            tv_msg.setVisibility(View.GONE);
            loading.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 点击显示分组信息
     *
     * @param text_user_group0 "分组"文字组件
     */
    public void onPopupTextClick(final TextView text_user_group0) {
        PopupMenu popup = new PopupMenu(mContext, text_user_group0);
        popup.getMenuInflater().inflate(R.menu.text_user_group_popup,
                popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                text_user_group0.setText("分组 : " + item.getTitle());
                return true;
            }
        });

        popup.show();
    }

    // 以下代码必须要，为了监听actionbar左上角的事件
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_home://获取最新微博
                long since_id = pref.getLong(AccessTokenKeeper.SINCE_ID,0);
                getNewStatus("home", since_id, 0, REFRESH_HOME);
                main_lv.setSelectionFromTop(0, 0);
                break;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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

    //将数据保存到数据库
    private void saveStatusIntoDatabase(){
        pageNumber = 0;

        // 将数据保存到数据库
        // 先清空数据库,
        db.delete("statusLists", null, null);
        int status_size = (status_list_all.size() >= pagesize?pagesize:status_list_all.size());
        for (int i = 0; i < status_size; i++) {
            Status status = status_list_all.get(i);
            //此处有一个很容易出的错误，就是每一条微博肯定有他自己的信息，但是不一定有转发，所以如果该条微博没有转发时，就要为转发的参数设置预设值，否则会空指针异常
            if(status.isHasRetweetedStatus()){
                db.execSQL(
                        "insert into statusLists(user_token,status_create_at,status_id,status_text,status_reposts_count," +
                                "status_comments_count,status_source,status_thumbnail_pic,"
                                + "status_user_id,status_user_name,status_user_profile_image_url," +
                                "status_retweeted_user_name,status_retweeted_status_text,status_retweeted_status_thumbnail_pic) " +
                                " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        new Object[]{mAccessToken.getUid(),status.getCreatedAt(), status.getId(),
                                status.getText(), status.getRepostsCount(), status.getCommentsCount(),
                                status.getSource(),
                                status.getThumbnailPic(), status.getUser().getId(),
                                status.getUser().getName(),
                                status.getUser().getProfileImageUrl(),
                                status.getRetweetedStatus().getUser().getName(),
                                status.getRetweetedStatus().getText(),
                                status.getRetweetedStatus().getThumbnailPic()
                        });
            }
            else{
                db.execSQL(
                        "insert into statusLists(user_token,status_create_at,status_id,status_text,status_reposts_count," +
                                "status_comments_count,status_source,status_thumbnail_pic,"
                                + "status_user_id,status_user_name,status_user_profile_image_url," +
                                "status_retweeted_user_name,status_retweeted_status_text,status_retweeted_status_thumbnail_pic) " +
                                " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        new Object[]{mAccessToken.getUid(),status.getCreatedAt(), status.getId(),
                                status.getText(), status.getRepostsCount(), status.getCommentsCount(),
                                status.getSource(),
                                status.getThumbnailPic(), status.getUser().getId(),
                                status.getUser().getName(),
                                status.getUser().getProfileImageUrl(),
                                "",//status.getRetweetedStatus().getUser().getName(),
                                "",//status.getRetweetedStatus().getText(),
                                ""//status.getRetweetedStatus().getThumbnailPic()
                        });
            }

        }
        db.close();
    }
    // 初始化drawer
    private void initDrawerLayout() {
        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);

        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open, /* "open drawer" description for accessibility */
                R.string.drawer_close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                // getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                // getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        // 必须有这一句，才会出现actionbar左边的那个伸缩的图标
        getActionBar().setDisplayShowHomeEnabled(true);
        // getActionBar().setHomeButtonEnabled(true);
        // Note: getActionBar() Added in API level 11
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.d("trace", ">>>>>>>>>>>>>>>>>>>>onDestroy");
        saveStatusIntoDatabase();

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Log.d("trace", ">>>>>>>>>>>>>>>>>>>>onPause");
    }

    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
        Log.d("trace", ">>>>>>>>>>>>>>>>>>>>onRestart");
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.d("trace", ">>>>>>>>>>>>>>>>>>>>onResume");
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        Log.d("trace", ">>>>>>>>>>>>>>>>>>>>onStart");
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub

        super.onStop();
        Log.d("trace", ">>>>>>>>>>>>>>>>>>>>onStop");
    }

}
