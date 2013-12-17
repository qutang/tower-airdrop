/***
 * Excerpted from "Hello, Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
 ***/
package edu.neu.madcourse.qutang.towerairdrop;

import android.content.Context;
import android.media.MediaPlayer;

public class Music {
	private static MediaPlayer mp = null;
	private static MediaPlayer background_mp = null;

	/** Stop old song and start new one */

	public static void playSound(Context context, int resource) {
		if (FinalPrefs.getMusic(context)) {
			if (mp == null) {
				mp = MediaPlayer.create(context, resource);
			}
			mp.setLooping(false);
			mp.start();
		}
	}

	public static void stopSound(Context context) {
		if (mp != null) {
			mp.stop();
			mp.release();
			mp = null;
		}
	}

	public static boolean isPlayingSound() {
		if (mp != null) {
			return (mp.isPlaying());
		} else {
			return false;
		}
	}

	public static void playBackgroundMusic(Context context, int resource) {
		if (FinalPrefs.getMusic(context)) {
			if (background_mp == null) {
				background_mp = MediaPlayer.create(context, resource);
			}
			background_mp.setLooping(true);
			background_mp.start();
		}
	}

	public static void stopBackgroundMusic(Context context) {
		if (background_mp != null) {
			background_mp.stop();
			background_mp.release();
			background_mp = null;
		}
	}

	public static boolean isPlayingBackgroundMusic() {
		if (background_mp != null) {
			return (background_mp.isPlaying());
		} else {
			return false;
		}
	}
}
