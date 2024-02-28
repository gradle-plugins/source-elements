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

import org.gradle.api.Project;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

final class TemplateCapability implements Capability {
	public static final String TEMPLATE_CAPABILITY_FEATURE_NAME = "templates";
	private static final String featureName = TEMPLATE_CAPABILITY_FEATURE_NAME;
	public static final String TEMPLATE_CAPABILITY_APPENDIX = "-" + TEMPLATE_CAPABILITY_FEATURE_NAME;
	private final Supplier<String> groupSupplier;
	private final Supplier<String> nameSupplier;
	private final Supplier<String> versionSupplier;
	private final int hashCode;

	public TemplateCapability(Project project) {
		// See ProjectDerivedCapability
		this.hashCode = 31 * project.hashCode() + featureName.hashCode();
		this.groupSupplier = () -> project.getGroup().toString();
		this.nameSupplier = () -> project.getName();
		this.versionSupplier = () -> project.getVersion().toString();
	}

	public TemplateCapability(ModuleDependency dependency) {
		// See ImmutableCapability
		this.hashCode = 31 * (dependency.getName() + TEMPLATE_CAPABILITY_APPENDIX).hashCode()
			+ dependency.getGroup().hashCode();
		this.groupSupplier = () -> dependency.getGroup();
		this.nameSupplier = () -> dependency.getName();
		this.versionSupplier = () -> null;
	}

	@Override
	public String getGroup() {
		return groupSupplier.get();
	}

	@Override
	public String getName() {
		return nameSupplier.get() + TEMPLATE_CAPABILITY_APPENDIX;
	}

	@Nullable
	@Override
	public String getVersion() {
		return versionSupplier.get();
	}

	//region See ProjectDerivedCapability and ImmutableCapability in Gradle codebase
	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Capability)) {
			return false;
		}

		Capability that = (Capability) o;
		return Objects.equals(getGroup(), that.getGroup())
			&& Objects.equals(getName(), that.getName())
			&& Objects.equals(getVersion(), that.getVersion());

	}
	//endregion


	@Override
	public String toString() {
		return getGroup() + ":" + getName() + ":" + getVersion();
	}
}
