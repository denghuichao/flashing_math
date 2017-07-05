package com.deng.flashingmath;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.umeng.analytics.MobclickAgent;


public class Welcome extends Activity{
	
	
	private ImageView welcomeImg;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		final View view = View.inflate(this, R.layout.welcome_page, null);
		
		
		setContentView(view);
		welcomeImg=(ImageView) findViewById(R.id.welcome_img);
		welcomeImg.setBackgroundResource(R.drawable.wellcomebg);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		double diagonalPixels = Math.sqrt(Math.pow(dm.widthPixels, 2) + Math.pow(dm.heightPixels, 2));
		double screenSize = diagonalPixels / (160*dm.density);
		
		AlphaAnimation aa = new AlphaAnimation(0.1f,0.9f);
		aa.setDuration(2500);
		view.startAnimation(aa);
		aa.setAnimationListener(new AnimationListener()
		{
			public void onAnimationEnd(Animation arg0) {
				redirectTo();
		}
			public void onAnimationRepeat(Animation animation) {}
			public void onAnimationStart(Animation animation) {}
			
	   });	
	}

	public void onResume() {
		super.onResume();
		 MobclickAgent.onResume(this);
	 }
     public void onPause() {
		 super.onPause();
		 MobclickAgent.onPause(this);
  }

	private void redirectTo(){    
		Intent intent =new Intent(Welcome.this,MainActivity.class);
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
    }
}

