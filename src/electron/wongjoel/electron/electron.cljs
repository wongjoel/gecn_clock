(ns wongjoel.electron.electron
  (:require [clock.config :as config]))

(def Electron (js/require "electron"))
(def BrowserWindow (.-BrowserWindow Electron))
(def app (.-app Electron))
(def ipcMain (.-ipcMain Electron))
(defonce clock-window (atom nil))
(defonce control-window (atom nil))

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
  (reset! clock-window (create-clock-window config/index-html))
  (reset! control-window (create-control-window config/control-html)))

(.on ipcMain "async-message"
     (fn [event arg] (do
                       (println "message received")
                       (.send (.-webContents @clock-window) "async-reply" "pong"))))

(.on app "window-all-closed"
     #(when-not (= js/process.platform "darwin")
        (.quit app)))

(.on app "ready" start)
