package cn.edu.pku.lxy.myweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import cn.edu.pku.lxy.bean.TodayWeather;
import cn.edu.pku.lxy.util.NetUtil;

public class MainActivity extends Activity implements View.OnClickListener{
    private static final int UPDATE_TODAY_WEATHER=1;
    private Handler mHandler=new Handler(){
        public void handleMessage(android.os.Message msg){
            switch (msg.what){
                case UPDATE_TODAY_WEATHER:updateTodayWeather((TodayWeather)msg.obj);break;
                default:break;
            }
        }
    };
    private ImageView mUpadateBtn;
    private ImageView mCitySelect;

    private TextView cityTv,timeTv,temperatureTv,fengliTv,humidityTv,fengxiangTv,pmDataTv,
            pmQualityTv,weekTv,climateTv,city_nameTv,suggestTv;
    private ImageView weatherImg,pmImg;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);
        mUpadateBtn=(ImageView)findViewById(R.id.title_update_btn);
        mUpadateBtn.setOnClickListener(this);
        mCitySelect=(ImageView)findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);
        initView();
    }
    @Override
    public void onClick(View v){
        if (v.getId()==R.id.title_city_manager){
            Intent i=new Intent(this,SelectCity.class);
            startActivityForResult(i,1);
        }
        if (v.getId()==R.id.title_update_btn){
            SharedPreferences sp=getSharedPreferences("config",MODE_PRIVATE);
            String cityCode=sp.getString("main_city_code","101010100");
            if (NetUtil.getNetworkState(this)!=NetUtil.NETWORK_NONE) queryWeatherCode(cityCode);
            else Toast.makeText(MainActivity.this,"网络错误！",Toast.LENGTH_LONG).show();
        }
    }
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode==1&&resultCode==RESULT_OK){
            String newCityCode=data.getStringExtra("cityCode");
            if(NetUtil.getNetworkState(this)!=NetUtil.NETWORK_NONE){
                queryWeatherCode(newCityCode);
            }else{ Toast.makeText(MainActivity.this,"网络挂了",Toast.LENGTH_LONG).show(); }
        }
    }
    private void queryWeatherCode(String cityCode){ //通过URL获得网络数据
        final String address="http://wthrcdn.etouch.cn/WeatherApi?citykey="+cityCode;
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con=null;
                TodayWeather todayWeather=null;
                try{
                    URL url=new URL(address);
                    con=(HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream i=con.getInputStream();
                    BufferedReader reader=new BufferedReader(new InputStreamReader(i));
                    StringBuilder response=new StringBuilder();
                    String str;
                    while ((str=reader.readLine())!=null){ response.append(str); }
                    String responseStr=response.toString();Log.d("weather",responseStr);
                    todayWeather=parseXML(responseStr);
                    if(todayWeather!=null) Log.d("weather",todayWeather.toString());
                    Message msg=new Message();
                    msg.what=UPDATE_TODAY_WEATHER;
                    msg.obj=todayWeather;
                    mHandler.sendMessage(msg);
                }catch (Exception e){ e.printStackTrace(); }finally { if(con!=null) con.disconnect(); }
            }
        }).start();
    }
    private TodayWeather parseXML(String xmldata) {
        TodayWeather todayWeather = null;
        int fengxiangCount = 0;
        int fengliCount = 0;
        int dataCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;
        int nameCount = 0;
        int valueCount = 0;
        int suggestCount = 0;
        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("resp")) {
                            todayWeather = new TodayWeather();
                        }
                        if (todayWeather != null) {
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date") && dataCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                dataCount++;
                            }else if(xmlPullParser.getName().equals("high")&&highCount==0){
                                eventType=xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }else if(xmlPullParser.getName().equals("low")&&lowCount==0){
                                eventType=xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }else if(xmlPullParser.getName().equals("type")&&typeCount==0){
                                eventType=xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                typeCount++;
                            }else if(xmlPullParser.getName().equals("suggest")){
                                eventType=xmlPullParser.next();
                                todayWeather.setSuggest(xmlPullParser.getText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) { e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }
        return todayWeather;
    }
    void initView(){
        city_nameTv=(TextView)findViewById(R.id.title_city_name);
        cityTv=(TextView)findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality
        );
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature
        );
        climateTv = (TextView) findViewById(R.id.climate);
        fengxiangTv=(TextView)findViewById(R.id.fengxiang);
        fengliTv = (TextView) findViewById(R.id.fengli);
        suggestTv=(TextView)findViewById(R.id.suggest);
        weatherImg = (ImageView) findViewById(R.id.weather_img);
        city_nameTv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        fengxiangTv.setText("N/A");
        fengliTv.setText("N/A");
        suggestTv.setText("N/A");
    }
    void updateTodayWeather(TodayWeather todayWeather){
        city_nameTv.setText(todayWeather.getCity()+"天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+"发布");
        humidityTv.setText("湿度："+todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getLow()+"~"+todayWeather.getHigh());
        climateTv.setText(todayWeather.getType());
        fengliTv.setText("风力："+todayWeather.getFengli());
        fengxiangTv.setText("风向："+todayWeather.getFengxiang());
        suggestTv.setText("  "+todayWeather.getSuggest()+"。");
    }
}
