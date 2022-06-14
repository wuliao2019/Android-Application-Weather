package com.cqu.weather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cqu.weather.database.CityWeather;
import com.cqu.weather.database.DatabaseAction;
import com.cqu.weather.ui.CityWeatherAdapter;
import com.cqu.weather.ui.CityWeatherPage;
import com.cqu.weather.ui.UiRes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int COMPLETED = -1;
    public static final int COMPLETED2 = -2;
    public static final int RETURNS = 2;
    private List<CityWeather> ct;
    private List<CityWeather> cws;
    private CityWeatherAdapter cityWeatherAdapter;
    private ViewPager2 viewPager2;
    private ConstraintLayout mainBack;
    private TextView titleCity;
    private EditText editText;
    private int oldBack;
    private int newBack;
    private int activePos = 0;
    private int tabPos = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("Main", "^^^^^^^^^^^^^^^^^^^^onCreate: ^^^^^^^^^^^^^^^^^^^^^^^^^^");
        UiRes.init();
        oldBack = getColor(R.color.sunny_back);
        newBack = oldBack;
        cityWeatherAdapter = new CityWeatherAdapter(this);
        mainBack = findViewById(R.id.mainBackground);
        ImageView addIcon = findViewById(R.id.addButton);
        titleCity = findViewById(R.id.cityName);
        titleCity.setOnClickListener(v -> {
            cityWeatherAdapter.clearPages();
            tabPos = activePos;
            Intent intent = new Intent(MainActivity.this, CityListActivity.class);
            startActivityForResult(intent, RETURNS);
        });

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);

        viewPager2 = findViewById(R.id.viewPager);
        viewPager2.setAdapter(cityWeatherAdapter);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                activePos = position;
                if (cityWeatherAdapter.getItemCount() > 0)
                    titleCity.setText(cityWeatherAdapter.getFragment(position).getCityName());
            }
        });
        addIcon.setOnClickListener(v -> addDialog());
        FloatingActionButton refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(v -> {
            if (cityWeatherAdapter.getItemCount() > activePos)
                cityWeatherAdapter.getFragment(activePos).refresh();
        });

        TabLayout tabLayout = findViewById(R.id.tabs);
        TabLayoutMediator mediator = new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            if (cityWeatherAdapter.getItemCount() > position)
                tab.setText(cityWeatherAdapter.getFragment(position).getCityName());
        });
        final Handler handler1 = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (cityWeatherAdapter.getItemCount() > activePos)
                    newBack = getColor(UiRes.wBack.get(cityWeatherAdapter.getFragment(activePos).getWid()));
                if (oldBack != newBack) {
                    ValueAnimator animator = ObjectAnimator.ofInt(mainBack, "backgroundColor", oldBack, newBack);
                    animator.setDuration(500);
                    animator.setEvaluator(new ArgbEvaluator());
                    animator.start();
                    oldBack = newBack;
                }
                handler1.postDelayed(this, 800);
            }
        };
        mediator.attach();
        handler1.postDelayed(runnable, 800);
//        handler1.removeCallbacks((Runnable) this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Main", "^^^^^^^^^^^^^^^^^^^^onStart: ^^^^^^^^^^^^^^^^^^^^^^^^^^");
        new Thread(() -> {
            cws = DatabaseAction.getInstance(this).getDao().getAllCity();
            if (cws.size() == 0) {
                DatabaseAction.getInstance(this).getDao().insert(new CityWeather(0, "重庆"));
                CityWeather cw = DatabaseAction.getInstance(this).getDao().getAllCity().get(0);
                cws.add(cw);
            }
            Message msg = new Message();
            msg.what = COMPLETED;
            handler.sendMessage(msg);
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RESULT_OK，判断另外一个activity已经结束数据输入功能，Standard activity result:
        // operation succeeded. 默认值是-1
        if (resultCode == 3) {
            if (requestCode == RETURNS) {
                tabPos = data.getIntExtra("tabPos", 0);
            }
        }
    }

    private void addDialog() {
        Dialog dialog = new Dialog(this);
        //去掉标题线
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(this).inflate(R.layout.add_dialog, null, false);
        dialog.setContentView(view);
        //背景透明
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER; // 居中位置
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.animStyle);  //添加动画

        editText = view.findViewById(R.id.cityNameText);
        Button addBt = view.findViewById(R.id.addBt);
        addBt.setOnClickListener(v -> {
            new Thread(() -> {
                if (DatabaseAction.getInstance(this).getDao().existCity(String.valueOf(editText.getText()))) {
                    Looper.prepare();
                    Toast.makeText(this, "该城市已存在", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                } else {
                    DatabaseAction.getInstance(this).getDao().insert(new CityWeather(cityWeatherAdapter.getItemCount(), String.valueOf(editText.getText())));
                    ct = DatabaseAction.getInstance(this).getDao().getAllCity();
                    Message msg = new Message();
                    msg.what = COMPLETED2;
                    handler.sendMessage(msg);
                }
            }).start();
            dialog.dismiss();
        });
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == COMPLETED) {
                cityWeatherAdapter.clearPages();
                for (CityWeather cwt : cws) {
                    CityWeatherPage cityWeatherPage = new CityWeatherPage(cwt);
                    cityWeatherAdapter.addFragment(cityWeatherPage);
                }
                viewPager2.setCurrentItem(tabPos);
                titleCity.setText(cws.get(tabPos).getCityName());
            } else if (msg.what == COMPLETED2) {
                CityWeatherPage cwp = new CityWeatherPage(ct.get(ct.size() - 1));
                cityWeatherAdapter.addFragment(cwp);
                viewPager2.setCurrentItem(cityWeatherAdapter.getItemCount() - 1);
            }
        }
    };
}