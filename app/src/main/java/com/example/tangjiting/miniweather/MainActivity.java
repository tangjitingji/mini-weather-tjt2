package com.example.tangjiting.miniweather;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.tangjiting.app.MyApplication;
import com.example.tangjiting.bean.City;
import com.example.tangjiting.bean.TodayWeather;
import com.example.tangjiting.util.NetUtil;
import com.example.tangjiting.util.PinYin;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tangjiting on 2017/9/25.
 */

public class MainActivity extends AppCompatActivity{

    public LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();

    //Log信息标签
    private String TAG = "myWeather";

    private static final int UPDATE_TODAY_WEATHER = 1;

    private ImageView mUpdateBtn;//信息更新按钮
    private Animation mRefreshAnim;//刷新转圈动画实现
    private ImageView mUpdateBtn_cancel;//信息更新按钮,正在更新时显示
    private ImageView mCitySelect;//城市选择按钮
    private ProgressBar title_update_progressbar;
    private ImageView mShareBtn;//分享按钮
    private ImageView mLocateBtn;
    private List<City> citylist;//城市列表

    private Context context2 = this;
    private ImageView[] dots2;//导航小圆点
    private int[] ids2 = {R.id.weatheriv1, R.id.weatheriv2};//小圆点的iamgeview值
    private ViewPagerAdapter vpAdapter2;
    private ViewPager vp2;
    private List<View> views2 = new ArrayList<>();

    //定位
    private  MyServiceConn myServiceConn;
    private LocationService.MyBinder binder = null;
    private static final int UPDATE_LOCATION = 3;

    //更新今日天气数据，定义相关的控件对象
    //今日天气详细信息
    private TextView pm2_5,cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv, climateTv,nowtemperatureTv, windTv, city_name_Tv;
    private ImageView weatherImg, weatherImg1, weatherImg2,weatherImg3,weatherImg4,weatherImg5,weatherImg6, pmImg;
    private RelativeLayout relativeLayout;
    private LinearLayout linearLayout;

    //近三日天气信息
    private TextView temperatureTv,temperatureTv1, temperatureTv2,temperatureTv3,temperatureTv4,temperatureTv5,temperatureTv6;//最高气温最低气温
    private TextView type1Tv,type2Tv,type3Tv,type4Tv,type5Tv,type6Tv;//天气状况，如，今日 晴


    //城市编码
    String cityCode,currentCityCode,selectCityCode,locateCityCode;
    String cityname;
    TodayWeather todayWeather = null;

    private TodayWeather todayweather;//今天的天气,给widget使用
    public static int[] appWidgetIds;//给widget使用
    private boolean isrunning = false;//后台线程是否运行

    //通过消息机制，将解析的天气对象，通过消息发送给主线程，主线程接收到消息数据后，调用updateTodayWeather函数，更新UI界面上的数据
    //处理天气更新的handler
    //更新UI控件
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                case UPDATE_LOCATION:
                    //后台实时更新位置信息，当用户点击定位的时候才更新UI
                    String cityname = (String) msg.obj;
                    String cityCode = null;
                    Log.d("myinfo",cityname);
                    for(City city:citylist) {
                        if(cityname.substring(0,cityname.length()-1) .equals( city.getCity()))
                            cityCode = city.getNumber();
                    }
                    SharedPreferences pref = getSharedPreferences("config",MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("cityCode", cityCode);
                    editor.commit();
                default:
                    break; }
        } };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);//关联布局
        mLocationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        //注册监听函数
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
//可选，是否需要地址信息，默认为不需要，即参数为false
//如果开发者需要获得当前点的地址信息，此处必须为true
        mLocationClient.setLocOption(option);
//mLocationClient为第二步初始化过的LocationClient对象
//需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
//更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明
        //程序第一次执行跳转到引导界面
        SharedPreferences preferences = getSharedPreferences("count", MODE_PRIVATE);
        int count = preferences.getInt("count", 0);
        //判断程序与第几次运行，如果是第一次运行则跳转到引导页面
        if (count == 0) {
            Intent i = new Intent(MainActivity.this,Guide.class);
            startActivity(i);
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("count", ++count);
        editor.commit();

        /*if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            Log.d("myWeather", "网络OK");
            Toast.makeText(MainActivity.this, "网络OK!", Toast.LENGTH_LONG).show();
        }
        else {
            Log.d("myWeather", "网络跪了");
            Toast.makeText(MainActivity.this, "网络跪了!", Toast.LENGTH_LONG).show();
        }*/



        //选择城市按钮
        mCitySelect = (ImageView)findViewById(R.id.title_city_manager);
        //mCitySelect.setOnClickListener(this);
        mCitySelect.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //为城市管理按钮添加意图,实现功能:点击后跳转到selectCity页面
            Intent i = new Intent(MainActivity.this,SelectCity.class);
            if(todayWeather != null){
                i.putExtra("cityName",todayWeather.getCity());
            }else{
                i.putExtra("cityName","南京");//默认城市为南京
            }
            startActivityForResult(i,1);
        }
        });

        //定位城市按钮
        initdata();
        initLocation();
        mLocateBtn = (ImageView)findViewById(R.id.title_location);
        //mCitySelect.setOnClickListener(this);
        mLocateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref = getSharedPreferences("config",MODE_PRIVATE);
                String citycode = pref.getString("cityCode", "101010100");
                queryWeatherCode(citycode);
            }
        });

        //更新天气信息按钮
        //点击更新按钮
        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);

        mUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //更新天气信息
                preUpdateWeather();
                //提示更新成功
                Toast.makeText(MainActivity.this,"更新成功!",Toast.LENGTH_SHORT).show();
            }
        });


        //分享按钮
//        mShareBtn = (ImageView) findViewById(R.id.btn_share);
//        mShareBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent=new Intent(Intent.ACTION_SEND);
//                intent.setType("image/*");
//                String str = todayWeather.getCity() + "今日天气：" + "当前温度"+todayWeather.getWendu()+"℃,"+todayWeather.getType() +
//                        "," + todayWeather.getFengxiang() + todayWeather.getFengli()+
//                        ","+ todayWeather.getHigh() + "~" + todayWeather.getLow()  +
//                        ",湿度" + todayWeather.getShidu() +
//                        ",pm2.5指数" + todayWeather.getPm25() +
//                        ",空气质量为" + todayWeather.getQuality() +
//                        "," + todayWeather.getDate() + " "+ todayWeather.getUpdatetime() + "发布";
//                intent.putExtra(Intent.EXTRA_SUBJECT, "Share");
//                intent.putExtra(Intent.EXTRA_TEXT,str+"."+ " (分享自tjt-mini-weather)");
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(Intent.createChooser(intent, getTitle()));
//            }
//        });
//        mLocateBtn = findViewById(R.id.title_location);
        //初始化UI控件
        initView();
    }

    private void initdata() {
        citylist = MyApplication.getInstance().getmCityList();
    }

    private void initLocation() {
        //初始化百度地图service
        Intent intent = new Intent(this, LocationService.class);
        myServiceConn = new MyServiceConn();
        startService(intent);//开启LocationService
        bindService(intent, myServiceConn, Context.BIND_AUTO_CREATE);//绑定service

    }


    /*
    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.title_city_manager){
            Intent i = new Intent(this,SelectCity.class);
            //startActivity(i);
            startActivityForResult(i,1);
        }

        //在UI线程中，为更新按钮（ImageView）增加单击事件
        if (view.getId() == R.id.title_update_btn) {
            //从SharedPreferences中读取城市的id，如果没有定义则缺省为101010100(北京城市 ID)。
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code", "101010100");
            Log.d("myWeather",cityCode);

            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
                Log.d("myWeather", "网络正常");
                queryWeatherCode(cityCode);
            }
            else {
                Log.d("myWeather", "网络跪了");
                Toast.makeText(MainActivity.this,"网络跪了!",Toast.LENGTH_LONG).show();
            }
        }
    }*/
    class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取地址相关的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明
            String city = location.getCity();    //获取城市
            String district = location.getDistrict();    //获取区县
            cityname = city;
            Log.d("myinfo",cityname);
        }
    }
    //返回主界面时，传递城市代码数据
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        if(requestCode == 1 && resultCode== RESULT_OK){
            //获取用户选择的城市编码
            String select_city_code = data.getStringExtra("cityCode");
            Log.d(TAG,"接收到的选择城市编码----------------->："+ select_city_code);

//            if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
//                Log.d("myWeather", "网络正常");
//                queryWeatherCode(select_city_code);
//            }
//            else {
//                Log.d("myWeather", "网络跪了");
//                Toast.makeText(MainActivity.this,"网络跪了!",Toast.LENGTH_LONG).show();
//            }

            //将用户选择的城市编码保存，用于下次访问自动更新天气信息
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putString("cityCode",select_city_code);
            editor.commit();

            //更新天气
            preUpdateWeather();

            //提示更新成功
            if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
//                queryWeatherCode(select_city_code);
                Toast.makeText(MainActivity.this,"更新成功啦!",Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MainActivity.this,"网络跪了!",Toast.LENGTH_LONG).show();
            }
        }

    }

    /** *
     * 从网络获取城市编码为cityCode的城市的天气信息
     * @param cityCode 城市编码（默认北京101010100）
     */
    private void queryWeatherCode(String cityCode)  {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d(TAG, "XML文件网络访问URL：" + address);
        Log.d("update", "queryWeatherCode中参数citycode=：" + cityCode);

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con=null;
                //定义TodayWeather对象
                TodayWeather todayWeather = null;
                try{
                    URL url = new URL(address);
                    con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();

                    String str;
                    while((str=reader.readLine()) != null){
                        response.append(str);
                        Log.d("myWeather", str);
                    }
                    //调用解析函数,解析获取到的网络数据
                    //responseStr获取的网络数据结果
                    String responseStr=response.toString();
                    Log.d("myWeather", responseStr);
                    //在获取网络数据后，调用解析函数
                    //parseXML(responseStr);

                    //调用parseXML，并返回TodayWeather对象。
                    todayWeather = parseXML(responseStr);
                    todayweather = todayWeather;//赋值全局变量
                    if (todayWeather != null) {
                        Log.d("myWeather", todayWeather.toString());

                        //使用handler发送消息
                        Message msg =new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj=todayWeather;
                        mHandler.sendMessage(msg);
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                finally {
                    if(con != null){
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

    //编写解析函数，解析出城市名称已经更新时间信息
    /*private void parseXML(String xmldata) {
        int fengxiangCount=0;
        int fengliCount =0;
        int dateCount=0;
        int highCount =0;
        int lowCount=0;
        int typeCount =0;
        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather", "parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    // 判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    // 判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("city")) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "city:    "+xmlPullParser.getText());
                        }
                        else if (xmlPullParser.getName().equals("updatetime")){
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "updatetime:    " + xmlPullParser.getText());
                        }
                        else if (xmlPullParser.getName().equals("shidu")) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "shidu:    " + xmlPullParser.getText());
                        }
                        else if (xmlPullParser.getName().equals("wendu")) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "wendu:    " + xmlPullParser.getText());
                        }
                        else if (xmlPullParser.getName().equals("pm25")) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "pm2.5:    " + xmlPullParser.getText());
                        }
                        else if (xmlPullParser.getName().equals("quality")) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "quality:    " + xmlPullParser.getText());
                        }
                        else if (xmlPullParser.getName().equals("fengxiang")&& fengxiangCount == 0) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "fengxiang:    " + xmlPullParser.getText());
                            fengxiangCount++;
                        }
                        else if (xmlPullParser.getName().equals("fengli")&& fengliCount == 0) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "fengli:    " + xmlPullParser.getText());
                            fengliCount++;
                        }
                        else if (xmlPullParser.getName().equals("date")&& dateCount == 0) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "date:    " + xmlPullParser.getText());
                            dateCount++;
                        }
                        else if (xmlPullParser.getName().equals("high")&& highCount == 0) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "high:    " + xmlPullParser.getText());
                            highCount++;
                        }
                        else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "low:    " + xmlPullParser.getText());
                            lowCount++;
                        }
                        else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                            eventType = xmlPullParser.next();
                            Log.d("myWeather", "type:    " + xmlPullParser.getText());
                            typeCount++;
                        }
                        break;
                    // 判断当前事件是否为标签元素结束事件
                    case XmlPullParser.END_TAG:
                        break;
                }
                // 进入下一个元素并触发相应事件
                eventType = xmlPullParser.next();
            }
        }
        catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    //修改解析函数
    private TodayWeather parseXML(String xmldata){
        int fengxiangCount=0;
        int fengliCount =0;
        int dateCount=0;
        int highCount =0;
        int lowCount=0;
        int typeCount =0;

        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather", "parseXML");

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    // 判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    // 判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp")){
                            todayWeather= new TodayWeather();
                        }
                        if (todayWeather != null) {
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            }
                            else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            }
                            else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            }
                            else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            }
                            else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            }
                            else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            }
                            else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            }
                            else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCount++;
                            }
                            else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                            }
                            //未来六天日期
                            else if (xmlPullParser.getName().equals("date") && dateCount == 1) {//明天日期
                                eventType = xmlPullParser.next();
                                todayWeather.setDate1(xmlPullParser.getText());
                                dateCount++;
                            }
                            else if (xmlPullParser.getName().equals("date") && dateCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate2(xmlPullParser.getText());
                                dateCount++;
                            }
                            else if (xmlPullParser.getName().equals("date") && dateCount == 3) {//第三天日期
                                eventType = xmlPullParser.next();
                                todayWeather.setDate3(xmlPullParser.getText());
                                dateCount++;
                            }
                            else if (xmlPullParser.getName().equals("date") && dateCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate4(xmlPullParser.getText());
                                dateCount++;
                            }
                            else if (xmlPullParser.getName().equals("date") && dateCount == 5) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate5(xmlPullParser.getText());
                                dateCount++;
                            }
                            else if (xmlPullParser.getName().equals("date") && dateCount == 6) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate6(xmlPullParser.getText());
                                dateCount++;
                            }
                            else if (xmlPullParser.getName().equals("high") && highCount == 0) {//今日最高温度
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }
                            else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                                //未来六天最高温和最低温
                            }else if (xmlPullParser.getName().equals("high") && highCount == 1) {//明日最高温度
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh1(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }else if (xmlPullParser.getName().equals("low") && lowCount == 1) {//明日最低温度
                                eventType = xmlPullParser.next();
                                todayWeather.setLow1(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }else if (xmlPullParser.getName().equals("high") && highCount == 2) {//后日最高温度
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh2(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }else if (xmlPullParser.getName().equals("low") && lowCount == 2) {//后日最低温度
                                eventType = xmlPullParser.next();
                                todayWeather.setLow2(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 3) {//第三天日最高温度
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh3(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }else if (xmlPullParser.getName().equals("low") && lowCount == 3) {//第三天最低温度
                                eventType = xmlPullParser.next();
                                todayWeather.setLow3(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }else if (xmlPullParser.getName().equals("high") && highCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh4(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }else if (xmlPullParser.getName().equals("low") && lowCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow4(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }else if (xmlPullParser.getName().equals("high") && highCount == 5) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh5(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }else if (xmlPullParser.getName().equals("low") && lowCount == 5) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow5(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 6) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh6(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }else if (xmlPullParser.getName().equals("low") && lowCount == 6) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow6(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }
                            //未来六天天气状况
                            else if (xmlPullParser.getName().equals("type") && typeCount == 0) {//今日天气状况
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                typeCount++;
                            }
                            else if (xmlPullParser.getName().equals("type") && typeCount == 1) {//明日天气状况
                                eventType = xmlPullParser.next();
                                todayWeather.setType1(xmlPullParser.getText());
                                typeCount++;
                            }
                            else if (xmlPullParser.getName().equals("type") && typeCount == 2) {//后日天气状况
                                eventType = xmlPullParser.next();
                                todayWeather.setType2(xmlPullParser.getText());
                                typeCount++;
                            }
                            else if (xmlPullParser.getName().equals("type") && typeCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType3(xmlPullParser.getText());
                                typeCount++;
                            }
                            else if (xmlPullParser.getName().equals("type") && typeCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType4(xmlPullParser.getText());
                                typeCount++;
                            }
                            else if (xmlPullParser.getName().equals("type") && typeCount == 5) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType5(xmlPullParser.getText());
                                typeCount++;
                            }
                            else if (xmlPullParser.getName().equals("type") && typeCount == 6) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType6(xmlPullParser.getText());
                                typeCount++;
                            }
                        }

                        break;


                    // 判断当前事件是否为标签元素结束事件
                    case XmlPullParser.END_TAG:
                        break;
                }
                // 进入下一个元素并触发相应事件
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //测试是否获取到天气--可删除
        if(todayWeather == null){
            Log.d(TAG,"todayWeather未存入");
        }else {
            Log.d(TAG,todayWeather.toString());
        }

        return todayWeather;
    }

    //初始化控件内容
    void initView(){
        //顶部工具栏城市名称
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        nowtemperatureTv = (TextView) findViewById(R.id.nowtemperature);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality);
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        pm2_5 = (TextView) findViewById(R.id.pm2_5);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);

        //以下是初始化viewPager
        LayoutInflater inflater = LayoutInflater.from(context2);
        View one_page = inflater.inflate(R.layout.weatherpage1, null);
        View two_page = inflater.inflate(R.layout.weatherpage2, null);
        views2.add(one_page);
        views2.add(two_page);
        vpAdapter2 = new ViewPagerAdapter(views2, context2);
        vp2 = (ViewPager) findViewById(R.id.mViewpager);
        vp2.setAdapter(vpAdapter2);

        //增加页面变化的监听事件，动态修改导航小圆点的属性
        dots2 = new ImageView[views2.size()];
        for (int i = 0; i < views2.size(); i++) {
            dots2[i] = (ImageView) findViewById(ids2[i]);
        }


        //天气状况图片(未来6天)
        weatherImg1 = (ImageView) one_page.findViewById(R.id.weather01_pic);//明天
        weatherImg2 = (ImageView) one_page.findViewById(R.id.weather02_pic);//后天
        weatherImg3 = (ImageView) one_page.findViewById(R.id.weather03_pic);//第三天
        weatherImg4 = (ImageView) two_page.findViewById(R.id.weather04_pic);
        weatherImg5 = (ImageView) two_page.findViewById(R.id.weather05_pic);
        weatherImg6 = (ImageView) two_page.findViewById(R.id.weather06_pic);


        //温度范围(未来6天)
        temperatureTv1 = (TextView) one_page.findViewById(R.id.weather01);//明天
        temperatureTv2 = (TextView) one_page.findViewById(R.id.weather02);//后日
        temperatureTv3 = (TextView) one_page.findViewById(R.id.weather03);//第三日
        temperatureTv4 = (TextView) two_page.findViewById(R.id.weather04);
        temperatureTv5 = (TextView) two_page.findViewById(R.id.weather05);
        temperatureTv6 = (TextView) two_page.findViewById(R.id.weather06);

        //近6天天气状况，如，今日 晴
        type1Tv = (TextView) one_page.findViewById(R.id.weather01_tips);//明日
        type2Tv = (TextView) one_page.findViewById(R.id.weather02_tips);//后日
        type3Tv = (TextView) one_page.findViewById(R.id.weather03_tips);//第三日
        type4Tv = (TextView) two_page.findViewById(R.id.weather04_tips);
        type5Tv = (TextView) two_page.findViewById(R.id.weather05_tips);
        type6Tv = (TextView) two_page.findViewById(R.id.weather06_tips);

        relativeLayout =  (RelativeLayout)findViewById(R.id.title);
        linearLayout = (LinearLayout)findViewById(R.id.mainLayout);

        //获取默认保存的天气信息
        preUpdateWeather();

        isrunning = true;
        updateAsyn task = new updateAsyn();
        task.execute();

    }

    //更新默认选择城市的天气信息
    private void preUpdateWeather(){

        Log.d(TAG,"preudateweather()");

        //config文件的cityCode中保存用户选择的城市编码
        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        //默认城市代码为北京
        cityCode=sharedPreferences.getString("cityCode","101010100");

        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            Log.d(TAG, "网络OK" );
            //更新天气信息
            queryWeatherCode(cityCode);

        } else {
            Log.d(TAG, "网络挂了");
            Toast.makeText(MainActivity.this, "网络连接失败，请检查网络!", Toast.LENGTH_LONG).show();
            //初始化UI
            city_name_Tv.setText("N/A");
            cityTv.setText("N/A");
            timeTv.setText("N/A");
            humidityTv.setText("N/A");
            nowtemperatureTv.setText("N/A");
            pmDataTv.setText("N/A");
            pmQualityTv.setText("N/A");
            weekTv.setText("N/A");
            temperatureTv.setText("N/A");
            temperatureTv2.setText("N/A");
            temperatureTv3.setText("N/A");
            temperatureTv4.setText("N/A");
            temperatureTv5.setText("N/A");
            temperatureTv6.setText("N/A");

            climateTv.setText("N/A");
            windTv.setText("N/A");
        }
    }


    /**
     * 更新天气信息到UI控件（从todayweather）
     *
     */

    void updateTodayWeather(TodayWeather todayWeather){
        if(todayWeather.getCity()==null){
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putString("cityCode",currentCityCode);//保存选择的城市
            editor.commit();
            Toast.makeText(MainActivity.this, "未找到该城市天气信息！", Toast.LENGTH_SHORT).show();
        }else {
            currentCityCode = selectCityCode;

            city_name_Tv.setText(todayWeather.getCity()+"天气");
            cityTv.setText(todayWeather.getCity());
            timeTv.setText(todayWeather.getUpdatetime()+ "发布");
            humidityTv.setText("湿度:"+todayWeather.getShidu());
            nowtemperatureTv.setText("当前温度:"+todayWeather.getWendu()+"°C");
            pmDataTv.setText(todayWeather.getPm25());
            pmQualityTv.setText(todayWeather.getQuality());
            weekTv.setText(todayWeather.getDate());
            temperatureTv.setText(todayWeather.getLow()+"~"+todayWeather.getHigh());
            temperatureTv1.setText(todayWeather.getLow1()+"~"+todayWeather.getHigh1());
            temperatureTv2.setText(todayWeather.getLow2()+"~"+todayWeather.getHigh2());
            temperatureTv3.setText(todayWeather.getLow3()+"~"+todayWeather.getHigh3());
            temperatureTv4.setText(todayWeather.getLow4()+"~"+todayWeather.getHigh4());
            temperatureTv5.setText(todayWeather.getLow5()+"~"+todayWeather.getHigh5());
            temperatureTv6.setText(todayWeather.getLow6()+"~"+todayWeather.getHigh6());
            climateTv.setText(todayWeather.getType());
            type1Tv.setText(todayWeather.getDate1() + " " + todayWeather.getType1());
            type2Tv.setText(todayWeather.getDate2() + " " + todayWeather.getType2());
            type3Tv.setText(todayWeather.getDate3() + " " + todayWeather.getType3());
            type4Tv.setText(todayWeather.getDate4() + " " + todayWeather.getType4());
            type5Tv.setText(todayWeather.getDate5() + " " + todayWeather.getType5());
            type6Tv.setText(todayWeather.getDate6() + " " + todayWeather.getType6());
            windTv.setText("风力:"+todayWeather.getFengli());
            updateWeatherImg(todayWeather.getType());

            if(todayWeather.getPm25()==null){
                pm2_5.setVisibility(View.INVISIBLE);
                pmImg.setVisibility(View.INVISIBLE);
            }else{
                pm2_5.setVisibility(View.VISIBLE);
                pmImg.setVisibility(View.VISIBLE);
                updatePm25Img(todayWeather.getPm25());
            }

            //根据天气状况显示不同背景（用文字反射拼音）
            String typeBgImg = "bg_day_" + PinYin.converterToSpell(todayWeather.getType());
            Class aClass = R.drawable.class;
            int typeBgId = -1;
            try {
                Field field = aClass.getField(typeBgImg);
                Object value = field.get(new Integer(0));
                typeBgId = (int) value;
            } catch (Exception e) {
                if (-1 == typeBgId)
                    typeBgId = R.drawable.bg_day_qing;
            } finally {
                Drawable drawable = getResources().getDrawable(typeBgId);
                linearLayout.setBackground(drawable);
            }

            //根据天气状况设置近6天的天气图标(用文字反射拼音)
            String type1Img = "biz_plugin_weather_" + PinYin.converterToSpell(todayWeather.getType1());
            String type2Img = "biz_plugin_weather_" + PinYin.converterToSpell(todayWeather.getType2());
            String type3Img = "biz_plugin_weather_" + PinYin.converterToSpell(todayWeather.getType3());
            String type4Img = "biz_plugin_weather_" + PinYin.converterToSpell(todayWeather.getType4());
            String type5Img = "biz_plugin_weather_" + PinYin.converterToSpell(todayWeather.getType5());
            String type6Img = "biz_plugin_weather_" + PinYin.converterToSpell(todayWeather.getType6());

            Class bClass = R.drawable.class;
            int type1Id = -1;
            int type2Id = -1;
            int type3Id = -1;
            int type4Id = -1;
            int type5Id = -1;
            int type6Id = -1;
            try {
                Field field = aClass.getField(type1Img);
                Object value = field.get(new Integer(0));
                type1Id = (int) value;
                Field field2 = aClass.getField(type2Img);
                Object value2 = field2.get(new Integer(0));
                type2Id = (int) value2;
                Field field3 = aClass.getField(type3Img);
                Object value3 = field3.get(new Integer(0));
                type3Id = (int) value3;
                Field field4 = aClass.getField(type4Img);
                Object value4 = field4.get(new Integer(0));
                type4Id = (int) value4;
                Field field5 = aClass.getField(type5Img);
                Object value5 = field5.get(new Integer(0));
                type5Id = (int) value5;
                Field field6 = aClass.getField(type6Img);
                Object value6 = field6.get(new Integer(0));
                type6Id = (int) value6;

            } catch (Exception e) {
                if (-1 == type1Id & -1 == type2Id & -1 == type3Id & -1 == type4Id & -1 == type5Id & -1 == type6Id) {
                    type1Id = R.drawable.biz_plugin_weather_qing;
                    type2Id = R.drawable.biz_plugin_weather_qing;
                    type3Id = R.drawable.biz_plugin_weather_qing;
                    type4Id = R.drawable.biz_plugin_weather_qing;
                    type5Id = R.drawable.biz_plugin_weather_qing;
                    type6Id = R.drawable.biz_plugin_weather_qing;
                }
            } finally {
                //weatherImg为明日天气情况的图标
                Drawable drawableTypeImg1 = getResources().getDrawable(type1Id);
                weatherImg1.setImageDrawable(drawableTypeImg1);
                //weatherImg为后日天气情况的图标
                Drawable drawableTypeImg2 = getResources().getDrawable(type2Id);
                weatherImg2.setImageDrawable(drawableTypeImg2);
                //weatherImg为第三日天气情况的图标
                Drawable drawableTypeImg3 = getResources().getDrawable(type3Id);
                weatherImg3.setImageDrawable(drawableTypeImg3);
                Drawable drawableTypeImg4 = getResources().getDrawable(type4Id);
                weatherImg4.setImageDrawable(drawableTypeImg4);
                Drawable drawableTypeImg5 = getResources().getDrawable(type5Id);
                weatherImg5.setImageDrawable(drawableTypeImg5);
                Drawable drawableTypeImg6 = getResources().getDrawable(type6Id);
                weatherImg6.setImageDrawable(drawableTypeImg6);

            }
        }
    }



    //更新天气图标
    private void updateWeatherImg(String weather) {
        switch (weather) {
            case "暴雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                break;
            case "暴雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                break;
            case "大暴雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                break;
            case "大雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_daxue);
                break;
            case "大雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_dayu);
                break;
            case "多云":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_yin);
                break;
            case "雷阵雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                break;
            case "雷阵雨冰雹":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                break;
            case "晴":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
                break;
            case "沙尘暴":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                break;
            case "特大暴雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                break;
            case "雾":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_wu);
                break;
            case "小雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                break;
            case "小雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                break;
            case "阴":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                break;
            case "雨夹雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                break;
            case "阵雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                break;
            case "阵雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                break;
            case "中雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                break;
            case "中雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                break;
            default:
                break;
        }
    }

    //更新pm2.5图标
    private void updatePm25Img(String pm25) {
        int pm = Integer.parseInt(pm25);
        if(pm>=0 && pm<=50){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
        }
        else if(pm>=51 && pm<=100){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
        }
        else if(pm>=101 && pm<=150){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
        }
        else if(pm>=151 && pm<=200){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
        }
        else if(pm>=201 && pm<=300){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
        }
    }


    /**
     * 负责MainActivity和LocationService进行通信的类
     */
    class MyServiceConn implements ServiceConnection {


        // 服务被绑定成功之后执行
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // IBinder service为onBind方法返回的Service实例
            binder = (LocationService.MyBinder) service;
            binder.getService().setDataCallback(new LocationService.DataCallback() {
                //执行回调函数
                @Override
                public void dataChanged(String str) {
                    Message msg = new Message();
/*                    Bundle bundle = new Bundle();
                    bundle.putString("str", str);
                    msg.setData(bundle);*/
                    msg.obj = str;
                    msg.what = UPDATE_LOCATION;
                    //发送通知
                    mHandler.sendMessage(msg);
                }
            });
        }

        // 服务奔溃或者被杀掉执行
        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder = null;
        }
    }

    private class updateAsyn extends AsyncTask<Void, Integer, Void>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            //uithread展示任务执行情况
            //Toast.makeText(MainActivity.this,"时间过去了一分钟", Toast.LENGTH_SHORT).show();
            SharedPreferences pref = getSharedPreferences("config", MODE_PRIVATE);
            String cityCode = pref.getString("cityCode", "101010100");
            queryWeatherCode(cityCode);
            AppWidgetManager manager = AppWidgetManager.getInstance(MainActivity.this);
            //将更新widget的活动放在这里面
            WeatherWidget.todayWeather = todayweather;
            RemoteViews views = WeatherWidget.updateRemoteViews(MainActivity.this);
            if(views!=null)
            {
                manager.updateAppWidget(appWidgetIds,views);
            }else
            {
                Log.d("myinfo","更新失败");
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while(isrunning){
                try {
                    Thread.sleep(360000);//一个小时自动更新一次
                    //Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                publishProgress(null);
            }
            return null;
        }
    }

}

