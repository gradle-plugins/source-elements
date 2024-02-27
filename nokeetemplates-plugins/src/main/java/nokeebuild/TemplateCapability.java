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
import org.gradle.api.capabilities.Capability;

import java.util.Objects;

class TemplateCapability implements Capability {
	private final Project project;
	private static final String featureName = "templates";

	public TemplateCapability(Project project) {
		this.project = project;
	}

	@Override
	public String getGroup() {
		return project.getGroup().toString();
	}

	@Override
	public String getName() {
		return project.getName() + "-" + featureName;
	}

	@Override
	public String getVersion() {
		return project.getVersion().toString();
	}

	//region See ProjectDerivedCapability in Gradle codebase
	@Override
	public int hashCode() {
		return 31 * project.hashCode() + featureName.hashCode();
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
