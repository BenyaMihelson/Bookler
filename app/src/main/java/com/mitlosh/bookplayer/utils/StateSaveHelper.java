package com.mitlosh.bookplayer.utils;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

public class StateSaveHelper {
	
	private static Map<String,Bundle> stateMap = new HashMap<>();

	public static void onSaveInstanceState(String key, Bundle outState) {
		stateMap.put(key, outState);
	}
	
	public static Bundle restoreState(String key){
		return stateMap.get(key);
	}

	public static Bundle restoreState(String key, boolean remove){
		if(remove){
			return stateMap.remove(key);
		}else{
			return restoreState(key);
		}
	}
	
	public static void clearAll(){
		stateMap.clear();		
	}
	
}
