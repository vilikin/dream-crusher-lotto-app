package in.vilik.dreamcrusher;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends MyBaseActivity {

    ArrayList<Integer> lotto;
    Button crushBtn;
    Intent crushIntent;
    LottoService.LocalBinder binder;
    boolean binded = false;
    LottoServiceConnection connection;
    ArrayList<Button> previousBtns;
    int selectedColor;
    int normalColor;
    int resultColor;
    TextView yearsPassed;

    int difficulty = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Debug.loadDebug(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectedColor = ResourcesCompat.getColor(getResources(), R.color.colorBtnMySelected, null);
        normalColor = ResourcesCompat.getColor(getResources(), R.color.colorBtnMy, null);
        resultColor = ResourcesCompat.getColor(getResources(), R.color.colorBtnMyResult, null);

        lotto = new ArrayList<>();
        crushBtn = (Button)findViewById(R.id.crushBtn);
        yearsPassed = (TextView)findViewById(R.id.years);
        connection = new LottoServiceConnection();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (binded) {
            unbindService(connection);
            binded = false;
        }
    }

    private int isInLotto(int num) {
        for (int i = 0; i < lotto.size(); i++) {
            if (lotto.get(i) == num) {
                return i;
            }
        }

        return -1;
    }

    public void select(View view) {
        Button btn = (Button)view;
        int num = Integer.parseInt(btn.getText().toString());

        int index = isInLotto(num);

        if (index != -1) {
            lotto.remove(index);
            btn.setTextColor(normalColor);
            Debug.print(TAG, "select()", "Removed: " + num + " from index " + index, 1);
        } else if (lotto.size() < 7) {
            lotto.add(num);
            btn.setTextColor(selectedColor);
            Debug.print(TAG, "select()", "Added: " + num, 1);
        }

        if (lotto.size() == 7) {
            crushBtn.setEnabled(true);
        } else {
            crushBtn.setEnabled(false);
        }
    }

    public void crush(View view) {
        String chosenNumbers = "";

        for (Integer integer : lotto) {
            chosenNumbers += integer + " ";
        }

        Debug.print(TAG, "crush()", chosenNumbers, 1);

        crushIntent = new Intent(this, LottoService.class);
        crushIntent.putExtra("lotto", lotto);
        crushIntent.putExtra("difficulty", difficulty);
        crushIntent.putExtra("speed", 100);

        previousBtns = new ArrayList<>();
        LottoReceiver listener = new LottoReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(listener, new IntentFilter("lottoResult"));
        startService(crushIntent);
        bindService(crushIntent, connection, BIND_AUTO_CREATE);
        crushBtn.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.plus:
                Debug.print(TAG, "onOptionsItemSelected()", "Plus tapped!", 1);

                if (binded) {
                    binder.setSpeed(binder.getSpeed() - 50);
                }

                return true;
            case R.id.minus:
                Debug.print(TAG, "onOptionsItemSelected()", "Minus tapped!", 1);

                if (binded) {
                    binder.setSpeed(binder.getSpeed() + 50);
                }

                return true;
            case R.id.diff:
                Debug.print(TAG, "onOptionsItemSelected()", "Difficulty tapped", 1);


                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                String[] items = {"7", "5", "3"};
                builder.setTitle("Choose difficulty")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        difficulty = 7;
                                        break;
                                    case 1:
                                        difficulty = 5;
                                        break;
                                    case 2:
                                        difficulty = 3;
                                        break;
                                }
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
        }

        return false;
    }

    public class LottoReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();

            long tries = extras.getLong("tries");
            ArrayList<Integer> numbers = extras.getIntegerArrayList("numbers");
            boolean win = extras.getBoolean("win");

            System.out.println("RECEIVED!");
            System.out.println(win ? "Victory!" : "No win!");

            for (Integer number : numbers) {
                System.out.print(number+ " ");
            }

            System.out.println();

            for (Button previousBtn : previousBtns) {
                boolean setSelected = false;
                for (Integer integer : lotto) {
                    if (integer == Integer.parseInt(previousBtn.getText().toString())) {
                        previousBtn.setTextColor(selectedColor);
                        setSelected = true;
                    }
                }

                if (!setSelected) {
                    previousBtn.setTextColor(normalColor);
                }
            }

            previousBtns.clear();

            if (!win) {
                for (Integer number : numbers) {
                    int btnId = MainActivity.this.getResources().getIdentifier("num" + number, "id", MainActivity.this.getPackageName());
                    Button btn = ((Button) MainActivity.this.findViewById(btnId));
                    btn.setTextColor(resultColor);
                    previousBtns.add(btn);
                }

                MainActivity.this.yearsPassed.setText((tries / 52) + " years passed");
            } else {
                MainActivity.this.crushBtn.setEnabled(true);
                MainActivity.this.unbindService(MainActivity.this.connection);
                MainActivity.this.binded = false;

                for (Integer integer : MainActivity.this.lotto) {
                    int btnId = MainActivity.this.getResources().getIdentifier("num" + integer, "id", MainActivity.this.getPackageName());
                    Button btn = ((Button) MainActivity.this.findViewById(btnId));
                    btn.setTextColor(normalColor);
                }

                MainActivity.this.lotto.clear();
            }
        }
    }

    public class LottoServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder localebinder) {
            binder = (LottoService.LocalBinder) localebinder;
            binded = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // NEVER GETS CALLED???
            binded = false;
            System.out.println("MainActivity has disconnected the service");
        }
    }
}
