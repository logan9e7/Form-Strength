package com.logan.formstrength;

import android.content.*;

/** Explicit, non-exported receiver; database and scheduling work runs off the broadcast thread. */
public final class ReminderReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){
        if(intent==null)return;final Context app=context.getApplicationContext();final String action=intent.getAction();
        if(!ReminderScheduler.ALARM.equals(action)&&!ReminderScheduler.LOG.equals(action)&&!Intent.ACTION_BOOT_COMPLETED.equals(action)&&!Intent.ACTION_TIME_CHANGED.equals(action)&&!Intent.ACTION_TIMEZONE_CHANGED.equals(action)&&!Intent.ACTION_MY_PACKAGE_REPLACED.equals(action))return;
        final PendingResult result=goAsync();
        new Thread(()->{try{
            if(ReminderScheduler.ALARM.equals(action)){
                String kind=intent.getStringExtra("kind");if(!valid(kind))return;
                try{ReminderScheduler.deliver(app,kind);}finally{ReminderScheduler.schedule(app,kind);}
            }else if(ReminderScheduler.LOG.equals(action)){
                String kind=intent.getStringExtra("kind");if(!"water".equals(kind)&&!"walk".equals(kind))return;
                try(LocalStore store=new LocalStore(app)){store.applyReminderAction(kind,intent.getStringExtra("token"),intent.getStringExtra("day"));}
                ReminderScheduler.cancelNotification(app,kind);
            }else ReminderScheduler.reschedule(app);
        }catch(Exception ignored){/* No synthetic record or success notification on failure. */}finally{result.finish();}},"form-strength-reminders").start();
    }
    private static boolean valid(String kind){return "water".equals(kind)||"walk".equals(kind)||"windup".equals(kind);}
}
