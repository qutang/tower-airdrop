package edu.neu.mhealth.qutang.towerairdrop;

import android.app.Application;
import android.content.Context;

public class GameApplication extends Application{

    private static Context context;
    public int score = 0;

    public void onCreate(){
        super.onCreate();
        GameApplication.context = getApplicationContext();
        Constants.SHAREDPREFERENCES=getSharedPreferences("TOWER_AIRDROP", Context.MODE_PRIVATE);//change to private, so that only accessible by this package
    }

    public static Context getAppContext() {
        return GameApplication.context;
    }

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
    
    
}