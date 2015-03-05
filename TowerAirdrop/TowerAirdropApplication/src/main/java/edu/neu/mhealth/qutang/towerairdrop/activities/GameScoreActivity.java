package edu.neu.mhealth.qutang.towerairdrop.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import edu.neu.mhealth.qutang.towerairdrop.Constants;
import edu.neu.mhealth.qutang.towerairdrop.GameApplication;
import edu.neu.mhealth.qutang.towerairdrop.R;

public class GameScoreActivity extends Activity implements OnClickListener{

	private ImageButton singleHelp;
	private ImageButton lifeHelp;
	private Dialog dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_score);
		if(Constants.SHAREDPREFERENCES == null){
			Constants.SHAREDPREFERENCES=getSharedPreferences("game_score", Context.MODE_PRIVATE);//change to private, so that only accessible by this package
		}
		singleHelp = (ImageButton) findViewById(R.id.help_image);
		lifeHelp = (ImageButton) findViewById(R.id.help_life_image);
		
		singleHelp.setOnClickListener(this);
		lifeHelp.setOnClickListener(this);
		
		setRankData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.game_score, menu);
		return true;
	}
	
	public void setRankData(){
		int hightestScore = Integer.valueOf(Constants.SHAREDPREFERENCES.getString("HightestScore", "0"));
		int totalScore = Integer.valueOf(Constants.SHAREDPREFERENCES.getString("TotalScore", "0"));
		int totalLevel = Integer.valueOf(Constants.SHAREDPREFERENCES.getString("totalLevel", "0"));
		int SingleLevel = Integer.valueOf(Constants.SHAREDPREFERENCES.getString("SingleLevel", "0"));
		
		//Most Box
		TextView mostBox = (TextView) findViewById(R.id.single_level);
		mostBox.setText(String.valueOf(SingleLevel));
		
		//Most Coin
		TextView mostCoin = (TextView) findViewById(R.id.single_points);
		mostCoin.setText(String.valueOf(hightestScore));
		
		//Total Box
		TextView lifeBox = (TextView) findViewById(R.id.life_level);
		lifeBox.setText(String.valueOf(totalLevel));
		
		TextView lifeScore = (TextView) findViewById(R.id.life_points);
		lifeScore.setText(String.valueOf(totalScore));
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.help_image:
			okEnablePopDialog("This score will be calculated/updated only under the following condition: (1) the box is completely broken; or (2) the parachute is not triggered successfully.");
			break;
		case R.id.help_life_image:
			okEnablePopDialog("This score is a cumulative score defined as the total number of boxes and score you have earned throughout the lifetime.");
			break;
		}
	}
	
	public void okEnablePopDialog(String msg) {// position:
		// Gravity.Bottom
		if (dialog == null || !dialog.isShowing()) {
			dialog = new Dialog(this, R.style.DialogBox);
			dialog.setCancelable(false);
			dialog.setContentView(R.layout.close);
			Button quiteGame = (Button) dialog.findViewById(R.id.quit_btn);
			quiteGame.setVisibility(View.INVISIBLE);
			TextView text = (TextView) dialog.findViewById(R.id.text);
			Typeface face = Typeface.createFromAsset(GameApplication.getAppContext().getAssets(), "fonts/Comic.ttf");
			text.setTypeface(face);
			text.setText(msg);
			final Button ok_btn = (Button) dialog.findViewById(R.id.ok_btn);
			ok_btn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			dialog.show();
		}
	}

}
