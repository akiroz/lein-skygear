#!/bin/bash
set -e

echo "Creating test data..."
export X=$(date +%s)
echo "my_string = '$X'" > "test/code/my_file.py"
mkdir "test/static"
echo "$X" > "test/static/$X.txt"
echo "Test string: $X"


if $TRAVIS; then
  git config --global user.email "travis.ci@leinskygeartest.skygeario.com"
  git config --global user.name "Travis CI"
fi
lein skygear-deploy test


echo "Waiting 2 minutes..."
sleep 120

echo -n "Testing cloud code... "
[[ $(curl -s "leinskygeartest.skygeario.com/test-string") = $X ]] && echo "OK!" || exit 1

echo -n "Testing static asset... "
[[ $(curl -s "leinskygeartest.skygeario.com/static/$X.txt") = $X ]] && echo "OK!" || exit 1
