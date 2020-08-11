(ns clock.test.runner-visual
  (:require [cljs-test-display.core]
            [figwheel.main.testing :refer-macros [run-tests]]
            [clock.renderer.arithmetic-test]
            [clock.main.arithmetic-test]))

(run-tests (cljs-test-display.core/init!))
