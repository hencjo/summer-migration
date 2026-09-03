#!/usr/bin/env bash
set -euo pipefail

TYPE="${1:-}"
COMMIT="${2:-}"
REFERENCE="${3:-}"

case "$TYPE" in
  feature|improvement) SECTION="Features and improvements" ;;
  fix)                 SECTION="Bug fixes" ;;
  chore)               SECTION="Chores" ;;
  breaking)            SECTION="Breaking changes" ;;
  *)
    echo "Usage: changelog <feature|improvement|fix|chore|breaking> <commit> [#pr-or-issue]" >&2
    exit 2
    ;;
esac

[[ -n "$COMMIT" ]] || {
  echo "Usage: changelog <feature|improvement|fix|chore|breaking> <commit> [#pr-or-issue]" >&2
  exit 2
}

FULL_COMMIT="$(git rev-parse --verify "$COMMIT^{commit}")"
SHORT_COMMIT="$(git rev-parse --short "$FULL_COMMIT")"
SUBJECT="$(git show -s --format=%s "$FULL_COMMIT")"

if [[ "$SUBJECT" == "Merge pull request "* ]]; then
  DESCRIPTION="$(git show -s --format=%b "$FULL_COMMIT" | sed -n '/./{p;q;}')"
  NUMBER="$(sed -n 's/^Merge pull request #\([0-9][0-9]*\).*/\1/p' <<< "$SUBJECT")"
  LINK="https://github.com/hencjo/summer-migration/pull/$NUMBER"
elif [[ "$REFERENCE" =~ ^#([0-9]+)$ ]]; then
  DESCRIPTION="$SUBJECT"
  NUMBER="${BASH_REMATCH[1]}"
  LINK="https://github.com/hencjo/summer-migration/issues/$NUMBER"
else
  echo "No PR or issue found; pass one as the third argument, for example '#5'." >&2
  exit 2
fi

ENTRY="* $DESCRIPTION ([#${NUMBER}](${LINK}))"
if grep -Fq "$LINK" CHANGELOG.md; then
  echo "#$NUMBER is already in CHANGELOG.md"
  exit 0
fi

TMP="$(mktemp)"
trap 'rm -f "$TMP"' EXIT

awk -v section="## $SECTION" -v entry="$ENTRY" '
  BEGIN { in_unreleased = 0; section_found = 0; inserted = 0 }
  $0 == "# UNRELEASED" {
    in_unreleased = 1
    print
    next
  }
  in_unreleased && /^# / {
    if (!section_found) {
      print ""
      print section
      print ""
      print entry
    }
    in_unreleased = 0
  }
  in_unreleased && $0 == section {
    section_found = 1
    print
    print ""
    print entry
    inserted = 1
    next
  }
  { print }
  END {
    if (in_unreleased && !section_found) {
      print ""
      print section
      print ""
      print entry
    }
  }
' CHANGELOG.md > "$TMP"

mv "$TMP" CHANGELOG.md
trap - EXIT
echo "Added $SHORT_COMMIT to $SECTION"
