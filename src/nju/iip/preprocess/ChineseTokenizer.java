package nju.iip.preprocess;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

/**
 * @Description:分词
 * @createDate：2014-9-27
 * @author wangqiang
 *
 */
public class ChineseTokenizer {
    /**
     * 
    * @Title: segStr
    * @Description: 返回LinkedHashMap的分词
    * @param @param content
    * @param @return    
    * @return Map<String,Integer>   
    * @throws
     */
    public static Map<String, Long> segStr(String content){
        // 分词
    	StringReader input = new StringReader(content);
        IKSegmenter iks = new IKSegmenter(input, true);
        Lexeme lexeme = null;
        Map<String, Long> words = new LinkedHashMap<String, Long>();
        try {
            while ((lexeme = iks.next()) != null) {
            	if(lexeme.getLength()>1){
                if (words.containsKey(lexeme.getLexemeText())) {
                    words.put(lexeme.getLexemeText(), words.get(lexeme.getLexemeText()) + 1);
                } else {
                    words.put(lexeme.getLexemeText(), 1L);
                }
                
            	}
            }       
        }catch(IOException e) {
            e.printStackTrace();
        }
        return words;
    }
    
    
//    public static void main(String args[]) throws IOException{
//    	String filePath = "test.txt";
//		String news=new String();
//        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
//	    String str;
//	    while ((str = in.readLine()) != null) {
//	    	news+=str;
//	    }
//	   in.close();
//	   System.out.println(news);
//	   Map<String, Long> segStr=new HashMap<String,Long>();
//	   segStr=segStr(news);
//	   Set<String> words=segStr.keySet();
//	   for(String word:words){
//		   System.out.println(word+"="+segStr.get(word));
//	   }
//    	
//    }
    
    
}
