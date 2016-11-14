package psn.ifplusor.persistence.entity;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;


/**
 * 编译时持久层注解处理器
 * <br/>
 * 处理由{@link javax.persistence.Entity}注解的类，解析{@link javax.persistence.Column}注解实现注入。
 * <br/>
 * {@link javax.persistence.Column}只能注解在属性和Getter方法上，同一个Column在一个实体类中只能出现一次。
 *
 * @author james
 * @version 10/19/16
 */

@SupportedAnnotationTypes("javax.persistence.Entity")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ColumnProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        // 初始化 Filer 和 Messager
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        messager.printMessage(Kind.NOTE, "process() is execute...");

        // 获取使用了 @Entity 注解的类元素
        Set<? extends Element> genElements = roundEnvironment.getElementsAnnotatedWith(Entity.class);
        for (Element e : genElements) {
            messager.printMessage(Kind.NOTE, "Generate deletage class for: " + e.getSimpleName());

            // 获取使用 @Column 注解的元素，收集 getter 和 setter 方法
            Map<String, Element> htColumnToElement = new HashMap<String, Element>();
            Map<String, Element> htGetterMethods = new HashMap<String, Element>();
            Map<String, Element> htSetterMethods = new HashMap<String, Element>();

            for (Element ee : e.getEnclosedElements()) {

                Column annotation = ee.getAnnotation(Column.class);
                if (annotation != null) {
                    if (htColumnToElement.containsKey(annotation.name())) {
                        messager.printMessage(Kind.ERROR, "\"@Column\" of \"" + annotation.name() + "\" is replicate!");
                        return true;
                    } else {
                        htColumnToElement.put(annotation.name(), ee);
                    }
                }

                if (ee.getKind() == ElementKind.METHOD && isPublic(ee)) {
                    String name = ee.toString();
                    String simpleName = ee.getSimpleName().toString();
                    if (name.startsWith("get") && name.endsWith("()")
                            && (name.charAt(3) < 'a' || name.charAt(3) > 'z')) {
                        htGetterMethods.put(simpleName.substring(3).toLowerCase(), ee);
                    } else if (name.startsWith("set") && !name.endsWith("()") && name.split(",").length == 1
                            && (name.charAt(3) < 'a' || name.charAt(3) > 'z')) {
                        htSetterMethods.put(simpleName.substring(3).toLowerCase(), ee);
                    }
                }
            }


            // 生成 Delegate 类
            String packagePath = null;
            Element pe = e.getEnclosingElement();
            while (pe != null && pe.getKind() != ElementKind.PACKAGE) {
                pe = pe.getEnclosingElement();
            }
            if (pe != null) {
                packagePath = pe.toString();
            }

            String clazzName = e.getSimpleName().toString();
            String className = clazzName + "Delegate";

            StringBuilder classString = new StringBuilder();
            classString
                    .append(packagePath != null ? "package " + packagePath + ";\n" : "")
                    .append("import java.sql.ResultSet;\n")
                    .append("import java.sql.SQLException;\n")
                    .append("import psn.ifplusor.persistence.jdbc.JdbcDaoDelegate;\n")
                    .append("import ").append(packagePath).append(".").append(clazzName).append(";\n")
                    .append("public class ").append(className).append(" implements JdbcDaoDelegate<").append(clazzName).append("> {\n");

            if (genSelect(classString, htColumnToElement)) return true;
            if (genBeanFromResultSet(classString, clazzName, htColumnToElement, htSetterMethods)) return true;

            classString
                    .append("}\n");


            // 生成 java 文件
            try {
                JavaFileObject jfo = filer.createSourceFile(packagePath + "." + className, e);
                Writer writer = jfo.openWriter();
                writer.flush();
                writer.append(classString);
                writer.flush();
                writer.close();
            } catch (IOException ex) {
                messager.printMessage(Kind.ERROR, "encounter IOException when generate \"" + packagePath + "." + clazzName + ".java");
                return true;
            }
        }

        return false;
    }

    private boolean genSelect(StringBuilder classString, Map<String, Element> htColumnToElement) {

        classString
                .append("    public String select() {\n")
                .append("        return \"");

        for (String column : htColumnToElement.keySet()) {
            classString.append(column).append(",");
        }

        if (htColumnToElement.size() != 0) {
            classString.deleteCharAt(classString.length() - 1);
        }

        classString
                .append("\";\n")
                .append("    }\n");

        return false;
    }

    private boolean genBeanFromResultSet(StringBuilder classString, String clazzName,
                                         Map<String, Element> htColumnToElement, Map<String, Element> htSetterMethods) {

        classString
                .append("    public ").append(clazzName).append(" beanFromResultSet(ResultSet rs) {\n")
                .append("        if (rs == null) return null;\n")
                .append("        ").append(clazzName).append(" obj = new ").append(clazzName).append("();\n");

        // 通过 setter 方法注入 column
        for (String column : htColumnToElement.keySet()) {
            Element element = htColumnToElement.get(column);
            String name = element.toString();
            ElementKind kind = element.getKind();
            Element setter = null;
            if (kind == ElementKind.FIELD) { // 字段
                setter = htSetterMethods.get(name.toLowerCase());
            } else if (kind == ElementKind.METHOD) { // 方法
                if (!name.startsWith("get") || !name.endsWith("()")) {
                    messager.printMessage(Kind.ERROR, "\"@Column\" could not annotate method \"" + name + "\"!");
                    return true;
                }
                setter = htSetterMethods.get(name.substring(3, name.length() - 2).toLowerCase());
            }

            if (setter == null) {
                messager.printMessage(Kind.ERROR, "setter method for field \"" + name + "\" is missing!", element);
                return true;
            }
            String setterMethod = setter.toString();
            String typeName = setterMethod.substring(setterMethod.indexOf("(") + 1, setterMethod.indexOf(")"));

//                    System.out.println("type name: " + typeName);

            classString
                    .append("        try {\n")
                    .append("            obj.").append(setter.getSimpleName()).append("(rs.");

            // 根据 setter 方法参数类型，选择 rs.getXXX 方法
            if ("java.lang.Byte".equals(typeName) || "byte".equals(typeName)) {
                classString.append("getByte");
            } else if ("java.lang.String".equals(typeName)) {
                classString.append("getString");
            } else if ("java.math.BigDecimal".equals(typeName)) {
                classString.append("getBigDecimal");
            } else if ("java.lang.Short".equals(typeName) || "short".equals(typeName)) {
                classString.append("getShort");
            } else if ("java.lang.Integer".equals(typeName) || "int".equals(typeName)) {
                classString.append("getInt");
            } else if ("java.lang.Long".equals(typeName) || "long".equals(typeName)) {
                classString.append("getLong");
            } else if ("java.lang.Float".equals(typeName) || "float".equals(typeName)) {
                classString.append("getFloat");
            } else if ("java.lang.Double".equals(typeName) || "double".equals(typeName)) {
                classString.append("getDouble");
            } else if ("java.sql.Date".equals(typeName)) {
                classString.append("getDate");
            } else { // 默认返回 Object
                classString.append("getObject");
            }

            classString
                    .append("(\"").append(column).append("\"));\n")
                    .append("        } catch (SQLException e) {\n")
                    .append("            e.printStackTrace();\n")
                    .append("        }\n");
        }

        classString
                .append("        return obj;\n")
                .append("    }\n");

        return false;
    }

    // 判断元素是否为 public
    private boolean isPublic(Element e) {

        /*
         * 获取元素的修饰符 Modifier, 注意此处的 Modifier
         * 非 java.lang.reflect.Modifier
         */
        Set<Modifier> modifiers = e.getModifiers();
        for (Modifier m : modifiers) {
            if (m.equals(Modifier.PUBLIC)) return true;
        }
        return false;
    }

}
