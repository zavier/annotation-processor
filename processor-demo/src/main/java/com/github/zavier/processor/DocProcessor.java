package com.github.zavier.processor;

import com.github.zavier.annotation.ClassDoc;
import com.github.zavier.annotation.FieldDoc;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.google.common.base.Charsets.UTF_8;

@SupportedAnnotationTypes({"com.github.zavier.annotation.ClassDoc"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class DocProcessor extends AbstractProcessor {

    private final ConcurrentHashMap<String, List<FieldDocInfo>> typeDocMap = new ConcurrentHashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            if (roundEnv.processingOver()) {
                StringBuilder sb = new StringBuilder();
                typeDocMap.forEach((typeName, docInfoList) -> {
                    sb.append("# ").append(typeName).append("\r\n").append("{\r\n");
                    for (FieldDocInfo fieldDocInfo : docInfoList) {
                        sb.append("    ").append("# ").append(fieldDocInfo.desc).append("\r\n");
                        sb.append("    ").append(fieldDocInfo.name).append(": ").append(fieldDocInfo.type).append("\r\n");
                    }
                    sb.append("}\r\n");
                });

                final FileObject resource = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "doc/doc.txt");
                try (OutputStream out = resource.openOutputStream()) {
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, UTF_8));
                    writer.write(sb.toString());
                    writer.newLine();
                    writer.flush();
                }
            } else {
                final Set<? extends Element> elements =
                        roundEnv.getElementsAnnotatedWith(ClassDoc.class)
                                .stream()
                                .filter(TypeElement.class::isInstance)
                                .collect(Collectors.toSet());
                for (Element element : elements) {
                    final String typeElementName = element.getAnnotation(ClassDoc.class).desc();
                    final List<FieldDocInfo> collect = element.getEnclosedElements()
                            .stream()
                            .filter(VariableElement.class::isInstance)
                            .map(VariableElement.class::cast)
                            .map(ele -> {
                                final String name = ele.getSimpleName().toString();
                                final String desc = ele.getAnnotation(FieldDoc.class).desc();
                                final TypeKind kind = ele.asType().getKind();
                                if (kind == TypeKind.DECLARED) {
                                    final TypeMirror typeMirror = ele.asType();
                                    final DeclaredType mirror = (DeclaredType) typeMirror;
                                    TypeElement e = (TypeElement) (mirror).asElement();
                                    final Name simpleName = e.getSimpleName();
                                    return new FieldDocInfo(name, desc, simpleName.toString());
                                } else {
                                    return new FieldDocInfo(name, desc, kind.name());
                                }
                            }).collect(Collectors.toList());
                    typeDocMap.put(typeElementName, collect);
                }
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
        return false;
    }

    static class FieldDocInfo {
        private String name;
        private String desc;
        private String type;

        public FieldDocInfo() {
        }

        public FieldDocInfo(String name, String desc, String type) {
            this.name = name;
            this.desc = desc;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
