#!/usr/bin/env bash
set -euo pipefail

decode_jwt() {
  local b64="${1:-}"

  if [[ -z "${b64}" ]]; then
    echo "Error: No base64-encoded JWT provided." >&2
    exit 1
  fi

  local jwt
  jwt=$(echo -n "${b64}" | base64 -d)

  if [[ -z "${jwt}" ]]; then
    echo "Error: Decoded JWT is empty. Please check the Chewie token." >&2
    exit 1
  fi

  echo -n "${jwt}"
}

decode_jwt "$@"
