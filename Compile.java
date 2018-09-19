package com.code;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/***
 * @author 牛奶冻荔枝
 */
public class Compile extends ClassLoader {
    private String javaFileName="";

    /***
     * 设置java文件
     * @param javaFileName
     */
    public void setJavaFileName(String javaFileName){
        this.javaFileName=javaFileName;
    }

    /***
     * 编译生成.class二进制文件
     * @param javaFileName String 文件名
     * @return 是否编译成功
     */
    private boolean compile(String javaFileName){
        try {
            Process exec=Runtime.getRuntime().exec("javac C:/Users/牛奶冻荔枝/Desktop/untitled/" + javaFileName);
            exec.waitFor();
            int result=exec.exitValue();
            return result==0;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /***
     * 读取.class文件
     * @param classFile 二进制文件
     * @return byte[] 字节数组
     * @throws IOException
     */
    private byte[] getBytes(File classFile) throws IOException {
        try(FileInputStream fileInputStream=new FileInputStream(classFile)){
            int len =(int)classFile.length();
            byte[] buf=new byte[len];
            int hasRead=fileInputStream.read(buf);
            if (hasRead!=len){
                throw new IOException("无法读取文件");
            }
            return buf;
        }
    }

    /***
     *
     * @param name
     * @return
     * @throws ClassNotFoundException
     */
    @Override
    protected Class<?> findClass(String name)throws ClassNotFoundException{
        String fileName=name.replace('.','/');
        String classFilename="./src/"+fileName+".class";
        File javaFile=new File(javaFileName);
        File classFile=new File(classFilename);
        if (javaFile.exists()&&(!classFile.exists())||javaFile.lastModified()>classFile.lastModified())
        {
            try {
                if (!compile(javaFileName)||!classFile.exists()){
                    throw new ClassNotFoundException("ClassNotFoundExecption:"+javaFileName);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        Class clazz=null;
        if (classFile.exists()){
            try {
                byte[] buf=getBytes(classFile);
                //将byte[]数组转化成class类的实例
                clazz=this.defineClass(name,buf,0,buf.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (clazz==null)
        {throw new ClassNotFoundException(name);}
        return clazz;
    }
        public Class<?> getClazz() throws ClassNotFoundException{
        Class clazz=this.findClass("com.code.temp.Temp");
        return clazz;
        }

}
