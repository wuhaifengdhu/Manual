package com.haifwu.manualspeechtest;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class LyricSearch extends AsyncTask<String, Void, String>{
	
	private Context context = null;
	private ProgressDialog pregDialog = null;
	
	public void setContext(Context context) {
		this.context = context;
	}


	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		pregDialog.dismiss();
	}



	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		pregDialog = new ProgressDialog(context);
		pregDialog.setMessage("识别中...");
		pregDialog.setIndeterminate(false);
		pregDialog.show();
	}



	@Override
	protected String doInBackground(String... arg0) {
		// TODO Auto-generated method stub
		String nameRet = "未识别出来";
		ZhiDaoParse zhidaoParse = new ZhiDaoParse(arg0[0]);
		Log.i("ZhiDaoParse","要识别的歌词：" + arg0[0]);
		nameRet = zhidaoParse.getSongName();
		return nameRet;
    }
	
}
