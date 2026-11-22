package com.logan.formstrength;

import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import org.json.*;
import java.time.*;

/** Inexact local reminders. Channels follow system DND; no exact-alarm or DND bypass. */
final class ReminderScheduler {
    static final String ALARM="com.logan.formstrength.REMINDER",LOG="com.logan.formstrength.REMINDER_LOG";
    private static final String PREF="local_reminders";
    private static final String[] KINDS={"water","walk","windup"};
    private static android.content.SharedPreferences prefs(Context c){return c.getSharedPreferences(PREF,Context.MODE_PRIVATE);}
    private static int index(String kind){for(int i=0;i<KINDS.length;i++)if(KINDS[i].equals(kind))return i;throw new IllegalArgumentException("Unknown reminder.");}
    static JSONObject configure(Context context,boolean water,boolean walk,boolean windup){
        if(!prefs(context).edit().putBoolean("water",water).putBoolean("walk",walk).putBoolean("windup",windup).commit())throw new IllegalStateException("Could not save reminders.");
        channels(context);reschedule(context);return status(context);
    }
    static JSONObject status(Context context){try{
        NotificationManager manager=context.getSystemService(NotificationManager.class);
        boolean permission=Build.VERSION.SDK_INT<33||context.checkSelfPermission("android.permission.POST_NOTIFICATIONS")==PackageManager.PERMISSION_GRANTED;
        JSONObject result=new JSONObject().put("permissionGranted",permission).put("notificationsAllowed",permission&&manager!=null&&manager.areNotificationsEnabled());
        for(String kind:KINDS)result.put(kind,prefs(context).getBoolean(kind,false));
        return result.put("schedule","Water every 2 hours, 08:00–20:00; walk every 3 hours, 09:00–18:00; wind-up at 20:00. Delivery may be delayed by Android battery settings and system DND.");
    }catch(JSONException e){throw new IllegalStateException("Could not read reminders.",e);}}
    static void channels(Context context){
        NotificationManager manager=context.getSystemService(NotificationManager.class);if(manager==null)return;
        for(String kind:KINDS){NotificationChannel channel=new NotificationChannel("fs_"+kind,kind.equals("water")?"Water reminders":kind.equals("walk")?"Walk reminders":"Daily wind-up",NotificationManager.IMPORTANCE_DEFAULT);channel.setDescription("Optional reminders scheduled on this device. Follows system Do Not Disturb.");manager.createNotificationChannel(channel);}
    }
    private static PendingIntent alarmIntent(Context context,String kind){return PendingIntent.getBroadcast(context,500+index(kind),new Intent(context,ReminderReceiver.class).setAction(ALARM).putExtra("kind",kind),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
    static long nextTime(String kind,long nowMillis,ZoneId zone){
        index(kind);ZonedDateTime now=Instant.ofEpochMilli(nowMillis).atZone(zone);int first=kind.equals("walk")?9:kind.equals("windup")?20:8,last=kind.equals("walk")?18:20,interval=kind.equals("walk")?3:kind.equals("windup")?24:2;
        for(int day=0;day<2;day++)for(int hour=first;hour<=last;hour+=interval){ZonedDateTime candidate=now.toLocalDate().plusDays(day).atTime(hour,0).atZone(zone);if(candidate.toInstant().toEpochMilli()>nowMillis)return candidate.toInstant().toEpochMilli();}
        throw new IllegalStateException("Could not schedule reminder.");
    }
    static void reschedule(Context context){for(String kind:KINDS)schedule(context,kind);}
    static void schedule(Context context,String kind){
        AlarmManager manager=context.getSystemService(AlarmManager.class);if(manager==null)return;
        PendingIntent intent=alarmIntent(context,kind);manager.cancel(intent);
        if(prefs(context).getBoolean(kind,false))manager.setWindow(AlarmManager.RTC_WAKEUP,nextTime(kind,System.currentTimeMillis(),ZoneId.systemDefault()),15*60*1000L,intent);
        else cancelNotification(context,kind);
    }
    static void cancelNotification(Context context,String kind){NotificationManager manager=context.getSystemService(NotificationManager.class);if(manager!=null)manager.cancel(700+index(kind));}
    static void deliver(Context context,String kind){
        index(kind);if(!prefs(context).getBoolean(kind,false))return;
        channels(context);NotificationManager manager=context.getSystemService(NotificationManager.class);
        if(manager==null||!manager.areNotificationsEnabled()||(Build.VERSION.SDK_INT>=33&&context.checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=PackageManager.PERMISSION_GRANTED))return;
        // A deferred alarm is not permission to interrupt the user's sleeping hours.
        int hour=LocalTime.now().getHour();if(kind.equals("windup")?(hour<20||hour>22):(hour<8||hour>20))return;
        Intent open=new Intent(context,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);if(kind.equals("windup"))open.putExtra("openYou",true);
        PendingIntent content=PendingIntent.getActivity(context,800+index(kind),open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        String title=kind.equals("water")?"Time for a water break?":kind.equals("walk")?"A little movement?":"Wind down and review your day";
        String text=kind.equals("water")?"Had a drink? Tap +250 ml to log it.":kind.equals("walk")?"Walk when it suits you. Log only after you finish.":"Open your daily review and plan a manageable tomorrow.";
        Notification.Builder builder=new Notification.Builder(context,"fs_"+kind).setSmallIcon(android.R.drawable.ic_popup_reminder).setContentTitle(title).setContentText(text).setContentIntent(content).setAutoCancel(true).setCategory(Notification.CATEGORY_REMINDER).setVisibility(Notification.VISIBILITY_PRIVATE).setOnlyAlertOnce(true);
        if(!kind.equals("windup")){
            String token=System.currentTimeMillis()+"-"+kind;
            Intent log=new Intent(context,ReminderReceiver.class).setAction(LOG).putExtra("kind",kind).putExtra("token",token).putExtra("day",LocalDate.now().toString());
            PendingIntent action=PendingIntent.getBroadcast(context,900+index(kind),log,PendingIntent.FLAG_CANCEL_CURRENT|PendingIntent.FLAG_ONE_SHOT|PendingIntent.FLAG_IMMUTABLE);
            builder.addAction(new Notification.Action.Builder(null,kind.equals("water")?"+250 ml":"+ Log walk",action).build());
        }
        try{manager.notify(700+index(kind),builder.build());}catch(SecurityException ignored){/* Permission can be revoked between check and delivery. */}
    }
}
