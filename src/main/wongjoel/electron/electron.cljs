(ns wongjoel.electron.electron
  (:require [clock.config :as config]))

(def Electron (js/require "electron"))
(def BrowserWindow (.-BrowserWindow Electron))
(def app (.-app Electron))

(defn create-clock-window [index]
  (let [win (BrowserWindow. (clj->js {:x 10 :y 10 :webPreferences {:nodeIntegration true}
                                      :show false}))]
    (doto win
      (.loadFile index)
      (.once "ready-to-show" #(.show win)))))

(defn create-control-window [index]
  (let [win (BrowserWindow. (clj->js {:x 100 :y 100 :webPreferences {:nodeIntegration true}
                                      :show false}))]
    (doto win
      (.loadFile index)
      (.once "ready-to-show" #(.show win)))))

(defn start []
  (create-clock-window config/index-html)
    (create-control-window config/test-html))

(.on app "window-all-closed"
     #(when-not (= js/process.platform "darwin")
        (.quit app)))

(.on app "ready" start)
