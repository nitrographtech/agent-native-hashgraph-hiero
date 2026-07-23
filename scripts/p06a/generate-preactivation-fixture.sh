#!/usr/bin/env bash
set -euo pipefail

readonly EXPECTED_COMMIT=ff6490d66994da11af72e1d2f185ec7874fa383a
readonly EXPECTED_JAVA=/home/ericf/.local/share/jdks/temurin-25.0.2+10
readonly SOURCE_DIR=${1:?usage: generate-preactivation-fixture.sh SOURCE_DIR OUTPUT_DIR}
readonly OUTPUT_DIR=${2:?usage: generate-preactivation-fixture.sh SOURCE_DIR OUTPUT_DIR}

test "$(git -C "$SOURCE_DIR" rev-parse HEAD)" = "$EXPECTED_COMMIT"
test -z "$(git -C "$SOURCE_DIR" status --porcelain)"
test ! -e "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR/logs" "$OUTPUT_DIR/resources"

export JAVA_HOME=$EXPECTED_JAVA
export PATH="$JAVA_HOME/bin:$PATH"

{
  date -u +generated_at_utc=%FT%TZ
  git -C "$SOURCE_DIR" show -s --format='source=%H%ntree=%T%nsource_date=%aI%nsubject=%s'
  uname -a
  nproc
  free -b
  "$SOURCE_DIR/gradlew" --version
} >"$OUTPUT_DIR/logs/environment.txt" 2>&1

echo "Starting pinned full-runtime node. Preserve this terminal until client population and freeze complete."
echo "Output directory: $OUTPUT_DIR"
echo "After gRPC 50211 is ready, run the documented population selectors from a second terminal."

cd "$SOURCE_DIR"
/usr/bin/time -v -o "$OUTPUT_DIR/logs/node-resource.txt" \
  ./gradlew :app:run --no-daemon --stacktrace \
  >"$OUTPUT_DIR/logs/node.stdout" 2>"$OUTPUT_DIR/logs/node.stderr"

