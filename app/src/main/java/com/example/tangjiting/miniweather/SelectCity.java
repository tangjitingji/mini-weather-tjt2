package com.example.tangjiting.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tangjiting.app.MyApplication;
import com.example.tangjiting.bean.City;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tangjiting on 2017/10/18.
 */

public class SelectCity extends Activity{

    private ImageView mBackBtn;
    private ListView mListView;
    private List<City> myList;
    private List<City> allCities;
    private ArrayList<City> filterDataList;
    private TextView titleName;
    private ClearEditText eSearch;
    private SelectCityAdapter adapter;

    private Button btn_city_00,btn_city_01,btn_city_02,btn_city_03,btn_city_10,btn_city_11,btn_city_12,btn_city_13,btn_city_20,btn_city_21,btn_city_22,btn_city_23;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_city);

        Intent intent=this.getIntent();
        String cityName=intent.getStringExtra("cityName");

        //标题当前城市
        titleName=(TextView)findViewById(R.id.title_name);
        titleName.setText("当前城市："+cityName);

        //返回按钮
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
                SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
                String cityCode = sharedPreferences.getString("cityCode", "101010100");
                back(cityCode);
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
        adapter = new SelectCityAdapter();
        mListView = (ListView)findViewById(R.id.list_view);
//        adapter=new SimpleAdapter(this, (List<? extends Map<String, ?>>) getdata(),R.layout.listview_item,
//                new String[]{"cityName","cityCode"},
//                new int[] {R.id.cityName,R.id.cityCode});
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new ItemClickEvent());


        //监听搜索框文本
        eSearch=(ClearEditText)findViewById(R.id.search_edit);
        eSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                ////这个方法被调用，说明在s字符串中，从start位置开始的count个字符即将被长度为after的新文本所取代。在这个方法里面改变s，会报错。

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //搜索匹配
                //这个方法被调用，说明在s字符串中，从start位置开始的count个字符刚刚取代了长度为before的旧文本。在这个方法里面改变s，会报错。
                filterData(s.toString());
                mListView.setAdapter(adapter);
//                adap  ter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //这个方法被调用，那么说明s字符串的某个地方已经被改变。

                String txt = eSearch.getText().toString();
                Pattern p=Pattern.compile("[A-Z]");
                Matcher m=p.matcher(txt);
                if(!m.matches()){
                    Toast.makeText(SelectCity.this,"请输入大写字母", Toast.LENGTH_SHORT).show();
                }

            }

            //        根据输入框中的值来过滤数据并更新ListView

            private void filterData(String filterStr){
                filterDataList = new ArrayList<City>();
                Log.d("filters",filterStr);
                if (TextUtils.isEmpty(filterStr)){
                    for (City city: allCities){
                        filterDataList.add(city);
                    }
                }else {
                    filterDataList.clear();
                    for (City city: allCities){
                        if (city.getAllPY().indexOf(filterStr.toString())!= -1|| city.getAllFirstPY().indexOf(filterStr.toString())!= -1){
                            filterDataList.add(city);
                        }
                    }
                }

                //根据a-z进行排序
                adapter.updateListView(filterDataList);
            }

        });
    }

    //继承OnItemClickListener，当子项目被点击的时候触发
    private final class ItemClickEvent implements AdapterView.OnItemClickListener {

//        adapter = new SelectCityAdapter();
        //将选择的城市编码，传给主界面
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


//                editor.putString("main_city_code",((City)displayCityAdapter.getItem(position)).getNumber());
//                editor.commit();
                Intent intent = new Intent();
                intent.putExtra("cityCode",((City)adapter.getItem(position)).getNumber());
                setResult(RESULT_OK,intent);
                finish();

        }
    }

    //将选择的城市编码，回传,并跳转到主界面
    public void back(String select_city_code){
        Intent i2 = new Intent();
        i2.putExtra("cityCode",select_city_code);
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

    class SelectCityAdapter extends BaseAdapter {
        private List<City> filterCityList;

        public SelectCityAdapter(){
            allCities = MyApplication.getInstance().getmCityList();
            filterCityList = allCities;
        }

        public void updateListView(ArrayList<City> newfilter){
            filterCityList  = newfilter;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return filterCityList.size();
        }

        @Override
        public Object getItem(int position) {
            return filterCityList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            City city = filterCityList.get(position);
            View view  = View.inflate(SelectCity.this,R.layout.listview_item,null);
            TextView cityName = (TextView) view.findViewById(R.id.cityName);
            TextView cityCode = (TextView) view.findViewById(R.id.cityCode);
            cityName.setText(city.getCity()+"，"+ city.getProvince() + "（" + city.getAllPY()+"）");
            cityCode.setText(city.getNumber());
            return view;
        }
    }




}
