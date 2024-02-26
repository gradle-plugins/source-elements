/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.gradleplugins.sources.processor;

import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class SourceFileProcessor extends AbstractProcessor {

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton(SourceFileLocation.class.getCanonicalName());
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (TypeElement annotation : annotations) {
			// Retrieve elements annotated with @SourceFileInfo
			Set<? extends Element> annotatedElements = (Set<Element>) roundEnv.getElementsAnnotatedWith(annotation);

			Set<String> allResources = new LinkedHashSet<>();
			for (Element element : annotatedElements) {
				if (element instanceof ExecutableElement) {
					SourceFileLocation info = element.getAnnotation(SourceFileLocation.class);
//					allResources.add(info.file());
					allResources.add(info.file());
//					generateClass((ExecutableElement) element, info);
				}
			}
			allResources.forEach(it -> copySourceToResource(it));
		}
		return true;
	}

	private void copySourceToResource(String path) {
		String basePath = processingEnv.getOptions().get("basePath");
		assert basePath != null;
		String[] tokens = path.split("/");
		Path sourcePath = Paths.get(basePath, path);
		try {
			FileObject resource = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/templates/" + tokens[0] + "/" + tokens[tokens.length - 1]);
			try (OutputStream os = resource.openOutputStream()) {
				Files.copy(sourcePath, os);
			}
		} catch (IOException ex) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to copy file: " + ex.getMessage());
		}
	}

	@Override
	public Set<String> getSupportedOptions() {
		return Collections.singleton("basePath");
	}

	private void generateClass(ExecutableElement element, SourceFileLocation info) {
		String packageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
		String className = element.getSimpleName() + "Impl";
		String qualifiedClassName = packageName + "." + className;
		Messager messager = processingEnv.getMessager();
		messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "PROCESSING " + qualifiedClassName);
//		String sourceFileContent = generateSourceFileContent(packageName, className, element, info);

//		try {
//			JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(qualifiedClassName);
//			try (Writer writer = sourceFile.openWriter()) {
//				writer.write(sourceFileContent);
//			}
//		} catch (IOException e) {
//			processingEnv.getMessager().printMessage(javax.tools.Diagnostic.Kind.ERROR, "Failed to generate class: " + e.getMessage());
//		}
	}

//	private String generateSourceFileContent(String packageName, String className, TypeElement element, SourceFileLocation info) {
//		return "package " + packageName + ";\n\n" +
//			"public class " + className + " extends " + element.getSimpleName() + " {\n" +
//			"    @Override\n" +
//			"    public SourceFile getSourceFile() {\n" +
//			"        // Implement loading of the file content from resources here\n" +
//			"        return new SourceFile(\"" + info.path() + "\", \"" + info.name() + "\", content);\n" +
//			"    }\n" +
//			"}\n";
//	}
}
