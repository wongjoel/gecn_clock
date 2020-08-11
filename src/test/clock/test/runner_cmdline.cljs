(ns clock.test.runner-cmdline
  (:require [figwheel.main.testing :refer-macros [run-tests-async]]
            [clock.renderer.arithmetic-test]
            [clock.main.arithmetic-test]))

(defn -main [& args]
  (run-tests-async 10000))
