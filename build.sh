#!/bin/bash

clojure -A:electron
clojure -A:clock
clojure -A:control
npm run package
