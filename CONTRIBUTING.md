# Contributing

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

Prepare the next semantic version from the entries under `# UNRELEASED`:

```sh
devenv shell -- release
```

Breaking changes bump the major version, features and improvements bump the minor version, and bug fixes or chores bump the patch version. Preparation signs locally, moves populated sections into the generated version section, creates a fresh `# UNRELEASED` template, commits the changelog, and creates an annotated tag without uploading anything.

Inspect the generated commit and tag, then explicitly publish. The publish command is non-interactive, uploads to Maven Central, and pushes the branch and tag only after publication succeeds:

```sh
devenv shell -- release --publish
```
