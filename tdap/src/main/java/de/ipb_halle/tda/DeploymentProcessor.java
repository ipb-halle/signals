/*
 * IPB Signals client
 * Copyright 2025 Leibniz-Institut f. Pflanzenbiochemie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.ipb_halle.tda;

import com.google.auto.service.AutoService;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Annotation processor to make test deployment easier. Originally,
 * each test class had to be annotated with all EJB beans (services)
 * and all JPA entities. Now all injection points in a test class 
 * are additionally annotated with @DeploymentElements and this 
 * DeploymentProcessor recursively determines all injected beans.
 * The database services are annotated with PersistenceElements.
 * Thus this processor can determine, which JPA entities need to 
 * be configured for the current test class.
 * @author fblocal
 */

@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes(value={"de.ipb_halle.tda.DeploymentElement"})
@AutoService(Processor.class)
public class DeploymentProcessor extends AbstractProcessor {

    private Logger logger = LoggerFactory.getLogger(DeploymentProcessor.class);
    
    private Set<String> modules;
    private Map<String, String> mocks;
    private DeploymentLoader loader;
    private Class<Annotation> inject;
    private String packageName;
    private String testName;
    private Set<Class> entities;
    private ProcessingEnvironment processingEnv;

    public DeploymentProcessor() {
        super();
        loader = new DeploymentLoader();
        inject = null;
    }


    /**
     * Do some initialization, especially make a local copy
     * of the ProcessingEnvironment and initialize our class 
     * loader (DeploymentLoader).
     */
    @Override
    public void init(ProcessingEnvironment env) {
        super.init(env);
        String classpath = env.getOptions().get("classpath");
        // logger.warn("CLASSPATH {}", classpath);
        loader.setClassPath(classpath);
        processingEnv= env;
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    /**
     * Process annotated fields of multiple test classes. If one file got
     * processed, a Java file is written and Maps and Sets are reset to 
     * receive the information of the next Java file. 
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            entities = new HashSet<> ();
            mocks = new HashMap<> ();
            modules = new HashSet<> ();
            String previousName = null;
            String tempName = null;
            for (Element element : roundEnv.getElementsAnnotatedWith(DeploymentElement.class)) {
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
                tempName = enclosingElement.getQualifiedName().toString();
                if (previousName == null) {
                    previousName = tempName;
                }
                if (! tempName.equals(previousName)) {
                    buildTestApp(previousName);
                    entities = new HashSet<> ();
                    mocks = new HashMap<> ();
                    modules = new HashSet<> ();
                    previousName = tempName;
                }
                logger.warn("PACKAGE.TEST: {}", tempName);
                DeploymentElement mock = element.getAnnotation(DeploymentElement.class);
                processDeploymentElement(element, mock);

            }
            buildTestApp(tempName);
        } catch(Exception e) {
            logger.warn("process() caught an exception", e);
        }
        return false;
    }

    private void processDeploymentElement(Element element, DeploymentElement annotation) {
        logger.warn("ELEMENT: {} {}", element.getClass().getName(), element.toString());
        TypeMirror tm = element.asType();
        String className = tm.toString();
        logger.warn("TYPE: {}", className);
        String mock = annotation.mock();
        logger.warn("MOCK: {}", mock);
        if ((mock != null) && (! mock.isEmpty())) {
            mocks.put(className, mock);
            modules.add(mock);
        } else {
            resolveInjects(className);
        }
    }

    private void buildTestApp(String name) {
        if (name == null) {
            return;
        }
        int dotIndex = name.lastIndexOf('.');
        testName = name.substring(dotIndex + 1);
        packageName = name.substring(0, dotIndex);
        logger.warn("PROCESSING COMPLETE: {}", name);

        try {
            JavaFileObject builderFile = processingEnv
                    .getFiler()
                    .createSourceFile(packageName + ".TDAGenerated" + testName);
            PrintWriter out = new PrintWriter(builderFile.openWriter());
            out.print(getAppCode());
            out.close();
        } catch(Exception e) {
            logger.warn("buildTestApp caught an exception", e);
        }
    }

    private void resolveInjects(String className) {
        if (! modules.add(className)) {
            logger.info("  --> {} is already known.", className);
            return;
        }
        logger.warn("resolveInjects {}", className);
        try {
            Class module = Class.forName(className, false, loader);
            resolveEntities(module);
            resolveServices(module);
        } catch(Exception e) {
            logger.warn("resolveInjects caugth an exception: {}", className, e);
        }
    }

    private void resolveEntities(Class<?> module) {
        PersistenceElements persistenceElements = module.getAnnotation(PersistenceElements.class);
        if (persistenceElements != null) {
            for (Class pe: persistenceElements.entities()) {
                entities.add(pe);
            }
        }
    }

    private void resolveServices(Class module) throws ClassNotFoundException {
        Field[] fields = module.getDeclaredFields();
        for (Field field : fields) {
            logger.warn("FIELD: {} {}", module.getName(), field.getName());
            if (inject == null) {
                inject = (Class<Annotation>) Class.forName("jakarta.inject.Inject");
            }
            Annotation annotation = field.getAnnotation(inject);
            if (annotation != null) {
                logger.warn("RESOLVE: {}", field.getType().getName());
                resolveInjects(field.getType().getName());
            }
        }
    }


    private String getAppCode() {
        return String.format("""
/*
 * IPB Signals client
 * Copyright 2025 Leibniz-Institut f. Pflanzenbiochemie
 *
 * Generated Test Code
 */

package %s;

%s
import de.ipb_halle.signals.TestBase;
// import jakarta.inject.Inject;
import java.util.Properties;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class TDAGenerated%s extends %s {

    @Module
    @Classes(cdi = true, value = { %s })
    public EjbJar app() {
        return new EjbJar();
    }

    @Module
    public PersistenceUnit persistence() {
        return TestBase.persistence(new String[]{ %s });
    }

    @Configuration
    public Properties configuration() {
        return TestBase.configuration();
    }
}
""",
        packageName,
        getImports(),
        testName, testName,
        getEjbClasses(), getJpaEntities());
    }


    /**
     * generate the imports for the JPA entities used in this test
     *
     * ToDo: this is mock, create a real list!
     */
    private String getImports() {
        StringBuilder sb = new StringBuilder();
        for (Class entity : entities) {
            sb.append("import ");
            sb.append(entity.getName());
            sb.append(";\n");
        }
        return sb.toString();
    }

    /**
     * generate a list of classes to be listed in the <code>@Classes</code> annotation
     * of the test class.
     * @return a list of classes
     */
    private String getEjbClasses() {
        AtomicReference<String> sep = new AtomicReference<> ("");
        StringBuilder sb = new StringBuilder();
        for (String module : modules) {
            logger.warn("EJB CLASSES {} --> {}", module, mocks.getOrDefault(module, module));
            sb.append(sep.getAndSet(",\n                "));
            sb.append(mocks.getOrDefault(module, module));
            sb.append(".class");
        }
        return sb.toString();
    }

    /**
     * generate a list of JPA entities to be used in this test
     * @return a list of class names
     */
    private String getJpaEntities() {
        AtomicReference<String> sep = new AtomicReference<> ("");
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Class entity : entities) {
            sb.append(sep.getAndSet(",\n                "));
            sb.append(entity.getName());
            sb.append(".class.getName()");
        }
        return sb.toString();
    }

}
