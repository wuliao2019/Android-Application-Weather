package com.cqu.weather;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cqu.weather.database.CityWeather;
import com.cqu.weather.database.DatabaseAction;
import com.cqu.weather.ui.UiRes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CityListActivity extends AppCompatActivity {
    public static final int COMPLETED = -1;
    public static final int DELETE = -2;
    private static List<CityWeather> cities = new ArrayList<>();
    private static CityAdapter adapter;
    private static int tempPos = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);
        Log.d("List", "^^^^^^^^^^^^^^^^^^^^onCreate: ^^^^^^^^^^^^^^^^^^^^^^^^^^");
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        new Thread(() -> {
            cities = DatabaseAction.getInstance(this).getDao().getAllCity();
            Message msg = new Message();
            msg.what = COMPLETED;
            handler.sendMessage(msg);
        }).start();
        RecyclerView cityList = findViewById(R.id.city_list);
        cityList.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new CityAdapter(this, cities);
        adapter.setOnItemClickListener(new CityAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position, View v) {
                Intent intent = new Intent();
                intent.putExtra("tabPos", position);
                setResult(3, intent);
                finish();
            }

            @Override
            public void onLongClick(int position, View v) {

            }
        });
        cityList.setAdapter(adapter);
        helper.attachToRecyclerView(cityList);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("List", "^^^^^^^^^^^^^^^^^^^^onStart: ^^^^^^^^^^^^^^^^^^^^^^^^^^");
    }

    private static final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == COMPLETED) {
                adapter.setList(cities);
            } else if (msg.what == DELETE) {
                adapter.remove(tempPos);
            }
        }
    };

    static class CityAdapter extends RecyclerView.Adapter<CityAdapter.Vh> {
        private final Context context;
        public List<CityWeather> citiesList;

        public CityAdapter(Context context, List<CityWeather> citiesList) {
            this.context = context;
            this.citiesList = citiesList;
        }

        @NonNull
        @Override
        public CityAdapter.Vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Vh(LayoutInflater.from(context).inflate(R.layout.city_item, null));
        }

        @Override
        public void onBindViewHolder(CityAdapter.Vh holder, final int position) {
            holder.itemName.setText(citiesList.get(position).getCityName());
            holder.itemTemp.setText(citiesList.get(position).getTemperature() + "℃");
            holder.itemBack.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, UiRes.wBack.get(citiesList.get(position).getWid()))));
            holder.itemIcon.setImageResource(UiRes.wIcon.get(citiesList.get(position).getWid()));
            holder.deleteBtn.setOnClickListener(view -> {
                new Thread(() -> {
                    tempPos = holder.getAdapterPosition();
                    DatabaseAction.getInstance(context).getDao().delete(citiesList.get(tempPos));
                    Message msg = new Message();
                    msg.what = DELETE;
                    handler.sendMessage(msg);
                }).start();
            });
            if (mOnItemClickListener != null) {
                holder.itemView.setOnClickListener(v -> mOnItemClickListener.onClick(holder.getAdapterPosition(), v));
                holder.itemView.setOnLongClickListener(v -> {
                    mOnItemClickListener.onLongClick(holder.getAdapterPosition(), v);
                    return true;
                });
            }
        }

        public interface OnItemClickListener {
            void onClick(int position, View v);

            void onLongClick(int position, View v);
        }

        private OnItemClickListener mOnItemClickListener;

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.mOnItemClickListener = onItemClickListener;
        }

        @Override
        public int getItemCount() {
            return citiesList.size();
        }

        public void setList(List<CityWeather> newList) {
            this.citiesList = newList;
            notifyDataSetChanged();
        }

        public void add(CityWeather item) {
            int position = citiesList.size();
            citiesList.add(item);
            notifyItemInserted(position);
        }

        public void add(int position, CityWeather item) {
            citiesList.add(position, item);
            notifyItemInserted(position);
        }
//        public void remove(T item) {
//            final int position = stringList.indexOf(item);
//            if (-1 == position)
//                return;
//            stringList.remove(item);
//            notifyItemRemoved(position);
//        }

        public void remove(int position) {
            citiesList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, citiesList.size());
        }

        public void updateOrder() {
            for (int i = 0; i < citiesList.size(); i++) {
                citiesList.get(i).setPosition(i);
                int finalI = i;
                new Thread(() -> DatabaseAction.getInstance(context).getDao().update(citiesList.get(finalI))).start();
            }
        }

        static class Vh extends RecyclerView.ViewHolder {
            private final TextView itemName;
            private final TextView itemTemp;
            private final ImageView itemIcon;
            private final ConstraintLayout itemBack;
            private final ImageView deleteBtn;

            public Vh(View itemView) {
                super(itemView);
                itemName = itemView.findViewById(R.id.item_name);
                itemIcon = itemView.findViewById(R.id.item_icon);
                itemTemp = itemView.findViewById(R.id.item_temp);
                itemBack = itemView.findViewById(R.id.item_back);
                deleteBtn = itemView.findViewById(R.id.delete_btn);
            }
        }
    }


    ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
        //线性布局和网格布局都可以使用
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int dragFrag = 0;
            if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                dragFrag = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            } else if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                dragFrag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            }
            return makeMovementFlags(dragFrag, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            //得到当拖拽的viewHolder的Position
            int fromPosition = viewHolder.getAdapterPosition();
            //拿到当前拖拽到的item的viewHolder
            int toPosition = target.getAdapterPosition();
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(cities, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(cities, i, i - 1);
                }
            }
            adapter.notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            //侧滑删除可以使用；
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        /**
         * 长按选中Item的时候开始调用
         * 长按高亮
         */
        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
//                viewHolder.itemView.setBackgroundColor(Color.RED);
                //获取系统震动服务//震动70毫秒
                Vibrator vib = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
                vib.vibrate(VibrationEffect.createOneShot(30, 128));
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        //手指松开的时候还原高亮
        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setBackgroundColor(0);
            adapter.notifyDataSetChanged();  //完成拖动后刷新适配器，这样拖动后删除就不会错乱
            adapter.updateOrder();
        }
    });
}
