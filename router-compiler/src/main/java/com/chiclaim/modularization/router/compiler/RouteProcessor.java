package com.chiclaim.modularization.router.compiler;

import com.chiclaim.modularization.router.annotation.Components;
import com.chiclaim.modularization.router.annotation.Constant;
import com.chiclaim.modularization.router.annotation.Route;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import static com.chiclaim.modularization.router.annotation.Constant.KEY_MODULE_NAME;

@AutoService(Processor.class)
@SupportedOptions(KEY_MODULE_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class RouteProcessor extends AbstractProcessor {

    private Filer filter;
    private Messager messager;
    private String moduleName;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {

        super.init(processingEnvironment);
        filter = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        printValue("init====" + this);
        processingEnvironment.getOptions();
        Map<String, String> options = processingEnv.getOptions();
        if (options != null && !options.isEmpty()) {
            moduleName = options.get(KEY_MODULE_NAME);
            if (moduleName != null && moduleName.length() > 0) {
                moduleName = moduleName.replaceAll("[^0-9a-zA-Z_]+", "");
            }
            printValue("moduleName:" + moduleName);
        }
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(Route.class.getCanonicalName());
        set.add(Components.class.getCanonicalName());
        return set;
    }

    private void printElement(Element element, Class annotationClazz) {
//        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
//        printValue("========annotation 所在的类完整名称 " + enclosingElement.getQualifiedName());
//        printValue("========annotation 所在类的类名 " + enclosingElement.getSimpleName());
//        printValue("========annotation 所在类的父类 " + enclosingElement.getSuperclass());

        printValue(element.getEnclosingElement().toString());
        printValue("========element name " + element.getSimpleName());
        printValue("        element type " + element.asType());
        if (element.getAnnotation(Route.class) != null) {
            printValue("        annotation value " + element.getAnnotation(Route.class).path());
        } else {
            printValue("        annotation value " + element.getAnnotation(Components.class).value());
        }

    }

    private void printValue(String obj) {
        messager.printMessage(Diagnostic.Kind.NOTE, obj);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        printValue("我进来了----");
        //Map<String, BindClass> map = new LinkedHashMap<>();
        ArrayList<BindClass> list = new ArrayList<>();
        Set<? extends Element> routeElements = roundEnvironment.getElementsAnnotatedWith(Route.class);
        for (Element element : routeElements) {
            printElement(element, Route.class);
            list.add(BindClass.createWhenApplyClass(element));
        }
        printValue("处理完毕----");
        if (!list.isEmpty()) {
            printValue("生成代码----");
            try {
                ClassName className = ClassName.get(Constant.ROUTE_INIT_CLASS_PACKAGE,
                        Constant.ROUTE_INIT_MODULE_CLASS_PREFIX + moduleName);
                RouteJavaFileUtil.preJavaFileByList(className, list).writeTo(filter);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        printValue("Components----");
        List<String> modules = new ArrayList<>();

        Set<? extends Element> ComponentsElements = roundEnvironment.getElementsAnnotatedWith(Components.class);
        for (Element element : ComponentsElements) {
            String[] moduleNames = element.getAnnotation(Components.class).value();
            modules.addAll(Arrays.asList(moduleNames));
        }


        for (String name : modules) {
            printValue("Components----" + name);
        }


        return true;
    }
}
