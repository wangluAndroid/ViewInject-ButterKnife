package com.example;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import javax.xml.transform.Source;

/**
 * Created by serenitynanian on 2018/7/6.
 */


@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes("com.example.BindView")
@AutoService(Processor.class)
public class ButterKnifeProcess extends AbstractProcessor {

//    /**
//     * 得到需要扫描源码中的注解类型
//     * @return
//     */
//    @Override
//    public Set<String> getSupportedAnnotationTypes() {
//        Set<String> types = new LinkedHashSet<>();
//        types.add(BindView.class.getCanonicalName());
//        return types;
//    }
//
//
//    @Override
//    public SourceVersion getSupportedSourceVersion() {
//        return SourceVersion.latestSupported();
//    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.out.println("---------------process----------------");

        //得到源代码中所有被BindView注解的变量Set集合
        Set<? extends Element> elementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        //1.创建一个map集合，用来保存各个类中被bindView注解注释的成员变量
        //集合的key：全类名=包名+类名   value:被bindview注解注释的成员变量集合
        Map<String, List<VariableElement>> cacheMap = new HashMap<>();

        //4.实现onClickListener时间的绑定
        Set<? extends Element> elementsAnnotatedOnClick = roundEnvironment.getElementsAnnotatedWith(OnClick.class);
        Map<String, List<ExecutableElement>> onClickCacheMap = new HashMap<>();
        for (Element element : elementsAnnotatedOnClick) {
            ExecutableElement executableElement = (ExecutableElement) element;
            String wholeClassName = getWholeClassName(element);
            List<ExecutableElement> executableElements = onClickCacheMap.get(wholeClassName);
            if (null == executableElements) {
                executableElements = new ArrayList<>();
                onClickCacheMap.put(wholeClassName, executableElements);
            }
            executableElements.add(executableElement);
        }

        //2.遍历被BindView注解注释的变量Set集合
        for (Element element : elementsAnnotatedWith) {
            VariableElement variableElement = (VariableElement) element;
            //2.1.得到全类名
            String wholeClassName = getWholeClassName(variableElement);
            //2.2 判断当前类是否已经有个保存所有被bindview注解的变量集合，如果有就直接添加，没有就创建出来，
            //              说明是一个新的类中的成员变量被bindview注解注释了，添加到map中，然后再将此变量添加到
            //                          属于自己类的变量集合中
            List<VariableElement> variableElementsList = cacheMap.get(wholeClassName);
            if(null == variableElementsList){
                variableElementsList = new ArrayList<>();
                cacheMap.put(wholeClassName, variableElementsList);
            }
            variableElementsList.add(variableElement);
            System.out.println("------wholeClassName--------->"+wholeClassName);
        }

        //3.为每一个类生成对应的java文件
        Iterator<String> iterator = cacheMap.keySet().iterator();
        while (iterator.hasNext()) {
            String wholeClassName = iterator.next();//得到的是全类名
            //3.1 得到该类中被bindView注解的成员变量集合
            List<VariableElement> variableElements = cacheMap.get(wholeClassName);
            //3.2 组装要生成的类名
            String newClassName = wholeClassName + "$ViewBinder";

            //3.3 得到创建java文件的对象
            Filer filer = processingEnv.getFiler();
            try {
                //注意 ：必须使用createSourceFile  而不是createClassFile
                JavaFileObject classFile = filer.createSourceFile(newClassName);
                //3.4 得到包名
                String packageName = getPackageName(variableElements.get(0));
                //3.5 得到创建文件的Writer对象
                Writer writer = classFile.openWriter();

                //3.6 得到类的SimpleName
                String simpleClassName = variableElements.get(0).getEnclosingElement().getSimpleName().toString()+"$ViewBinder";

                //3.7 组装java文件的头部分
                writerHeader(writer,packageName,wholeClassName,simpleClassName);

                //3.8 组装java文件的方法体
                for (VariableElement variableElement : variableElements) {
                    BindView bindView = variableElement.getAnnotation(BindView.class);
                    //3.8.1 得到注解上面的viewId
                    int viewId = bindView.value();
                    // 3.8.2 得到成员变量的属性名字
                    String fieldName = variableElement.getSimpleName().toString();
                    //3.8.3 得到成员变量类型
                    TypeMirror typeMirror = variableElement.asType();

                    writer.write("target." + fieldName + "=(" + typeMirror.toString() + ")target.findViewById(" + viewId + ");");
                    writer.write("\n");
                }

                //4.1 实现onClickListener事件方法的组装
                List<ExecutableElement> executableElements = onClickCacheMap.get(wholeClassName);
                for (ExecutableElement executableElement : executableElements) {
                    OnClick onClick = executableElement.getAnnotation(OnClick.class);
                    int viewId = onClick.value();
                    String methodName = executableElement.getSimpleName().toString();
                    System.out.println("-------------methodName----------->"+methodName);
                    /**
                     *  findViewById(R.id.tv_text).setOnClickListener(new View.OnClickListener() {
                             @Override
                            public void onClick(View v) {

                            }
                        });
                     */
                    writer.write("target.findViewById(" + viewId + ").setOnClickListener(new View.OnClickListener() {");
                    writer.write("\n");
                    writer.write("@Override");
                    writer.write("\n");
                    writer.write(" public void onClick(View v) {");
                    writer.write("\n");
                    writer.write("target."+methodName+"(v);");
                    writer.write("\n");
                    writer.write("}");
                    writer.write("\n");
                    writer.write("});");
                    writer.write("\n");
                }


                //3.9 组装java的结尾部分
                writer.write("\n");
                writer.write("}");
                writer.write("\n");
                writer.write("}");
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        return false;
    }

    private void writerHeader(Writer writer, String packageName, String wholeClassName, String simpleClassName) {
        try {
            writer.write("package "+packageName+";");
            writer.write("\n");
            writer.write("import com.example.ViewBinder;");
            writer.write("\n");
            writer.write("import android.view.View;");
            writer.write("\n");

            writer.write("public class "+simpleClassName+" implements ViewBinder<"+wholeClassName+"> {");

            writer.write("\n");
            writer.write(" public void bind(final "+wholeClassName+" target) {");
            writer.write("\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getPackageName(Element variableElement) {
        //得到父节点
        TypeElement enclosingElement = (TypeElement) variableElement.getEnclosingElement();
        String packageName = processingEnv.getElementUtils().getPackageOf(enclosingElement).getQualifiedName().toString();
        return packageName ;
    }

    private String getWholeClassName(Element variableElement) {
        String packageName = getPackageName(variableElement);
        TypeElement enclosingElement = (TypeElement) variableElement.getEnclosingElement();
        String wholeClassName = packageName+"."+enclosingElement.getSimpleName().toString();
        return wholeClassName;
    }
}
