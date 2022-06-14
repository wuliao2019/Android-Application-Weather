package com.cqu.weather.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cqu.weather.R;
import com.cqu.weather.database.CityWeather;
import com.cqu.weather.database.DatabaseAction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

public class CityWeatherPage extends Fragment {
    static String apiURL = "http://apis.juhe.cn/simpleWeather/query";
    public static final int COMPLETED = -1;
    private final CityWeather cityWeather;
    private ProgressBar progressBar;
    private ImageView weatherIcon;
    private TextView temperature;
    private TextView temperature2;
    private TextView weather;
    private TextView power;
    private TextView direct;
    private TextView humidity;
    private TextView humidity2;
    private TextView aqi;
    private TextView aqi2;
    private TextView date;
    private List<TextView> dayT;
    private List<ImageView> dayIcon1;
    private List<ImageView> dayIcon2;
    private List<TextView> dayWeather;
    private LineChartView lineChartView;
    private final LineChartData data = new LineChartData();
    private String toastMsg = "";

    public CityWeatherPage(CityWeather c) {
        this.cityWeather = c;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.city_weather_page, container, false);
        Log.d("TAG", cityWeather.getCityName() + "---------------------onCreateView: ----------------------");
        progressBar = view.findViewById(R.id.progressBar);
        weatherIcon = view.findViewById(R.id.imageView);
        temperature = view.findViewById(R.id.temperature0);
        temperature2 = view.findViewById(R.id.temperature2);
        weather = view.findViewById(R.id.weather);
        power = view.findViewById(R.id.power);
        direct = view.findViewById(R.id.direct);
        humidity = view.findViewById(R.id.humidity);
        humidity2 = view.findViewById(R.id.humidity2);
        aqi = view.findViewById(R.id.aqi);
        aqi2 = view.findViewById(R.id.aqi2);
        dayT = new ArrayList<>();
        dayT.add(view.findViewById(R.id.day1));
        dayT.add(view.findViewById(R.id.day2));
        dayT.add(view.findViewById(R.id.day3));
        dayT.add(view.findViewById(R.id.day4));
        dayT.add(view.findViewById(R.id.day5));
        dayWeather = new ArrayList<>();
        dayWeather.add(view.findViewById(R.id.day1_weather));
        dayWeather.add(view.findViewById(R.id.day2_weather));
        dayWeather.add(view.findViewById(R.id.day3_weather));
        dayWeather.add(view.findViewById(R.id.day4_weather));
        dayWeather.add(view.findViewById(R.id.day5_weather));
        dayIcon1 = new ArrayList<>();
        dayIcon2 = new ArrayList<>();
        dayIcon1.add(view.findViewById(R.id.day1_icon1));
        dayIcon1.add(view.findViewById(R.id.day2_icon1));
        dayIcon1.add(view.findViewById(R.id.day3_icon1));
        dayIcon1.add(view.findViewById(R.id.day4_icon1));
        dayIcon1.add(view.findViewById(R.id.day5_icon1));
        dayIcon2.add(view.findViewById(R.id.day1_icon2));
        dayIcon2.add(view.findViewById(R.id.day2_icon2));
        dayIcon2.add(view.findViewById(R.id.day3_icon2));
        dayIcon2.add(view.findViewById(R.id.day4_icon2));
        dayIcon2.add(view.findViewById(R.id.day5_icon2));
        lineChartView = view.findViewById(R.id.line_chart);
        lineChartView.setInteractive(false);
        data.setValueLabelBackgroundColor(Color.TRANSPARENT);  //此处设置坐标点旁边的文字背景
        data.setValueLabelBackgroundEnabled(false);
        data.setValueLabelTextSize(10);
        date = view.findViewById(R.id.date);
        update();
        if (cityWeather.getInfo().equals("--"))
            refresh();
        return view;
    }

    public void refresh() {
        if (progressBar != null)
            progressBar.setVisibility(View.VISIBLE);
        new Thread(this::queryWeather).start();
    }

    public String getCityName() {
        return cityWeather.getCityName();
    }

    public int getWid() {
        return cityWeather.getWid();
    }

    private void update() {
        weatherIcon.setImageResource(UiRes.wIcon.get(cityWeather.getWid()));
        temperature.setText(String.valueOf(cityWeather.getTemperature()));
        date.setText("更新时间：" + cityWeather.getDate());
        weather.setText(cityWeather.getInfo());
        power.setText(cityWeather.getPower());
        direct.setText(cityWeather.getDirect());
        humidity.setText(cityWeather.getHumidity() + "%");
        humidity2.setText(cityWeather.getHumidity() < 70 ? (cityWeather.getHumidity() > 30 ? "舒适" : "干燥") : "湿润");
        int aqiV = cityWeather.getAqi();
        aqi.setText("AQI " + aqiV);
        if (aqiV <= 50)
            aqi2.setText("优");
        else if (aqiV <= 100)
            aqi2.setText("良");
        else if (aqiV <= 150)
            aqi2.setText("轻度污染");
        else if (aqiV <= 200)
            aqi2.setText("中度污染");
        else if (aqiV <= 300)
            aqi2.setText("重度污染");
        else
            aqi2.setText("严重污染");
        if (cityWeather.getFuture() != null) {
            try {
                JSONArray future = new JSONArray(cityWeather.getFuture());
                String temp = future.getJSONObject(0).getString("temperature");
                int fd = Integer.parseInt(temp.substring(temp.indexOf("/") + 1, temp.indexOf("℃")));
                int fn = Integer.parseInt(temp.substring(0, temp.indexOf("/")));
                temperature2.setText(String.format(getString(R.string.temp_range), fd, fn));
                JSONObject jo;
                List<Line> lines = new ArrayList<>();
                List<PointValue> valuesD = new ArrayList<>();
                List<PointValue> valuesN = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    jo = future.getJSONObject(i);
                    temp = jo.getString("date");
                    dayT.get(i).setText(temp.substring(temp.indexOf("-") + 1));
                    dayWeather.get(i).setText(jo.getString("weather"));
                    dayIcon1.get(i).setImageResource(UiRes.wIcon.get(jo.getJSONObject("wid").getInt("day")));
                    dayIcon2.get(i).setImageResource(UiRes.wIcon.get(jo.getJSONObject("wid").getInt("night")));
                    temp = jo.getString("temperature");
                    fd = Integer.parseInt(temp.substring(temp.indexOf("/") + 1, temp.indexOf("℃")));
                    fn = Integer.parseInt(temp.substring(0, temp.indexOf("/")));
                    valuesD.add(new PointValue(i, fd));
                    valuesN.add(new PointValue(i, fn));
                }
                lines.add(new Line(valuesD).setHasLabels(true).setStrokeWidth(1).setCubic(true).setPointRadius(2));
                lines.add(new Line(valuesN).setHasLabels(true).setStrokeWidth(1).setCubic(true).setPointRadius(2));
                data.setLines(lines);
                lineChartView.setLineChartData(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == COMPLETED) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getContext(), toastMsg, Toast.LENGTH_SHORT).show();
                update();
            }
        }
    };

    public void queryWeather() {
        Map<String, Object> params = new HashMap<>();//组合参数
        params.put("city", cityWeather.getCityName());
        params.put("key", "b5a48875066798b0620911733464a1d3");
        String queryParams = urlEncode(params);
        String response = doGet(apiURL, queryParams);
        try {
            JSONObject jsonObject = new JSONObject(response);
            int error_code = jsonObject.getInt("error_code");
            if (error_code == 0) {
                System.out.println("调用接口成功");

                JSONObject result = jsonObject.getJSONObject("result");
                JSONObject realtime = result.getJSONObject("realtime");
                JSONArray future = result.getJSONArray("future");

                cityWeather.setTemperature(realtime.getInt("temperature"));
                cityWeather.setInfo(realtime.getString("info"));
                cityWeather.setPower(realtime.getString("power"));
                cityWeather.setDirect(realtime.getString("direct"));
                cityWeather.setHumidity(realtime.getInt("humidity"));
                cityWeather.setAqi(realtime.getInt("aqi"));
                cityWeather.setWid(realtime.getInt("wid"));
                cityWeather.setFuture(future.toString());
                Calendar calendar = Calendar.getInstance();
                cityWeather.setDate(String.format(Locale.CHINA, "%d年%d月%d日 %02d:%02d", calendar.get(Calendar.YEAR), (calendar.get(Calendar.MONTH) + 1), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));
                DatabaseAction.getInstance(getContext()).getDao().update(cityWeather);
                System.out.printf("城市：%s%n", result.getString("city"));
                System.out.printf("天气：%s%n", realtime.getString("info"));
                System.out.printf("温度：%s%n", realtime.getString("temperature"));
                System.out.printf("湿度：%s%n", realtime.getString("humidity"));
                System.out.printf("风向：%s%n", realtime.getString("direct"));
                System.out.printf("风力：%s%n", realtime.getString("power"));
                System.out.printf("空气质量：%s%n", realtime.getString("aqi"));
                toastMsg = "刷新成功";
            } else {
                System.out.println("调用接口失败：" + jsonObject.getString("reason"));
                toastMsg = "不支持该城市";
            }
        } catch (Exception e) {
            e.printStackTrace();
            toastMsg = "无法连接网络";
        } finally {
            Message msg = new Message();
            msg.what = COMPLETED;
            handler.sendMessage(msg);
        }
    }

    /**
     * get方式的http请求
     *
     * @param httpUrl 请求地址
     * @return 返回结果
     */
    public static String doGet(String httpUrl, String queryParams) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        String result = null;// 返回结果字符串
        try {
            // 创建远程url连接对象
            URL url = new URL(httpUrl + "?" + queryParams);
            // 通过远程url连接对象打开一个连接，强转成httpURLConnection类
            connection = (HttpURLConnection) url.openConnection();
            // 设置连接方式：get
            connection.setRequestMethod("GET");
            // 设置连接主机服务器的超时时间：15000毫秒
            connection.setConnectTimeout(5000);
            // 设置读取远程返回的数据时间：60000毫秒
            connection.setReadTimeout(6000);
            // 发送请求
            connection.connect();
            // 通过connection连接，获取输入流
            if (connection.getResponseCode() == 200) {
                inputStream = connection.getInputStream();
                // 封装输入流，并指定字符集
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                // 存放数据
                StringBuilder sbf = new StringBuilder();
                String temp;
                while ((temp = bufferedReader.readLine()) != null) {
                    sbf.append(temp);
                    sbf.append(System.getProperty("line.separator"));
                }
                result = sbf.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (null != bufferedReader) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();// 关闭远程连接
            }
        }
        return result;
    }

    /**
     * 将map型转为请求参数型
     */
    public static String urlEncode(Map<String, ?> data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, ?> i : data.entrySet()) {
            try {
                sb.append(i.getKey()).append("=").append(URLEncoder.encode(i.getValue() + "", "UTF-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        String result = sb.toString();
        result = result.substring(0, result.lastIndexOf("&"));
        return result;
    }
}
