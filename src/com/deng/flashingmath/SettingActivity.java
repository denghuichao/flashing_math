package com.deng.flashingmath;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;


public class SettingActivity extends Activity  {
	
	private Typeface tf;
	private TextView teamName;
	private TextView emailText;
	private TextView emailNum;
	private Button rate;
	private Button feedback;
	private Button more;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.setting_layout);
		AssetManager mgr=getAssets();//得到AssetManager
		tf=Typeface.createFromAsset(mgr, "fonts/fontss.TTF");//根据路径得到Typeface
		
		teamName=(TextView)findViewById(R.id.teamname);
		emailText=(TextView)findViewById(R.id.emailtext);
		emailNum=(TextView)findViewById(R.id.emailnum);
		rate=(Button) findViewById(R.id.gradeButton);
		feedback=(Button) findViewById(R.id.contactButton);
		more=(Button) findViewById(R.id.goMarketButton);
		
		teamName.setTypeface(tf);
		emailText.setTypeface(tf);
		emailNum.setTypeface(tf);
		
		rate.setTypeface(tf);
		feedback.setTypeface(tf);
		more.setTypeface(tf);
	}

	public void onResume() {
		super.onResume();
		 MobclickAgent.onResume(this);
	 }
     public void onPause() {
		 super.onPause();
		 MobclickAgent.onPause(this);
  }
  
     public boolean onKeyDown(int keyCode, KeyEvent event) {
     	if (keyCode == KeyEvent.KEYCODE_BACK) { 
     		finish();
     		overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
     	}
     	return super.onKeyDown(keyCode, event);
     }
     
     public void buttonClick(View view)
     {
    	 switch(view.getId())
    	 {
    	 case R.id.gradeButton:
    		 Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.deng.flashingmath"));
			 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			 startActivity(intent);
    		 break;
    	 case R.id.contactButton:
    			FeedbackAgent agent = new FeedbackAgent(this);
    		    agent.startFeedbackActivity();
    		    break;
    	 case R.id.goMarketButton:
    		// goToMarket();
    		 break;
    	 }
     }
     
//     private void goToMarket() {
//    	 GdtAppwall wall =
//		            new GdtAppwall(SettingActivity.this, Constants.APPId, Constants.APPWallPosId, false);
//		        wall.doShowAppWall();
// 	}
}
