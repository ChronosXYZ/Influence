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

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.stfalcon.chatkit.commons.ImageLoader;

import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import io.github.chronosx88.influence.R;
import java9.util.concurrent.CompletableFuture;

public class AvatarImageLoader implements ImageLoader {
    private Fragment fragment = null;
    private Activity activity = null;

    public AvatarImageLoader(Fragment fragment) {
        this.fragment = fragment;
    }

    public AvatarImageLoader(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
        if(url.length() != 0) {
            if(!url.contains("http")) {
                if(AppHelper.avatarsCache.containsKey(url)) {
                    byte[] avatarBytes = AppHelper.avatarsCache.get(url);
                    Bitmap avatar = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
                    imageView.setImageBitmap(avatar);
                    return;
                }
                String firstLetter = Character.toString(Character.toUpperCase(url.charAt(0)));
                imageView.setImageDrawable(TextDrawable.builder()
                        .beginConfig()
                        .width(64)
                        .height(64)
                        .endConfig()
                        .buildRound(firstLetter, ColorGenerator.MATERIAL.getColor(firstLetter)));
                CompletableFuture.supplyAsync(() -> {
                    while (AppHelper.getXmppConnection() == null);
                    while (AppHelper.getXmppConnection().isConnectionAlive() != true);
                    EntityBareJid jid = null;
                    try {
                        jid = JidCreate.entityBareFrom(url);
                    } catch (XmppStringprepException e) {
                        e.printStackTrace();
                    }
                    return AppHelper.getXmppConnection().getAvatar(jid);
                }).thenAccept((avatarBytes) -> {
                    AppHelper.getMainUIThread().post(() -> {
                        if(avatarBytes != null) {
                            Bitmap avatar = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
                            imageView.setImageBitmap(avatar);
                            AppHelper.avatarsCache.put(url, avatarBytes);
                        }
                    });
                });
            } else {
                RequestOptions requestOptions = new RequestOptions()
                        .placeholder(R.drawable.no_image_picture)
                        .error(R.drawable.no_image_picture);
                if(fragment != null) {
                    Glide.with(fragment).setDefaultRequestOptions(requestOptions).load(url).into(imageView);
                } else if(activity != null) {
                    Glide.with(activity).setDefaultRequestOptions(requestOptions).load(url).into(imageView);
                }
            }
        }
    }
}
