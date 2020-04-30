package com.june;


import com.intellij.ide.highlighter.JavaFileType;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.ui.ImageUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class FlutterAddImage extends AnAction {
    Project project;
    PsiDirectory psiDirectory;
    VirtualFile lib;
    VirtualFile gen_a;
    VirtualFile a_file;
    VirtualFile assets;
    HashMap<String,String> list=new HashMap<>();   PsiFile psifile =null;
    PsiFile psifile2 =null;
    PsiFile psifile3 =null;
    boolean isFilter=false;
    JTextField jTextField_filter;
    //file name
    String name ="";
    @Override
    public void actionPerformed(AnActionEvent e) {
         project = e.getData(PlatformDataKeys.PROJECT);
        PsiElement psiElement = e.getData(PlatformDataKeys.PSI_ELEMENT);
        if(psiElement instanceof  PsiFile){
            Messages.showErrorDialog("just open with dir","Attention!");
            return;
        }
        psiDirectory= (PsiDirectory) psiElement;

        //添加   批量  自动创建 2.0x 3.0x 的图片和文件夹, 至于自动生成pubspec的行数 再说  //批量
//        data.createSubdirectory("2.0x");
//        data.createSubdirectory("3.0x");

        if (project == null) {
            return;
        }
        VirtualFile virtualFile = project.getWorkspaceFile().getParent().getParent();
           if( virtualFile.isDirectory()){
               VirtualFile[] children = virtualFile.getChildren();
               for(VirtualFile virtualFile1:children){
                   if(virtualFile1.getName().equals("lib")){
                       lib=virtualFile1;

                         }
                   if(virtualFile1.getName().equals("assets")){
                       assets=virtualFile1;
                   }
               }
            init();
            addFilePath(assets);

           }


        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                //------------------
                JFrame frame = new JFrame();
                frame.setTitle("Flutter Add Image -- Drag File To Here ----> "+psiDirectory.getName());
                frame.setSize(500,300);
                frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

                frame.setVisible(true);

                 jTextField_filter = new JTextField();

//                frame.add(jTextField_filter);

                JCheckBox checkBox=new JCheckBox();
                checkBox.setText("Use Name Filter");
                checkBox.setSize(20,20);
//                frame.add(checkBox);
                checkBox.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        JCheckBox checkBox = (JCheckBox) e.getSource();
                        isFilter= checkBox.isSelected();

                    }
                });
                JCheckBox checkBox1=new JCheckBox();
                checkBox1.setText("always on top");
                checkBox1.setSize(20,20);

                checkBox1.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        JCheckBox checkBox = (JCheckBox) e.getSource();
                        if(  checkBox.isSelected()){

                            frame.setAlwaysOnTop(true);
                        }else{
                            frame.setAlwaysOnTop(false);
                        }

                    }
                });
                JTextField jTextField = new JTextField();
                jTextField.setSize(500,200);
                jTextField.setText("Drag to here");
                jTextField.setHorizontalAlignment(SwingConstants.CENTER);
//                frame.add(jTextField);

                JPanel panel = new JPanel();
                panel.setPreferredSize(new Dimension(500,250));
                panel.add(jTextField_filter);
                panel.add(checkBox);
                panel.add(jTextField);
                panel.add(checkBox1);


                jTextField.setEditable(false);
                jTextField.setPreferredSize(new Dimension(500,200));
                frame.setContentPane(panel);
                jTextField.setTransferHandler(new TransferHandler()
                {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public boolean importData(JComponent comp, Transferable t) {
                        try {
                            Object o = t.getTransferData(DataFlavor.javaFileListFlavor);

                            String filepath = o.toString();
                            if (filepath.startsWith("[")) {
                                filepath = filepath.substring(1);
                            }
                            if (filepath.endsWith("]")) {
                                filepath = filepath.substring(0, filepath.length() - 1);
                            }
                            if(filepath.endsWith("png")|filepath.endsWith("jpg")|filepath.endsWith("jpeg")){

                            }else{
                                Messages.showErrorDialog("Just for Image","Attention!");
                                return false;
                            }


                                //filepath 拖拽的文件
                            genImages(new File(filepath));
                            //路径
//                    System.out.println(filepath);
//                    field.setText(filepath);

                            return true;
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        return false;
                    }
                    @Override
                    public boolean canImport(JComponent comp, DataFlavor[] flavors) {
                        for (int i = 0; i < flavors.length; i++) {
                            if (DataFlavor.javaFileListFlavor.equals(flavors[i])) {
                                return true;
                            }
                        }
                        return false;
                    }
                });







               //------------------
            }
        });



    }
    PsiDirectory subdirectory2=null;
    PsiDirectory subdirectory3=null;
    //生成图片文件
    void    genImages(File file){
        if(file==null){
            return;
        }
        if(file.isDirectory()){
            Messages.showErrorDialog("Just for Image","Attention!");
            return;
        }

       try{
           WriteCommandAction.runWriteCommandAction(project, new Runnable() {
               @Override
               public void run() {
                   subdirectory2 = psiDirectory.createSubdirectory("2.0x");

                   subdirectory3 = psiDirectory.createSubdirectory("3.0x");
               }
           });

       }catch (Exception e){
           PsiDirectory[] subdirectories = psiDirectory.getSubdirectories();

               subdirectory2= psiDirectory.findSubdirectory("2.0x");
               subdirectory3= psiDirectory.findSubdirectory("3.0x");



       }
       File selectedFile=file;

        try{

            if(isFilter){
                String text = jTextField_filter.getText();
                if(text==null){
                    Messages.showErrorDialog(   "Please enter content","Attention!");
                    return;
                }
                if("".equals(text)){
                    Messages.showErrorDialog(   "Please enter content,or do not use filter","Attention!");
                    return;
                }
                name=selectedFile.getName();
                if(name.contains(text)){
                    name=  name.replaceAll(text,"");
                }else{
                    Messages.showErrorDialog(   "必须包含被过滤文本","Attention!");
                    return;
                }
            }else{
                name=selectedFile.getName();
            }
           WriteCommandAction.runWriteCommandAction(project, new Runnable() {
               @Override
               public void run() {
                   // x1
                   psifile = psiDirectory.createFile(name);
                   //x2
                   psifile2 = subdirectory2.createFile(name);
                   //x3
                   psifile3 = subdirectory3.createFile(name);
               }
           });
        }catch (Exception e){
            Messages.showErrorDialog("File already exists","Attention!");

            return;

        }


        WriteCommandAction.runWriteCommandAction(project, new Runnable() {
            @Override
            public void run() {
                try {


                            flex(1,selectedFile,psifile);
                            flex(2,selectedFile,psifile2);
                            flex(3,selectedFile,psifile3);
                            list=null;
                            list=new HashMap<>();
                            addFilePath(assets);
                            gen_AContent();


                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    //change images
   // float num  缩小倍数
    public void flex(float num,File file,PsiFile psifile) throws IOException{
        //1原来宽和高与新的宽高的比例
        FileInputStream fis=new FileInputStream(file);
        BufferedImage read = ImageIO.read(fis);
        float width=(read.getWidth()/3)*num;
        float height=(read.getHeight()/3)*num;
        Image image = read.getScaledInstance((int)width, (int)height,
                Image.SCALE_SMOOTH);
        BufferedImage outputImage =ImageUtil.toBufferedImage(image);
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        ImageIO.write(outputImage,file.getPath().substring(file.getPath().lastIndexOf(".")+1),baos);
       psifile.getVirtualFile().setBinaryContent(baos.toByteArray());
    }
        //初始化A文件
    void init(){

        WriteCommandAction.runWriteCommandAction(project, new Runnable() {
            @Override
            public void run() {
                try {
                    gen_a = lib.findChild("gen_a");
                    if(gen_a==null){
                        gen_a = lib.createChildDirectory(null, "gen_a");
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                if(gen_a==null){
                    return;
                }
                a_file = gen_a.findChild("A.dart");
                if(a_file==null){
                    PsiFileFactory.getInstance(project);
                    String s="class A{\n" +
                            " //auto gen ,do net edit !!! \n" +
                            "  static String test=\"test\";\n" +

                            "}";
                    PsiFile initFile = PsiFileFactory.getInstance(project).createFileFromText("A.dart", JavaFileType.INSTANCE, s);
                    PsiManager.getInstance(project).findDirectory(gen_a).add(initFile);
                }else{


                }


//                                    PsiFile fileFromText = PsiFileFactory.getInstance(project).createFileFromText(Language.ANY, "A.dart");

            }
        });


    }
    //生成A文件的内容
    void gen_AContent(){
        a_file = gen_a.findChild("A.dart");
        StringBuilder sb=new StringBuilder();
        String s="class A{\n" +
                " //auto gen ,do not edit! \n" ;
        sb.append(s);
        Iterator<Map.Entry<String, String>> iterator = list.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, String> next = iterator.next();
            sb.append( "static  final String "+ next.getKey()+"=\""+next.getValue()+"\";\n" );
        }

        sb.append("}");
        try {
            a_file.setBinaryContent(sb.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void  addFilePath(VirtualFile f){
        VirtualFile[] files = f.getChildren();
        for (VirtualFile ff:files) {
            if(ff.isDirectory()){
                if(ff.getName().equals("2.0x")){
                    continue;
                }
                if(ff.getName().equals("3.0x")){
                    continue;
                }
                 addFilePath(ff);
            }else{
                if(ff.getPath().endsWith("png")|ff.getPath().endsWith("jpg")|ff.getPath().endsWith("jpeg")){
                   String path= ff.getPath();
                    String path1 =path.substring(path.indexOf("assets"),path.lastIndexOf("."));

                    String path2=path1.replaceAll("/","_");
                    list.put(path2,  path.substring(path.indexOf("assets")));
                }

            }
        }
    }


}



//google Thumbnails
//Thumbnails.of(new File("C:\\Users\\Administrator\\Desktop\\bigImage\\7.jpg")).scale(1f).outputFormat("jpg")
//        .outputQuality(0.5)
//        .toFile("C:\\Users\\Administrator\\Desktop\\bigImage\\compress2.jpg");