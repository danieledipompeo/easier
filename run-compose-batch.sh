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
    [--project-prefix=easier-batch] \
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
PROJECT_PREFIX="easier-batch"
COMPOSE_FILE="docker-compose.yml"
CONFIG_INPUT=""
COMPOSE_FILES=()
TMP_DIR=""
CONFIG_OVERRIDE_FILE=""

cleanup() {
  if [[ -n "$TMP_DIR" && -d "$TMP_DIR" ]]; then
    rm -rf "$TMP_DIR"
  fi
}

trap cleanup EXIT

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
    --project-prefix=*)
      PROJECT_PREFIX="${arg#*=}"
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

if [[ ! -f "$COMPOSE_FILE" ]]; then
  echo "Compose file not found: $COMPOSE_FILE" >&2
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
  TMP_DIR="$(mktemp -d)"
  CONFIG_OVERRIDE_FILE="${TMP_DIR}/config-mount.override.yml"
  cat > "$CONFIG_OVERRIDE_FILE" <<EOF
services:
  easier-uml-job:
    volumes:
      - ${host_config}:${CONFIG_INPUT}:ro
EOF
fi

if ! [[ "$RUNS" =~ ^[0-9]+$ ]] || [[ "$RUNS" -lt 1 ]]; then
  echo "--runs must be a positive integer." >&2
  exit 1
fi

PROJECT_PREFIX="$(printf '%s' "$PROJECT_PREFIX" | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9_-]+/-/g; s/^-+//; s/-+$//; s/-{2,}/-/g')"

if [[ -z "$PROJECT_PREFIX" ]]; then
  echo "--project-prefix must contain at least one letter or digit." >&2
  exit 1
fi

COMPOSE_FILES=(-f "$COMPOSE_FILE")
if [[ -n "$CONFIG_OVERRIDE_FILE" ]]; then
  COMPOSE_FILES+=(-f "$CONFIG_OVERRIDE_FILE")
fi

mkdir -p "$OUTPUT_ROOT"
PROJECTS_FILE="${OUTPUT_ROOT}/batch-projects.txt"
: > "$PROJECTS_FILE"

echo "Submitting $RUNS isolated easier-uml + easier-surrogate pairs..."

for i in $(seq 1 "$RUNS"); do
  run_dir="${OUTPUT_ROOT}/run${i}"
  project_name="${PROJECT_PREFIX}-run${i}"
  mkdir -p "$run_dir"

    # 1. Start the surrogate first
  RUN_OUTPUT_DIR="$run_dir" \
    CONFIG_URL="$CONFIG_INPUT" \
    CASE_STUDY_SUBDIR="$CASE_STUDY_SUBDIR" \
    NOTIFY_EMAIL="$NOTIFY_EMAIL" \
    docker compose "${COMPOSE_FILES[@]}" -p "$project_name" up -d easier-surrogate

  # 2. Then start the job (depends_on will enforce the health check before connecting)
  RUN_OUTPUT_DIR="$run_dir" \
    CONFIG_URL="$CONFIG_INPUT" \
    CASE_STUDY_SUBDIR="$CASE_STUDY_SUBDIR" \
    NOTIFY_EMAIL="$NOTIFY_EMAIL" \
    docker compose "${COMPOSE_FILES[@]}" -p "$project_name" --profile jobs up -d easier-uml-job


  #RUN_OUTPUT_DIR="$run_dir" \
  #  CONFIG_URL="$CONFIG_INPUT" \
  #  CASE_STUDY_SUBDIR="$CASE_STUDY_SUBDIR" \
  #  NOTIFY_EMAIL="$NOTIFY_EMAIL" \
  #  docker compose "${COMPOSE_FILES[@]}" -p "$project_name" --profile jobs up -d easier-surrogate easier-uml-job

  uml_cid=$(docker compose "${COMPOSE_FILES[@]}" -p "$project_name" ps -q easier-uml-job)

  printf '%s %s\n' "$project_name" "$run_dir" >> "$PROJECTS_FILE"
  echo "run${i}: project=${project_name} uml=${uml_cid} -> ${run_dir}"
done

echo "All jobs submitted."
echo "Per-run project list saved to: ${PROJECTS_FILE}"
echo "Inspect one run with: docker compose -f ${COMPOSE_FILE} -p ${PROJECT_PREFIX}-run1 ps"
echo "Tail surrogate logs for one run with: docker compose -f ${COMPOSE_FILE} -p ${PROJECT_PREFIX}-run1 logs -f easier-surrogate"
echo "Stop all run pairs with: while read -r project _; do docker compose -f ${COMPOSE_FILE} -p \"\$project\" down; done < ${PROJECTS_FILE}"
