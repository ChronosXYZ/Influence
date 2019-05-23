/*
 *    Copyright 2019 ChronosX88
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.chronosx88.influence.helpers;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {
    public static String sha1(final String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(text.getBytes("UTF-8"), 0, text.length());
            byte[] sha1hash = md.digest();
            return hashToString(sha1hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String hashToString(final byte[] buf) {
        if (buf == null) return "";
        int l = buf.length;
        StringBuffer result = new StringBuffer(2 * l);
        for (int i = 0; i < buf.length; i++) {
            appendByte(result, buf[i]);
        }
        return result.toString();
    }

    private final static String HEX_PACK = "0123456789ABCDEF";

    private static void appendByte(final StringBuffer sb, final byte b) {
        sb
                .append(HEX_PACK.charAt((b >> 4) & 0x0f))
                .append(HEX_PACK.charAt(b & 0x0f));
    }
}
