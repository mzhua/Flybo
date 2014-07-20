package me.hua.flybo.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.hua.flybo.R;
import me.hua.flybo.model.Status;
import me.hua.flybo.model.User;
import me.hua.flybo.ui.MainActivity;
import me.hua.flybo.ui.UserInfoActivity;
import me.hua.flybo.utils.AsyncImageLoader;
import me.hua.flybo.utils.AsyncImageLoader.ImageCallback;
import me.hua.flybo.utils.CheckNetwork;
import me.hua.flybo.utils.Tools;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainAdapter extends BaseAdapter {

	private ArrayList<Status> status_list = null;
	private Context context = null;
	
	AsyncImageLoader asyncImageLoader = null;

    //设置微博正文中的@，话题和链接所用
    final String START = "start";
    final String END = "end";
    final String TOPIC = "#[^#]+# ";
    final String NAMEH = "@[\\u4e00-\\u9fa5a-zA-Z0-9_-]{4,30}[ :]";
    final String URLH = "(((http|ftp|https|file)://)|((?<!((http|ftp|https|file)://))www\\.))" // 以http...或www开头
            + ".*?" // 中间为任意内容，惰性匹配
            + "(?=(&nbsp;|\\s|　|<br />|$|[<>]))"; // 结束条件

	public MainAdapter(Context context,ArrayList<Status> status_list){
		this.context = context;
		this.status_list = status_list;
	}
	
	class ContentHolder{
        //微博
		public ImageView status_head;
		public ImageView status_image;
		public TextView status_user_name;
		public TextView status_time;
		public TextView status_text;
		public TextView status_source;
        //转发的微博
        public TextView retweeted_status_text;
        public ImageView retweeted_status_image;
        //评论数，转发数
        public TextView text_repost;//转发
        public TextView text_comment;//评论
        public LinearLayout btn_repost;
        public LinearLayout btn_comment;
        public LinearLayout status_retweeted_linear_layout;//转发的微博
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		convertView = View.inflate(context, R.layout.status_list_item, null);

		ContentHolder holder = new ContentHolder();
		//关联组件,提高效率
		holder.status_head = (ImageView)convertView.findViewById(R.id.status_head);
		holder.status_image = (ImageView)convertView.findViewById(R.id.status_image);
		holder.status_user_name = (TextView)convertView.findViewById(R.id.status_user_name);
		holder.status_time = (TextView)convertView.findViewById(R.id.status_time);
		holder.status_text = (TextView)convertView.findViewById(R.id.status_text);
		holder.status_source = (TextView)convertView.findViewById(R.id.status_source);

        //转发微博的内容
        holder.status_retweeted_linear_layout = (LinearLayout)convertView.findViewById(R.id.status_retweeted_linear_layout);
        holder.retweeted_status_text = (TextView)convertView.findViewById(R.id.retweeted_status_text);
        holder.retweeted_status_image = (ImageView)convertView.findViewById(R.id.retweeted_status_image);

        //评论数，转发数
        holder.text_repost = (TextView)convertView.findViewById(R.id.text_repost);
        holder.text_comment = (TextView)convertView.findViewById(R.id.text_comment);
        holder.btn_repost = (LinearLayout)convertView.findViewById(R.id.btn_repost);
        holder.btn_comment  = (LinearLayout)convertView.findViewById(R.id.btn_comment);

        Status status = status_list.get(position);
		if(status != null){
			//设置标签，方便下次获取
//			convertView.setTag(status.getId());
			convertView.setTag(position);
			holder.status_head.setTag(position);
			holder.status_user_name.setText(status.getUser().getName());
			holder.status_time.setText(Tools.getInstance().getFormatDate(status.getCreatedAt())); //显示的时候格式化时间

			holder.status_source.setText(status.getSource());
            holder.text_repost.setText(status.getRepostsCount()+"");    //设置int类型时要将其转成string,否则会报错
            holder.text_comment.setText(status.getCommentsCount()+"");

            //设置微博正文中的@，话题和链接
            String status_text_str = status.getText();
            SpannableString status_spannable_str = new SpannableString(status_text_str);
            heightLight(TOPIC, Color.BLUE,status_spannable_str,status_text_str);
            heightLight(NAMEH, Color.RED,status_spannable_str,status_text_str);
//            heightLight(URLH, Color.GREEN,status_spannable_str,status_text_str);
            holder.status_text.setText(status_spannable_str);
            holder.status_text.setMovementMethod(LinkMovementMethod.getInstance());//一定要设置这个才有文字的点击事件

            //设置监听器
            setListenerOnView(holder);

            //微博正文缩略图
			if(status.isHasImage()){//如果没有图片，则开启子线程下载
				holder.status_image.setImageDrawable(context.getResources().getDrawable(R.drawable.wb_pic_loading));
				Drawable image = AsyncImageLoader.loadDrawable(status.getThumbnailPic(), holder.status_image,"image_thumbnailpic", new ImageCallback(){

					@Override
					public void imageSet(Drawable drawable, ImageView iv) {
						// TODO Auto-generated method stub
						iv.setImageDrawable(drawable);
					}
					
				});
				if(image != null){
					holder.status_image.setImageDrawable(image);
				}
				
			}
			else{//如果没有图片，则隐藏预测的图片
//				RelativeLayout.LayoutParams content_image_params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
//						LayoutParams.WRAP_CONTENT);
				holder.status_image.setVisibility(View.GONE);//.setLayoutParams(content_image_params);
			}
			//微博头像
			Drawable head_image = AsyncImageLoader.loadDrawable(status.getUser().getProfileImageUrl(), holder.status_head,"image_head", new ImageCallback(){

				@Override
				public void imageSet(Drawable drawable, ImageView iv) {
					// TODO Auto-generated method stub
					iv.setImageDrawable(drawable);
				}
				
			});
			if(head_image != null){
				holder.status_head.setImageDrawable(head_image);
			}
            //转发微博
            if(status.isHasRetweetedStatus()){
                holder.status_retweeted_linear_layout.setVisibility(View.VISIBLE);
//                holder.retweeted_status_text.setText("@"+status.getRetweetedStatus().getUser().getName()+" :"+status.getRetweetedStatus().getText());
                //转发微博的缩略图
                if(status.getRetweetedStatus().isHasImage()){//如果没有图片，则开启子线程下载
                    holder.retweeted_status_image.setImageDrawable(context.getResources().getDrawable(R.drawable.wb_pic_loading));
                    Drawable image = AsyncImageLoader.loadDrawable(status.getRetweetedStatus().getThumbnailPic(), holder.retweeted_status_image,"retweeted_image_thumbnailpic", new ImageCallback(){

                        @Override
                        public void imageSet(Drawable drawable, ImageView iv) {
                            // TODO Auto-generated method stub
                            iv.setImageDrawable(drawable);
                        }

                    });
                    if(image != null){
                        holder.retweeted_status_image.setImageDrawable(image);
                    }

                }
                else{//没有图片，则隐藏预设的图片
                    holder.retweeted_status_image.setVisibility(View.GONE);
                }

                //设置转发微博正文中的@，话题和链接
                String retweeted_status_text_str = "@"+status.getRetweetedStatus().getUser().getName()+" : "+status.getRetweetedStatus().getText();
                SpannableString retweeted_status_spannable_str = new SpannableString(retweeted_status_text_str);
                heightLight(TOPIC, Color.BLUE,retweeted_status_spannable_str,retweeted_status_text_str);
                heightLight(NAMEH, Color.RED,retweeted_status_spannable_str,retweeted_status_text_str);
//                heightLight(URLH, Color.GREEN,retweeted_status_spannable_str,retweeted_status_text_str);
                holder.retweeted_status_text.setText(retweeted_status_spannable_str);
                holder.retweeted_status_text.setMovementMethod(LinkMovementMethod.getInstance());//一定要设置这个才有文字的点击事件
            }

		}
		
		return convertView;
	}

    //为各个v组件设置监听器
    private void setListenerOnView(ContentHolder holder){
        holder.status_head.setOnClickListener(new myOnClickListener());
//        holder.text_repost.setOnClickListener(new myOnClickListener());//不能设置linearlayout下面的textview，否则会截断消息，造成linearlayout没有按下效果
//        holder.text_comment.setOnClickListener(new myOnClickListener());
        holder.btn_repost.setOnClickListener(new myOnClickListener());
        holder.btn_comment.setOnClickListener(new myOnClickListener());

    }

    private class myOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.status_head://后期完善，此处跳转到个人主页activity
                	if(CheckNetwork.checkNet(context)){
                		Intent intent = new Intent(context,UserInfoActivity.class);
                    	Bundle user_info = new Bundle();
                    	int view_tag = (Integer)view.getTag(); //view的标识
                    	
                    	user_info.putString("user_name", status_list.get(view_tag).getUser().getName());//传递用户id，在UserInfoActivity中从新浪下载

//                    	user_info.putString("head_image_url", status.getUser().getProfileImageUrl());
//                    	user_info.putString("user_name",status.getUser().getName());
//                    	user_info.putString("user_location",status.getUser().getLocation());
//                    	user_info.putInt("user_followers_count",status.getUser().getFollowersCount());
//                    	user_info.putInt("user_friends_count",status.getUser().getFriendsCount());
//                    	user_info.putInt("user_status_count",status.getUser().getStatusesCount());
//                    	user_info.putString("user_description",status.getUser().getDescription());
//                    	user_info.putString("user_url",status.getUser().getUrl());
                    	
                    	intent.putExtras(user_info);
                    	context.startActivity(intent);
                    	
                	}
                	else{
                		CheckNetwork.AlertNetError(context);
                	}
                	
                	
                    break;
                case R.id.btn_repost://后期完善，此处跳转到转发activity
                    LinearLayout ll_repost = (LinearLayout)view;
                    TextView text_repost = (TextView) ll_repost.findViewById(R.id.text_repost);
                    Toast.makeText(context,text_repost.getText(),Toast.LENGTH_SHORT).show();
                    break;
                case R.id.btn_comment://后期完善，此处跳转到评论activity
                    LinearLayout ll_comment = (LinearLayout)view;
                    TextView text_comment = (TextView) ll_comment.findViewById(R.id.text_comment);
                    Toast.makeText(context,text_comment.getText(),Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    /**
     * 文字高亮设置
     * @param pattern 正则表达式字符串
     * @param color	颜色
     * @param sas 
     * @param spannableString
     */
    private void heightLight(String pattern, int color,SpannableString sas,String spannableString) {
        ArrayList<Map<String, String>> lists = getStartAndEnd(Pattern.compile(pattern),spannableString);
        for (Map<String, String> str : lists) {
//            ForegroundColorSpan span = new ForegroundColorSpan(color);
            WeiboAtClickSpan caspan = new WeiboAtClickSpan(spannableString.substring(Integer.parseInt(str.get(START)), Integer.parseInt(str.get(END))));
//            spannableString.setSpan(span,Integer.parseInt(str.get(START)), Integer.parseInt(str.get(END)), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            sas.setSpan(caspan, Integer.parseInt(str.get(START)), Integer.parseInt(str.get(END)), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        }
    }

    /**
     * 获取指定规则字符串的开始、结束位置
     * @param pattern 正则表达式
     * @param spannableString 字符串
     * @return 开始、结束位置
     */
    
    private ArrayList<Map<String, String>> getStartAndEnd(Pattern pattern,String spannableString) {
        ArrayList<Map<String, String>> lists = new ArrayList<Map<String, String>>(0);

        Matcher matcher = pattern.matcher(spannableString);
        while (matcher.find()) {
            Map<String, String> map = new HashMap<String, String>(0);
            map.put(START, matcher.start() + "");
            map.put(END, matcher.end() + "");
            lists.add(map);
        }
        return lists;
    }
    //微博正文中@，话题，和链接的点击监听器
    class WeiboAtClickSpan extends ClickableSpan implements View.OnClickListener {
        String tag;

        WeiboAtClickSpan(String tag) {
            this.tag = tag;
        }

        @Override
        public void onClick(View arg0) {
            //响应文字点击事件,记得，如果要“@用户名”的这个字符串时，一定要把最后一个字符（空格或者“:”）去掉

            if(tag.startsWith("@")){
            	if(CheckNetwork.checkNet(context)){ //网络正常
            		Intent intent = new Intent(context,UserInfoActivity.class);
                	Bundle user_info = new Bundle();
                	String temp = tag.substring(1).trim();
                	String user_name_str = null;
                	if(temp.endsWith(":")){
                		user_name_str = temp.substring(0, temp.lastIndexOf(":"));
                	}
                	else if(temp.endsWith("："))
                	{
                		user_name_str = temp.substring(0, temp.lastIndexOf("："));
                	}
                	else{
                		user_name_str = temp;
                	}
                	user_info.putString("user_name", user_name_str);//传递用户id，在UserInfoActivity中从新浪下载

                	intent.putExtras(user_info);
                	context.startActivity(intent);
            	}
            	else{
            		CheckNetwork.AlertNetError(context);
            	}
            	
            }
            else{
                Toast.makeText(context, tag, 2000).show();
            }
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            //设置没有下划线
            ds.setUnderlineText(false);
            //设置颜色高亮
            ds.setARGB(255, 28, 134, 238);
//            ds.setARGB(255, 124, 205, 234);
        }
    }

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return status_list.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return status_list.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	

}
