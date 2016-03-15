package barqsoft.footballscores.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;

/**
 * Created by jyo on 13/03/2016.
 */
public class NextMatchIntentService extends IntentService {

    private static final String[] MATCH_COLUMNS = {
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.DATE_COL
    };

    // these indices must match the projection
    private static final int INDEX_AWAY = 0;
    private static final int INDEX_HOME = 1;
    private static final int INDEX_TIME = 2;
    private static final int INDEX_DATE = 3;

    private static final String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    private static final String WILDCARD = "%";
    private static final String ORDER_TYPE = " ASC";
    private static final String VS = "VS";


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public NextMatchIntentService() {
        super("NextMatchIntentService");
    }

    @Override
    public void onHandleIntent(Intent intent) {

        // Get data from the ContentProvider
        Uri scoreWithDateUri = DatabaseContract.scores_table.buildScoreWithDate();

        //Format date
        SimpleDateFormat formatedDate = new SimpleDateFormat("yyyy-MM-dd");

        Calendar todayCalendar = Calendar.getInstance();
        String today = formatedDate.format(todayCalendar.getTime());

        Calendar tomorrowCalendar = Calendar.getInstance();
        tomorrowCalendar.add(Calendar.DAY_OF_YEAR, 1);
        String tomorrow = formatedDate.format(tomorrowCalendar.getTime());

        Calendar afterTomorrowCalendar = Calendar.getInstance();
        afterTomorrowCalendar.add(Calendar.DAY_OF_YEAR, 2);
        String afterTomorrow = formatedDate.format(afterTomorrowCalendar.getTime());

        //Call data for today and the next two days
        String[] selectionArgs =
                {
                        formatQuerySelectionArg(today),
                        formatQuerySelectionArg(tomorrow),
                        formatQuerySelectionArg(afterTomorrow)
                };

        Cursor data = getContentResolver().query(scoreWithDateUri, MATCH_COLUMNS, null,
                selectionArgs, DatabaseContract.scores_table.DATE_COL + ORDER_TYPE);

        if (data == null) {
            return;
        }

        // Extract the scores data from the Cursor
        SimpleDateFormat matchDateFormat = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
        String awayTeam = null;
        String homeTeam = null;
        String matchTime = null;
        while (data.moveToNext()) {
            try {
                //match date
                String matchDate = data.getString(INDEX_DATE);
                String matchDTime = data.getString(INDEX_TIME);
                StringBuilder stringBuilder =
                        new StringBuilder(matchDate).append(":").append(matchDTime);
                Date date = matchDateFormat.parse(stringBuilder.toString());

                Calendar matchCalendar = Calendar.getInstance();
                matchCalendar.setTime(date);

                //Comparing time
                if (todayCalendar.before(matchCalendar)) {
                    awayTeam = data.getString(INDEX_AWAY);
                    homeTeam = data.getString(INDEX_HOME);
                    if (matchCalendar.get(Calendar.DAY_OF_MONTH) ==
                            todayCalendar.get(Calendar.DAY_OF_MONTH)) {
                        matchTime = matchDTime;
                    } else {
                        int matchTimeInd = matchCalendar.get(Calendar.DAY_OF_WEEK) - 1;
                        matchTime = days[matchTimeInd];
                    }
                    break;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        data.close();

        // Perform this loop procedure for each widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        ComponentName componentName = new ComponentName(this, NextMatchWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);

        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.widget_next_match_layout;
            RemoteViews remoteViews = new RemoteViews(getPackageName(), layoutId);

            // Add the data to the RemoteViews
            if (null != homeTeam && null != homeTeam && null != awayTeam) {
                remoteViews.setTextViewText(R.id.widget_match_home_team, homeTeam);
                remoteViews.setTextViewText(R.id.widget_match_time, matchTime);
                remoteViews.setTextViewText(R.id.widget_match_vs, VS);
                remoteViews.setTextViewText(R.id.widget_match_away_team, awayTeam);
            }

            // Create an Intent to launch MainActivity
            Intent mainActivityIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, mainActivityIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    private String formatQuerySelectionArg(String arg) {

        StringBuilder argBuilder =
                new StringBuilder(WILDCARD).append(arg).append(WILDCARD);

        return argBuilder.toString();
    }
}
