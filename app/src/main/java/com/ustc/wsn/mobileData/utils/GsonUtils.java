package com.ustc.wsn.mobileData.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by Reinhard Tristan Eugen Heydrich on 2016/12/11 12:22
 */
public class GsonUtils {
	private static Gson mGson;

	public static <T> T fromData(String s, Class<T> classOfT){
		mGson = new GsonBuilder()
				.serializeNulls()
				.create();
		return mGson.fromJson(s,classOfT);
	}
	public static <T> T fromUnencryptedData(String s, Class<T> classOfT){
		mGson = new GsonBuilder()
				.serializeNulls()
				.create();
		String json = s;
		return mGson.fromJson(json,classOfT);
	}

	public static <T> ArrayList<T> jsonToArrayList(String json, Class<T> clazz)
	{
		Type type = new TypeToken<ArrayList<JsonObject>>()
		{}.getType();
		ArrayList<JsonObject> jsonObjects = new GsonBuilder()
				.serializeNulls()
				.create().fromJson(json, type);

		ArrayList<T> arrayList = new ArrayList<>();
		for (JsonObject jsonObject : jsonObjects)
		{
			arrayList.add(new GsonBuilder().serializeNulls().create().fromJson(jsonObject, clazz));
		}
		return arrayList;
	}


}