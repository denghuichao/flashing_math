package com.deng.flashingmath;

import java.util.HashMap;
import java.util.Random;

import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private TextView num1Tv;
    private TextView num2Tv;
    private TextView opTv;
    private TextView resultTv;
    private TextView equalsOp;
    private ProgressBar progress;
    private RelativeLayout container;
    private TableLayout table;
    private TextView bestScoreTv;
    private TextView currentScoreTv;
    private TextView title;
    private TextView bestTitle;
    private TextView scoreTitle;
    private TextView numOfRightTv;
    private CustomProgressDialog loadingPd;

    private int SLEEP_TIME = 20;
    private int MAX_SLEEP_TIME = 20;
    private int numOfRight = 0;

    private int currentScore = 0;
    private int highestSore = 0;
    private Button[] btns = new Button[9];

    int[] nums = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 9, 8, 7, 6};
    char[] ops = new char[]{'+', '-', '×', '÷', '+', '-', '×', '÷', '×', '÷'};

    private ExpressionThread myThread;
    private SharedPreferences myPerf;
    private Expression currentExpression;

    private SoundPool soundPool;
    private int buttonRightSoundId = -1;
    private int buttonSoundId = -1;
    private int victorySoundId = -1;
    private boolean soundEnabled = true;
    private boolean viberateEanbled = true;
    private Typeface tf;
    private Vibrator mVibrator;  //声明一个振动器对象
    private Handler myUiHandler = new Handler() {
        public void handleMessage(Message m) {
            if (m.what == 1111) {
                num1Tv.setText(currentExpression.num1 + "");
                num2Tv.setText(currentExpression.num2 + "");
                opTv.setText(currentExpression.op + "");
                resultTv.setText("?");
                resultTv.setTextColor(Color.rgb(0xff, 0xff, 0xff));
                progress.setProgress(100);
            } else if (m.what == 1112) {
                progress.setProgress((Integer) m.obj);
                //if((Integer) m.obj==90)autoTest();
            } else if (m.what == 1113) {
                showDialog();//游戏结束
            }
        }
    };

    private int[] nightColors = new int[]{
            0xffffffcc, 0xffccffff, 0xffffcccc,
            0xffffff99, 0xffccccff, 0xffff9966,
            0xffcccccc, 0xffccffcc, 0xff99cccc,
            0xffccff99, 0xff99ccff, 0xff66cccc,
            0xff66ccff, 0xffccff66
    };

    private int reserveColor(int c) {
        return c ^ 0xffffff;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        AssetManager mgr = getAssets();//得到AssetManager
        tf = Typeface.createFromAsset(mgr, "fonts/fontss.TTF");//根据路径得到Typeface

        initUi();
        myPerf = this.getSharedPreferences("com.example.react_preferences", MODE_PRIVATE);
        mVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);

        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        buttonRightSoundId = soundPool.load(this, R.raw.passed, 1);
        buttonSoundId = soundPool.load(this, R.raw.sfx_menu_buttonclick, 1);
        victorySoundId = soundPool.load(this, R.raw.victory, 1);
        myThread = new ExpressionThread();
        myThread.start();
        currentExpression = expressionGernerate(0);
        startNextExpression();

        highestSore = myPerf.getInt("highestSore", 0);
        bestScoreTv.setText(this.highestSore + "");

        soundEnabled = myPerf.getBoolean("soundEnabled", true);//true;
        viberateEanbled = myPerf.getBoolean("viberateEanbled", true);
    }

    @Override
    public void onStop() {
        super.onStop();
        saveScore();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myThread.ok = false;

    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    public void saveConfig() {
        Editor e = myPerf.edit();//.getInt("highestSore", 0);
        e.putBoolean("soundEnabled", soundEnabled);//true;
        e.putBoolean("viberateEanbled", viberateEanbled);
        e.commit();
    }

    public void saveScore() {
        Editor e = myPerf.edit();//.getInt("highestSore", 0);
        e.putInt("highestSore", highestSore);
        e.commit();
    }

    private void initUi() {
        container = (RelativeLayout) findViewById(R.id.container);
        num1Tv = (TextView) findViewById(R.id.num1);
        num2Tv = (TextView) findViewById(R.id.num2);
        opTv = (TextView) findViewById(R.id.fuhao);
        resultTv = (TextView) findViewById(R.id.result);// TODO Auto-generated method stub
        equalsOp = (TextView) findViewById(R.id.equalsOp);// TODO Auto-generated method stub
        bestScoreTv = (TextView) findViewById(R.id.bestscore);
        currentScoreTv = (TextView) findViewById(R.id.currentscore);
        numOfRightTv = (TextView) findViewById(R.id.numOfRight);
        title = (TextView) findViewById(R.id.title);
        bestTitle = (TextView) findViewById(R.id.bestText);
        scoreTitle = (TextView) findViewById(R.id.scoreText);

        bestScoreTv.setText(highestSore + "");
        currentScoreTv.setText(currentScore + "");
        bestScoreTv.setAnimation(AnimationUtils.loadAnimation(this, R.anim.zoomin));
        currentScoreTv.setAnimation(AnimationUtils.loadAnimation(this, R.anim.zoomin));

        table = (TableLayout) findViewById(R.id.table);
        LayoutParams params = (LayoutParams) table.getLayoutParams();

        params.width = this.getResources().getDisplayMetrics().widthPixels;
        params.height = params.width;
        table.setLayoutParams(params);
        progress = (ProgressBar) findViewById(R.id.progress);
        progress.setMax(100);

        btns[0] = (Button) findViewById(R.id.id1);
        btns[1] = (Button) findViewById(R.id.id2);
        btns[2] = (Button) findViewById(R.id.id3);
        btns[3] = (Button) findViewById(R.id.id4);
        btns[4] = (Button) findViewById(R.id.id5);
        btns[5] = (Button) findViewById(R.id.id6);
        btns[6] = (Button) findViewById(R.id.id7);
        btns[7] = (Button) findViewById(R.id.id8);
        btns[8] = (Button) findViewById(R.id.id9);


        num1Tv.setTypeface(tf);//
        num2Tv.setTypeface(tf);//
        opTv.setTypeface(tf);//
        resultTv.setTypeface(tf);//
        equalsOp.setTypeface(tf);//
        currentScoreTv.setTypeface(tf);//
        bestScoreTv.setTypeface(tf);//
        title.setTypeface(tf);//=(TextView) findViewById(R.id.title);
        bestTitle.setTypeface(tf);//=(TextView) findViewById(R.id.besttext);
        scoreTitle.setTypeface(tf);//=(TextView) findViewById(R.id.scoreText);
        numOfRightTv.setTypeface(tf);//=(TextView) findViewById(R.id.scoreText);

        for (int i = 0; i < btns.length; i++) {
            btns[i].setTypeface(tf);//
        }

        showUI();
    }


    public void showUI() {
        int[] tempNums = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};

        for (int i = 0; i < tempNums.length; i++) {
            int index = new Random().nextInt(tempNums.length);
            int a = tempNums[i];
            tempNums[i] = tempNums[index];
            tempNums[index] = a;
        }

        for (int i = 0; i < nightColors.length; i++) {
            int index = new Random().nextInt(nightColors.length);
            int a = nightColors[i];
            nightColors[i] = nightColors[index];
            nightColors[index] = a;
        }

        int i = 0;
        for (i = 0; i < btns.length; i++) {
            btns[i].setText(tempNums[i] + "");
            int color = nightColors[i];
            btns[i].setBackgroundColor(color);
            btns[i].setTextColor(reserveColor(color));
        }

    }

    public void showDialog() {

        final AlertDialog dlg = new AlertDialog.Builder(this).create();
        dlg.setCancelable(false);
        dlg.show();
        Window window = dlg.getWindow();
        window.setContentView(R.layout.my_dialog_ll);

        TextView nowScore = (TextView) window.findViewById(R.id.scroeNum);
        TextView bestScore = (TextView) window.findViewById(R.id.bestNum);

        TextView title = (TextView) window.findViewById(R.id.title);
        TextView scoreText = (TextView) window.findViewById(R.id.scoretext);
        TextView bestText = (TextView) window.findViewById(R.id.besttext);

        nowScore.setTypeface(tf);//
        bestScore.setTypeface(tf);//
        title.setTypeface(tf);//
        scoreText.setTypeface(tf);//
        bestText.setTypeface(tf);//

        ImageButton continueBtn = (ImageButton) window.findViewById(R.id.continueplay);
        ImageButton shareBtn = (ImageButton) window.findViewById(R.id.share);
        ImageButton exitBtn = (ImageButton) window.findViewById(R.id.exit);
        ImageView vImg = (ImageView) window.findViewById(R.id.vImg);

        if (currentScore > highestSore) {
            playButtonSound(victorySoundId, 1f);
            highestSore = currentScore;
            saveScore();
            vImg.setVisibility(View.VISIBLE);
            vImg.setAnimation(AnimationUtils.loadAnimation(this, R.anim.v_anim));
            title.setText("NEW RECORD");
        }

        nowScore.setText(currentScore + "");
        bestScore.setText(highestSore + "");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("type", "游戏成绩");
        map.put("score", currentScore + "");
        MobclickAgent.onEvent(MainActivity.this, "user_score", map);

        continueBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                startNextExpression();
                SLEEP_TIME = MAX_SLEEP_TIME;
                numOfRight = 0;
                currentScore = 0;
                currentScoreTv.setText(0 + "");
                numOfRightTv.setText(0 + "");//=(TextView) findViewById(R.id.scoreText);
                dlg.cancel();
            }

        });

        exitBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                dlg.cancel();
                finish();
                overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
            }
        });

        shareBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                goToSharePage(currentScore);
                startNextExpression();
                SLEEP_TIME = MAX_SLEEP_TIME;
                numOfRight = 0;
                currentScore = 0;
                numOfRight = 0;
                dlg.cancel();
            }
        });
    }

    protected void goToSharePage(final int currentScore) {
        loadingPd = CustomProgressDialog.show(this, "Loading", false, null);
        new Thread() {
            public void run() {

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");

                intent.putExtra(Intent.EXTRA_SUBJECT, "我真是太尼玛机智啦");
                intent.putExtra(Intent.EXTRA_TITLE, "快来看，我真是个数学天才，我在Flashing Math中获得了" + currentScore + "分的成绩~~大家也来下载玩玩吧" + Constants.APP_URL);
                intent.putExtra(Intent.EXTRA_TEXT, "快来看，我真是个数学天才，我在Flashing Math中获得了" + currentScore + "分的成绩~~大家也来下载玩玩吧" + Constants.APP_URL);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(Intent.createChooser(intent,
                        "Share"), 0x88);

            }
        }.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x88) {
            if (loadingPd != null && loadingPd.isShowing())
                loadingPd.dismiss();

            currentScoreTv.setText(0 + "");
            numOfRightTv.setText(0 + "");//=(TextView) findViewById(R.id.scoreText);
        }
    }

    public void buttonClick(View v) {
        if (currentExpression.hasAnswered && !myThread.isCounting()) return;

        if (viberateEanbled) mVibrator.vibrate(30);

        if (myThread.isCounting()) {
            String read = ((Button) v).getText().toString();
            int num = read.charAt(0) - '0';
            resultTv.setText(read);
            if (currentExpression.getResult() == num) {
                playButtonSound(buttonRightSoundId, 0.5f);

                currentExpression.setHasAnswered(true);

                resultTv.setTextColor(Color.GREEN);
                currentScore += (MAX_SLEEP_TIME - SLEEP_TIME + 1) * 2;

                if (SLEEP_TIME == 9)
                    currentScore += 11;
                else if (SLEEP_TIME == 10)
                    currentScore += 6;
                else if (SLEEP_TIME == 11)
                    currentScore += 3;
                else if (SLEEP_TIME == 12)
                    currentScore += 1;

                numOfRight++;
                numOfRightTv.setText(numOfRight + "");

                currentExpression = expressionGernerate(0);
                startNextExpression();

                if (numOfRight % ((((MAX_SLEEP_TIME - SLEEP_TIME) >> 2) + 1) << 2) == 0 /*&& SLEEP_TIME>7*/)//SLEEP_TIME-=1;
                {
                    if (SLEEP_TIME > 9) {
                        SLEEP_TIME -= 1;
                    } else {
                        currentScore += 7;
                        myThread.stopCounting();
                    }
                }

                bestScoreTv.setText((highestSore > currentScore ? highestSore : currentScore) + "");
                currentScoreTv.setText(this.currentScore + "");
                if (!myThread.isRunning()) myThread.startRuning();
                showUI();
            } else {
                playButtonSound(buttonSoundId, 1f);
                currentExpression.setHasAnswered(true);
                resultTv.setTextColor(Color.RED);
                myThread.stopCounting();
            }
        }
    }

    public void autoTest() {
        if (currentExpression.hasAnswered && !myThread.isCounting()) return;

        //if(viberateEanbled)mVibrator.vibrate( 30);

        if (myThread.isCounting()) {

            int num = currentExpression.getResult();
            String read = num + "";
            resultTv.setText(read);

            if (currentExpression.getResult() == num) {
                //playButtonSound(buttonRightSoundId,0.5f);

                currentExpression.setHasAnswered(true);

                resultTv.setTextColor(Color.GREEN);
                currentScore += (MAX_SLEEP_TIME - SLEEP_TIME + 1);

                if (SLEEP_TIME == 9)
                    currentScore += 11;
                else if (SLEEP_TIME == 10)
                    currentScore += 6;
                else if (SLEEP_TIME == 11)
                    currentScore += 3;
                else if (SLEEP_TIME == 12)
                    currentScore += 1;

                numOfRight++;
                numOfRightTv.setText(numOfRight + "");

                currentExpression = expressionGernerate(0);
                startNextExpression();

                if (numOfRight % ((((MAX_SLEEP_TIME - SLEEP_TIME) >> 2) + 1) << 3) == 0 /*&& SLEEP_TIME>7*/)//SLEEP_TIME-=1;
                {
                    if (SLEEP_TIME > 9) {
                        SLEEP_TIME -= 1;
                    } else {
                        currentScore += 7;
                        myThread.stopCounting();
                    }
                }

                bestScoreTv.setText((highestSore > currentScore ? highestSore : currentScore) + "");
                currentScoreTv.setText(this.currentScore + "");

                if (!myThread.isRunning()) myThread.startRuning();
                showUI();
            } else {
                //playButtonSound(buttonSoundId,1f);
                currentExpression.setHasAnswered(true);
                resultTv.setTextColor(Color.RED);
                myThread.stopCounting();
            }
        }
    }

    public void playButtonSound(final int id, final float v) {
        if (soundEnabled)
            new Thread() {
                public void run() {
                    int i = 0;
                    while (i == 0)
                        i = soundPool.play(id, v, v, 100, 0, 1);
                }
            }.start();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            overridePendingTransition(R.anim.zoomin, R.anim.zoomout);

        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            new PopupWindows(MainActivity.this, container);

        } else if (keyCode == KeyEvent.KEYCODE_HOME) {

        }
        return super.onKeyDown(keyCode, event);
    }

    public class PopupWindows extends PopupWindow {

        public PopupWindows(final Context mContext, View parent) {

            View view = View
                    .inflate(mContext, R.layout.pop_ll, null);
            view.startAnimation(AnimationUtils.loadAnimation(mContext,
                    R.anim.fade_ins));
            LinearLayout input_ll = (LinearLayout) view
                    .findViewById(R.id.ll_popup);
            input_ll.startAnimation(AnimationUtils.loadAnimation(mContext,
                    R.anim.push_bottom_in_2));

            setWidth(LayoutParams.FILL_PARENT);
            setHeight(LayoutParams.WRAP_CONTENT);
            setBackgroundDrawable(new BitmapDrawable());
            setFocusable(true);
            setOutsideTouchable(true);
            setContentView(view);
            setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            showAtLocation(parent, Gravity.BOTTOM, 0, 0);
            update();

            final Button sound = (Button) view
                    .findViewById(R.id.sound);
            sound.setText(soundEnabled ? "Disable Music" : "Enable Music");
            sound.setTypeface(tf);
            final Button viberate = (Button) view
                    .findViewById(R.id.viberate);
            viberate.setText(viberateEanbled ? "Disable Viberation" : "Enable Viberation");
            viberate.setTypeface(tf);
            Button cancel = (Button) view
                    .findViewById(R.id.cancel);
            cancel.setTypeface(tf);

            Button more = (Button) view
                    .findViewById(R.id.more);
            more.setTypeface(tf);

            sound.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    soundEnabled = !soundEnabled;
                    sound.setText(soundEnabled ? "Disable Music" : "Enable Music");
                    saveConfig();
                }
            });

            viberate.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    viberateEanbled = !viberateEanbled;
                    viberate.setText(viberateEanbled ? "Disable Viberation" : "Enable Viberation");
                    saveConfig();
                }
            });

            cancel.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    dismiss();
                }
            });

            more.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
                    dismiss();
                }
            });
        }

    }

    public Expression expressionGernerate(int n) {
        Expression e = new Expression();
        e.num1 = nums[new Random().nextInt(nums.length)];
        e.num2 = nums[new Random().nextInt(nums.length)];
        e.op = ops[new Random().nextInt(ops.length)];

        if (e.isValide()) return e;
        else if (n < 5 || currentExpression == null) return expressionGernerate(n + 1);
        else return currentExpression;
    }


    public void startNextExpression() {
        myThread.restartThread();
    }


    private class ExpressionThread extends Thread {
        private int count = 100;
        private boolean counting = false;
        private boolean running = false;
        private boolean ok = true;

        public void run() {
            try {
                while (ok) {
                    if (counting && running) {
                        sleep(SLEEP_TIME);
                        count -= 1;
                        Message m = new Message();
                        m.what = 1112;
                        m.obj = count;

                        myUiHandler.sendMessage(m);
                        if (count < 0) stopCounting();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void restartThread() {
            counting = true;
            count = 100;
            myUiHandler.sendEmptyMessage(1111);
        }

        public boolean isCounting() {
            return counting;
        }

        public void stopCounting() {
            if (isCounting()) myUiHandler.sendEmptyMessage(1113);
            stopRuning();
            counting = false;
            count = 100;
        }

        public boolean isRunning() {
            return running;
        }

        public void stopRuning() {
            running = false;
        }

        public void startRuning() {
            running = true;
        }
    }

    private class Expression {
        int num1;
        int num2;
        char op;

        private int result = -1;
        private boolean hasAnswered = false;

        public int caculate() {
            if (op == '+')
                result = num1 + num2;
            if (op == '-')
                result = num1 - num2;
            if (op == '×')
                result = num1 * num2;
            if (op == '÷')
                if (num1 % num2 == 0)
                    result = num1 / num2;

            return result;
        }

        public boolean isValide() {

            caculate();
            return (result > 0 && result <= 9);
        }

        public int getResult() {
            return result;
        }

        public boolean hasAnswered() {
            return hasAnswered;
        }

        public void setHasAnswered(boolean h) {
            this.hasAnswered = h;
        }
    }
}
