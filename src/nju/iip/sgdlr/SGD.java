package nju.iip.sgdlr;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
	
	private static int iterationTimes=10;//迭代次数
	
	private static Double alpha=0.0000001;//步长
	
	private static String SamplePath="D:/dir";//文本路径
	
	private static ArrayList<Double>accuracyList=new ArrayList<Double>();//元素为每折准确率
	
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
		Vector.add(1.0);
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
	public static Double hxValue(ArrayList<Double>x,ArrayList<Double>theta){
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
		Double yh=alpha*(y-hxValue(x,theta));
		for(int i=0;i<801;i++){
			theta2.add(theta.get(i)+yh*x.get(i));
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
		
		for(int n=0;n<iterationTimes;n++){
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
		//System.out.println("******************************theta训练开始！*************************************");
		Map<String,ArrayList<Double>>allClassTheta=new LinkedHashMap<String,ArrayList<Double>>();
		Set<String>classList=TrainSample.keySet();
		for(String classname:classList){
			ArrayList<Double>theta=getOneClassTheta(classname,TrainSample);
			allClassTheta.put(classname, theta);
			
		}
		
		//System.out.println("******************************theta训练结束！*************************************");
		return allClassTheta;
	}
	
	
	/**
	 * @description 求得某篇帖子所属类别
	 * @param x
	 * @return 该帖子属于哪一类
	 */
	public static String getOneVectorTestResult(ArrayList<Double>x,Map<String,ArrayList<Double>>allClasstheta){
		//Map<String,Double>resultMap=new HashMap<String,Double>();
		Set<String>classnames=allClasstheta.keySet();
		String classifierName="";
		Double result=0.0;
		for(String classname:classnames){
			Double classifierValue=hxValue(x,allClasstheta.get(classname));
			//System.out.println(classifierValue);
		    if(classifierValue>=result){
		    	result=classifierValue;
		    	classifierName=classname;
		    }
		}
		//System.out.println(classifierName);
		//System.out.println(value);
		return classifierName;	
	}
	
	
	
	
	
	
	
	/**
	 * @description 计算一次十折交叉验证的命中率
	 * @param testSample
	 * @param TrainSample
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static Double oneTimeTenCross(Map<String,ArrayList<ArrayList<Double>>> testSample,Map<String,ArrayList<ArrayList<Double>>> TrainSample) throws FileNotFoundException, IOException{
		
		Map<String,ArrayList<Double>>allClasstheta=getAllClassTheta(TrainSample);
		Set<String>classnames=testSample.keySet();
		int discount=0;
		for(String classname:classnames){
			ArrayList<ArrayList<Double>>classVector=testSample.get(classname);
			for(int i=0;i<classVector.size();i++){
				if(!classname.equals(getOneVectorTestResult(classVector.get(i),allClasstheta))){
					discount++;
				}
			}
		}
		
		return (100-discount)/100.0;
		
	
	}
	
	
	
	
	public static void process() throws FileNotFoundException, IOException{
		for(int j=0;j<10;j++){
		
		Map<String,ArrayList<ArrayList<Double>>> testSample=new LinkedHashMap<String,ArrayList<ArrayList<Double>>>();
		Map<String,ArrayList<ArrayList<Double>>> TrainSample=new LinkedHashMap<String,ArrayList<ArrayList<Double>>>();
		
		Set<String>classnames=allVector.keySet();
		for(String classname:classnames){
			ArrayList<ArrayList<Double>>classVector=allVector.get(classname);
			ArrayList<ArrayList<Double>>testSampleclassVector=new ArrayList<ArrayList<Double>>();
			ArrayList<ArrayList<Double>>TrainSampleclassVector=new ArrayList<ArrayList<Double>>();
			for(int i=0;i<100;i++){
				if(i>=j*10&&i<(j+1)*10)
					testSampleclassVector.add(classVector.get(i));
				else
					TrainSampleclassVector.add(classVector.get(i));
			}
			testSample.put(classname, testSampleclassVector);
			TrainSample.put(classname, TrainSampleclassVector);
		}
		Double accuracy=oneTimeTenCross(testSample,TrainSample);
		System.out.println("第"+(j+1)+"折验证结束!命中率为:"+accuracy);
		accuracyList.add(accuracy);
		}
		
		System.out.println("平均准确率为:"+Tools.getMean(accuracyList));
		System.out.println("标准差为:"+Tools.getDeviation(accuracyList));
		
	}
	
	
	public static void main(String args[]) throws IOException{
		getAllVector();
		System.out.println("预处理结束！");
		long startTime=System.currentTimeMillis();   //获取开始时间
		process();
		long endTime=System.currentTimeMillis(); //获取结束时间   
		System.out.println("时间： "+(endTime-startTime)/1000+"s");
		
	}
	

	

}
