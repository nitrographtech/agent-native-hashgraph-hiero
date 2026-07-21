 #!/usr/bin/env bash
 set -euo pipefail

build_compute_request() {
  local QUANTITY=0
  local CPU=0
  local MEMORY=0
  local DURATION=0
  local RUN_ID=0
  local RUN_NUMBER=0
  local RUN_ATTEMPT=0
  local OWNER=""
  local REPOSITORY=""
  local JOB=""
  while getopts "q:c:m:d:i:n:a:o:r:j:h" arg; do
    case $arg in
      q) QUANTITY=${OPTARG} ;;
      c) CPU=${OPTARG} ;;
      m) MEMORY=${OPTARG} ;;
      d) DURATION=${OPTARG} ;;
      i) RUN_ID=${OPTARG} ;;
      n) RUN_NUMBER=${OPTARG} ;;
      a) RUN_ATTEMPT=${OPTARG} ;;
      o) OWNER="${OPTARG}" ;;
      r) REPOSITORY="${OPTARG}" ;;
      j) JOB="${OPTARG}" ;;
      h)
        echo "Usage: ${0} -q <quantity> -c <cpu> -m <memory> -d <duration> -i <run_id> -n <run_number> -a <run_attempt> -o <owner> -r <repository> -j <job>"
        exit 0
        ;;
      *)
        echo "Error: Invalid option"
        exit 1
        ;;
    esac
  done

  # validate that all required options are provided
  if [[ "$QUANTITY" == "0" ]] || [[ "$CPU" == "0" ]] || \
     [[ "$MEMORY" == "0" ]] || [[ "$DURATION" == "0" ]] || \
     [[ "$RUN_ID" == "0" ]] || [[ "$RUN_NUMBER" == "0" ]] || \
     [[ "$RUN_ATTEMPT" == "0" ]] || [[ -z "$OWNER" ]] || \
     [[ -z "$REPOSITORY" ]] || [[ -z "$JOB" ]]; then
    echo "Error: Missing required options"
    exit 1
  fi

  # build curl body into a variable
  local REQUEST_BODY
  REQUEST_BODY=$(cat <<EOF
{
  "instances":[{
    "quantity":${QUANTITY},
    "resources":{"cpu":${CPU},"memory":${MEMORY}}
  }],
  "duration":${DURATION},
  "workflow":{
    "run":{
      "id":${RUN_ID},
      "number":${RUN_NUMBER},
      "attempt":${RUN_ATTEMPT}
    },
    "owner":"${OWNER}",
    "repository":"${REPOSITORY}",
    "job":"${JOB}"
  }
}
EOF
)

  REQUEST_BODY=$(echo "${REQUEST_BODY}" | jq -c .)
  echo "${REQUEST_BODY}"
}

build_compute_request "$@"