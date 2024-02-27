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

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ModuleDependencyCapabilitiesHandler;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;

public final class TemplateDependencyModifier {
	private final DependencyHandler dependencyFactory;

	private TemplateDependencyModifier(DependencyHandler dependencyFactory) {
		this.dependencyFactory = dependencyFactory;
	}

	public <DependencyType extends ModuleDependency> DependencyType modify(DependencyType dependency) {
		dependency.capabilities(requireTemplateCapability(dependency));
		return dependency;
	}

	public ExternalModuleDependency modify(CharSequence dependencyNotation) {
		final ExternalModuleDependency result = (ExternalModuleDependency) dependencyFactory.create(dependencyNotation);
		result.capabilities(requireTemplateCapability(result));
		return result;
	}

	public ProjectDependency modify(Project project) {
		final ProjectDependency result = (ProjectDependency) dependencyFactory.create(project);
		result.capabilities(requireTemplateCapability(result));
		return result;
	}

	//region Groovy DSL support
	public ProjectDependency call(Project project) {
		return modify(project);
	}

	public ExternalModuleDependency call(CharSequence dependencyNotation) {
		return modify(dependencyNotation);
	}

	public <DependencyType extends ModuleDependency> DependencyType call(DependencyType dependency) {
		return modify(dependency);
	}
	//endregion

	private Action<ModuleDependencyCapabilitiesHandler> requireTemplateCapability(ModuleDependency dependency) {
		return it -> {
			if (dependency instanceof ProjectDependency) {
				it.requireCapability(new TemplateCapability(((ProjectDependency) dependency).getDependencyProject()));
			} else {
				it.requireCapability(new TemplateCapability(dependency));
			}
		};
	}

	public static TemplateDependencyModifier forProject(Project project) {
		return new TemplateDependencyModifier(project.getDependencies());
	}
}
