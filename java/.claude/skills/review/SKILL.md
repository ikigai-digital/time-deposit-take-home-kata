---
name: review
description: Independent review of the uncommitted changes by the read-only code-reviewer subagent, in a fresh context. Use before every commit.
argument-hint: "[optional focus, e.g. concurrency]"
context: fork
agent: code-reviewer
disable-model-invocation: true
allowed-tools: Bash(git status *) Bash(git diff *)
---

Review the uncommitted changes in this repository using your checklist.

## Changed files
!`git status --short`

## Diff against HEAD (tracked files)
!`git diff HEAD --stat`

!`git diff HEAD`

Files marked `??` above are new and untracked: read them in full, they are not part of the diff.

Extra focus requested by the user (may be empty): $ARGUMENTS
