package pku.ss.cyy.myweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.zip.GZIPInputStream;

import pku.ss.cyy.bean.TodayWeather;
import pku.ss.cyy.util.NetUtil;


public class MainActivity extends Activity implements View.OnClickListener {
    private static final int UPDATE_TODAY_WEATHER = 1;
    private static final int UPDATE_NO_DATA = 2;
    private ImageView mUpdateBtn, mCitySelect;
    private ProgressBar mUpdateProgress;
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv, temperatureTv,
        climateTv, windTv, titleCityTv;
    private ImageView weatherImg, pmImg;
    private static final String TAG = "MyAPP";
    SharedPreferences sharedPreferences;
    String cityCode, cityName;
    public static final int RESULT_CITY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"MainActivity->OnCreate");

        setContentView(R.layout.weather_info);

        mUpdateProgress = (ProgressBar) findViewById(R.id.title_update_progress);
        mUpdateBtn = (ImageView)findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);

        sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        cityCode = sharedPreferences.getString("main_city_code","101010100");

        mCitySelect = (ImageView)findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);

        Log.d("myWeather",cityCode);
        if(NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE) {
            Log.d("MyWeather", "网络正常！");
            queryWeatherCode(cityCode);
//            mUpdateBtn.setVisibility(View.VISIBLE);
//            mUpdateProgress.setVisibility(View.GONE);
        }
        else {
            Log.d("MyWeather", "网络错误！");
            mUpdateBtn.setVisibility(View.VISIBLE);
            mUpdateProgress.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(),"网络未连接！",Toast.LENGTH_LONG).show();
        }

        initView();
    }

    void initView() {
        cityTv = (TextView)findViewById(R.id.city);
        timeTv = (TextView)findViewById(R.id.publish_time);
        humidityTv = (TextView)findViewById(R.id.humidity);
        weekTv = (TextView)findViewById(R.id.week_today);
        pmDataTv = (TextView)findViewById(R.id.pm_data);
        pmQualityTv = (TextView)findViewById(R.id.pm2_5_quality);
        temperatureTv = (TextView)findViewById(R.id.temperature);
        climateTv = (TextView)findViewById(R.id.climate);
        windTv = (TextView)findViewById(R.id.wind);
        pmImg = (ImageView)findViewById(R.id.pm2_5_img);
        weatherImg = (ImageView)findViewById(R.id.weather_img);
        titleCityTv = (TextView) findViewById(R.id.title_city_name);
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");
        titleCityTv.setText(sharedPreferences.getString("main_city_name_home", "北京天气"));
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.title_update_btn) {
            Log.d("myWeather",cityCode);
            mUpdateBtn.setVisibility(View.INVISIBLE);
            mUpdateProgress.setVisibility(View.VISIBLE);
            if(NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE) {
                Log.d("MyWeather", "网络正常！");
                queryWeatherCode(cityCode);
            }
            else {
                Log.d("MyWeather", "网络错误！");
                mUpdateBtn.setVisibility(View.VISIBLE);
                mUpdateProgress.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(),"网络未连接！",Toast.LENGTH_LONG).show();
            }
        }
        if(v.getId() == R.id.title_city_manager) {
            Intent intent = new Intent(getApplicationContext(),SelectCity.class);
            startActivityForResult(intent, RESULT_CITY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_CITY) {
            cityCode = data.getStringExtra("code");
            cityName = data.getStringExtra("city");
            mUpdateBtn.setVisibility(View.INVISIBLE);
            mUpdateProgress.setVisibility(View.VISIBLE);
            if(NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE) {
                Log.d("MyWeather", "网络正常！");
                queryWeatherCode(cityCode);
            }
            else {
                Log.d("MyWeather", "网络错误！");
                mUpdateBtn.setVisibility(View.VISIBLE);
                mUpdateProgress.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(),"网络未连接！",Toast.LENGTH_LONG).show();
            }
        }
    }

    void updateTodayWeather(TodayWeather todayWeather) {
        //Log.d("MyWeather", todayWeather.toString());
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime() + "发布");
        humidityTv.setText("湿度：" + todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getLow().substring(3,todayWeather.getLow().length()) + " ~ " + todayWeather.getHigh().substring(3,todayWeather.getHigh().length()));
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力" + todayWeather.getFengli());
        titleCityTv.setText(todayWeather.getCity()+"天气");

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("main_city_name_home",todayWeather.getCity()+"天气");
        editor.commit();

        switch (todayWeather.getType()) {
            case "晴":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
                break;
            case "阴":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_yin);
                break;
            case "雾":
            case "霾":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_wu);
                break;
            case "多云":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                break;
            case "沙尘暴":
            case "扬沙":
            case "浮尘":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                break;
            case "雷阵雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                break;
            case "雷阵雨冰雹":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                break;
            case "雨夹雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                break;
            case "阵雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                break;
            case "小雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                break;
            case "中雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                break;
            case "大雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_daxue);
                break;
            case "暴雪":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                break;
            case "阵雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                break;
            case "小雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                break;
            case "中雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                break;
            case "大雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_dayu);
                break;
            case "暴雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                break;
            case "大暴雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                break;
            case "特大暴雨":
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                break;
            default:
                weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
                break;
        }

        if (todayWeather.getPm25()!=null) {
            int pm = Integer.parseInt(todayWeather.getPm25());
            if (pm > 200) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_greater_300);
            } else if (pm > 200) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
            } else if (pm > 150) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
            } else if (pm > 100) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
            } else if (pm > 50) {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
            } else {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
            }
        }
        else {
            pmDataTv.setText("0");
            pmQualityTv.setText("无");
        }
        mUpdateBtn.setVisibility(View.VISIBLE);
        mUpdateProgress.setVisibility(View.GONE);
        Toast.makeText(getApplicationContext(),"更新成功！",Toast.LENGTH_SHORT).show();
    }

    void updateTodayWeather() {
        cityTv.setText("无数据");
        titleCityTv.setText(sharedPreferences.getString("main_city_name_home", "北京天气"));
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");

        weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
        pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);

        mUpdateBtn.setVisibility(View.VISIBLE);
        mUpdateProgress.setVisibility(View.GONE);
        Toast.makeText(getApplicationContext(),"更新失败：无数据！",Toast.LENGTH_SHORT).show();
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather)msg.obj);
                    break;
                case UPDATE_NO_DATA:
                    updateTodayWeather();
                    break;
                default:
                    break;
            }
        }
    };

    private void queryWeatherCode(String cityCode) {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("MyWeather", address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(address);
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    if(httpResponse.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = httpResponse.getEntity();

                        InputStream inputStream = entity.getContent();
                        inputStream = new GZIPInputStream(inputStream);

                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder response = new StringBuilder();
                        String str;
                        while ((str=reader.readLine()) != null) {
                            response.append(str);
                        }
                        String responseStr = response.toString();
                        Log.d("myWeather", responseStr);
                        TodayWeather todayWeather = parseXML(responseStr);
                        if (todayWeather != null) {
                            Log.d("MyWeather", todayWeather.toString());
                            Message msg = new Message();
                            msg.what = UPDATE_TODAY_WEATHER;
                            msg.obj = todayWeather;
                            handler.sendMessage(msg);
                        }
                        else {
                            Message msg = new Message();
                            msg.what = UPDATE_NO_DATA;
                            handler.sendMessage(msg);
                        }
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private TodayWeather parseXML(String xmlData) {
        TodayWeather todayWeather = null;
        try {
            int fengxiangCount = 0;
            int fengliCount = 0;
            int dateCount = 0;
            int highCount = 0;
            int lowCount = 0;
            int typeCount = 0;
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlData));
            int eventType = xmlPullParser.getEventType();
            Log.d("MyWeather","parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp")) {
                            todayWeather = new TodayWeather();
                        }
                        if(xmlPullParser.getName().equals("error")) {
                            eventType = xmlPullParser.next();
                            return null;
                        }
                        if (todayWeather != null) {
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                                Log.d("MyWeather", "city: " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                                Log.d("MyWeather","updatetime: "+xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                                Log.d("MyWeather","shidu: "+xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                                Log.d("MyWeather","wendu: "+xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                                Log.d("MyWeather","pm2.5: "+xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                                Log.d("MyWeather","quality: "+xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                Log.d("MyWeather", "fengxiang: " + xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                Log.d("MyWeather","fengli: "+xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                Log.d("MyWeather","date: "+xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText());
                                Log.d("MyWeather","high: "+xmlPullParser.getText());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText());
                                Log.d("MyWeather","low: "+xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                Log.d("MyWeather","type: "+xmlPullParser.getText());
                                typeCount++;
                            }
                        }

                        break;

                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = xmlPullParser.next();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return todayWeather;
    }
}
