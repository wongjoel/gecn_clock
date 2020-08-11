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
  (let [clock (create-clock-window config/clock-html)
        control (create-control-window config/control-html)]
    (reset! clock-window clock)
    (reset! control-window control)
    (.on clock "closed"
         (fn [] (do
                  (println "Clock Window closed")
                  (reset! clock-window nil)
                  (when @control-window (.close @control-window)))))
    (.on control "closed"
         (fn [] (do
                  (println "Control Window closed")
                  (reset! control-window nil)
                  (when @clock-window (.close @clock-window)))))))


(.on ipcMain "control-countdown-enable"
     (fn [event arg]
       (println (str "Main received " arg))
       (.send
        (.-webContents @clock-window)
        "clock-countdown-enable" arg)))

(.on app "window-all-closed"
     #(when-not (= js/process.platform "darwin")
        (.quit app)))

(.on app "ready" start)
