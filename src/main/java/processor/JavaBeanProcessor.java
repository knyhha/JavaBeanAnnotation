package processor;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes("annotation.JavaBean")
public class JavaBeanProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(
            Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv
    ) {
        annotations.forEach(annotation ->
                roundEnv.getElementsAnnotatedWith(annotation)
                        .forEach(this::generateJavaBeanFile)
        );

        return true;
    }

    private void generateJavaBeanFile(Element element) {
        String className = element.getSimpleName().toString();
        String packageName = processingEnv
                .getElementUtils()
                .getPackageOf(element)
                .getQualifiedName()
                .toString();
        String beanName = className + "Bean";
        String beanFullName = packageName + "." + beanName;

        List<? extends Element> fields =
                element.getEnclosedElements()
                        .stream()
                        .filter(e -> e.getKind() == ElementKind.FIELD)
                        .filter(e -> {
                            Set<Modifier> m = e.getModifiers();
                            return !m.contains(Modifier.STATIC)
                                    && !m.contains(Modifier.FINAL)
                                    && !m.contains(Modifier.TRANSIENT);
                        })
                        .toList();

        try (PrintWriter writer = new PrintWriter(
                processingEnv.getFiler()
                        .createSourceFile(beanFullName)
                        .openWriter()
        )) {

            writer.println("""
                    package %s;

                    import java.io.Serializable;

                    public class %s implements Serializable {
                        private static final long serialVersionUID = 1L;
                    """.formatted(packageName, beanName));

            fields.forEach(field ->
                    writer.println("""
                                private %s %s;
                            """.formatted(
                            field.asType(),
                            field.getSimpleName()
                    ))
            );

            writer.println("""
                        public %s() {}
                    """.formatted(beanName));

            fields.forEach(field -> {
                String name = field.getSimpleName().toString();
                String capitalized = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                String getterPrefix = field.asType().getKind() == TypeKind.BOOLEAN ? "is" : "get";

                writer.println("""
                            public void set%s(%s %s) { this.%s = %s; }
                        """.formatted(
                        capitalized,
                        field.asType(),
                        name,
                        name,
                        name));

                writer.println("""
                            public %s %s%s() { return %s; }
                        """.formatted(
                                field.asType(),
                                getterPrefix,
                                capitalized,
                                name
                ));
            });

            writer.println("}");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}