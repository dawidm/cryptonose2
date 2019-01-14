package com.dawidmotyka.cryptonose2;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Created by dawid on 8/15/17.
 */
public class CryptonoseGuiSoundAlerts {

    public static final int ALERT_RISING = 1;
    public static final int ALERT_FALLING = 2;

    private static final Logger logger = Logger.getLogger(CryptonoseGuiSoundAlerts.class.getName());

    private final Preferences preferences;
    Clip clip;
    AudioInputStream audioInputStream;

    public CryptonoseGuiSoundAlerts(Preferences preferences) {
        this.preferences = preferences;
    }

    public static AudioFileFormat.Type[] getAudioFileTypes() {
        return AudioSystem.getAudioFileTypes();
    }

    public void soundAlert(PriceAlert priceAlert) {
        if(priceAlert.getPriceChange()>=0)
            soundAlert(CryptonoseGuiSoundAlerts.ALERT_RISING);
        else
            soundAlert(CryptonoseGuiSoundAlerts.ALERT_FALLING);
    }

    public void soundAlert(int type) {
        String audioPath;
        if(type==ALERT_RISING)
            audioPath = preferences.get("soundRisingPath", "");
        else
            audioPath = preferences.get("soundDroppingPath", "");
        if (audioPath.equals("")) {
            logger.warning("Alert sound file not set");
        }
        new Thread(()->playFile(audioPath)).start();
    }

    private synchronized void playFile(String filename) {
        try {
            if (clip != null) {
                if (clip.isRunning())
                    return;
                else {
                    clip.close();
                    audioInputStream.close();
                }
            }
            audioInputStream = AudioSystem.getAudioInputStream(new File(filename));
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            logger.severe("When playing audio file: " + e.getLocalizedMessage());
        }
    }


}
