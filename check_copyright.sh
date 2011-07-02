#!/bin/bash

echo 'Java files with no copyright notice:' >&2

find -name '*.java' | {
while read f
do
  head "$f" -n 1 >/tmp/f
  if echo '/*  Cupido - An online Hearts game.' | cmp -s - /tmp/f
  then
    :
  else
    echo "$f"
  fi
done
}
