(ns wongjoel.electron.electron
  (:require [clock.config :as config]))

(def Electron (js/require "electron"))
(def BrowserWindow (.-BrowserWindow Electron))
(def app (.-app Electron))
(def ipcMain (.-ipcMain Electron))
(def dialog (.-dialog Electron))
(defonce clock-window (atom nil))
(defonce control-window (atom nil))

(defn create-clock-window [index]
  (let [win (BrowserWindow. (clj->js {:x 10 :y 10 :webPreferences {:nodeIntegration true}
                                      :show false}))]
    (set! (. win -autoHideMenuBar) true)
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

(.on ipcMain "control-countdown-minutes"
     (fn [event arg]
       (println (str "Main received " arg))
       (.send
        (.-webContents @clock-window)
        "clock-countdown-minutes" arg)))

(.on ipcMain "clock-countdown-finished"
     (fn [event arg]
       (println (str "Main received " arg))
       (.send
        (.-webContents @control-window)
        "control-countdown-finished" arg)))

(.on ipcMain "control-open-file-request"
     (fn [event arg]
       (println (str "Main received " arg))
       (-> dialog
           (.showOpenDialog
            @control-window
            (clj->js {:properties ["openFile"]}))
           (.then (fn [result]
                    (when-not (.-canceled result)
                      (.send
                       (.-webContents @control-window)
                       "control-open-file-result" (.-filePaths result)))))
           (.catch (fn [err] (println (str "Error " err)))))))

(.on app "window-all-closed"
     #(when-not (= js/process.platform "darwin")
        (.quit app)))

(.on app "ready" start)
