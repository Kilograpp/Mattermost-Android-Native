#!/bin/bash
set -ev
if [ "${TRAVIS_BRANCH}" == "master" ]; then
  bundle exec fastlane release
else 
  bundle exec fastlane beta
fi 
