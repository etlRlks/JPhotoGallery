package com.jogue.photogallery.utils;

import android.net.Uri;
import android.util.Log;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.jogue.photogallery.bean.GalleryItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jogue- on 2016/8/15.
 */
public class FlickFetchr {

    private static final String TAG = "FlickFetchr";
    private static final String API_KEY = "693a795650ac2e9873a983a9affb7246";
    private static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";
    private static final String SERACH_METHOD = "flickr.photos.search";

    private static final Uri ENDPOINT = Uri
            .parse("https://api.flickr.com/services/rest")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();

    /*OkHttpClient mOkHttpClient = new OkHttpClient();
    String post(String url, String json) throws IOException {
        FormBody formBody = new FormBody.Builder()
                .add("api_key", API_KEY)
                .add("format", "json")
                .add("nojsoncallback", "1")
                .add("extras", "url_s")
                .build();
        Request request = new Request.Builder()
                .url("https://api.flickr.com/services/rest")
                .post(formBody)
                .build();
        Response response = mOkHttpClient.newCall(request).execute();
    }*/


    public byte[] getUrlBytes(String urlSpec) throws IOException {
        //1. 创建URL对象
        URL url = new URL(urlSpec);
        //2. 使用URL创建链接，url.openConnection()返回一个URLConnection对象，需要进行转换
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage()
                        + ": with " + urlSpec);
            }
            int byteRead = 0;
            byte[] buffer = new byte[1024];
            while ((byteRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, byteRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    /*
    获取图片
     */
    public List<GalleryItem> fetchRecentPhotos() {
        String url = buildUrl(FETCH_RECENTS_METHOD, null);
        return downloadGalleryItems(url);
    }
    /*
    搜索图片
     */

    public List<GalleryItem> searchPhotos(String query) {
        String url = buildUrl(SERACH_METHOD, query);
        return downloadGalleryItems(url);
    }

    //接受url，不再需要在内部自己创建url
    private List<GalleryItem> downloadGalleryItems(String url) {
        List<GalleryItem> items = new ArrayList<>();
        try {
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);// 接受List<>, JSONObject
            /*
            challenge
             */
//            parseGsonItems(items, jsonString);
        } catch (IOException e) {
            Log.e(TAG, "Failed to fetch items", e);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }
        return items;
    }

    private String buildUrl(String method, String query) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", method);
        if (method.equals(SERACH_METHOD)) {
            uriBuilder.appendQueryParameter("text", query);
        }
        return uriBuilder.build().toString();
    }

    private void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws JSONException {
        //JSONObject：photos
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        //JSONArray: photos下的JSON数组photo/
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");
        //循环遍历JSON数组photo中的每一个JSON对象
        for (int i = 0; i < photoJsonArray.length(); i++) {
            //jSON数组中的每一个JSON对象
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);
            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));
            if (!photoJsonObject.has("url_s")) {
                continue;
            }
            item.setUrl(photoJsonObject.getString("url_s"));
            item.setOwner(photoJsonObject.getString("owner"));
            items.add(item);
        }
    }

    /*
    challenge:Gson实现
     */
    private void parseGsonItems(List<GalleryItem> items, String jsonBody) {
//        TypeAdapter 以及 JsonSerializer 和 JsonDeserializer都需要与
//        GsonBuilder.registerTypeAdapter 示或GsonBuilder.registerTypeHierarchyAdapter配合使用
        Gson gson = new GsonBuilder()
                //为GalleryItem注册TypeAdapter
                .registerTypeAdapter(GalleryItem[].class, new ChallengeDeserializer())
                .create();
        GalleryItem[] photosList = gson.fromJson(jsonBody, GalleryItem[].class);
        for (GalleryItem item : photosList
                ) {
            if (item.getUrl() == null) {
                items.add(item);
            }
        }
    }


//    setFieldNamingStrategy(new FieldNamingStrategy())
//    该方法需要与FieldNamingStrategy接口配合使用
    class ChallengeDeserializer implements JsonDeserializer<GalleryItem[]> {  //反序列化

        @Override
        public GalleryItem[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            //从JSON获取"photos"元素
            JsonElement photos = json.getAsJsonObject().get("photos");
            JsonElement photoArray = photos.getAsJsonObject().get("photo");
            //setFieldNamingStrategy(new FieldNamingStrategy())
            // 该方法需要与FieldNamingStrategy接口配合使用
            Gson gson = new GsonBuilder()
                    .setFieldNamingStrategy(new ChallengeFieldNamingStrategy())
                    .create();
            return gson.fromJson(photoArray, GalleryItem[].class);
        }
    }

    /*
    实现FieldNamingStrategy接口
     */
    class ChallengeFieldNamingStrategy implements FieldNamingStrategy {

        //实现自己的规则
        //但是@SerializedName有最高优先级，
        // 在加有@SerializedName注解的字段上FieldNamingStrategy不生效！
        @Override
        public String translateName(Field f) {
            switch (f.getName()) {
                case "mId":
                    return "id";
                case "mCaption":
                    return "title";
                case "mUrl":
                    return "url_s";
                default:
                    return f.getName();
            }
        }
    }
}
