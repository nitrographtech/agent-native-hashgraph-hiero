#!/usr/bin/env bash
set -euo pipefail

readonly EXPECTED_COMMIT=ff6490d66994da11af72e1d2f185ec7874fa383a
readonly EXPECTED_TREE=7f0bf298d73d386b0483282033eaccdc2268a9b8
readonly EXPECTED_JAVA=/home/ericf/.local/share/jdks/temurin-25.0.2+10
readonly SOURCE_DIR=${1:?usage: verify-fixture-environment.sh SOURCE_DIR}

test "$(git -C "$SOURCE_DIR" rev-parse HEAD)" = "$EXPECTED_COMMIT"
test "$(git -C "$SOURCE_DIR" rev-parse 'HEAD^{tree}')" = "$EXPECTED_TREE"
test -z "$(git -C "$SOURCE_DIR" status --porcelain)"
test -x "$EXPECTED_JAVA/bin/java"

export JAVA_HOME=$EXPECTED_JAVA
export PATH="$JAVA_HOME/bin:$PATH"

git -C "$SOURCE_DIR" show -s --format='source=%H%ntree=%T%nauthor_date=%aI%nsubject=%s'
"$SOURCE_DIR/gradlew" --version
uname -a
nproc
free -b

