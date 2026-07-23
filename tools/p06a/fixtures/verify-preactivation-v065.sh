#!/usr/bin/env bash
set -euo pipefail

readonly REPOSITORY_ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)
readonly METADATA="$REPOSITORY_ROOT/docs/nitrograph/fixtures/preactivation-v065.manifest.json"
readonly CHECKSUMS="$REPOSITORY_ROOT/docs/nitrograph/fixtures/preactivation-v065.SHA256"
readonly ARCHIVE=${1:?usage: verify-preactivation-v065.sh ARCHIVE EXTRACTION_DIRECTORY}
readonly DESTINATION=${2:?usage: verify-preactivation-v065.sh ARCHIVE EXTRACTION_DIRECTORY}
readonly FIXTURE_NAME=nitrograph-p06a-preactivation-v065-ff6490d-round4744

test -f "$ARCHIVE"
test ! -e "$DESTINATION"

readarray -t expected < <(python3 - "$METADATA" <<'PY'
import json, sys
m=json.load(open(sys.argv[1], encoding="utf-8"))
print(m["archive"]["sha256"])
print(m["file_count"])
print(m["saved_state_round"])
print(m["state_root"])
print(m["public_fixture_certificate_sha256"])
PY
)

printf '%s  %s\n' "${expected[0]}" "$ARCHIVE" | sha256sum --check --status
mkdir -p "$DESTINATION"
tar -xzf "$ARCHIVE" -C "$DESTINATION"
readonly ROOT="$DESTINATION/$FIXTURE_NAME"
test -d "$ROOT"

(
  cd "$ROOT"
  sha256sum --check --strict "$CHECKSUMS"
  find . -type f -printf '%p\n' | LC_ALL=C sort >"$DESTINATION/actual-files.txt"
)
cut -d' ' -f3- "$CHECKSUMS" | LC_ALL=C sort >"$DESTINATION/expected-files.txt"
cmp "$DESTINATION/expected-files.txt" "$DESTINATION/actual-files.txt"
test "$(wc -l <"$DESTINATION/actual-files.txt")" -eq "${expected[1]}"

python3 - "$ROOT" "${expected[2]}" "${expected[3]}" "${expected[4]}" <<'PY'
import base64, hashlib, json, pathlib, sys
root=pathlib.Path(sys.argv[1])
round_expected=int(sys.argv[2])
root_expected=sys.argv[3]
cert_expected=sys.argv[4]
state=root/f"data/saved/com.hedera.services.ServicesMain/0/123/{round_expected}"
metadata=(state/"stateMetadata.txt").read_text(encoding="utf-8")
values={line.split(":",1)[0]:line.split(":",1)[1].strip()
        for line in metadata.splitlines() if ":" in line}
if int(values["ROUND"]) != round_expected or values["HASH"] != root_expected:
    raise SystemExit("saved-state round or root mismatch")
roster=json.loads((state/"currentRoster.json").read_text(encoding="utf-8"))
cert=base64.b64decode(roster["rosterEntries"][0]["gossipCaCertificate"])
if hashlib.sha256(cert).hexdigest() != cert_expected:
    raise SystemExit("roster certificate fingerprint mismatch")
PY

test "$(find "$ROOT" -type f -iname '*private*.pem' | wc -l)" -eq 0
if rg -a -l --no-messages 'BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY' "$ROOT" >/dev/null; then
  echo "Private key material found in fixture" >&2
  exit 1
fi
printf 'Verified P06A fixture: %s files, round %s, root %s\n' \
  "${expected[1]}" "${expected[2]}" "${expected[3]}"
