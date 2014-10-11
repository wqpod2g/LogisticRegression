package nju.iip.sgdlr;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nju.iip.preprocess.ChineseTokenizer;
import nju.iip.preprocess.Tools;

/**
 * 
 * @author wangqiang
 * @time 2014-10-11
 */

public class SGD {
	
	
	private static String SamplePath="D:/lily";//文本路径
	
	/**
	 * 整个样本向量，值为tf*idf
	 */
	private static Map<String,ArrayList<ArrayList<Double>>>allVector=new LinkedHashMap<String,ArrayList<ArrayList<Double>>>();
	
	
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
        	//System.out.println(i+"      "+line);
        	line = br.readLine();
        }
        br.close();
		
		return classVector;
		
	}
	
	
	/**
	 * @description 获得整个样本的特征向量
	 * @return Map<String,ArrayList<ArrayList<Double>>>allVector
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	
	public static Map<String,ArrayList<ArrayList<Double>>>getAllVector() throws FileNotFoundException, IOException{
		List<String>fileList=Tools.readDirs(SamplePath);
		for(String article:fileList){
			allVector.put(article, getOneClassVector(article));
		}
		
		return allVector;
	}
	
	
	
	
	
	
	
	
	
	
	/**
	 * @description 求theta和x的数量积
	 * @param theta
	 * @param x
	 * @return
	 */
	public static Double vectorProduct(ArrayList<Double>theta,ArrayList<Double>x){
		Double value=0.0;
		for(int i=0;i<801;i++){
			value=value+theta.get(i)*x.get(i);
		}
		return value;

	}
	
	/**
	 * @description 求得h(x)的值
	 * @param theta
	 * @param x
	 * @return
	 */
	public static Double hxValue(ArrayList<Double>theta,ArrayList<Double>x){
		double hx;
		hx=1.0/(1.0+Math.exp(-vectorProduct(theta,x)));
		return hx;
	}
	
	
	/**
	 * @description 计算一次迭代后的theta值
	 * @param y
	 * @param theta
	 * @param x
	 * @return
	 */
	public static ArrayList<Double> theta2(Double y,ArrayList<Double>theta,ArrayList<Double>x){
		ArrayList<Double>theta2=new ArrayList<Double>();
		for(int i=0;i<801;i++){
			theta2.add(theta.get(i)+0.0001*(y-hxValue(theta,x))*x.get(i));
		}
		
		return theta2;
	}
	
	
	
	/**
	 * @求取某一个one VS rest 的theta向量
	 * @param classification
	 * @param TrainSample
	 * @return
	 */
	public static ArrayList<Double>getOneClassTheta(String classification,Map<String,ArrayList<ArrayList<Double>>> TrainSample){
		ArrayList<Double>theta=new ArrayList<Double>();
		for(int i=0;i<801;i++){//初始化theta
			theta.add(0.0);
		}
		
		Double y;
		Set<String>classlist=TrainSample.keySet();
		for(String classname:classlist){
			if(classname.equals(classification))
				y=1.0;
			else
				y=0.0;
			ArrayList<ArrayList<Double>>classVector=TrainSample.get(classname);
			for(int i=0;i<classVector.size();i++){
				theta=theta2(y,theta,classVector.get(i));
			}
			
		}
		
		
		return theta;
	}
	
	
	/**
	 * @description 获取整个训练样本的theta向量
	 * @param TrainSample
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static Map<String,ArrayList<Double>>getAllClassTheta(Map<String,ArrayList<ArrayList<Double>>> TrainSample) throws FileNotFoundException, IOException{
		Map<String,ArrayList<Double>>allClassTheta=new HashMap<String,ArrayList<Double>>();
		Set<String>classList=TrainSample.keySet();
		for(String classname:classList){
			allClassTheta.put(classname, getOneClassTheta(classname,TrainSample));
			System.out.println("theta------"+classname+"   finish!");
		}
		
		System.out.println("******************************theta训练结束！*************************************");
		return allClassTheta;
	}
	
	
	/**
	 * @description 求得某篇帖子所属类别
	 * @param x
	 * @return 该帖子属于哪一类
	 */
	public static String getOneVectorTestResult(ArrayList<Double>x,Map<String,ArrayList<Double>>allClasstheta){
		String result=null;
		//Map<String,Double>resultMap=new HashMap<String,Double>();
		Set<String>classnames=allClasstheta.keySet();
		int i=0;
		Double value = null ;
		for(String classname:classnames){
			//resultMap.put(classname,vectorProduct(allClasstheta.get(classname),x));
			if(i==0){
				value=vectorProduct(allClasstheta.get(classname),x);
				result=classname;
			}
			else{
				if(vectorProduct(allClasstheta.get(classname),x)>=value){
					value=vectorProduct(allClasstheta.get(classname),x);
					result=classname;
				}
				
				
				
				
			}
			System.out.println(value);
			i++;		
		}
		
		return result;	
	}
	
	
	
	
	
	
	
	/**
	 * @description 计算一次十折交叉验证的命中率
	 * @param testSample
	 * @param TrainSample
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static String oneTimeTenCross(Map<String,ArrayList<ArrayList<Double>>> testSample,Map<String,ArrayList<ArrayList<Double>>> TrainSample) throws FileNotFoundException, IOException{
		
		Map<String,ArrayList<Double>>theta=getAllClassTheta(TrainSample);
		Set<String>classnames=testSample.keySet();
		int discount=0;
		int j=1;
		for(String classname:classnames){
			ArrayList<ArrayList<Double>>classVector=testSample.get(classname);
			for(int i=0;i<classVector.size();i++){
				if(!classname.equals(getOneVectorTestResult(classVector.get(i),theta))){
					discount++;
					System.out.println(j+"未命中！当前未命中数:"+discount);
				}
				else
					System.out.println(j);
				
				j++;
			}
		}
		
		return getPercent(100-discount,100);
		
	
	}
	
	public static String getPercent(int x,int total){
		   String result="";//接受百分比的值
		   double x1=x*1.0;
		   double tempresult=x1/total;
		   DecimalFormat df1 = new DecimalFormat("0%");    //##.00%   百分比格式，后面不足2位的用0补齐
		   result= df1.format(tempresult);  
		   return result;
		}
	
	
	public static void process() throws FileNotFoundException, IOException{
		Map<String,ArrayList<ArrayList<Double>>> testSample=new HashMap<String,ArrayList<ArrayList<Double>>>();
		Map<String,ArrayList<ArrayList<Double>>> TrainSample=new HashMap<String,ArrayList<ArrayList<Double>>>();
		
		Set<String>classnames=allVector.keySet();
		for(String classname:classnames){
			ArrayList<ArrayList<Double>>classVector=allVector.get(classname);
			ArrayList<ArrayList<Double>>testSampleclassVector=new ArrayList<ArrayList<Double>>();
			ArrayList<ArrayList<Double>>TrainSampleclassVector=new ArrayList<ArrayList<Double>>();
			//System.out.println("#######"+classVector.size());
			for(int i=0;i<100;i++){
				if(i>=90&&i<100)
					testSampleclassVector.add(classVector.get(i));
				else
					TrainSampleclassVector.add(classVector.get(i));
			}
			//System.out.println("#######"+testSampleclassVector.size());
			//System.out.println("#######"+TrainSampleclassVector.size());
			testSample.put(classname, testSampleclassVector);
			TrainSample.put(classname, TrainSampleclassVector);
		}
		System.out.println("样本划分完毕！");
		System.out.println("命中率为:"+oneTimeTenCross(testSample,TrainSample));
		
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
		//ArrayList<ArrayList<Double>>classVector=getOneClassVector("D:/dir/Girls.txt");
		
		getAllVector();
		//System.out.println(allVector.size());
		System.out.println("预处理结束！");
		
		
//		long startTime=System.currentTimeMillis();   //获取开始时间
//		Map<String,ArrayList<Double>>allClassTheta=getAllClassTheta(allVector);
//		System.out.println(allClassTheta.size());
//		long endTime=System.currentTimeMillis(); //获取结束时间   
//		System.out.println("样本训练时间： "+(endTime-startTime)/1000+"s");
		long startTime=System.currentTimeMillis();   //获取开始时间
		process();
		long endTime=System.currentTimeMillis(); //获取结束时间   
		System.out.println("时间： "+(endTime-startTime)/1000+"s");
//		ArrayList<Double>vector=allClassTheta.get("D:"+File.separator+"dir"+File.separator+"JobExpress.txt");
//		System.out.println(vector.size());
//		for(int i=0;i<801;i++){
//			System.out.println(vector.get(i));
//		}
		
		
//		Set<String>list=allVector.keySet();
//		for(String article:list ){
//			ArrayList<ArrayList<Double>>classVector=allVector.get(article);
//			System.out.println(article+"  "+classVector.size());
//		}
	}
	

	

}
