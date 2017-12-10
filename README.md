# scala-envconfig

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
