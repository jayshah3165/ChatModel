package com.echo.allscenarioapp.custom;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;
import java.util.HashMap;

public class PicassoVideoFrameRequestHandler extends RequestHandler {
    @Override
    public boolean canHandleRequest(Request data) {
        return true;
    }

    @Override
    public Result load(Request data, int networkPolicy) throws IOException {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(data.uri.toString(), new HashMap<String, String>());
        Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime();
        return new Result(bitmap, Picasso.LoadedFrom.DISK);
    }
}