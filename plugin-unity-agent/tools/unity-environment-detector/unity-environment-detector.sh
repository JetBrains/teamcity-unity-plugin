#!/bin/sh

# This script tries to determine Unity executable path and Unity version.
#
# The following sources are checked:
# 1) UNITY_ROOT_PARAMETER environment variable (can be set from agent's process)
# 2) UNITY_HOME environment variable
# 3) UNITY_HINT_PATH environment variable
# 4) PATH environment variable
# 5) /opt directory (for Linux)
# 6) /Applications directory (for MacOS)
#
# The output can consist of several lines.
# Each line represents either an info related to detected Unity, or an error:
# 1) path=path/to/Unity;version=unity version
# 2) error="error description"

# $1 - Unity path; $2 - Unity version
echo_results() {
  if [ -n "$1" ] && [ -n "$2" ]; then
    echo "path=$1;version=$2"
  elif [ -n "$1" ] && [ -z "$2" ]; then
    echo "error=Failed to determine Unity version for path $1" >&2
  fi
}

# $1 - Unity path hint (prefix, or even the complete path to the executable)
find_unity_path() {
  UNITY_PATH=$(find "$1" -type f -iname unity 2>/dev/null)
  echo "$UNITY_PATH"
}

# $1 - Unity path
find_unity_version() {
  UNITY_VERSION=$("$1" -version 2>/dev/null)
  echo "$UNITY_VERSION"
}

# $1 - Environment variable containing unity path hint
find_in_environment_variable() {
  [ -n "$1" ] && echo "$1" | tr ':' '\n' | while read -r UNITY_PATH_HINT; do
    UNITY_PATH=$(find_unity_path "$UNITY_PATH_HINT")
    UNITY_VERSION=$(find_unity_version "$UNITY_PATH")
    echo_results "$UNITY_PATH" "$UNITY_VERSION"
  done
}

# Since PATH can contain a lot of values,
# only values ending with 'Editor' or containing 'Unity' are considered
find_in_path_environment_variable() {
  [ -n "${PATH}" ] && echo "$PATH" | tr ':' '\n' | grep -e 'Editor$' -e 'Unity' | while read -r UNITY_PATH_HINT; do
    UNITY_PATH=$(find_unity_path "$UNITY_PATH_HINT")
    UNITY_VERSION=$(find_unity_version "$UNITY_PATH")
    echo_results "$UNITY_PATH" "$UNITY_VERSION"
  done
}

# $1 - Directory
find_in_directory() {
  if [ -d "$1" ]; then
    find_unity_path "$1" | while read -r UNITY_PATH; do
      UNITY_VERSION=$(find_unity_version "$UNITY_PATH")
      echo_results "$UNITY_PATH" "$UNITY_VERSION"
    done
  fi
}

if [ -n "${UNITY_ROOT_PARAMETER}" ]; then
  find_in_environment_variable "$UNITY_ROOT_PARAMETER"
else
  find_in_environment_variable "$UNITY_HOME"
  find_in_environment_variable "$UNITY_HINT_PATH"
  find_in_path_environment_variable
  find_in_directory "/opt"          # for Linux
  find_in_directory "/Applications" # for MacOS
fi
