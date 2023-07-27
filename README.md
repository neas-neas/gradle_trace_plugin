apply plugin in **settings.gradle**

```groovy
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
buildscript{
    repositories {
        mavenCentral()
    }
    dependencies{
        classpath 'com.neas:gradle-trace-plugin:0.0.1'
    }
}
plugins {
    id('build.trace') version('0.0.1')
}
```

after build, it will generate two file under the `build/trace`

```
buildOpTrace.json
buildOpTrace-analyzer.txt
```
`buildOpTrace.json` contains the raw data collect by the plugin  
and `buildOpTrace-analyzer.txt` contains some human-readable information about the build