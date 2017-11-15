package com.example.tangjiting.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.tangjiting.app.MyApplication;
import com.example.tangjiting.bean.City;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tangjiting on 2017/10/18.
 */

public class SelectCity extends Activity{

    private ImageView mBackBtn;
    private ListView mListView;
    private List<City> myList;
    private TextView titleName;
    private EditText eSearch;
    private SimpleAdapter adapter;

    private Button btn_city_00,btn_city_01,btn_city_02,btn_city_03,btn_city_10,btn_city_11,btn_city_12,btn_city_13,btn_city_20,btn_city_21,btn_city_22,btn_city_23;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_city);

        eSearch=(EditText)findViewById(R.id.search_edit);

        Intent intent=this.getIntent();
        String cityName=intent.getStringExtra("cityName");

        //标题当前城市
        titleName=(TextView)findViewById(R.id.title_name);
        titleName.setText("当前城市："+cityName);

        mBackBtn = (ImageView)findViewById(R.id.title_back);
        //mBackBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                finish();
            }
        });

        //热门城市
        btn_city_00 = (Button) findViewById(R.id.btn_city_00);
        btn_city_00.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //定位当前城市
            }
        });
        //北京101010100
        btn_city_01 = (Button) findViewById(R.id.btn_city_01);
        btn_city_01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back("101010100");
            }
        });
        //上海101020100
        btn_city_02 = (Button) findViewById(R.id.btn_city_02);
        btn_city_02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back("101020100");
            }
        });
        //广州101280101
        btn_city_03 = (Button) findViewById(R.id.btn_city_03);
        btn_city_03.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back("101280101");
            }
        });
        //深圳101280601
        btn_city_10 = (Button) findViewById(R.id.btn_city_10);
        btn_city_10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back("101280601");
            }
        });
        //长春101060101
        btn_city_11 = (Button) findViewById(R.id.btn_city_11);
        btn_city_11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back("101060101");
            }
        });
        //武汉101200101
        btn_city_12 = (Button) findViewById(R.id.btn_city_12);
        btn_city_12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back("101200101");
            }
        });
        //兰州101160101
        btn_city_13 = (Button) findViewById(R.id.btn_city_13);
        btn_city_13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back("101160101");
            }
        });

        //长沙101250101
        btn_city_20 = (Button) findViewById(R.id.btn_city_20);
        btn_city_20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back("101250101");
            }
        });
        //合肥 101220101
        btn_city_21 = (Button) findViewById(R.id.btn_city_21);
        btn_city_21.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back("101220101");
            }
        });
        //安庆101220601
        btn_city_22 = (Button) findViewById(R.id.btn_city_22);
        btn_city_22.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back("101220601");
            }
        });
        //桐城101220609
        btn_city_23 = (Button) findViewById(R.id.btn_city_23);
        btn_city_23.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back("101220609");
            }
        });

        //显示城市列表
        mListView = (ListView)findViewById(R.id.list_view);
        adapter=new SimpleAdapter(this, (List<? extends Map<String, ?>>) getdata(),R.layout.listview_item,
                new String[]{"cityName","cityCode"},
                new int[] {R.id.cityName,R.id.cityCode});
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new ItemClickEvent());


        //监听搜索框文本
        eSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //搜索匹配
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    //继承OnItemClickListener，当子项目被点击的时候触发
    private final class ItemClickEvent implements AdapterView.OnItemClickListener {

        //将选择的城市编码，传给主界面
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            HashMap<String,Object> city = (HashMap<String,Object>)mListView.getItemAtPosition(position);
            String select_city_code=city.get("cityCode").toString();
            Log.d("选择的城市编码：",select_city_code);
            back(select_city_code);

        }
    }

    //将选择的城市编码，回传,并跳转到主界面
    public void back(String select_city_code){
        Intent i2 = new Intent();
        i2.putExtra("select_city_code",select_city_code);
        setResult(RESULT_OK,i2);
        finish();
    }

    //列表显示的内容
    private List<Map<String, Object>> getdata() {
        myList= new ArrayList<City>();
        //获取全部城市列表（从数据库中）
        MyApplication myApplication = MyApplication.getInstance();
        myList = myApplication.getmCityList();

        String []cityName=new String[myList.size()];
        String []cityCode=new String[myList.size()];
        int i=0;
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        for(City city : myList) {
            Map<String, Object> map = new HashMap<String, Object>();
            cityName[i] = city.getCity();
            cityCode[i] = city.getNumber();
            map.put("cityName",cityName[i] +"，"+ city.getProvince() + "（" + city.getAllPY()+"）");
            map.put("cityCode",cityCode[i]);
            list.add(map);
            i++;
        }
        return list;
    }

    /*
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.title_back:
                Intent i = new Intent();
                //长春
                i.putExtra("cityCode","101060101");
                //桐城 报错了！强行退出
                //i.putExtra("cityCode","101220609");
                //安庆 报错了！强行退出
                //i.putExtra("cityCode","101220601");
                setResult(RESULT_OK,i);
                finish();
                break;
            default:
                break;
        }
     }
     */




}
