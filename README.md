# Summer Migration

Summer Migration is a simple library for doing database migrations. It includes facilities for upgrading using scripts and doing java based migrations.

It's design goals are:
* Minimalistic dependencies. Summer Migration don't clutter your classpath. And it doesn't depend on any logging framework. 
* Configuration in code, not XML or Annotations. 
* No resource discovery. Upgrades fail when needed resources aren't found, but the found resources does not determine how your migration turn out.
* Simple to extend with own migrations.

Example:

```java
import static com.hencjo.summer.migration.dsl.DSL.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.postgresql.ds.PGSimpleDataSource;
import com.hencjo.summer.migration.Migrator;
import com.hencjo.summer.migration.dsl.MigrationsDescription;

public class Upgrader {
	public static MigrationsDescription upgradeDescription() {
		return migrations(
			migration("1.0-baseline").installsThrough(script("1.0-baseline.sql")),
			migration("1.1-base64-decode-some-field", V1_1_Migrations.base64decodeSomeField())
		);
	}

	public void upgrade(PGSimpleDataSource datasource) throws SQLException, IOException {
		Migrator migrator = new Migrator();
		try (Connection connection = datasource.getConnection()) {
			migrator.migrate(connection, upgradeDescription());
		}
	}
}
```

Include it in your pom like this:
```xml
<dependency>
    <groupId>com.hencjo.summer</groupId>
    <artifactId>summer-migration</artifactId>
    <version>1.0</version>
</dependency>
```

## Release Notes

### Version 1.1

* **Changed output to stdout while applying migrations** Before 1.1, Summer Migration would print 'Applying migration "X" ... ' (without newline) and then completing the  line with 'DONE.' (with newline). It was nice on your console, but didn't work well with logging systems that only replicate lines when newline has been sent. Version 1.1 instead prints "Applying migration "x" ..." (with newline). "DONE." will no longer be printed.
