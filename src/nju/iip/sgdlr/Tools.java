package nju.iip.sgdlr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Tools {
	  private static final List<String> FileList =new ArrayList<String>();

	//get list of file for the directory, including sub-directory of it
    public static List<String> readDirs(String filepath) throws FileNotFoundException, IOException
    {
        try
        {
            File file = new File(filepath);
            if(!file.isDirectory())
            {
                System.out.println("输入的[]");
                System.out.println("filepath:" + file.getAbsolutePath());
            }
            else
            {
                String[] flist = file.list();
                for(int i = 0; i < flist.length; i++)
                {
                    File newfile = new File(filepath + "\\" + flist[i]);
                    if(!newfile.isDirectory())
                    {
                        FileList.add(newfile.getAbsolutePath());
                    }
                    else if(newfile.isDirectory()) //if file is a directory, call ReadDirs
                    {
                        readDirs(filepath + "\\" + flist[i]);
                    }                    
                }
            }
        }catch(FileNotFoundException e)
        {
            System.out.println(e.getMessage());
        }
        return FileList;
    }
    
    
  //read file
    public static String readFile(String file) throws FileNotFoundException, IOException
    {
        StringBuffer strSb = new StringBuffer(); //String is constant， StringBuffer can be changed.
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8")); 
        String line = br.readLine();
        while(line != null){
            strSb.append(line).append("\r\n");
            line = br.readLine();    
        }
        br.close();
        return strSb.toString();
    }
    
    
    
//    public static void main(String args[]) throws FileNotFoundException, IOException{
//    	List<String>file=readDirs("D:\\dir");
//    	for(String f:file){
//    		System.out.println(readFile(f));
//    	}
//    }

}
