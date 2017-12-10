# scala-envconfig

[![Build status][shield-build]][info-build]
[![Apache 2.0 licensed][shield-license]][info-license]

`scala-envconfig` is a simple Scala library for parsing configuration
 from environmental variables inspired by 
 [kelseyhightower/envconfig](https://github.com/kelseyhightower/envconfig) 
 and [joeshaw/envdecode](https://github.com/joeshaw/envdecode) libraries for Go.
 
 ## Usage
 
 ```scala
 import com.github.foxmk.envconfig._
 
 @envprefix("FEATURE_")
 case class Config(
   @env("NAME") name: String,
   @env("ENABLED", default = Some("true")) enabled: Boolean
 )
 ```

Set up your enviroment:

```bash
export FEATURE_NAME=foo
export FEATURE_ENABLED=false
```

and parse the configuration:

```scala
import com.github.foxmk.envconfig._

val config = ConfigParser.parse[Config](sys.env) 
```

## Known limitations

- Config class should be top-level declaration
- Parameters in `env` annotation should go in defined order (`name` and then `default`)

[info-build]: https://travis-ci.org/foxmk/scala-envconfig
[info-license]: LICENSE
[shield-build]: https://travis-ci.org/foxmk/scala-envconfig.svg?branch=master
[shield-license]: https://img.shields.io/badge/license-Apache_2.0-blue.svg
