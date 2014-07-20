package me.hua.flybo.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.hua.flybo.R;

/**
 * Created by Hua on 14-1-15.
 */
public class TestActivity extends Activity {
    private TextView myTextView;

    final String START = "start";
    final String END = "end";
    String str_main = "";
    final String TOPIC = "#.+?#";
    final String NAMEH = "@([\u4e00-\u9fa5A-Za-z0-9_]*)";
    final String URLH = "http://.*";
    SpannableString spannableString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_test);

        myTextView = (TextView) findViewById(R.id.text_test);
        str_main = "#jone# 我的小小测试关于微博中的高亮呵呵呵#jone# 大家都来看看啊@疾风Hua www.baidu.com";

        spannableString = new SpannableString(str_main);
        heightLight(TOPIC, Color.BLUE);
        heightLight(NAMEH, Color.RED);
        heightLight(URLH, Color.GREEN);


        myTextView.setText(spannableString);
        myTextView.setMovementMethod(LinkMovementMethod.getInstance());


//        int flags = Pattern.CASE_INSENSITIVE;
//        Pattern p = Pattern.compile("\\bquake[0-9]*\\b", flags);
//        Linkify.addLinks(myTextView, p, "me.hua.flybo.ui.MainActivity.class");
    }

    private void heightLight(String pattern, int color) {
        ArrayList<Map<String, String>> lists = getStartAndEnd(Pattern.compile(pattern));
        for (Map<String, String> str : lists) {
//            ForegroundColorSpan span = new ForegroundColorSpan(color);
            WeiboAtClickSpan caspan = new WeiboAtClickSpan(str_main.substring(Integer.parseInt(str.get(START)), Integer.parseInt(str.get(END))));
//            spannableString.setSpan(span,Integer.parseInt(str.get(START)), Integer.parseInt(str.get(END)), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            spannableString.setSpan(caspan, Integer.parseInt(str.get(START)), Integer.parseInt(str.get(END)), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        }
    }

    private ArrayList<Map<String, String>> getStartAndEnd(Pattern pattern) {
        ArrayList<Map<String, String>> lists = new ArrayList<Map<String, String>>(0);

        Matcher matcher = pattern.matcher(str_main);
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
            //响应文字点击事件
            Intent intent = new Intent(TestActivity.this,MainActivity.class);
            startActivity(intent);
            Toast.makeText(TestActivity.this, tag, 2000).show();
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            //设置没有下划线
            ds.setUnderlineText(false);
            //设置颜色高亮
            ds.setARGB(255, 0, 71, 112);
        }
    }
}
