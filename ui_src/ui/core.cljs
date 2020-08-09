(ns ui.core
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [cljs.pprint :as pprint]
            [tick.alpha.api :as t]
            [tick.locale-en-us]))

(def join-lines (partial str/join "\n"))

(enable-console-print!)

(defonce state        (r/atom 0))
(defonce shell-result (r/atom ""))
(defonce command      (r/atom ""))

(defonce timer (r/atom (t/time)))
(defonce time-updater (js/setInterval
                       #(reset! timer (t/time)) 1000))

(defonce countdown-end (r/atom (t/+ (t/instant) (t/new-duration 15 :minutes))))
(defonce enable-countdown (r/atom true))

(defonce proc (js/require "child_process"))
(defonce ipcRenderer (.-ipcRenderer (js/require "electron")))
(defonce ping-pong (r/atom "ping"))

(.on ipcRenderer "async-reply" (fn [event arg] (reset! ping-pong "pong")))

(defn append-to-out [out]
  (swap! shell-result str out))

(defn run-process []
  (when-not (empty? @command)
    (println "Running command" @command)
    (let [[cmd & args] (str/split @command #"\s")
          js-args (clj->js (or args []))
          p (.spawn proc cmd js-args)]
      (.on p "error" (comp append-to-out
                           #(str % "\n")))
      (.on (.-stderr p) "data" append-to-out)
      (.on (.-stdout p) "data" append-to-out))
    (reset! command "")))

(defn clock [timer]
  (let [time-str (t/format (tick.format/formatter "hh:mm:ss a") @timer)]
    [:div.clock
     time-str]))

(defn countdown-mm-ss
  [now end-time]
  (let [duration (t/duration
                  {:tick/beginning now
                   :tick/end end-time})
        minutes (t/minutes duration)
        seconds (t/seconds (t/- duration (t/new-duration minutes :minutes)))]
    (if (t/< (t/instant) end-time)
      (pprint/cl-format nil "~2,'0d:~2,'0d" minutes seconds)
      "Time's up!")))

(defn countdown-timer
  [timer end-time enable-countdown]
  [:div.clock (if @enable-countdown
                (countdown-mm-ss
                 (t/instant (-> @timer (t/on (t/date))  (t/in (t/zone))))
                 @end-time)
                "Stopped")])

;(countdown-mm-ss (t/instant) (t/+ (t/instant) (t/new-duration 71 :minutes)))

(defn root-component []
  [:div
   [:div.logos
    [:img.electron {:src "img/electron-logo.png"}]
    [:img.cljs {:src "img/cljs-logo.svg"}]
    [:img.reagent {:src "img/reagent-logo.png"}]]
   [:pre "Versions:"
    [:p (str "Node     " js/process.version)]
    [:p (str "Electron " ((js->clj js/process.versions) "electron"))]
    [:p (str "Chromium " ((js->clj js/process.versions) "chrome"))]]
   [:p.clock (.toLocaleTimeString (js/Date.) "en-AU")]
   [:p.clock "Hello world! 2"]
   [:p.clock (t/format (tick.format/formatter "hh:mm:ss a") (t/time))]
   [clock timer]
   [countdown-timer timer countdown-end enable-countdown]
   [:button
    {:on-click #(reset! countdown-end (t/+ (t/instant) (t/new-duration 15 :minutes)))}
    "Reset countdown timer"]
   [:button
    {:on-click #(reset! enable-countdown false)}
    "Stop countdown timer"]
   [:button
    {:on-click #(reset! enable-countdown true)}
    "Start countdown timer"]
   [:button
    {:on-click #(.send ipcRenderer "async-message" "ping")}
    "Ping"]
   [:p
    [:form
     {:on-submit (fn [^js/Event e]
                   (.preventDefault e)
                   (run-process))}
     [:input#command
      {:type :text
       :on-change (fn [^js/Event e]
                    (reset! command
                            ^js/String (.-value (.-target e))))
       :value @command
       :placeholder "type in shell command"}]]]
   [:pre (join-lines (take 100 (reverse (str/split-lines @shell-result))))]])

(r/render
  [root-component]
  (js/document.getElementById "app-container"))
