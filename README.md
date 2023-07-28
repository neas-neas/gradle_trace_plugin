apply plugin in **settings.gradle**

not upload to gradlePluginPortal repository yet, so only support apply plugin in old-fashioned way

```groovy
buildscript{
    repositories {
        mavenCentral()
    }
    dependencies{
        classpath 'io.github.neas-neas:gradle-trace-plugin:0.0.2'
    }
}
apply plugin: 'build.trace'
```

after build, it will generate two file under the `build/trace`

```
buildOpTrace.json
buildOpTrace-analyzer.txt
```
`buildOpTrace.json` contains the raw data collect by the plugin  
and `buildOpTrace-analyzer.txt` contains some human-readable information about the build

not support configuration-cache yet