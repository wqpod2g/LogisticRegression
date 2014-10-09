package nju.iip.sgdlr;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TfIdf {
	/**
	 * 所有文件分词结果.key:文件名,value:该文件分词统计
	 */
    private static Map<String, Map<String,Long>> allSegsMap = new HashMap<String, Map<String,Long>>();
    
    /**
	 * 所有文件tf结果.key:文件名,value:该文件tf
	 */
	private static Map<String, Map<String, Double>> allTfMap = new HashMap<String, Map<String, Double>>();  
 
    
	 /**
     * 统计包含单词的文档数  key:单词  value:包含该词的文档数
     */
    private static Map<String, Long> containWordOfAllDocNumberMap=new HashMap<String, Long>();
 
    
    
    /**
	 * 所有文件分词的idf结果.key:文件名,value:词w在整个文档集合中的逆向文档频率idf (Inverse Document Frequency)，即文档总数n与词w所出现文件数docs(w, D)比值的对数
	 */
    private static Map<String, Double> idfMap = new HashMap<String, Double>(); 
    
    
    
    /**
     * 统计单词的TF-IDF
     * key:文件名 value:该文件tf-idf
     */
    private static Map<String, Map<String, Double>> tfIdfMap = new HashMap<String, Map<String, Double>>();
    
    
    
    
    
    
    
    
    
    
    
    /**
     * 
    * @Title: tf
    * @Description: 分词结果转化为tf,公式为:tf(w,d) = count(w, d) / size(d)
    * 即词w在文档d中出现次数count(w, d)和文档d中总词数size(d)的比值
    * @param @param segWordsResult
    * @param @return    
    * @return HashMap<String,Long>   
    * @throws
     */
    private static HashMap<String,Double> tf(Map<String,Long> segWordsResult) { 
 
        HashMap<String, Double> tf = new HashMap<String, Double>();// 正规化  
        if(segWordsResult==null || segWordsResult.size()==0){
    		return tf;
    	}
        Long size=Long.valueOf(segWordsResult.size());
        Set<String> keys=segWordsResult.keySet();
        for(String key: keys){
        	Long value=segWordsResult.get(key);
        	tf.put(key, Double.valueOf(value)/size);
        }
        return tf;  
    }  
    
    /**
     * 
    * @Title: allTf
    * @Description: 得到所有文件的tf和所有文件分词后的map(allSegsMap)
    * @param @param dir
    * @param @return Map<String, Map<String, Double>>
    * @return Map<String,Map<String,Double>>   
    * @throws
     */
    public static Map<String, Map<String, Double>> allTf(String dir){
    	try{
    		List<String>fileList=Tools.readDirs(dir);
    		for(String filePath : fileList){
    			String content=Tools.readFile(filePath);
    			Map<String, Long> segs=ChineseTokenizer.segStr(content);
  			    allSegsMap.put(filePath, segs);
    			allTfMap.put(filePath, tf(segs));
    		}
    	}catch(FileNotFoundException ffe){
    		ffe.printStackTrace();
    	}catch(IOException io){
    		io.printStackTrace();
    	}
    	return allTfMap;
    	
    }
    
    
    /**
     * 
    * @Title: containWordOfAllDocNumber
    * @Description: 统计包含单词的文档数  key:单词  value:包含该词的文档数
    * @param @param allSegsMap
    * @param @return    
    * @return Map<String,Long>   
    * @throws
     */
    private static Map<String,Long> containWordOfAllDocNumber(Map<String, Map<String, Long>> allSegsMap){
    	if(allSegsMap==null || allSegsMap.size()==0){
    		return containWordOfAllDocNumberMap;
    	}
 
    	Set<String> fileList=allSegsMap.keySet();
    	for(String filePath: fileList){
    		Map<String, Long> fileSegs=allSegsMap.get(filePath);
    		//获取该文件分词为空或为0,进行下一个文件
    		if(fileSegs==null || fileSegs.size()==0){
    			continue;
    		}
    		//统计每个分词的idf
    		Set<String> segs=fileSegs.keySet();
    		for(String seg : segs){
    			if (containWordOfAllDocNumberMap.containsKey(seg)) {
    				containWordOfAllDocNumberMap.put(seg, containWordOfAllDocNumberMap.get(seg) + 1);
                } else {
                	containWordOfAllDocNumberMap.put(seg, 1L);
                }
    		}
 
    	}
    	return containWordOfAllDocNumberMap;
    }
    
    
    
    
    /**
     * 
    * @Title: idf
    * @Description: idf = log(n / docs(w, D)) 
    * @param @param containWordOfAllDocNumberMap
    * @param @return Map<String, Double> 
    * @return Map<String,Double>   
    * @throws
     */
    public static Map<String, Double> idf(Map<String, Map<String, Long>> allSegsMap){
    	if(allSegsMap==null || allSegsMap.size()==0){
    		return idfMap;
    	}
    	containWordOfAllDocNumberMap=containWordOfAllDocNumber(allSegsMap);
    	Set<String> words=containWordOfAllDocNumberMap.keySet();
    	for(String word: words){
    		Double number=Double.valueOf(containWordOfAllDocNumberMap.get(word));
    		//System.out.println(number);
    		idfMap.put(word, Math.log(10/number));
    	}
    	return idfMap;
    }
    
    
    /**
     * 
    * @Title: tfIdfMap
    * @Description: tf-idf
    * @param @param tf,idf
    * @return Map<String, Map<String, Double>>   
    * @throws
     */
    public static Map<String, Map<String, Double>> tfIdf(Map<String, Map<String, Double>> allTfMap,Map<String, Double> idf){
    	Set<String>files=allTfMap.keySet();
    	for(String file:files){
    		Map<String,Double>tfMap=allTfMap.get(file);
    		Map<String, Double> docTfIdf=new HashMap<String,Double>();
    		Set<String>words=tfMap.keySet();
    		for(String word:words){
    			Double tfValue=Double.valueOf(tfMap.get(word));
        		Double idfValue=idf.get(word);
        		docTfIdf.put(word, tfValue*idfValue);
    		}
    		tfIdfMap.put(file, docTfIdf);
    	}
    	
    	return tfIdfMap;
    }
    
    
    
    
    
    
    
    public static void main(String args[]){
    	Map<String, Map<String, Double>> allTfMap=allTf("D:/dir");
    	Map<String, Double> idf=idf(allSegsMap);
    	Map<String, Map<String, Double>> tfIdfMap=tfIdf(allTfMap,idf);
    	
    	System.out.println(tfIdfMap.size());
//    	Map<String, Double>allIdf=idf(allSegsMap);
//    	Set<String>words=allIdf.keySet();
//    	for(String word:words){
//    		System.out.println(word+"="+allIdf.get(word));
//    	}
    	//System.out.println(allIdf.size());
    	Set<String>classify=tfIdfMap.keySet();
    	for(String file:classify){
    		System.out.println(file+"*************************************************************");
    		Map<String,Double>tf=tfIdfMap.get(file);
    		Set<String>words=tf.keySet();
    		for(String word:words){
    			System.out.println(word+"="+tf.get(word));
    		}
    		
    	}
//    	System.out.println(allTf.size());
    	
    }
    
}
