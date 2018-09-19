package com.code;

import com.code.Compile;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Scanner;

public class myUtil {
    public static void eval(String str) throws IOException {
        String content ="package com.code.temp;\n"+
                "public class Temp{\n" +
                "public static void eval(){\n" +
                "\t\t" + str + "\n" + "}\n" +
                "}";
        File tempDir = new File("./src/com/code/temp");
        tempDir.deleteOnExit();
        if (!tempDir.exists()) {
            if (!tempDir.mkdirs()) {
                throw new FileNotFoundException("创建失败");
            }
        }
        File tempFile = new File("./src/com/code/temp/Temp.java");
        tempFile.deleteOnExit();
        if (!tempFile.exists()) {
            if (!tempFile.createNewFile()) {
                throw new FileNotFoundException("创建失败");

            }
        }
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempFile));
        bufferedWriter.write(content, 0, content.length());
        bufferedWriter.flush();
        bufferedWriter.close();
        Compile classLoader = new Compile();
        classLoader.setJavaFileName("./src/com/code/temp/Temp.java");
        Class clazz = null;
        try {
            clazz = classLoader.getClazz();
            Method eval = clazz.getMethod("eval");
            eval.invoke(null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException {
        String str=new Scanner(System.in).next();
        eval(str);
    }
}


