#!/bin/bash



pushd $(dirname $0)
  #compile all java files
  find . -name "*.java" \
    | xargs javac -g

  #pack source and java files into an executable jar file
  jar -cvfe bisUtils.jar tools.bis.bisUtils  $(find . -type f -name "*.java" -o -name "*.class")

  #clean up and remove class files
  find . -name "*.class" \
    | xargs rm
popd

