package com.haifwu.manualspeechtest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.Environment;


public class ZhiDaoParse {
	private String lyric = null;
	
	public ZhiDaoParse(String lyric){
		this.lyric = lyric;
	}
	
	private String getDefaultUrl(){
		String baseUrl = "http://zhidao.baidu.com/search?lm=0&rn=1&pn=0&fr=search&ie=gbk&word=";
		String keyWord = "歌词 " + lyric;
		try {
			keyWord = URLEncoder.encode(keyWord, "gbk");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return baseUrl + keyWord;
	}
	
	private String getFirstLink(){
		String link = null;
		String url = getDefaultUrl();
		System.out.println("搜索链接:" + url);
		try {
			Document document = Jsoup.connect(url).get();
			writeObject(document.toString(), "link.txt");
			Elements dt =document.getElementsByAttributeValueContaining("class", "fbig").select("a");
			/*Iterator<Element> iterator = dt.iterator();
			while(iterator.hasNext()){
				String href = iterator.next().attr("href");
				System.out.println(href);
			}*/
			if(dt != null && dt.first() != null){
				link = "http://zhidao.baidu.com" + dt.first().attr("href");
				System.out.println("结果链接：" + link);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return link;
	}
	
	private void writeObject(String content, String fileName){
		try {
			File file = new File(Environment.getExternalStorageDirectory() + File.separator + fileName);
			file.createNewFile();
			ObjectOutputStream os = new ObjectOutputStream(  
			        new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator + fileName));
			os.writeObject(content);
			os.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	public String getSongName(){
		String songName = "不能识别";
		String url = getFirstLink();
		if(url == null) return songName;
		
		try {
			Document document = Jsoup.connect(url).get();
			writeObject(document.toString(), "answer.txt");
			Element bestAnswer = document.getElementsByAttributeValue("class", "t-txt").first();
			if(bestAnswer == null){
				bestAnswer = document.getElementsByAttributeValue("class", "content").first();
				if(bestAnswer == null){
					bestAnswer = document.getElementsByAttribute("body").first();
				}
			}
			if(songName != null){
				songName = bestAnswer.html();//.ownText();//.replaceAll("<[^>]*>","");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return songName;
	}
	
}
