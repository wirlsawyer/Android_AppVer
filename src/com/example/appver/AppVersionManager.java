package com.example.appver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

public class AppVersionManager {
	final private static String package_name = "com.example.appver";
	private static AppVersionManager mInstance;
	private static AppVersionManagerListener mListener = null;
	
	public static AppVersionManager getInstance() {
		if (mInstance == null)
			mInstance = new AppVersionManager();
		return mInstance;
	}

	public void check(final Activity activity, final AppVersionManagerListener onListener) {
		final String url = "https://play.google.com/store/apps/details?id=" + package_name + "&hl=en";
		
		try {					
			final String curVersion = activity.getPackageManager()
					.getPackageInfo(package_name, 0).versionName;	
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					
					//for test
					//String package_name = "com.asus.wifi.go.nfc";	
					//String url = "https://play.google.com/store/apps/details?id=" + package_name + "&hl=en";
					
					String html = GetWithUrl(url);
					String storeVersion = curVersion;  
					if (html.length() > 0) {
						String strFind = "itemprop=\"softwareVersion\">";
						if (IsExist(html, strFind))
						{
							storeVersion = GetMidWithTagString(html, strFind, "</div>").trim();
							if (storeVersion.length() == 0) {
								callback(activity, onListener, false, url);
								return;
							}
						}					
					}					
								
					//Log.v("ssssss", "curVersion="+curVersion);
					//Log.v("ssssss", "storeVersion="+storeVersion);
					
					final boolean bNeed = (value(curVersion) < value(storeVersion)) ? true : false;
					callback(activity, onListener, bNeed, url);					
				}
			}).start();
			
		} catch (Exception e) {
			//e.printStackTrace();	
			callback(activity, onListener, false, url);
		}		
		
	}

	private void callback(Activity activity, final AppVersionManagerListener onListener, final boolean need, final String url)
	{
		
		if (onListener != null)
		{
			activity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					onListener.onDidChecked(need, url);
				}
			});
		}
	}
	private long value(String string) {		
		string = string.trim().toLowerCase().replace("v", "");
		
		if (string.contains(".")) {
			final int index = string.lastIndexOf(".");
			return value(string.substring(0, index)) * 100
					+ value(string.substring(index + 1));
		} else {
			return Long.valueOf(string);
		}
	}

	private boolean IsExist(String src, String dest)
	{
		return src.contains(dest);	
	}
	
	private String GetMidWithTagString(String context, String begin,
			String end) {

		int pos1 = context.indexOf(begin)+begin.length();
		int pos2 = context.indexOf(end, pos1);		

		if (pos1 == pos2) {
			return "";
		}
		
		if (pos1 > pos2) {
			return "";
		}

		String tmp = context.substring(pos1, pos2);
		return tmp;
	}
	
	private String GetWithUrl(String url) {

		// set HttpClient client
		HttpClient client = new DefaultHttpClient();
		// require Http to get the values
		HttpGet httpGet = new HttpGet(url);
		StringBuilder sb = new StringBuilder();
		httpGet.addHeader("Referer", "http://www.google.com");
         
         
		try {
			// get the reply from http
			HttpResponse response = client.execute(httpGet);
			// get the entity of reply from http
			HttpEntity entity = response.getEntity();
			// get the contents from the entity of reply
			InputStream stream = entity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					stream));
			// count the length of stream of contents from reply
			String line = null;
			// the loop which receiving the reply from http get
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
