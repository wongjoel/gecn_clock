(ns electron.core)

(def electron       (js/require "electron"))
(def app            (.-app electron))
(def browser-window (.-BrowserWindow electron))
(def crash-reporter (.-crashReporter electron))
(def ipcMain        (.-ipcMain electron))

(def clock-window (atom nil))
(def control-window (atom nil))

(defn init-clock-window
  []
  (reset! clock-window (browser-window.
                        (clj->js {:width 800
                                  :height 600
                                  :webPreferences {:nodeIntegration true}})))
  ; Path is relative to the compiled js file (main.js in our case)
  (.loadURL ^js/electron.BrowserWindow @clock-window (str "file://" js/__dirname "/public/clock.html"))
  (.on ^js/electron.BrowserWindow @clock-window "closed" #(reset! clock-window nil)))

(defn init-control-window
  []
  (reset! control-window (browser-window.
                          (clj->js {:width 800
                                    :height 600
                                    :webPreferences {:nodeIntegration true}})))
  ; Path is relative to the compiled js file (main.js in our case)
  (.loadURL ^js/electron.BrowserWindow @control-window (str "file://" js/__dirname "/public/control.html"))
  (.on ^js/electron.BrowserWindow @control-window "closed" #(reset! control-window nil)))

(defn init-browser []
  (init-clock-window)
  ;(init-control-window)
)

; CrashReporter can just be omitted
(.start crash-reporter
        (clj->js
          {:companyName "No Company"
           :productName "GECN Clock"
           :submitURL "https://example.com/submit-url"
           :autoSubmit false}))

(.on ipcMain "async-message"
     (fn [event arg]
       (.send (.-sender event) "async-reply" "pong")))

(.on app "window-all-closed"
     #(when-not (= js/process.platform "darwin")
        (.quit app)))

(.on app "ready"
     init-browser)
