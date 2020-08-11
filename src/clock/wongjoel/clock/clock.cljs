(ns wongjoel.clock.clock
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [clojure.string :as str]
            [cljs.pprint :as pprint]
            [tick.alpha.api :as t]
            [tick.locale-en-us]))


(defonce timer (r/atom (t/time)))
(defonce time-updater (js/setInterval #(reset! timer (t/time)) 1000))

(defonce countdown-end (r/atom (t/+ (t/instant) (t/new-duration 15 :minutes))))
(defonce countdown-display (r/atom true))
(defonce countdown-enable (r/atom false))

(defonce proc (js/require "child_process"))
(defonce ipcRenderer (.-ipcRenderer (js/require "electron")))

(defonce ping-pong (r/atom "ping"))
(.on ipcRenderer "async-reply" (fn [event arg] (do
                                                 (reset! ping-pong "pong")
                                                 (println "Recieved Message"))))

(defn set-countdown-end
  [minutes]
  (reset! countdown-end (r/atom (t/+ (t/instant) (t/new-duration minutes :minutes)))))

(defn clock
  [timer]
  (let [time-str (t/format (tick.format/formatter "hh:mm:ss a") @timer)]
    [:section
     [:div.clock-label "Time:"]
     [:div.clock time-str]]))

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
  [timer end-time display enable]
  (if @display
    [:section
     [:div.clock-label "Countdown Timer:"]
     [:div.clock
      (if @enable
        (countdown-mm-ss
         (t/instant (-> @timer (t/on (t/date))  (t/in (t/zone))))
         @end-time)
        "-")]]
    [:section.hidden ""]))

(defn root-component []
  [:div
   [clock timer]
   [countdown-timer timer countdown-end countdown-display countdown-enable]])

(defn start []
  (rdom/render [root-component]
            (js/document.getElementById "app")))

(start)
