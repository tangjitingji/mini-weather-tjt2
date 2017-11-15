package com.example.tangjiting.miniweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tangjiting.bean.TodayWeather;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by tangjiting on 2017/9/25.
 */

public class MainActivity extends AppCompatActivity{

    //Log信息标签
    private String TAG = "tjtWeather";

    private static final int UPDATE_TODAY_WEATHER = 1;

    private ImageView mUpdateBtn;

    private ImageView mCitySelect;

    //更新今日天气数据，定义相关的控件对象
    private TextView pm2_5,cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv, nowtemperatureTv, temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;

    //城市编码
    String cityCode,currentCityCode,selectCityCode;

    TodayWeather todayWeather = null;

    //通过消息机制，将解析的天气对象，通过消息发送给主线程，主线程接收到消息数据后，调用updateTodayWeather函数，更新UI界面上的数据
    //处理天气更新的handler
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
        setContentView(R.layout.weather_info);//关联布局

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
                i.putExtra("cityName","北京");//默认城市为北京
            }
            startActivityForResult(i,1);
        }
    });

        //初始化UI控件
        initView();
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

    //返回主界面时，传递城市代码数据
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        if(requestCode == 1 && resultCode== RESULT_OK){
            //获取用户选择的城市编码
            String select_city_code = data.getStringExtra("select_city_code");
            Log.d(TAG,"接收到的选择城市编码----------------->："+ select_city_code);

            if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
                Log.d("myWeather", "网络正常");
                queryWeatherCode(select_city_code);
            }
            else {
                Log.d("myWeather", "网络跪了");
                Toast.makeText(MainActivity.this,"网络跪了!",Toast.LENGTH_LONG).show();
            }

            //将用户选择的城市编码保存，用于下次访问自动更新天气信息
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putString("cityCode",select_city_code);
            editor.commit();

            //更新天气
            preUpdateWeather();

            //提示更新成功
            Toast.makeText(MainActivity.this,"更新成功啦!",Toast.LENGTH_SHORT).show();
        }

    }

    /** *
     * 从网络获取城市编码为cityCode的城市的天气信息
     * @param cityCode 城市编码（默认北京101010100）
     */
    private void queryWeatherCode(String cityCode)  {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather", address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con=null;
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
                            else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }
                            else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }
                            else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
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

        //获取默认保存的天气信息
        preUpdateWeather();

//        city_name_Tv.setText("N/A");
//        cityTv.setText("N/A");
//        timeTv.setText("N/A");
//        humidityTv.setText("N/A");
//        nowtemperatureTv.setText("N/A");
//        pmDataTv.setText("N/A");
//        pmQualityTv.setText("N/A");
//        weekTv.setText("N/A");
//        temperatureTv.setText("N/A");
//        climateTv.setText("N/A");
//        windTv.setText("N/A");
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
            climateTv.setText("N/A");
            windTv.setText("N/A");
        }
    }


//    void updateTodayWeather(TodayWeather todayWeather){
//
//        city_name_Tv.setText(todayWeather.getCity()+"天气");
//        cityTv.setText(todayWeather.getCity());
//        timeTv.setText(todayWeather.getUpdatetime()+ "发布");
//        humidityTv.setText("湿度:"+todayWeather.getShidu());
//        nowtemperatureTv.setText("当前温度:"+todayWeather.getWendu()+"°C");
//        pmDataTv.setText(todayWeather.getPm25());
//        pmQualityTv.setText(todayWeather.getQuality());
//        weekTv.setText(todayWeather.getDate());
//        temperatureTv.setText(todayWeather.getLow()+"~"+todayWeather.getHigh());
//        climateTv.setText(todayWeather.getType());
//        windTv.setText("风力:"+todayWeather.getFengli());
//        updateWeatherImg(todayWeather.getType());
//        updatePm25Img(todayWeather.getPm25());
//        Toast.makeText(MainActivity.this,"更新成功!",Toast.LENGTH_SHORT).show();
//    }

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
            climateTv.setText(todayWeather.getType());
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
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_duoyun);
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
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_yin);
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


