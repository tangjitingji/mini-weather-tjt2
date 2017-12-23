package com.example.tangjiting.miniweather;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.tangjiting.bean.TodayWeather;

/**
 * Implementation of App Widget functionality.
 */
public class WeatherWidget extends AppWidgetProvider {
    public static TodayWeather todayWeather = new TodayWeather();
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        CharSequence widgetText = "天气预报";
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget_layout);
        views.setTextViewText(R.id.city_name, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
    public static RemoteViews updateRemoteViews(Context context) {
        RemoteViews view = new RemoteViews(context.getPackageName(),R.layout.weather_widget_layout);
        if (null == todayWeather) {
            return null;
        } else {
            view.setTextViewText(R.id.city_name,todayWeather.getCity());
            view.setTextViewText(R.id.cloud,todayWeather.getFengxiang());
            view.setTextViewText(R.id.cur_temp,todayWeather.getType() );
            view.setTextViewText(R.id.low_temp, "低" + todayWeather.getLow());
            view.setTextViewText(R.id.high_temp, "高" + todayWeather.getHigh());
//            Toast.makeText(context,"更新成功",Toast.LENGTH_SHORT).show();
            return view;
        }
    }
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }
    private void updateWidget(Context context, long time) {
        //RemoteViews处理异进程中的View
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.weather_widget_layout);
        System.out.println("time=" + time);
        //rv.setTextViewText(R.id.counter, DateUtils.formatElapsedTime(time / 1000));

        AppWidgetManager am = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = am.getAppWidgetIds(new ComponentName(context, WeatherWidget.class));
        am.updateAppWidget(appWidgetIds, rv);//更新 所有实例
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        MainActivity.appWidgetIds = appWidgetIds;
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        // 开启更新小部件的服务
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        super.onEnabled(context);
    }
}

