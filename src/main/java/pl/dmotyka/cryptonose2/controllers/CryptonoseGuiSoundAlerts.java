/*
 * Cryptonose
 *
 * Copyright © 2019-2021 Dawid Motyka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package pl.dmotyka.cryptonose2.controllers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import pl.dmotyka.cryptonose2.dataobj.PriceAlert;
import pl.dmotyka.cryptonose2.settings.CryptonoseSettings;

public class CryptonoseGuiSoundAlerts {

    public static final int ALERT_RISING = 1;
    public static final int ALERT_DROPPING = 2;

    private static final Logger logger = Logger.getLogger(CryptonoseGuiSoundAlerts.class.getName());

    private Clip clip;
    private AudioInputStream audioInputStream;

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
        try {
            URL audioURL;
            if (type == ALERT_RISING) {
                String audioPath = CryptonoseSettings.getString(CryptonoseSettings.General.SOUND_RISING_FILE_PATH);
                if (CryptonoseSettings.getBool(CryptonoseSettings.General.USE_DEF_RISING_SOUND) || audioPath == null || audioPath.isBlank()) {
                    audioURL = Objects.requireNonNull(getClass().getClassLoader().getResource(CryptonoseSettings.DEFAULT_RISING_SOUND_FILE));
                } else {
                    audioURL = new File(audioPath).toURI().toURL();
                }
            } else {
                String audioPath = CryptonoseSettings.getString(CryptonoseSettings.General.SOUND_DROPPING_FILE_PATH);
                if (CryptonoseSettings.getBool(CryptonoseSettings.General.USE_DEF_DROPPING_SOUND) || audioPath == null || audioPath.isBlank()) {
                    audioURL = Objects.requireNonNull(getClass().getClassLoader().getResource(CryptonoseSettings.DEFAULT_DROPPING_SOUND_FILE));
                } else {
                    audioURL = new File(audioPath).toURI().toURL();
                }
            }
            logger.fine("Audio file: " + audioURL.getFile());
            URL finalAudioURL = audioURL;
            new Thread(() -> playFile(finalAudioURL)).start();
        } catch (MalformedURLException e) {
            logger.severe("cannot play custom audio file (malformed audio file path)");
        }
    }

    private synchronized void playFile(URL audioURL) {
        try {
            if (clip != null) {
                if (clip.isRunning())
                    return;
                clip.close();
            }
            if (audioInputStream != null)
                audioInputStream.close();
            audioInputStream = AudioSystem.getAudioInputStream(audioURL);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            logger.severe("When playing audio file: " + e.getLocalizedMessage());
        }
    }


}
