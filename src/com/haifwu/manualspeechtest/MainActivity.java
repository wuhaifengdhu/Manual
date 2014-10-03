package com.haifwu.manualspeechtest;

import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.speech.ErrorCode;
import com.iflytek.speech.ISpeechModule;
import com.iflytek.speech.InitListener;
import com.iflytek.speech.RecognizerListener;
import com.iflytek.speech.RecognizerResult;
import com.iflytek.speech.SpeechConstant;
import com.iflytek.speech.SpeechRecognizer;

public class MainActivity extends Activity implements OnClickListener{
	protected static final String TAG = "MainActivity";
	// 语音识别对象。
	private SpeechRecognizer mIat;
	private Toast mToast;
	private String recordText;
	
	private EditText editor;
	private SharedPreferences mSharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		((Button)findViewById(R.id.sppechButton)).setOnClickListener(this);
		findViewById(R.id.sppechButton).setEnabled(false);
		
		editor = (EditText)findViewById(R.id.showText);
		editor.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		editor.setGravity(Gravity.TOP);
		editor.setSingleLine(false);
		editor.setHorizontallyScrolling(false);
		editor.setFocusable(false);
		
		mSharedPreferences = getSharedPreferences("com.iflytek.setting", Activity.MODE_PRIVATE);
		
		mIat = new SpeechRecognizer(this, mInitListener);
		setParam();
		mToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
		
	/**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(ISpeechModule module, int code) {
			Log.d(TAG, "SpeechRecognizer init() code = " + code);
        	if (code == ErrorCode.SUCCESS) {
        		findViewById(R.id.sppechButton).setEnabled(true);
        	}
		}
    };
    
       
    /**
	 * 参数设置
	 * @param param
	 * @return 
	 */
	public void setParam(){
		
		mIat.setParameter(SpeechConstant.LANGUAGE, mSharedPreferences.getString("iat_language_preference", "zh_cn"));
		mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));
		mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));
		
		String param = null;
		param = "asr_ptt="+mSharedPreferences.getString("iat_punc_preference", "1");
		mIat.setParameter(SpeechConstant.PARAMS, param+",asr_audio_path=/sdcard/iflytek/wavaudio.pcm");

	}
    
    /**
     * 识别回调。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener.Stub() {
        
       	@Override
        public void onVolumeChanged(int v) throws RemoteException {
            showTip("音量："	+ v);
        }
        
        @Override
        public void onResult(final RecognizerResult result, final boolean isLast)
                throws RemoteException {
        	runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (null != result) {
		            	// 显示
						//Log.d(TAG, "recognizer result：" + result.getResultString());
						String iattext = JsonParser.parseIatResult(result.getResultString());
						Log.d(TAG, "recognizer result：" + iattext);
						recordText += iattext;
						if(isLast){
							//一次识别结束
							Log.d(TAG, "recognizer result(is Last)：" + recordText);
							mIat.stopListening(mRecognizerListener);
							editor.setText("正在识别...");
							
							//搜索歌词
							try {
								LyricSearch lyricSearch = new LyricSearch();
							    lyricSearch.setContext(MainActivity.this);
								String name = lyricSearch.execute(recordText.replaceAll("[^\\u4e00-\\u9fa5]", " ").trim()).get();
								editor.setText(Html.fromHtml(name));
								
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (ExecutionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
		            } else {
		                Log.d(TAG, "recognizer result : null");
		                ((EditText) findViewById( R.id.showText )).setText("无识别结果");
		            }	
					
				}
			});
        }
        @Override
        public void onError(int errorCode) throws RemoteException {
			//Log.i(TAG, "onError Code："	+ errorCode);
        }
        
        @Override
        public void onEndOfSpeech() throws RemoteException {
        	Log.i(TAG, "onEndOfSpeech");
        }
        
        @Override
        public void onBeginOfSpeech() throws RemoteException {
        	recordText = "";
        	Log.i(TAG, "onBeginOfSpeech");
        }
    };
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 退出时释放连接
        mIat.cancel(mRecognizerListener);
        mIat.destory();
    }
	
	private void showTip(final String str)
	{
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
		//启动识别
		((EditText)findViewById(R.id.showText)).setText("正在录音...");
		mIat.startListening(mRecognizerListener);		
	}

}
