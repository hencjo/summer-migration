#!/usr/bin/env bash
set -euo pipefail

usage() {
  echo "Usage: $0 <version> [--publish]" >&2
  exit 2
}

VERSION="${1:-}"
MODE="${2:-}"

[[ -n "$VERSION" && "$VERSION" =~ ^[0-9][0-9A-Za-z._-]*$ ]] || usage
[[ -z "$MODE" || "$MODE" == "--publish" ]] || usage

if [[ -n "$(git status --porcelain)" ]]; then
  echo "Refusing to release with an unclean worktree." >&2
  exit 1
fi

TAG_COMMIT="$(git rev-list -n 1 "$VERSION" 2>/dev/null || true)"
HEAD_COMMIT="$(git rev-parse HEAD)"
if [[ "$TAG_COMMIT" != "$HEAD_COMMIT" ]]; then
  echo "Refusing to release: tag '$VERSION' must point at HEAD." >&2
  exit 1
fi

PROFILES="release,release-dry-run"
if [[ "$MODE" == "--publish" ]]; then
  read -r -p "Type '$VERSION' to publish it to Maven Central: " CONFIRMATION
  if [[ "$CONFIRMATION" != "$VERSION" ]]; then
    echo "Publishing cancelled."
    exit 1
  fi
  PROFILES="release"
else
  echo "Dry run: building and signing locally; nothing will be uploaded."
fi

mvn -P"$PROFILES" -Drevision="$VERSION" clean deploy
