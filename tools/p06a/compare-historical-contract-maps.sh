#!/usr/bin/env bash
set -euo pipefail

readonly REPOSITORY_ROOT=${1:?usage: compare-historical-contract-maps.sh REPOSITORY_ROOT PRE_STATE_DIR POST_STATE_DIR OUTPUT_DIR}
readonly PRE_STATE_DIR=${2:?usage: compare-historical-contract-maps.sh REPOSITORY_ROOT PRE_STATE_DIR POST_STATE_DIR OUTPUT_DIR}
readonly POST_STATE_DIR=${3:?usage: compare-historical-contract-maps.sh REPOSITORY_ROOT PRE_STATE_DIR POST_STATE_DIR OUTPUT_DIR}
readonly OUTPUT_DIR=${4:?usage: compare-historical-contract-maps.sh REPOSITORY_ROOT PRE_STATE_DIR POST_STATE_DIR OUTPUT_DIR}
readonly JAVA_HOME=${JAVA_HOME:?JAVA_HOME must identify the validated JDK}
readonly MODULE=com.hedera.state.validator/com.hedera.statevalidation.StateOperatorCommand
readonly INSTALL_LIB="$REPOSITORY_ROOT/hedera-state-validator/build/install/hedera-state-validator/lib"
readonly BUILD_JAR="$REPOSITORY_ROOT/hedera-state-validator/build/libs/hedera-state-validator-0.75.0-SNAPSHOT.jar"

test -d "$REPOSITORY_ROOT"
test -d "$PRE_STATE_DIR"
test -d "$POST_STATE_DIR"
test -d "$OUTPUT_DIR"
test -x "$JAVA_HOME/bin/java"

"$REPOSITORY_ROOT/gradlew" -p "$REPOSITORY_ROOT" :hedera-state-validator:jar --no-daemon
test -f "$BUILD_JAR"
test -d "$INSTALL_LIB"

readonly MODULE_PATH=$(mktemp -d "$OUTPUT_DIR/module-path.XXXXXX")
trap 'find "$MODULE_PATH" -type l -delete; rmdir "$MODULE_PATH"' EXIT
find "$INSTALL_LIB" -maxdepth 1 -type f ! -name 'hedera-state-validator-*.jar' \
  -exec ln -s '{}' "$MODULE_PATH/" \;
ln -s "$BUILD_JAR" "$MODULE_PATH/"

"$JAVA_HOME/bin/java" -p "$MODULE_PATH" -m "$MODULE" \
  "$PRE_STATE_DIR" p06a-contract-map-fingerprint -o "$OUTPUT_DIR/preactivation.json"
"$JAVA_HOME/bin/java" -p "$MODULE_PATH" -m "$MODULE" \
  "$POST_STATE_DIR" p06a-contract-map-fingerprint -o "$OUTPUT_DIR/postactivation.json"

sed '/"state_root":/d' "$OUTPUT_DIR/preactivation.json" >"$OUTPUT_DIR/preactivation.maps.json"
sed '/"state_root":/d' "$OUTPUT_DIR/postactivation.json" >"$OUTPUT_DIR/postactivation.maps.json"
cmp "$OUTPUT_DIR/preactivation.maps.json" "$OUTPUT_DIR/postactivation.maps.json"
printf '%s\n' 'P06A historical contract maps: UNCHANGED'
