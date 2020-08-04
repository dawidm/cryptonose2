/*
 * Cryptonose2
 *
 * Copyright © 2019-2020 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.cryptonose2;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class CryptonoseGuiSoundAlerts {

    public static final int ALERT_RISING = 1;
    public static final int ALERT_DROPPING = 2;
    public static final String DEFAULT_RISING_SOUND_FILE="soundR.wav";
    public static final String DEFAULT_DROPPING_SOUND_FILE="soundD.wav";

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
            soundAlert(CryptonoseGuiSoundAlerts.ALERT_DROPPING);
    }

    public void soundAlert(int type) {
        String audioPath = null;
        if(type==ALERT_RISING) {
            audioPath = preferences.get("soundRisingPath", "");
            if (preferences.getBoolean("defaultRisingSound", true) || audioPath == null || audioPath.isBlank()) {
                audioPath = Objects.requireNonNull(getClass().getClassLoader().getResource(DEFAULT_RISING_SOUND_FILE)).getFile();
            }
        } else {
            audioPath = preferences.get("soundDroppingPath", "");
            if (preferences.getBoolean("defaultDroppingSound",true) || audioPath == null || audioPath.isBlank()) {
                audioPath = Objects.requireNonNull(getClass().getClassLoader().getResource(DEFAULT_DROPPING_SOUND_FILE)).getFile();
            }
        }
        logger.fine("Audio file path: " + audioPath);
        String finalAudioPath = audioPath;
        new Thread(()->playFile(finalAudioPath)).start();
    }

    private synchronized void playFile(String filename) {
        try {
            if (clip != null) {
                if (clip.isRunning())
                    return;
                clip.close();
            }
            if (audioInputStream != null)
                audioInputStream.close();
            audioInputStream = AudioSystem.getAudioInputStream(new File(filename));
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            logger.severe("When playing audio file: " + e.getLocalizedMessage());
        }
    }


}
