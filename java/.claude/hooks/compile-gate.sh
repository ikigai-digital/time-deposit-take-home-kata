#!/usr/bin/env bash
#
# Stop hook - Claude may not finish a turn while the Java code does not compile.
#
# Runs only when files under java/ changed (git), so conversations without code changes stay fast.
# Compiles main AND test sources (mvn test-compile, ~10-20 s); the full test suite is run on demand with /verify.
# Exit 2 sends the compiler errors back to Claude, which then has to fix them before it can stop.
# Requires: bash, jq, git, mvn.

set -uo pipefail

input=$(cat)

# Already continuing because of this hook: let Claude stop, to avoid an endless loop.
[[ "$(jq -r '.stop_hook_active // false' <<<"$input")" == "true" ]] && exit 0

cd "${CLAUDE_PROJECT_DIR:-.}" || exit 0

# Nothing changed in the Java project -> nothing to check.
git status --porcelain -- java 2>/dev/null | grep -q . || exit 0

if ! output=$(mvn -q -f java/pom.xml test-compile 2>&1); then
  {
    echo "Build gate (.claude/hooks/compile-gate.sh): the code does not compile. Fix these errors before finishing:"
    errors=$(grep -E '\[ERROR\]' <<<"$output" | head -40)
    if [[ -n "$errors" ]]; then echo "$errors"; else tail -40 <<<"$output"; fi
  } >&2
  exit 2
fi

exit 0
