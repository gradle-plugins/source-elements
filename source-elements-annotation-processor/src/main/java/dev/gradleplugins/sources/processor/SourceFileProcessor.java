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
import dev.gradleplugins.fixtures.sources.annotations.SourceFileProperty;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
			Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);

			Set<SourceFileLocation> allResources = new LinkedHashSet<>();
			for (Element element : annotatedElements) {
				if (element.getKind().equals(ElementKind.INTERFACE) || element.getKind().equals(ElementKind.CLASS)) {
					SourceFileLocation info = element.getAnnotation(SourceFileLocation.class);
					allResources.add(info);
//					if (element.getKind().equals(ElementKind.INTERFACE)) {
//						generateClass((TypeElement) element);
//					}
				}
			}
			allResources.forEach(it -> copySourceToResource(it));
		}
		return true;
	}

	private void copySourceToResource(SourceFileLocation info) {
		String path = info.file();
		String basePath = processingEnv.getOptions().get("basePath");
		assert basePath != null;
		String[] tokens = path.split("/");
		Path sourcePath = Paths.get(basePath, path);
		processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING, "PROCESSING '" + sourcePath + "'");

		if (Files.isDirectory(sourcePath)) {
			copyDirToResource(Paths.get(basePath), sourcePath, info.excludes());
		} else {
			try {
				String content = new String(Files.readAllBytes(sourcePath));
				for (SourceFileProperty property : info.properties()) {
					int i = 0;
					Matcher p = Pattern.compile(property.regex(), Pattern.MULTILINE | Pattern.DOTALL).matcher(content);
					while (p.find()) {
						i++;
					}

					// Ensure property has at least one match
					if (i == 0) {
						processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Property '" + property.name() + "' for '" + info.file() + "' not found");
					}
				}

				FileObject resource = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/templates/" + tokens[0] + "/" + tokens[tokens.length - 1]);
				try (OutputStream os = resource.openOutputStream()) {
					Files.copy(sourcePath, os);
				}
			} catch (
				IOException ex) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to copy file: " + ex.getMessage());
			}
		}
	}

	private void copyDirToResource(Path basePath, Path sourcePath, String[] excludes) {
		try {
			List<Pattern> excludePatterns = Arrays.stream(excludes).map(it -> it.replace(".", "\\.").replace("**", "{{dir}}").replace("*", "{{all}}").replace("{{dir}}", ".*").replace("{{all}}", "[^/]*")).map(it -> Pattern.compile(it, Pattern.DOTALL)).collect(Collectors.toList());
			excludePatterns.add(Pattern.compile(".*/\\.DS_Store", Pattern.DOTALL));

			Path p = basePath.relativize(sourcePath);
			String n = p.getName(0) + "/" + sourcePath.getFileName();
			if (p.getNameCount() == 1) {
				n = sourcePath.getFileName().toString();
			}
			final String namespace = n;

			List<String> paths = new ArrayList<>();
			Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					String relativePath = sourcePath.relativize(file).toString();
					if (excludePatterns.stream().noneMatch(it -> it.matcher("/" + relativePath).find())) {
						FileObject resource = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/templates/" + namespace + "/" + relativePath);
						try (OutputStream os = resource.openOutputStream()) {
							Files.copy(file, os);
						}

						paths.add(namespace + "/" + relativePath);
					}
					return FileVisitResult.CONTINUE;
				}
			});

			FileObject resource = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/templates/" + namespace + ".sample");
			try (OutputStream os = resource.openOutputStream()) {
				os.write(paths.stream().collect(Collectors.joining("\n")).getBytes(StandardCharsets.UTF_8));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Set<String> getSupportedOptions() {
		return Collections.singleton("basePath");
	}

	private void generateClass(TypeElement element) {
		Messager messager = processingEnv.getMessager();
		String packageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
		String className = ClassNameUtil.getClassName(element);
		String qualifiedClassName = packageName + "." + className;
		messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "PROCESSING " + ClassNameUtil.getClassName(element));
		String sourceFileContent = generateSourceFileContent(packageName, className, element);

		try {
			JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(qualifiedClassName);
			try (Writer writer = sourceFile.openWriter()) {
				writer.write(sourceFileContent);
			}
		} catch (IOException e) {
			processingEnv.getMessager().printMessage(javax.tools.Diagnostic.Kind.ERROR, "Failed to generate class: " + e.getMessage());
		}
	}

	private String generateSourceFileContent(String packageName, String className, TypeElement element) {
		return String.join("\n",
			"package " + packageName + ";",
			"",
			"public final class " + className + " implements " + ClassNameUtil.get(element).collect(Collectors.joining(".")) + " {",
			"}");
	}

	private static class ClassNameUtil {
		public static String getClassName(Element element) {
			return get(element).collect(Collectors.joining("$")) + "Impl";
		}

		public static Stream<String> get(Element element) {
			List<String> result = new ArrayList<>();

			while (element != null && element.getKind() != ElementKind.PACKAGE) {
				result.add(0, element.getSimpleName().toString());
				element = element.getEnclosingElement();
			}

			return result.stream();
		}
	}
}
