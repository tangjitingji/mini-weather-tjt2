package com.example.tangjiting.miniweather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
 * Created by tangjiting on 2017/11/29.
 */

public class FutureWeather extends AppCompatActivity {

    private String TAG = "FutureWeather";
    private static final int UPDATE_TODAY_WEATHER = 1;
    //近三日天气信息
    private ImageView weatherImg,weatherImg1, weatherImg2,weatherImg3,pmImg;
    private TextView temperatureTv1, temperatureTv2,temperatureTv3;//最高气温最低气温
    private TextView type1Tv,type2Tv,type3Tv;//天气状况，如，今日 晴

    private Context context2 = this;
    private ImageView[] dots2;//导航小圆点
    private int[] ids2 = {R.id.weatheriv1, R.id.weatheriv2};//小圆点的iamgeview值
    private ViewPagerAdapter vpAdapter2;
    private ViewPager vp2;
    private List<View> views2 = new ArrayList<>();

    //城市编码
    String cityCode,currentCityCode,selectCityCode;

    TodayWeather todayWeather = null;

    //通过消息机制，将解析的天气对象，通过消息发送给主线程，主线程接收到消息数据后，调用updateTodayWeather函数，更新UI界面上的数据
    //处理天气更新的handler
    //更新UI控件
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                default:
                    break; }
        } };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weatherpage1);//关联布局
        //初始化UI控件
        initView();
    }

    //返回主界面时，传递城市代码数据
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        if(requestCode == 1 && resultCode== RESULT_OK){
            //获取用户选择的城市编码
            String select_city_code = data.getStringExtra("select_city_code");
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
//            Toast.makeText(MainActivity.this,"更新成功啦!",Toast.LENGTH_SHORT).show();
            if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
//                Log.d("myWeather", "网络正常");
//                queryWeatherCode(select_city_code);
                Toast.makeText(FutureWeather.this,"更新成功啦!",Toast.LENGTH_SHORT).show();
            }
            else {
//                Log.d("myWeather", "网络跪了");
                Toast.makeText(FutureWeather.this,"网络跪了!",Toast.LENGTH_LONG).show();
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

    //修改解析函数
    private TodayWeather parseXML(String xmldata){
        TodayWeather todayWeather = null;
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
                            else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
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
                            }else if (xmlPullParser.getName().equals("high") && highCount == 1) {//明日最高温度
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh2(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }else if (xmlPullParser.getName().equals("low") && lowCount == 1) {//明日最低温度
                                eventType = xmlPullParser.next();
                                todayWeather.setLow2(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 2) {//后日最高温度
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh3(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }else if (xmlPullParser.getName().equals("low") && lowCount == 2) {//后日最低温度
                                eventType = xmlPullParser.next();
                                todayWeather.setLow3(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }
                            else if (xmlPullParser.getName().equals("type") && typeCount == 0) {//今日天气状况
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                typeCount++;
                            }
                            else if (xmlPullParser.getName().equals("type") && typeCount == 1) {//明日天气状况
                                eventType = xmlPullParser.next();
                                todayWeather.setType2(xmlPullParser.getText());
                                typeCount++;
                            }
                            else if (xmlPullParser.getName().equals("type") && typeCount == 2) {//后日天气状况
                                eventType = xmlPullParser.next();
                                todayWeather.setType3(xmlPullParser.getText());
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

        return todayWeather;
    }

    //初始化控件内容
    void initView() {

        //以下是初始化viewPager
        LayoutInflater inflater = LayoutInflater.from(context2);
        View one_page = inflater.inflate(R.layout.weatherpage1, null);
        View two_page = inflater.inflate(R.layout.weatherpage1, null);
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

        //天气状况图片(未来3天中的今日)
        weatherImg1 = (ImageView) findViewById(R.id.weather01_pic);//今日
        weatherImg2 = (ImageView) findViewById(R.id.weather02_pic);//明日
        weatherImg3 = (ImageView) findViewById(R.id.weather03_pic);//后日
        //温度范围(未来3天中的今日)
        temperatureTv1 = (TextView) findViewById(R.id.weather01);//今日
        temperatureTv2 = (TextView) findViewById(R.id.weather02);//明日
        temperatureTv3 = (TextView) findViewById(R.id.weather03);//后日

        //近3天天气状况，如，今日 晴
        type1Tv = (TextView) findViewById(R.id.weather01_tips);//今日
        type2Tv = (TextView) findViewById(R.id.weather02_tips);//明日
        type3Tv = (TextView) findViewById(R.id.weather03_tips);//后日


        //获取默认保存的天气信息
        preUpdateWeather();
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
            Toast.makeText(FutureWeather.this, "网络连接失败，请检查网络!", Toast.LENGTH_LONG).show();
            //初始化UI

            temperatureTv1.setText("N/A");
            temperatureTv2.setText("N/A");
            temperatureTv3.setText("N/A");

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
            Toast.makeText(FutureWeather.this, "未找到该城市天气信息！", Toast.LENGTH_SHORT).show();
        }else {
            currentCityCode = selectCityCode;


            temperatureTv1.setText(todayWeather.getLow()+"~"+todayWeather.getHigh());
            temperatureTv2.setText(todayWeather.getLow2()+"~"+todayWeather.getHigh2());
            temperatureTv3.setText(todayWeather.getLow3()+"~"+todayWeather.getHigh3());
            type1Tv.setText("今日 " + todayWeather.getType());
            type2Tv.setText("明日 " +todayWeather.getType2());
            type3Tv.setText("后日 " +todayWeather.getType3());
            updateWeatherImg(todayWeather.getType());

            Class aClass = R.drawable.class;
            //根据天气状况设置近3天的天气图标(用文字反射拼音)
            String type1Img = "biz_plugin_weather_" + PinYin.converterToSpell(todayWeather.getType());
            String type2Img = "biz_plugin_weather_" + PinYin.converterToSpell(todayWeather.getType2());
            String type3Img = "biz_plugin_weather_" + PinYin.converterToSpell(todayWeather.getType3());
            Class bClass = R.drawable.class;
            int type1Id = -1;
            int type2Id = -1;
            int type3Id = -1;
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
            } catch (Exception e) {
                if (-1 == type1Id & -1 == type2Id & -1 == type3Id)
                    type1Id = R.drawable.biz_plugin_weather_qing;
                type2Id = R.drawable.biz_plugin_weather_qing;
                type3Id = R.drawable.biz_plugin_weather_qing;
            } finally {
                //weatherImg为今日天气情况的图标
                Drawable drawableTypeImg = getResources().getDrawable(type1Id);
                weatherImg1.setImageDrawable(drawableTypeImg);
                //weatherImg为明日天气情况的图标
                Drawable drawableTypeImg2 = getResources().getDrawable(type2Id);
                weatherImg2.setImageDrawable(drawableTypeImg2);
                //weatherImg为后日天气情况的图标
                Drawable drawableTypeImg3 = getResources().getDrawable(type3Id);
                weatherImg3.setImageDrawable(drawableTypeImg3);
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

}


