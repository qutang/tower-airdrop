/***
 * Excerpted from "Hello, Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
***/
package edu.neu.madcourse.qutang.towerairdrop;
import edu.neu.madcourse.qutang.towerairdrop.R;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class FinalPrefs extends PreferenceActivity {
   // Option names and default values
   private static final String OPT_MUSIC = "music";
   private static final boolean OPT_MUSIC_DEF = true;
   
   private static final String OPT_VIBRATION = "vibration";
   private static final boolean OPT_VIBRATION_DEF = true;
   
   private static final String OPT_INSTRUCTION = "instruction";
   private static final boolean OPT_OPT_INSTRUCTION_DEF = true;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.final_settings);
   }
   /** Get the current value of the music option */
   
   public static boolean getMusic(Context context) {
      return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(OPT_MUSIC, OPT_MUSIC_DEF);
   }
   
   public static boolean getVibration(Context context) {
	      return PreferenceManager.getDefaultSharedPreferences(context)
	            .getBoolean(OPT_VIBRATION, OPT_VIBRATION_DEF);
	   }
   
   public static boolean getInstruction(Context context) {
	      return PreferenceManager.getDefaultSharedPreferences(context)
	            .getBoolean(OPT_INSTRUCTION, OPT_OPT_INSTRUCTION_DEF);
	   }
   
   public static void disableInstruction(Context context){
	   Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
	   edit.putBoolean(OPT_INSTRUCTION, false);
	   edit.commit();
   }
  
}
