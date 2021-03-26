#!/bin/bash

find . -type f -name "*.class" -exec rm {} \;

find . -type f -name "*.zip" -exec rm {} \;
