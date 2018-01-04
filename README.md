# Hydra microframework

Hypermedia-driven Web API microframework which supports Hydra (http://www.hydra-cg.com/) and json-ld (http://json-ld.org/).

Library use AspectJ, Spring Core & MVC, Lombok and Gson.  

Written for personal purposes. Use at your own risk.

## Installation
Just add dependency to you `pom.xml`. You can do it using JitPack:
```
<dependency>
    <groupId>com.github.Omenstudio</groupId>
    <artifactId>hydra-microframework</artifactId>
    <version>1.1</version>
</dependency>
```

And do not forget to add repository url:
```
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

That's all.

## How to use
The best way to understand the library - see how it already been used. For example:
* https://github.com/Omenstudio/library-hypermedia-service-articles
* https://github.com/Omenstudio/library-hypermedia-service-books 


In common way you need to
- Add annotations to you models: `@HydraEntity`, `@HydraField`, `@HydraLink`
- Add annotations to your MVC controllers: `@HydraGetRequest`, `@HydraPostRequest`, `@HydraPutRequest`, `@HydraDeleteRequest`
- Add vocab(apidoc) controller: Autowire `VocabBuilder` and call method `buildVocabulary`
- Add context controller if you want: `@HydraContextClass`, `@HydraContextCollection`, `@HydraContextEntryPoint`
- Enable AspectJAutoProxy and set components scan location in your Configuration class. For instance:
```
@Configuration
@EnableAspectJAutoProxy
@ComponentScan("com.github.omenstudio.hydra.*")
public class HydraConfig {
}
```

## See also
- Simple books web service, which use the library: https://github.com/Omenstudio/library-hypermedia-service-books
- Simple articles web service, which use the library: https://github.com/Omenstudio/library-hypermedia-service-articles
- WEB UI library application which work with articles and books services: https://github.com/Omenstudio/hypermedia-library-web-ui


