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
    <version>1.2</version>
</dependency>
```

## Releasing

Releases are made locally from an annotated version tag. Maven Central deployments are immutable, so the default command only builds and signs the publishable artifacts locally; it never uploads them.

One-time setup:

1. Log in to the [Central Publisher Portal](https://central.sonatype.com/) and confirm access to the `com.hencjo.summer` namespace.
2. Generate a Portal user token and put its username and password in `~/.m2/settings.xml` under the `central` server id. Do not commit this file.
3. Restore the existing PGP signing key, or generate a replacement and publish its public key. Keep the private key and passphrase out of the repository; `gpg-agent` handles the interactive passphrase prompt.

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>PORTAL_TOKEN_USERNAME</username>
      <password>PORTAL_TOKEN_PASSWORD</password>
    </server>
  </servers>
</settings>
```

For a release, commit the changes, create and push an annotated tag, then run:

```sh
devenv shell -- release 1.3
```

Inspect the signed JARs and `.asc` files in `target/`. To upload and automatically publish that exact tagged version, rerun the command with the explicit `--publish` confirmation:

```sh
devenv shell -- release 1.3 --publish
```
