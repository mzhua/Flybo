package me.hua.flybo.baidumap;

import me.hua.flybo.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;

/**
 * 此demo用来展示如何进行地理编码搜索（用地址检索坐标）、反地理编码搜索（用坐标检索地址）
 * 同时展示了如何使用ItemizedOverlay在地图上标注结果点
 * 
 */
public class GeoCoderActivity extends Activity {

	private ImageView my_location;
	
	private String location_str;
	private String user_name;
	// 地图相关
	MapView mMapView = null; // 地图View
	// 搜索相关
	MKSearch mSearch = null; // 搜索模块，也可去掉地图模块独立使用
	
	private boolean isMyLocation = false; //进入时，默认显示所选用户的位置

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.geocoder);
		
		Toast.makeText(GeoCoderActivity.this, "点击右下角定位图标来切换我和网友的位置", Toast.LENGTH_SHORT).show();
		
		my_location = (ImageView)findViewById(R.id.my_location);
		my_location.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!isMyLocation){
					setTitle("我的位置");
					Location myLocation = getMyLocation();
					GetMyLocation(myLocation);
					Toast.makeText(GeoCoderActivity.this, "可缩放来查看我与网友的相对位置", Toast.LENGTH_LONG).show();
					
				}
				else{
					setTitle(user_name+"的位置");
					GetUserLocation(location_str);

				}
				isMyLocation = !isMyLocation;
			}
			
		});
		// CharSequence titleLable="地理编码功能";
		// setTitle(titleLable);
		initBaiduMap();
		
		Intent intent = this.getIntent();
		Bundle location_data = intent.getExtras();
		location_str = location_data.getString("location");
		user_name = location_data.getString("user_name");
		setTitle(user_name+"的位置");
		GetUserLocation(location_str);
	}

	/**
	 * 初始化百度地图
	 */
	private void initBaiduMap() {
		BaiduMapApplication app = (BaiduMapApplication) this.getApplication();
		// 地图初始化
		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapView.getController().enableClick(true);
		mMapView.getController().setZoom(12);

		// 初始化搜索模块，注册事件监听
		mSearch = new MKSearch();
		mSearch.init(app.mBMapManager, new MKSearchListener() {
			@Override
			public void onGetPoiDetailSearchResult(int type, int error) {
			}

			public void onGetAddrResult(MKAddrInfo res, int error) {
				if (error != 0) {
					String str = String.format("错误号：%d", error);
					Toast.makeText(GeoCoderActivity.this, str,
							Toast.LENGTH_LONG).show();
					return;
				}
				// 地图移动到该点
				mMapView.getController().animateTo(res.geoPt);
				// if (res.type == MKAddrInfo.MK_GEOCODE) {
				// // 地理编码：通过地址检索坐标点
				// String strInfo = String.format("纬度：%f 经度：%f",
				// res.geoPt.getLatitudeE6() / 1e6,
				// res.geoPt.getLongitudeE6() / 1e6);
				// Toast.makeText(GeoCoderActivity.this, strInfo,
				// Toast.LENGTH_LONG).show();
				// }
				// if (res.type == MKAddrInfo.MK_REVERSEGEOCODE) {
				// // 反地理编码：通过坐标点检索详细地址及周边poi
				// String strInfo = res.strAddr;
				// Toast.makeText(GeoCoderActivity.this, strInfo,
				// Toast.LENGTH_LONG).show();
				//
				// }
				// 生成ItemizedOverlay图层用来标注结果点
				ItemizedOverlay<OverlayItem> itemOverlay = new ItemizedOverlay<OverlayItem>(
						null, mMapView);
				// 生成Item
				OverlayItem item = new OverlayItem(res.geoPt, "", null);
				// 得到需要标在地图上的资源
				Drawable marker = marker = getResources().getDrawable(
						R.drawable.icon_markf);
				if(isMyLocation){
					marker = getResources().getDrawable(
							R.drawable.icon_gcoding);
				}
				// 为maker定义位置和边界
				marker.setBounds(0, 0, marker.getIntrinsicWidth(),
						marker.getIntrinsicHeight());
				// 给item设置marker
				item.setMarker(marker);
				// 在图层上添加item
				itemOverlay.addItem(item);

				// 清除地图其他图层
//				mMapView.getOverlays().clear();
				// 添加一个标注ItemizedOverlay图层
				mMapView.getOverlays().add(itemOverlay);
				// 执行刷新使生效
				mMapView.refresh();
			}

			public void onGetPoiResult(MKPoiResult res, int type, int error) {

			}

			public void onGetDrivingRouteResult(MKDrivingRouteResult res,
					int error) {
			}

			public void onGetTransitRouteResult(MKTransitRouteResult res,
					int error) {
			}

			public void onGetWalkingRouteResult(MKWalkingRouteResult res,
					int error) {
			}

			public void onGetBusDetailResult(MKBusLineResult result, int iError) {
			}

			@Override
			public void onGetSuggestionResult(MKSuggestionResult res, int arg1) {
			}

			@Override
			public void onGetShareUrlResult(MKShareUrlResult result, int type,
					int error) {
				// TODO Auto-generated method stub

			}

		});
	}

	/**
	 * 获取当前位置
	 */
	private Location getMyLocation() {
		// 授权可以使用定位服务

//		try {
//			Settings.Secure.setLocationProviderEnabled(getContentResolver(),
//					LocationManager.NETWORK_PROVIDER, true);
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		try {
//			Settings.Secure.setLocationProviderEnabled(getContentResolver(),
//					LocationManager.PASSIVE_PROVIDER, true);
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
		// 获取位置服务管理工具
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Location location = null;
		// 判断是否支持GPS定位，绑定GPS定位事件,并进行定位

		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			// 获取最近一次定位数据
			location = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location != null) {
				// Log.e("s8_1_location","GPS_last：" +
				// String.valueOf(location.getLongitude()) + "," +
				// String.valueOf(location.getLatitude()));
			}

//			locationManager.requestLocationUpdates(
//					LocationManager.GPS_PROVIDER, 1000, 0, gpsListener);
		}
		else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			//获取最近一次定位数据
			location = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (location != null) {
				Log.e("s8_1_location","NETWORK_last：" + String.valueOf(location.getLongitude()) + "," + String.valueOf(location.getLatitude()));
			}
			
			
//			locationManager.requestLocationUpdates(
//					LocationManager.NETWORK_PROVIDER, 1000, 0, networkListener);
		}
		else if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
			//获取最近一次定位数据
			location = locationManager
					.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
			if (location != null) {
				Log.e("s8_1_location","PASSIVE_last：" + String.valueOf(location.getLongitude()) + "," + String.valueOf(location.getLatitude()));
			}
			
			
//			locationManager.requestLocationUpdates(
//					LocationManager.PASSIVE_PROVIDER, 1000, 0, passiveListener);
		}
		return location;
	}

	// gps监听器
	private LocationListener gpsListener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLocationChanged(Location location) {
			if (location != null) {
				Log.e("s8_1_location",
						"GPS：" + String.valueOf(location.getLongitude()) + ","
								+ String.valueOf(location.getLatitude()));
			}
		}
	};

	// network监听
	private LocationListener networkListener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLocationChanged(Location location) {
			if (location != null) {
				Log.e("s8_1_location",
						"NETWORK：" + String.valueOf(location.getLongitude())
								+ "," + String.valueOf(location.getLatitude()));
			}
		}
	};

	// network定位，
	private LocationListener passiveListener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLocationChanged(Location location) {
			if (location != null) {
				Log.e("s8_1_location",
						"PASSIVE：" + String.valueOf(location.getLongitude())
								+ "," + String.valueOf(location.getLatitude()));
			}
		}
	};

	/**
	 * 发起搜索
	 * 
	 * @param v
	 */
	private void GetUserLocation(String location_str0) {

		// 下面两个搜索的执行顺序不能乱
		// 反Geo搜索
//		if(myLocation != null){
//			GeoPoint ptCenter = new GeoPoint(
//					 (int) (Float.valueOf((float) myLocation.getLatitude()) * 1e6),
//					 (int) (Float.valueOf((float)myLocation.getLongitude()) * 1e6));
//					 mSearch.reverseGeocode(ptCenter);
//		}
		 
		mSearch.geocode(location_str0, "");

	}
	
	/**
	 * 获取当前位置
	 * @param myLocation
	 * @param location_str0
	 */
	private void GetMyLocation(Location myLocation) {

		// 反Geo搜索
		if(myLocation != null){
			GeoPoint ptCenter = new GeoPoint(
					 (int) (Float.valueOf((float) myLocation.getLatitude()) * 1e6),
					 (int) (Float.valueOf((float)myLocation.getLongitude()) * 1e6));
					 mSearch.reverseGeocode(ptCenter);
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
	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		mMapView.destroy();
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mMapView.onRestoreInstanceState(savedInstanceState);
	}
}
