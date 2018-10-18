package com.adidas.hackathon.smartjacket.util;

import android.media.AudioManager;
import android.view.KeyEvent;

public class Media {

    /**
     * Increase the media volume.
     */
    public static void mediaVolumeUp(AudioManager audioManager) {
        int currVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (currVol < maxVol) {
            currVol++;
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currVol, 0);
        }
    }

    /**
     * Decrease the media volume.
     */
    public static void mediaVolumeDown(AudioManager audioManager) {
        int currVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int minVol = 0;
        if (currVol > minVol) {
            currVol--;
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currVol, 0);
        }
    }

    /**
     * Toggle the current media state between play and pause.
     */
    public static void mediaPlayPause(AudioManager audioManager) {
        audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
        audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
    }

    /**
     * Go to the next song.
     */
    public static void mediaNext(AudioManager audioManager) {
        audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
        audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));
    }

    /**
     * Get back to the previous song.
     */
    public static void mediaPrevious(AudioManager audioManager) {
        audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
        audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
    }

}
