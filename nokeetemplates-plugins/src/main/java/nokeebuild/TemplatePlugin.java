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

package nokeebuild;

import groovy.lang.Closure;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.attributes.Usage;
import org.gradle.api.initialization.Settings;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;

public class TemplatePlugin implements Plugin<Object> {
	@Override
	public void apply(Object target) {
		if (target instanceof Settings) {
			doApply((Settings) target);
		} else {
			doApply((Project) target);
		}
	}

	private void doApply(Settings settings) {
		settings.getGradle().rootProject(project -> {
			project.getPluginManager().apply(TemplatePlugin.class);
		});
	}

	private void doApply(Project project) {
		project.getPluginManager().apply("java-base");

		project.getExtensions().getExtraProperties().set("templates", new Closure(project) {
			private final TemplateDependencyModifier delegate = TemplateDependencyModifier.forProject(project);

			public ProjectDependency doCall(Project project) {
				return delegate.modify(project);
			}

			public ExternalModuleDependency doCall(CharSequence dependencyNotation) {
				return delegate.modify(dependencyNotation);
			}

			public <DependencyType extends ModuleDependency> DependencyType doCall(DependencyType dependency) {
				return delegate.modify(dependency);
			}
		});

		project.getExtensions().getByType(SourceSetContainer.class).create("templates", sourceSet -> {
			project.getTasks().named(sourceSet.getCompileJavaTaskName(), JavaCompile.class, task -> {
				task.getOptions().getCompilerArgs().add("-AbasePath=" + project.getProjectDir());
			});

			final TaskProvider<Jar> jarTask = project.getTasks().register(sourceSet.getJarTaskName(), Jar.class, task -> {
				task.from(sourceSet.getOutput());
				task.getArchiveClassifier().set(TemplateCapability.TEMPLATE_CAPABILITY_FEATURE_NAME);
			});

			project.getConfigurations().create(sourceSet.getApiConfigurationName(), it -> {
				it.setCanBeResolved(false);
				it.setCanBeConsumed(false);
			});

			project.getConfigurations().getByName(sourceSet.getImplementationConfigurationName()).extendsFrom(project.getConfigurations().getByName(sourceSet.getApiConfigurationName()));

			project.getConfigurations().create(sourceSet.getApiElementsConfigurationName(), it -> {
				it.setCanBeConsumed(true);
				it.setCanBeResolved(false);
				it.extendsFrom(project.getConfigurations().getByName(sourceSet.getApiConfigurationName()));
				it.attributes(attributes -> {
					attributes.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, Usage.JAVA_API));
				});
				it.getOutgoing().capability(new TemplateCapability(project));
				it.getOutgoing().artifact(jarTask);
			});

			project.getConfigurations().create(sourceSet.getRuntimeElementsConfigurationName(), it -> {
				it.setCanBeConsumed(true);
				it.setCanBeResolved(false);
				it.extendsFrom(project.getConfigurations().getByName(sourceSet.getImplementationConfigurationName()));
				it.attributes(attributes -> {
					attributes.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, Usage.JAVA_RUNTIME));
				});
				it.getOutgoing().capability(new TemplateCapability(project));
				it.getOutgoing().artifact(jarTask);
			});

			project.getDependencies().add(sourceSet.getAnnotationProcessorConfigurationName(), "dev.gradleplugins:source-elements-annotation-processor:latest.release");
			project.getDependencies().add(sourceSet.getApiConfigurationName(), "dev.gradleplugins:gradle-fixtures-source-elements:latest.release");
		});
	}
}
