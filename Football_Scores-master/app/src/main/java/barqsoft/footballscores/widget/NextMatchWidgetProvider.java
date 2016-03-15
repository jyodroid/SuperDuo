package barqsoft.footballscores.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import barqsoft.footballscores.MainScreenFragment;

/**
 * Created by jyo on 13/03/2016.
 */
public class NextMatchWidgetProvider extends AppWidgetProvider{

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, NextMatchIntentService.class));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (MainScreenFragment.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            context.startService(new Intent(context, NextMatchIntentService.class));
        }
    }
}
