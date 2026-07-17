#!/bin/bash
set -euo pipefail

usage() {
  cat <<EOF
Usage:
  ./run-compose-batch-shared-surrogate-fastapi.sh \
    --config-url=<url-or-local-path> \
    --case-study-subdir=<branch-or-folder> \
    --output-root=<host-output-root> \
    [--notify-email=<email>] \
    [--runs=31] \
    [--project-name=easier-fastapi-batch] \
    [--compose-file=docker-compose.yml] \
    [--surrogate-tag=fast-api] \
    [--surrogate-port=5000]

Example:
  ./run-compose-batch-shared-surrogate-fastapi.sh \
    --config-url=https://raw.githubusercontent.com/danieledipompeo/easier-experiment-data/main/ecsa26/config.ini \
    --case-study-subdir=modelling-energy \
    --output-root=/mnt/data/easier/fastapi-batch \
    --runs=31 \
    --surrogate-tag=fast-api
EOF
}

CONFIG_URL=""
CASE_STUDY_SUBDIR=""
OUTPUT_ROOT=""
NOTIFY_EMAIL=""
RUNS="31"
PROJECT_NAME="easier-fastapi-batch"
COMPOSE_FILE="docker-compose.yml"
SURROGATE_TAG="fast-api"
SURROGATE_PORT="5000"

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
    --project-name=*)
      PROJECT_NAME="${arg#*=}"
      ;;
    --compose-file=*)
      COMPOSE_FILE="${arg#*=}"
      ;;
    --surrogate-tag=*)
      SURROGATE_TAG="${arg#*=}"
      ;;
    --surrogate-port=*)
      SURROGATE_PORT="${arg#*=}"
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

if ! [[ "$RUNS" =~ ^[0-9]+$ ]] || [[ "$RUNS" -lt 1 ]]; then
  echo "--runs must be a positive integer." >&2
  exit 1
fi

if ! [[ "$SURROGATE_PORT" =~ ^[0-9]+$ ]] || [[ "$SURROGATE_PORT" -lt 1 || "$SURROGATE_PORT" -gt 65535 ]]; then
  echo "--surrogate-port must be an integer in range 1..65535." >&2
  exit 1
fi

PROJECT_NAME="$(printf '%s' "$PROJECT_NAME" | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9_-]+/-/g; s/^-+//; s/-+$//; s/-{2,}/-/g')"
if [[ -z "$PROJECT_NAME" ]]; then
  echo "--project-name must contain at least one letter or digit." >&2
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

COMPOSE_FILES=(-f "$COMPOSE_FILE")
if [[ -n "$CONFIG_OVERRIDE_FILE" ]]; then
  COMPOSE_FILES+=(-f "$CONFIG_OVERRIDE_FILE")
fi

mkdir -p "$OUTPUT_ROOT"
CONTAINERS_FILE="${OUTPUT_ROOT}/batch-containers.txt"
: > "$CONTAINERS_FILE"

echo "Building shared surrogate image easier-surrogate:${SURROGATE_TAG} ..."
EASIER_SURROGATE_VERSION="$SURROGATE_TAG" \
PORT="$SURROGATE_PORT" \
docker compose "${COMPOSE_FILES[@]}" -p "$PROJECT_NAME" build easier-surrogate

echo "Building easier-uml-job image locally (avoids registry pull attempts) ..."
EASIER_SURROGATE_VERSION="$SURROGATE_TAG" \
PORT="$SURROGATE_PORT" \
docker compose "${COMPOSE_FILES[@]}" -p "$PROJECT_NAME" --profile jobs build easier-uml-job

echo "Starting single shared surrogate container for project ${PROJECT_NAME} ..."
EASIER_SURROGATE_VERSION="$SURROGATE_TAG" \
PORT="$SURROGATE_PORT" \
docker compose "${COMPOSE_FILES[@]}" -p "$PROJECT_NAME" up -d easier-surrogate

surrogate_cid="$(docker compose "${COMPOSE_FILES[@]}" -p "$PROJECT_NAME" ps -q easier-surrogate)"
if [[ -z "$surrogate_cid" ]]; then
  echo "Unable to resolve easier-surrogate container ID after startup." >&2
  exit 1
fi

echo "Waiting for surrogate to become healthy ..."
for attempt in $(seq 1 60); do
  status="$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}' "$surrogate_cid" 2>/dev/null || true)"
  if [[ "$status" == "healthy" ]]; then
    echo "Shared surrogate is healthy."
    break
  fi
  if [[ "$attempt" -eq 60 ]]; then
    echo "Timed out waiting for easier-surrogate health check." >&2
    docker compose "${COMPOSE_FILES[@]}" -p "$PROJECT_NAME" logs easier-surrogate >&2 || true
    exit 1
  fi
  sleep 2
done

echo "Launching ${RUNS} easier-uml-job containers using one shared surrogate ..."
for i in $(seq 1 "$RUNS"); do
  run_dir="${OUTPUT_ROOT}/run${i}"
  mkdir -p "$run_dir"

  container_name="${PROJECT_NAME}-job-${i}"

  cid="$(
    RUN_OUTPUT_DIR="$run_dir" \
    CONFIG_URL="$CONFIG_INPUT" \
    CASE_STUDY_SUBDIR="$CASE_STUDY_SUBDIR" \
    NOTIFY_EMAIL="$NOTIFY_EMAIL" \
    EASIER_SURROGATE_VERSION="$SURROGATE_TAG" \
    PORT="$SURROGATE_PORT" \
    docker compose "${COMPOSE_FILES[@]}" -p "$PROJECT_NAME" --profile jobs \
      run -d --no-deps --name "$container_name" easier-uml-job
  )"

  printf '%s %s %s\n' "$container_name" "$cid" "$run_dir" >> "$CONTAINERS_FILE"
  echo "run${i}: name=${container_name} cid=${cid} -> ${run_dir}"
done

echo "All jobs submitted with one shared surrogate."
echo "Containers list: ${CONTAINERS_FILE}"
echo "Shared surrogate logs: docker compose -f ${COMPOSE_FILE} -p ${PROJECT_NAME} logs -f easier-surrogate"
echo "Stop all jobs: while read -r _ cid _; do docker rm -f \"\$cid\"; done < ${CONTAINERS_FILE}"
echo "Stop shared surrogate: docker compose -f ${COMPOSE_FILE} -p ${PROJECT_NAME} down"
