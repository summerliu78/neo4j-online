package com.ambergarden.samples.neo4j.util;


import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.*;

/**
 * Created by lw on 2017/12/04.
 */
public class TrustRankUtils {


    public Map<String, String> readScore(String filePath) {
        FileReader fr = null;
        BufferedReader br = null;
        try {

            File file = new File(filePath);
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String word = null;
            Map<String, String> map = Maps.newHashMap();
            while ((word = br.readLine()) != null) {
                String[] s = word.split(",");

//                System.out.println("读取文件" + word);
                if (s.length == 4) {
                    String mobile = s[0].trim();
                    String finalScore = s[1].trim();
                    String goodScore = s[2].trim();
                    String badScore = s[3].trim();
                    if (StringUtils.isNotBlank(mobile) &&
                            StringUtils.isNotBlank(finalScore) &&
                            StringUtils.isNotBlank(goodScore) &&
                            StringUtils.isNotBlank(badScore)) {
                        String k = s[0];
                        String v = s[1] + "," + s[2] + "," + s[3];
                        map.put(k, v);
                    }
                }
            }
            return map;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
                if (fr != null) fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
//    public static void main(String[] argv) throws IOException{
//        String Dir="D:/input.txt";
//        Csv c=new Csv();
//        c.readText(Dir);
//    }

    public void saveFileToLocalFileSystem(String filepath, Map<String, Double> map) {
        File f = null;
        FileWriter fw = null;
        try {
            f = new File(filepath);
            fw = new FileWriter(f);
            Iterator entries = map.entrySet().iterator();
            while (entries.hasNext()) {

                Map.Entry entry = (Map.Entry) entries.next();

                String k = (String) entry.getKey();
                Double v = (Double) entry.getValue();
                fw.write(k + "," + v + "\n");
            }
            fw.flush();
        } catch (Exception e) {
//            System.out.println("-0--------------------");
            e.printStackTrace();
        } finally {
            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public Set<String> readWordFile(String pathName) {
        Set<String> wordSet = null;

// 要读取的文件路径，这里自行更改
        File file = new File(pathName);
        try {
// 读取文件输入流
            InputStreamReader read = new InputStreamReader(new FileInputStream(file), "utf-8");
// 文件是否是文件 和 是否存在
            if (file.isFile() && file.exists()) {

                wordSet = new HashSet<>();
//字符缓存输入流
                BufferedReader br = new BufferedReader(read);
                String txt = null;


// 读取文件，将文件内容放入到set中
                while ((txt = br.readLine()) != null) {
                    wordSet.add(txt);
                }
                br.close();

            }
// 关闭文件流
            read.close();


        } catch (Exception e) {
            e.printStackTrace();
        }


        return wordSet;
    }

    public Map<String, Set<String>> getDegree(ArrayList<String> a) {


        return null;
    }


    //    !file .exists()  && !file .isDirectory()
    public boolean fileInLocalFileSystemOrElse(String filePath) {
        return new File(filePath).exists();
    }

    public boolean directoryInLocalFileSystemOrElse(String dirPath) {
        return new File(dirPath).isDirectory();
    }

    public boolean FileOrDirExistOrElse(String path) {

        return directoryInLocalFileSystemOrElse(path) || fileInLocalFileSystemOrElse(path);
    }
}
