package derpibooru.derpy.server.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import derpibooru.derpy.data.types.ImageFullInfo;
import derpibooru.derpy.data.types.ImageThumb;

public class JsonParser {
    private String mRawJson;

    public JsonParser(String raw) {
        mRawJson = raw;
    }

    public ArrayList<ImageThumb> readImageThumbs() {
        ArrayList<ImageThumb> output = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(mRawJson);
            JSONArray images = json.getJSONArray("images");

            int imgCount = images.length();
            for (int x = 0; x < imgCount; x++) {
                JSONObject img = images.getJSONObject(x);

                /* TODO: move the magic strings into a dedicated data structure */
                ImageThumb it = new ImageThumb(img.getInt("id_number"),
                        img.getInt("score"), img.getInt("upvotes"),
                        img.getInt("downvotes"), img.getInt("faves"),
                        img.getInt("comment_count"),
                        img.getJSONObject("representations").getString("thumb"),
                        img.getString("image"),
                        img.getString("source_url"),
                        img.getString("uploader"),
                        img.getString("description"));

                output.add(it);
            }

            return output;
        } catch (JSONException e) {
            Log.e("Json readImageThumbs", "Could not process JSON response.");
            return new ArrayList<>();
        }
    }
}
