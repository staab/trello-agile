#!/bin/bash

lein run $1

while inotifywait -e close_write -r src; do lein run $1; done