#!/usr/bin/env bash
set -euo pipefail

usage() {
  echo "Usage: release [--publish]" >&2
  exit 2
}

MODE="${1:-}"
[[ -z "$MODE" || "$MODE" == "--publish" ]] || usage

if [[ -n "$(git status --porcelain)" ]]; then
  echo "Refusing to release with an unclean worktree." >&2
  exit 1
fi

if [[ "$MODE" == "--publish" ]]; then
  VERSION="$(git describe --exact-match --tags HEAD 2>/dev/null || true)"
  [[ "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || {
    echo "Refusing to publish: HEAD must have an exact semantic-version tag." >&2
    exit 1
  }
  grep -Fq "# Version $VERSION" CHANGELOG.md || {
    echo "Refusing to publish: $VERSION is not finalized in CHANGELOG.md." >&2
    exit 1
  }
  BRANCH="$(git symbolic-ref --quiet --short HEAD || true)"
  [[ -n "$BRANCH" ]] || {
    echo "Refusing to publish from a detached HEAD." >&2
    exit 1
  }
  git push --dry-run origin "$BRANCH" "$VERSION"
  echo "Publishing $VERSION to Maven Central."
  mvn -Prelease -Drevision="$VERSION" clean deploy
  git push origin "$BRANCH" "$VERSION"
  exit 0
fi

LATEST_TAG="$(git tag --list | grep -E '^[0-9]+\.[0-9]+(\.[0-9]+)?$' | sort -V | tail -n 1)"
[[ -n "$LATEST_TAG" ]] || {
  echo "No semantic version tag found." >&2
  exit 1
}

IFS=. read -r MAJOR MINOR PATCH <<< "$LATEST_TAG"
PATCH="${PATCH:-0}"

UNRELEASED="$(awk '
  $0 == "# UNRELEASED" { found = 1; next }
  found && /^# / { exit }
  found { print }
' CHANGELOG.md)"

has_entries() {
  local section="$1"
  awk -v section="## $section" '
    $0 == section { found = 1; next }
    found && /^## / { exit }
    found && /^\* / { entry = 1 }
    END { exit !entry }
  ' <<< "$UNRELEASED"
}

section_entries() {
  local section="$1"
  awk -v section="## $section" '
    $0 == section { found = 1; next }
    found && /^## / { exit }
    found && /^\* / { print }
  ' <<< "$UNRELEASED"
}

if has_entries "Breaking changes"; then
  VERSION="$((MAJOR + 1)).0.0"
elif has_entries "Features and improvements"; then
  VERSION="$MAJOR.$((MINOR + 1)).0"
elif has_entries "Bug fixes" || has_entries "Chores"; then
  VERSION="$MAJOR.$MINOR.$((PATCH + 1))"
else
  echo "Nothing to release in the UNRELEASED section." >&2
  exit 1
fi

echo "Preparing $VERSION from $LATEST_TAG; nothing will be uploaded."
mvn -Prelease,release-dry-run -Drevision="$VERSION" clean deploy

TMP="$(mktemp)"
trap 'rm -f "$TMP"' EXIT
{
  cat <<'EOF'
# UNRELEASED

## Breaking changes

## Features and improvements

## Bug fixes

## Chores

EOF
  printf '# Version %s\n' "$VERSION"
  for SECTION in "Breaking changes" "Features and improvements" "Bug fixes" "Chores"; do
    ENTRIES="$(section_entries "$SECTION")"
    if [[ -n "$ENTRIES" ]]; then
      printf '\n## %s\n\n%s\n' "$SECTION" "$ENTRIES"
    fi
  done
  printf '\n'
  awk '
    $0 == "# UNRELEASED" { found = 1; next }
    found && /^# / { history = 1 }
    history { print }
  ' CHANGELOG.md
} > "$TMP"
mv "$TMP" CHANGELOG.md
trap - EXIT

git add CHANGELOG.md
git commit -m "Release $VERSION"
git tag -a "$VERSION" -m "Version $VERSION"

echo "Prepared and tagged $VERSION locally. Run 'release --publish' to publish and push it."
