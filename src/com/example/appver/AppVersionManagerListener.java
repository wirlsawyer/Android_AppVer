package com.example.appver;

public abstract class AppVersionManagerListener {

	public abstract void onDidChecked(boolean hasNew, String url);
}
