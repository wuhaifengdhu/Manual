package com.haifwu.manualspeechtest;

import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;

public class MainActivity extends Activity implements OnClickListener {
	protected static final String TAG = "MainActivity";
	// 语音识别对象。
	private SpeechRecognizer mIat;
	private Toast mToast;
	private String recordText;

	private TextView editor;
	private SharedPreferences mSharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		((Button) findViewById(R.id.sppechButton)).setOnClickListener(this);

		editor = (TextView) findViewById(R.id.showText);
		editor.setMovementMethod(ScrollingMovementMethod.getInstance());
		/*
		 * editor = (TextView)findViewById(R.id.showText);
		 * editor.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		 * editor.setGravity(Gravity.TOP); editor.setSingleLine(false);
		 * editor.setHorizontallyScrolling(false);
		 * editor.setVerticalScrollBarEnabled(true);
		 */

		SpeechUtility.createUtility(MainActivity.this, "appid=5428ea92");
		mSharedPreferences = getSharedPreferences("com.iflytek.setting",
				Activity.MODE_PRIVATE);
		mIat = SpeechRecognizer.createRecognizer(this, mInitListener);
		setParam();
		mToast = Toast.makeText(this, "", Toast.LENGTH_LONG);

	}

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
	 * menu; this adds items to the action bar if it is present.
	 * getMenuInflater().inflate(R.menu.main, menu); return true; }
	 * 
	 * 
	 * @Override public boolean onOptionsItemSelected(MenuItem item) { // TODO
	 * Auto-generated method stub switch(item.getItemId()){ case
	 * R.id.action_chinese: showTip("你选择了中文作为输入语言");
	 * mIat.setParameter(SpeechConstant.LANGUAGE,
	 * mSharedPreferences.getString("iat_language_preference", "zh_cn")); break;
	 * case R.id.action_english:
	 * showTip("You choosed English as the input language"); break; } return
	 * super.onOptionsItemSelected(item); }
	 */

	/**
	 * 初始化监听器。
	 */
	private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			// TODO Auto-generated method stub
			Log.d(TAG, "SpeechRecognizer init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				showTip("初始化失败,错误码："+code);
			}
		}
	};

	/**
	 * 参数设置
	 * 
	 * @param param
	 * @return
	 */
	public void setParam() {

		String lag = mSharedPreferences.getString("iat_language_preference", "mandarin");
		if (lag.equals("en_us")) {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
		}else {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
			// 设置语言区域
			mIat.setParameter(SpeechConstant.ACCENT,lag);
		}
		// 设置语音前端点
		mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));
		// 设置语音后端点
		mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));
		// 设置标点符号
		mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));
		// 设置音频保存路径
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, "/sdcard/iflytek/wavaudio.pcm");

	}

	/**
	 * 识别回调。
	 */
	private RecognizerListener mRecognizerListener = new RecognizerListener() {

		@Override
		public void onVolumeChanged(int volume) {
			showTip("当前正在说话，音量大小：" + volume);
		}

		@Override
		public void onResult(final RecognizerResult result, final boolean isLast) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (null != result) {
						// 显示
						// Log.d(TAG, "recognizer result：" +
						// result.getResultString());
						String iattext = JsonParser.parseIatResult(result
								.getResultString());
						Log.d(TAG, "recognizer result：" + iattext);
						recordText += iattext;
						if (isLast) {
							// 一次识别结束
							Log.d(TAG, "recognizer result(is Last)："
									+ recordText);
							mIat.stopListening();
							editor.setText("正在识别...");

							// 搜索歌词
							try {
								LyricSearch lyricSearch = new LyricSearch();
								lyricSearch.setContext(MainActivity.this);
								String name = lyricSearch.execute(
										recordText.replaceAll(
												"[^\\u4e00-\\u9fa5]", " ")
												.trim()).get();
								editor.setText(Html.fromHtml(name));

							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								Log.i(TAG, "InterruptedException");
							} catch (ExecutionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								Log.i(TAG, "ExecutionException");
							}

						}
					} else {
						Log.d(TAG, "recognizer result : null");
						editor.setText("无识别结果");
					}

				}
			});
		}

		@Override
		public void onError(SpeechError arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onBeginOfSpeech() {
			// TODO Auto-generated method stub
			recordText = "";
		}

		@Override
		public void onEndOfSpeech() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
			// TODO Auto-generated method stub

		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 退出时释放连接
		mIat.cancel();
		mIat.destroy();
	}

	private void showTip(final String str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mToast.setText(str);
				mToast.show();
			}
		});
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		// 启动识别
		editor.setText("正在录音...");
		int ret = mIat.startListening(mRecognizerListener);
		if(ret != ErrorCode.SUCCESS){
			showTip("听写失败,错误码：" + ret);
		}else {
			showTip("开始录音，停止说话自动开始识别");
		}
	}

}
