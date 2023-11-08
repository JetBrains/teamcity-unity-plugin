#!/bin/bash

set -e

version=$(./gradlew getLatestChangelogVersion -q)
git tag -a "v$version" -m "release v$version"
git push origin "v$version"