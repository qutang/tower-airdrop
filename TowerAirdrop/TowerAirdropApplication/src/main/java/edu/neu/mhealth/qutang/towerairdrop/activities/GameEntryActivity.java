package edu.neu.mhealth.qutang.towerairdrop.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import edu.neu.mhealth.qutang.towerairdrop.Constants;
import edu.neu.mhealth.qutang.towerairdrop.Flags;
import edu.neu.mhealth.qutang.towerairdrop.GameMusicPlayer;
import edu.neu.mhealth.qutang.towerairdrop.R;
import edu.neu.mhealth.qutang.towerairdrop.Constants.RESUME_FROM;

public class GameEntryActivity extends Activity implements OnClickListener{
	private ImageButton btnNewGame;
	private ImageButton btnScore;
	private ImageButton btnQuit;
	private ImageButton btnSet;
	private ImageButton btnResume;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_entry);
		
		btnNewGame = (ImageButton) findViewById(R.id.button_new_tower);
		btnScore = (ImageButton) findViewById(R.id.button_tower_rank);
		btnSet = (ImageButton) findViewById(R.id.button_tower_set);
		btnQuit = (ImageButton) findViewById(R.id.button_quit_tower);
		btnResume = (ImageButton) findViewById(R.id.button_resume_tower);
		
		btnNewGame.setOnClickListener(this);
		btnScore.setOnClickListener(this);
		btnSet.setOnClickListener(this);
		btnQuit.setOnClickListener(this);
		btnResume.setOnClickListener(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.final_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.finalsettings:
			startActivity(new Intent(this, GameSettingActivity.class));
			return true;
		}
		return false;
	}
	
	
	
	@Override
	protected void onPause() {
		super.onPause();
		GameMusicPlayer.stopBackgroundMusic(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		GameMusicPlayer.playBackgroundMusic(this, R.raw.canon);
		SharedPreferences pref = Constants.SHAREDPREFERENCES;
		int level = pref.getInt("CURRENT_LEVEL", Constants.BASE_LEVEL);
		int scene = pref.getInt("CURRENT_SCENE", Constants.INIT_SCENARIO);
		if(level == Constants.BASE_LEVEL && scene == Constants.INIT_SCENARIO){
			//hide resume button
			btnResume.setVisibility(View.INVISIBLE);
		}else{
			btnResume.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.button_new_tower:
			Flags.getFlags().resumeFrom = RESUME_FROM.MENU;
			Intent startIntent = new Intent(this, GameActivity.class);
			startIntent.putExtra("CONTINUE", false);
			startActivity(startIntent);
			break;
		case R.id.button_resume_tower:
			Flags.getFlags().resumeFrom = RESUME_FROM.MENU;
			Intent resumeIntent = new Intent(this, GameActivity.class);
			resumeIntent.putExtra("CONTINUE", true);
			startActivity(resumeIntent);
			break;
		case R.id.button_tower_rank:
			Intent scoreIntent = new Intent(this, GameScoreActivity.class);
			startActivity(scoreIntent);
			break;
		case R.id.button_tower_set:
			startActivity(new Intent(this, GameSettingActivity.class));
			break;
		case R.id.button_quit_tower:
			finish();
			break;
		}
	}
	
}
