package me.hua.flybo.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.URLEncoder;
import java.util.HashMap;

import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

public class AsyncImageLoader {
	/**
	 * 这里不知道为什么不会调用构造方法里面的实例创建，只能暂时这样解决，就是在定义的时候就创建实例
	 * 软引用，如果内存不够了，则会自动清除
	 * key = url  value = 图片资源对象
	 */
	private static  HashMap<String,SoftReference<Drawable>> imageCache = new HashMap<String,SoftReference<Drawable>>();
	
	private static String sdCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
	private static String HEAD_IMAGE_PATH = sdCardPath+"/Flybo";
	
	public AsyncImageLoader(){
		if(imageCache == null){
			imageCache = new HashMap<String,SoftReference<Drawable>>();
		}
	}
	/**
	 * 异步下载图片
	 * @param url 图片链接
	 * @param imageView 需要显示图片的组件
	 * @param image_type 下载图片的类型(头像，内容图片等)
	 * @param callback 回调函数
	 * @return 图片资源
	 */
	public static Drawable loadDrawable(final String url,final ImageView imageView,final String image_type,final ImageCallback callback){
		//如果缓存里面已经有了，说明已经下载了，则不需要再次下载
		String encode_url = null;
		try {
			encode_url = URLEncoder.encode(url,"utf-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(imageCache.containsKey(url)){
			SoftReference<Drawable> soft_drawable = imageCache.get(url);
			Drawable drawable = soft_drawable.get();
			if(drawable != null){
				return drawable;
			}
		}
		//判断在本地是否已经保存过了
		
		File dir = new File(HEAD_IMAGE_PATH+"/"+image_type); 

		if(dir.exists()){
			File[] fileList = dir.listFiles();
			if(fileList.length > 0){
				int i = 0;
				for(; i < fileList.length; i++){
					if(encode_url.equals(fileList[i].getName())){
						break;
					}
				}
				if(i < fileList.length){//存在，表示之前已经下载过了
					Drawable bd = new BitmapDrawable(BitmapFactory.decodeFile(HEAD_IMAGE_PATH+"/"+image_type+"/"+encode_url));
					return bd;
				}
			}
		}
		
		final Handler handler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				//图片资源设置操作
				callback.imageSet((Drawable)msg.obj, imageView);
			}
			
		};
		//具体的下载操作
		new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				Drawable drawable = Tools.getDrawableFromUrl(url);
				
				if(drawable != null){
					//放入缓存
					imageCache.put(url, new SoftReference<Drawable>(drawable));
					
					Message msg = handler.obtainMessage();
					msg.obj = drawable;
					handler.sendMessage(msg);
					//保存到本地
					try {
						Tools.getInstance().savePicture(drawable, HEAD_IMAGE_PATH,URLEncoder.encode(url,"utf-8") ,image_type);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
			
		}.start();
		return null;
	}
	/**
	 * 回调接口 
	 * @author Hua
	 *
	 */
	public interface ImageCallback{
		public void imageSet(Drawable drawable,ImageView iv);
	}

}
