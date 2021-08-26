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

package pl.dmotyka.cryptonose2.updatechecker;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpdateChecker {

    public static final Logger logger = Logger.getLogger(UpdateChecker.class.getName());

    public static final String GITHUB_USER = "dawidm";
    public static final String GITHUB_REPO = "cryptonose2";

    public static final String GITHUB_URL = String.format("https://api.github.com/repos/%s/%S/releases", GITHUB_USER, GITHUB_REPO);

    public static VersionInfo getNewVersionURLOrNull() throws GetVersionException, IOException {
        String currentVer = System.getProperty("version");
        if (currentVer==null)
            return null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode releasesJsonNode = objectMapper.readValue(new URL(GITHUB_URL), JsonNode.class);
            logger.fine(String.format("got %d releases from github", releasesJsonNode.size()));
            JsonNode releaseJsonNode = releasesJsonNode.get(0);
            String tagName = releaseJsonNode.get("tag_name").asText();
            logger.fine("newest tag name: " + tagName);
            if (tagName.charAt(0) != 'v')
                throw new IllegalStateException(String.format("incorrect tag (%s) on newest asset on github", tagName));
            tagName = tagName.substring(1);
            int[] versionRelease = versionFromString(tagName);
            int[] versionCurrent = versionFromString(currentVer);
            if (versionIsHigher(versionRelease, versionCurrent)) {
                logger.fine(String.format("new version available: %s", tagName));
                return new VersionInfo(tagName, releaseJsonNode.get("html_url").asText(), releaseJsonNode.get("body").asText());
            }
            else
                logger.fine(String.format("release version: %s not newer than current: %s", tagName, currentVer));
                return null;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean versionIsHigher(int[] versionRelease, int[] versionCurrent) {
        return versionRelease[0] > versionCurrent[0] ||
                (versionRelease[0] == versionCurrent[0] && versionRelease[1] > versionCurrent[1]) ||
                (versionRelease[0] == versionCurrent[0] && versionRelease[1] == versionCurrent[1] && versionRelease[2] > versionCurrent[2]);
    }

    private static int[] versionFromString(String versionString) throws GetVersionException{
        String[] versionSplit = versionString.split(Pattern.quote("."));
        if (versionSplit.length!=3)
            throw new GetVersionException(String.format("incorrect version (%s) in newest release on github", versionString));
        try {
            int majorVer = Integer.parseInt(versionSplit[0]);
            int minorVer = Integer.parseInt(versionSplit[1]);
            int pathVer = Integer.parseInt(versionSplit[2]);
            return new int[] {majorVer, minorVer, pathVer};
        } catch (NumberFormatException e) {
            throw new GetVersionException(String.format("incorrect version (%s) in newest release on github", versionString));
        }
    }


}
