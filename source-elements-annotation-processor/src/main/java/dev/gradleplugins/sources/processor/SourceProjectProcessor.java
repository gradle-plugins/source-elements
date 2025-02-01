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

import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileProperty;
import dev.gradleplugins.fixtures.sources.annotations.SourceProject;
import org.apache.commons.text.StringEscapeUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SourceProjectProcessor extends AbstractProcessor {

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton(SourceProject.class.getCanonicalName());
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
				if (element.getKind().equals(ElementKind.INTERFACE) || element.getKind().equals(ElementKind.CLASS)) {
					SourceProject info = element.getAnnotation(SourceProject.class);
					copySourceToResource(info, element);
				}
			}
		}
		return true;
	}

	private void copySourceToResource(SourceProject info, Element element) {
		String path = info.value();
		String basePath = processingEnv.getOptions().get("basePath");
		assert basePath != null;
		Path sourcePath = Paths.get(basePath, path);

		assert Files.isDirectory(sourcePath);
		List<dev.gradleplugins.fixtures.sources.Element> elements = copyDirToResource(sourcePath, info.excludes(), info.includes());

		try {
			StringBuilder filename = new StringBuilder();
			filename.append(element.getSimpleName());
			Element ee = element;
			while ((ee = ee.getEnclosingElement()) != null && ee instanceof TypeElement) {
				filename.insert(0, ee.getSimpleName() + "$");
			}
			FileObject resource = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/elements/" + filename + ".xml");
			try (PrintWriter out = new PrintWriter(resource.openOutputStream())) {
				assert !elements.isEmpty();
				assert elements.size() == 1 : "assuming single source set, but we can definitely support more";
				SourceElement e = (SourceElement) elements.get(0);
				out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				out.println("<SourceElement name=\"" + e.getSourceSetName() + "\">");

				for (SourceFileProperty property : info.properties()) {
					out.println("  <Property name=\"" + property.name() + "\" regex=\"" + StringEscapeUtils.escapeXml11(property.regex()) + "\"/>");
				}


				for (SourceFile sourceFile : e.getFiles()) {
					for (SourceFileProperty property : info.properties()) {
						int i = 0;
						Matcher p = Pattern.compile(property.regex(), Pattern.MULTILINE | Pattern.DOTALL).matcher(sourceFile.getContent());
						while (p.find()) {
							i++;
						}

						// Ensure property has at least one match
						if (i == 0) {
							processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Property '" + property.name() + "' for '" + sourceFile + "' not found");
						}
					}

					out.println("  <SourceFile path=\"" + sourceFile.getPath() + "\" name=\"" + sourceFile.getName() + "\"><![CDATA[");
					out.println(sourceFile.getContent());
					out.println("  ]]></SourceFile>");
				}
				out.println("</SourceElement>");
			}
		} catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

	private List<dev.gradleplugins.fixtures.sources.Element> copyDirToResource(Path sourcePath, String[] excludes, String[] includes) {
		List<Pattern> excludePatterns = Arrays.stream(excludes).map(it -> it.replace(".", "\\.").replace("**", "{{dir}}").replace("*", "{{all}}").replace("{{dir}}", ".*").replace("{{all}}", "[^/]*")).map(it -> Pattern.compile(it, Pattern.DOTALL)).collect(Collectors.toList());
		excludePatterns.add(Pattern.compile(".*/\\.DS_Store", Pattern.DOTALL));

		List<Pattern> includePatterns = Arrays.stream(includes).map(it -> it.replace(".", "\\.").replace("**", "{{dir}}").replace("*", "{{all}}").replace("{{dir}}", ".*").replace("{{all}}", "[^/]*")).map(it -> Pattern.compile(it, Pattern.DOTALL)).collect(Collectors.toList());

		Path srcDir = sourcePath.resolve("src");
		assert Files.isDirectory(srcDir);

		List<dev.gradleplugins.fixtures.sources.Element> result = new ArrayList<>();
		try (Stream<Path> sourceSetDirs = Files.list(srcDir).filter(Files::isDirectory)) {
			for (Path sourceSetDir : sourceSetDirs.collect(Collectors.toList())) {
				List<SourceFile> sourceFiles = new ArrayList<>();
				Files.walkFileTree(sourceSetDir, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Path relativePath = sourceSetDir.relativize(file);
						if (!includePatterns.isEmpty() && includePatterns.stream().noneMatch(it -> it.matcher("/" + relativePath).find())) {
							return FileVisitResult.CONTINUE;
						}

						if (excludePatterns.stream().anyMatch(it -> it.matcher("/" + relativePath).find())) {
							return FileVisitResult.CONTINUE;
						}

						sourceFiles.add(SourceFile.from(relativePath, () -> new String(Files.readAllBytes(file))));
						return FileVisitResult.CONTINUE;
					}
				});
				result.add(SourceElement.ofFiles(sourceFiles).withSourceSetName(sourceSetDir.getFileName().toString()));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	@Override
	public Set<String> getSupportedOptions() {
		return Collections.singleton("basePath");
	}
}
