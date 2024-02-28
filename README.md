## What is it?

The template project groups everything required to build programmatic templates for testing, samples, init-template, etc.

## Use Cases

### Testing - Source Element

For testing, we encourage to use the source elements directly.
It's a simple as extending one of the several pre-built element and completing the API.
We compose elements together to create bigger and more complex elements.
Including the content of the elements can be cumbersome (writing, testing, linting).
For this reason, we include a processor layer.

### Annotation Processor

The annotation processor serves the purpose of linking an on-disk file/directory to a source element.
It ensures the elements and the on-disk location stays in sync and are validated at compile time.
Although developers can use the annotation processor manually, we recommend using the Gradle plugin.
The annotation processor also allow for text files to include replacement pattern.
The existence of those patterns are validated at compile time.

### Gradle Plugin

The plugin can be used for a whole build (when developing the source element's content) or for a particular project.
In both cases, a `templates` source set is created and wired with the annotation processor.
Other project can depend on the source elements using `templates(<coordinate>)`.

### Embedded Application (not yet implemented)

The embedded application is a way for the source elements to aggregate into ready-made template that can be manipulated using the normal command-line or similar client.
The goal here is to allow reusability as init-template for Gradle `init`, for example.
