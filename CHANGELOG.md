# Summer Migration 1.2

## Bug fixes

* ScriptUpgradeStep does not support ; in migration script body (as part of stored procedure, for instance).

## Other

* Made `./release.sh` nix-friendly.
* Started this CHANGELOG.md.
* Upgrade junit:junit to version 4.13.1, as per dependabot.
* Minimum version of JDK supported changed to 1.8.

# Version 1.1

* **Changed output to stdout while applying migrations** Before 1.1, Summer Migration would print 'Applying migration "X" ... ' (without newline) and then completing the  line with 'DONE.' (with newline). It was nice on your console, but didn't work well with logging systems that only replicate lines when newline has been sent. Version 1.1 instead prints "Applying migration "x" ..." (with newline). "DONE." will no longer be printed.
