#!/bin/bash
set -euo pipefail

usage() {
  cat <<EOF
Usage:
  ./run-compose-batch.sh \
    --config-url=<url-or-local-path> \
    --case-study-subdir=<branch-or-folder> \
    --output-root=<host-output-root> \
    [--notify-email=<email>] \
    [--runs=31] \
    [--compose-file=docker-compose.yml]

Example:
  ./run-compose-batch.sh \
    --config-url=https://raw.githubusercontent.com/danieledipompeo/easier-experiment-data/main/ecsa26-nsgaii-ccm-eval-102-surrogate--50/config.ini \
    --case-study-subdir=modelling-energy \
    --output-root=/mnt/data/easier/ecsa26-nsgaii-ccm-eval-102-surrogate--50 \
    --notify-email=foo@example.com \
    --runs=31
EOF
}

CONFIG_URL=""
CASE_STUDY_SUBDIR=""
OUTPUT_ROOT=""
NOTIFY_EMAIL=""
RUNS="31"
COMPOSE_FILE="docker-compose.yml"
CONFIG_INPUT=""
COMPOSE_RUN_EXTRA_ARGS=()

for arg in "$@"; do
  case "$arg" in
    --config-url=*)
      CONFIG_URL="${arg#*=}"
      ;;
    --case-study-subdir=*)
      CASE_STUDY_SUBDIR="${arg#*=}"
      ;;
    --output-root=*)
      OUTPUT_ROOT="${arg#*=}"
      ;;
    --notify-email=*)
      NOTIFY_EMAIL="${arg#*=}"
      ;;
    --runs=*)
      RUNS="${arg#*=}"
      ;;
    --compose-file=*)
      COMPOSE_FILE="${arg#*=}"
      ;;
    --help|-h)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $arg" >&2
      usage
      exit 1
      ;;
  esac
done

if [[ -z "$CONFIG_URL" || -z "$CASE_STUDY_SUBDIR" || -z "$OUTPUT_ROOT" ]]; then
  echo "Missing required arguments." >&2
  usage
  exit 1
fi

if [[ "$CONFIG_URL" =~ ^https?:// ]]; then
  CONFIG_INPUT="$CONFIG_URL"
else
  if [[ ! -f "$CONFIG_URL" ]]; then
    echo "Config file not found: $CONFIG_URL" >&2
    exit 1
  fi
  host_config="$(realpath "$CONFIG_URL")"
  CONFIG_INPUT="/tmp/easier-input-config.ini"
  COMPOSE_RUN_EXTRA_ARGS=(-v "${host_config}:${CONFIG_INPUT}:ro")
fi

if ! [[ "$RUNS" =~ ^[0-9]+$ ]] || [[ "$RUNS" -lt 1 ]]; then
  echo "--runs must be a positive integer." >&2
  exit 1
fi

echo "Starting surrogate service..."
docker compose -f "$COMPOSE_FILE" up -d easier-surrogate

echo "Submitting $RUNS easier-uml jobs..."
for i in $(seq 1 "$RUNS"); do
  run_dir="${OUTPUT_ROOT}/run${i}"
  mkdir -p "$run_dir"

  cid=$(RUN_OUTPUT_DIR="$run_dir" \
    CONFIG_URL="$CONFIG_INPUT" \
    CASE_STUDY_SUBDIR="$CASE_STUDY_SUBDIR" \
    NOTIFY_EMAIL="$NOTIFY_EMAIL" \
    docker compose -f "$COMPOSE_FILE" --profile jobs run -d --rm "${COMPOSE_RUN_EXTRA_ARGS[@]}" easier-uml-job)

  echo "run${i}: ${cid} -> ${run_dir}"
done

echo "All jobs submitted."
echo "Check surrogate logs with: docker compose -f ${COMPOSE_FILE} logs -f easier-surrogate"
echo "Check running containers with: docker ps"
