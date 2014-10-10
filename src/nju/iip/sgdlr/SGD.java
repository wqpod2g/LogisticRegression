package nju.iip.sgdlr;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import nju.iip.preprocess.ChineseTokenizer;

public class SGD {
	/**
	 * 整个样本向量，值为tf*idf
	 */
	private static Map<String,ArrayList<ArrayList<Double>>>allTfIdfMap=new HashMap<String,ArrayList<ArrayList<Double>>>();
	
	
	/**
	 * 特征向量所包含的关键词Map（每类中取tf*idf前100大的）
	 */	
	private static Map<String,Double> keywordsMap=new LinkedHashMap<String,Double>();
	
	/**
	 * @Description: 返回特征向量词的map<word,idf>，800个词
	 * @return keywordsMap
	 * @throws IOException
	 */
	public static Map<String,Double>getKeyWordsMap() throws IOException{
		    
	        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("keywords.txt"), "UTF8")); 
	        String line = br.readLine();
	        while(line != null){  
	        	String[] str=line.split("=");
	        	Double idf=Double.parseDouble(str[1]);
	        	keywordsMap.put(str[0],idf);
	            line = br.readLine();    
	        }
	        br.close();
	       
		return keywordsMap;
	}
	
	/**
	 * @description 计算一个帖子的特征向量
	 * @param content
	 * @return ArrayList
	 * @throws IOException 
	 */
	public static ArrayList<Double>getOneArticleVector(String content) throws IOException{
		ArrayList<Double>Vector=new ArrayList<Double>();
		Vector.add(Double.valueOf(1));
		Map<String,Long>articleWordsMap=ChineseTokenizer.segStr(content);
		Set<String>words=articleWordsMap.keySet();
		Long size=Long.valueOf(0);
		for(String word:words){
			size=size+articleWordsMap.get(word);
		}
		keywordsMap=getKeyWordsMap();
		Set<String>keywords=keywordsMap.keySet();
		for(String keyword:keywords){
			if(articleWordsMap.containsKey(keyword)){
				Double tf=Double.valueOf(articleWordsMap.get(keyword))/size;
				Vector.add(tf*keywordsMap.get(keyword));
			}
			
			else
				Vector.add(Double.valueOf(0));
		}
		
		return Vector;//返回一个帖子的特征向量X(1,x1，x2....x800)
	}
	
	/**
	 * @description 计算并获得某一类所有帖子的特征向量
	 * @param classification(某一类txt文件的路径)
	 * @return ArrayList<ArrayList<Double>>
	 * @throws IOException
	 */
	
	public static ArrayList<ArrayList<Double>>getOneClassVector(String classification) throws IOException{
		ArrayList<ArrayList<Double>>classVector=new ArrayList<ArrayList<Double>>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(classification), "UTF8")); 
        String line = br.readLine();
        while(line != null){  
        	ArrayList<Double>Vector=getOneArticleVector(line);
        	classVector.add(Vector);
        	line = br.readLine();
        }
        br.close();
		
		return classVector;
		
	}
	
	
	
	
	public static void main(String args[]) throws IOException{
//		keywordsMap=getKeyWordsMap();
//		Set<String>words=keywordsMap.keySet();
//		for(String word:words){
//			System.out.println(word+"="+keywordsMap.get(word));
//		}
		
//		ArrayList<Double>Vector=getOneArticleVector("我就说一句	充满天赋的森林狼队，今后会被哪些球队挖角呢？");
//		System.out.println(Vector.size());
//		for(int i=0;i<801;i++){
//			System.out.println(Vector.get(i));
//		}
		ArrayList<ArrayList<Double>>classVector=getOneClassVector("D:/dir/Basketball.txt");
		System.out.println(classVector.size());
		
	}
	

	

}
