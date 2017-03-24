package in.vilik.dreamcrusher;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;

/**
 * Created by vili on 11/03/2017.
 */

public class LottoService extends Service {
    private int difficulty;


    LocalBroadcastManager manager;


    private int currentSpeed;
    private static final int MIN_SPEED = 1000;
    private static final int MAX_SPEED = 50;

    private ArrayList<Integer> lotto;

    private IBinder localBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        // Return the binder
        return localBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        manager = LocalBroadcastManager.getInstance(this);

        System.out.println("Starting...");

        lotto = intent.getExtras().getIntegerArrayList("lotto");
        difficulty = intent.getExtras().getInt("difficulty");
        currentSpeed = intent.getExtras().getInt("speed");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Integer> used = new ArrayList<>(7);
                long tries = 0;

                while(true) {
                    used.clear();
                    int right = 0;

                    for (int i = 0; i < 7; i++) {
                        int random;

                        do {
                            random = (int) (Math.ceil(Math.random() * 38)) + 1;
                        } while (hasNumber(used, random));

                        used.add(random);

                        if (lotto.get(i) == random) {
                            right++;
                        }
                    }

                    if (right >= difficulty) {
                        System.out.println(right + " correct!");
                        Intent i = new Intent("lottoResult");
                        i.putExtra("win", true);
                        i.putExtra("numbers", used);
                        i.putExtra("tries", tries);

                        manager.sendBroadcast(i);
                        displayVictoryNotification();
                        break;
                    } else {
                        tries++;
                        Intent i = new Intent("lottoResult");
                        i.putExtra("win", false);
                        i.putExtra("numbers", used);
                        i.putExtra("tries", tries);

                        manager.sendBroadcast(i);

                        System.out.println("Incorrect row! Only " + right + " numbers.");
                    }

                    try {
                        Thread.sleep(currentSpeed);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                stopSelf();
            }
        });

        t.start();

        return START_NOT_STICKY;
    }

    public boolean hasNumber(ArrayList<Integer> numbers, int number) {
        for (Integer integer : numbers) {
            if (integer == number) {
                return true;
            }
        }

        return false;
    }

    public void displayVictoryNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle("YOU WON!");
        mBuilder.setContentText("You got " + difficulty + " right!");
        long[] shit = {1345, 32};
        mBuilder.setVibrate(shit);

        Notification notification = mBuilder.build();

        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int mId = 1;

        manager.notify(1, notification);
    }

    @Override
    public void onDestroy() {
        lotto = null;
        difficulty = 7;
        System.out.println("Destroyed");
    }

    public class LocalBinder extends Binder {
        public void setSpeed(int speed) {
            if (speed >= LottoService.MAX_SPEED && speed <= LottoService.MIN_SPEED) {
                LottoService.this.currentSpeed = speed;
            }
        }

        public int getSpeed() {
            return LottoService.this.currentSpeed;
        }
    }
}
