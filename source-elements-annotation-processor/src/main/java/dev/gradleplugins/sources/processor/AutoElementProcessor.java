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

import dev.nokee.elements.AutoElement;
import dev.nokee.elements.ElementFileTree;
import dev.nokee.elements.core.SourceElement;
import dev.nokee.elements.core.SourceFile;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AutoElementProcessor extends AbstractProcessor {
	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton(AutoElement.class.getCanonicalName());
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (TypeElement annotation : annotations) {
			Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);

			for (Element element : annotatedElements) {
				System.out.println(element);
				assert element.getModifiers().contains(Modifier.ABSTRACT) && element.getKind().equals(ElementKind.CLASS);
				AutoElement info = element.getAnnotation(AutoElement.class);

				assert element instanceof TypeElement;
				generateSubclass(new AutoElementInfo(info), (TypeElement) element);
			}
		}
		return true;
	}

	private static final class AutoElementInfo {
		private final AutoElement info;

		private AutoElementInfo(AutoElement info) {
			this.info = info;
		}

		public String classNameOf(TypeElement typeElement) {
			String result = info.className();

			if (result.isEmpty()) {
				// Check if the class is a nested class
				if (typeElement.getEnclosingElement() instanceof TypeElement) {
					// It's a nested class
					String outerClassSimpleName = typeElement.getEnclosingElement().getSimpleName().toString();
					String innerClassSimpleName = typeElement.getSimpleName().toString();
					result = outerClassSimpleName + "_" + innerClassSimpleName + "Element";
				} else {
					// It's a top-level class
					result = typeElement.getSimpleName() + "Element";
				}
			}

			return result;
		}
	}

	private void generateSubclass(AutoElementInfo info, TypeElement typeElement) {
		String className = info.classNameOf(typeElement);
		String packageName = processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
		StringBuilder classBuilder = new StringBuilder();

		classBuilder.append("package ").append(packageName).append(";\n\n");
		classBuilder.append("import ").append(dev.nokee.elements.core.SourceElement.class.getCanonicalName()).append(";\n\n");
		classBuilder.append("import ").append(dev.nokee.elements.core.SourceFile.class.getCanonicalName()).append(";\n\n");
		classBuilder.append("public final class ").append(className).append(" extends ").append(typeElement.getQualifiedName()).append(" {\n");

		// Get methods annotated with @ElementFileTree
		typeElement.getEnclosedElements().stream()
			.flatMap(it -> (it.getKind().equals(ElementKind.METHOD)) ? Stream.of((ExecutableElement) it) : Stream.empty())
			.filter(method -> method.getAnnotation(ElementFileTree.class) != null)
			.forEach(method -> {
				ElementFileTree annotation = method.getAnnotation(ElementFileTree.class);
				List<SourceFile> e = copySourceToResource(annotation, typeElement, method);
				String methodName = method.getSimpleName().toString();
				String returnType = method.getReturnType().toString();
				if (methodName.equals("getSourceFile")) {
					assert e.size() == 1;
					classBuilder.append("\t@Override\n");
					classBuilder.append("\tpublic ").append(returnType).append(" ").append(methodName).append("() {\n");
					classBuilder.append("\t\treturn " + toCode(e.get(0)) + ";\n");
					classBuilder.append("\t}\n");
				} else if (methodName.equals("getFiles")) {
					classBuilder.append("\t@Override\n");
					classBuilder.append("\tpublic ").append(returnType).append(" ").append(methodName).append("() {\n");
					classBuilder.append("\t\treturn java.util.Arrays.asList(\n");
					classBuilder.append(e.stream().map(this::toCode).collect(Collectors.joining(",\n")));
					classBuilder.append("\n\t\t);\n");
					classBuilder.append("\t}\n");
				} else {
					classBuilder.append("\t@Override\n");
					classBuilder.append("\tpublic ").append(returnType).append(" ").append(methodName).append("() {\n");
					classBuilder.append("\t\treturn SourceElement.ofFiles(\n");
					classBuilder.append(e.stream().map(this::toCode).collect(Collectors.joining(",\n")));
					classBuilder.append("\n\t\t);\n");
					classBuilder.append("\t}\n");
				}
			});

		classBuilder.append("}\n");

		// Write the generated class to a Java file
		try {
			JavaFileObject fileObject = processingEnv.getFiler().createSourceFile(packageName + "." + className);
			try (Writer writer = fileObject.openWriter()) {
				writer.write(classBuilder.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private String toCode(SourceFile file) {
		StringBuilder builder = new StringBuilder();
		builder.append("\t\t\tnew SourceFile(\"" + file.getPath() + "\", \"" + file.getName() + "\", ");
		builder.append(file.getContent().lines().map(it -> it.replace("\"", "\\\"").replace("\\n", "\\\\n")).map(it -> it + "\\n").map(it -> "\"" + it + "\"").collect(Collectors.joining("\n\t\t\t\t+ ")));
		builder.append("\n\t\t\t)");
		return builder.toString();
	}


	private List<SourceFile> copySourceToResource(ElementFileTree info, Element element, Element method) {
		String path = info.value();
		String basePath = processingEnv.getOptions().get("basePath");
		assert basePath != null;
		Path sourcePath = Paths.get(basePath, path);

		assert Files.isDirectory(sourcePath);
		SourceElement elements = copyDirToResource(sourcePath, new ArrayList<PathMatcher>() {{
			addAll(patternsOf(info.excludes()));
			add(FileSystems.getDefault().getPathMatcher("glob:**/.DS_Store"));
		}}, patternsOf(info.includes()));

		if (elements.getFiles().isEmpty()) {
			throw new UnsupportedOperationException("no source files: " + element);
		}

		try {
			StringBuilder filename = new StringBuilder();
			filename.append(element.getSimpleName()).append("-").append(method.getSimpleName().toString().substring(3)).append(".xml");
			Element ee = element;
			while ((ee = ee.getEnclosingElement()) != null) {
				if (ee instanceof TypeElement) {
					filename.insert(0, ee.getSimpleName() + "$");
				} else if (ee instanceof PackageElement) {
					filename.insert(0, ((PackageElement) ee).getQualifiedName().toString().replace('.', '/') + "/");
				}
			}

			FileObject resource = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", filename);
			try (PrintWriter out = new PrintWriter(resource.openOutputStream())) {
				SourceElement e = elements;
				out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				out.println("<SourceElement>");

				for (SourceFile sourceFile : e.getFiles()) {
					out.println("  <SourceFile path=\"" + sourceFile.getPath() + "\"><![CDATA[");
					out.println(sourceFile.getContent());
					out.println("  ]]></SourceFile>");
				}
				out.println("</SourceElement>");
			}
			return elements.getFiles();
		} catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

	private List<PathMatcher> patternsOf(String[] patterns) {
		return Arrays.stream(patterns).map(it -> FileSystems.getDefault().getPathMatcher("glob:" + it)).collect(Collectors.toList());
	}

	private SourceElement copyDirToResource(Path srcDir, List<PathMatcher> excludes, List<PathMatcher> includes) {
		List<SourceFile> sourceFiles = new ArrayList<>();
		try {
			Files.walkFileTree(srcDir, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Path relativePath = srcDir.relativize(file);
					if (!includes.isEmpty() && includes.stream().noneMatch(it -> {
						return it.matches(relativePath);
					})) {
						return FileVisitResult.CONTINUE;
					}

					if (excludes.stream().anyMatch(it -> it.matches(relativePath))) {
						return FileVisitResult.CONTINUE;
					}

					sourceFiles.add(SourceFile.from(srcDir.relativize(file), () -> new String(Files.readAllBytes(file))));
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return SourceElement.ofFiles(sourceFiles);
	}

	@Override
	public Set<String> getSupportedOptions() {
		return Collections.singleton("basePath");
	}
}
