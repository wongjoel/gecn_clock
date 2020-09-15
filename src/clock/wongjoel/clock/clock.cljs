(ns wongjoel.clock.clock
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [clojure.string :as str]
            [cljs.pprint :as pprint]
            [tick.alpha.api :as t]
            [tick.locale-en-us]))

(defonce ipcRenderer (.-ipcRenderer (js/require "electron")))

(defonce timer (r/atom (t/time)))
(defonce time-updater (js/setInterval #(reset! timer (t/time)) 1000))

(defonce countdown-minutes (r/atom 15))
(defonce countdown-end (r/atom (t/+ (t/instant) (t/new-duration @countdown-minutes :minutes))))
(defonce countdown-display (r/atom true))

(defonce countdown-enable (r/atom true))

(defn set-countdown-end
  [minutes]
  (reset! countdown-end (t/+ (t/instant) (t/new-duration @minutes :minutes))))

(defn reset-countdown
  [minutes enable]
  (set-countdown-end minutes)
  (reset! enable true))

(.on ipcRenderer "clock-countdown-enable"
     (fn [event arg]
       (println (str "Clock received " arg))
       (case arg
         "stop" (reset! countdown-enable false)
         "start" (reset-countdown countdown-minutes countdown-enable)
         :default (js/alert "Error handling clock-countdown-enable")
         )))

(.on ipcRenderer "clock-countdown-minutes"
     (fn [event arg]
       (println (str "Clock received " arg))
       (reset! countdown-minutes arg)))

(defn clock
  [timer]
  (let [time-str (t/format (tick.format/formatter "hh:mm a") @timer)]
    [:section
     [:div.clock-label "Time:"]
     [:div.clock time-str]]))

(defn countdown-mm-ss
  [now end-time enable]
  (let [duration (t/duration
                  {:tick/beginning now
                   :tick/end end-time})
        minutes (t/minutes duration)
        seconds (t/seconds (t/- duration (t/new-duration minutes :minutes)))]
    (if (t/< (t/instant) end-time)
      (pprint/cl-format nil "~2,'0d:~2,'0d" minutes seconds)
      (do
        (.send ipcRenderer "clock-countdown-finished" "finished")
        (reset! enable false)
        "Time's up!"))))

(defn countdown-timer
  [timer end-time display enable]
  (if @display
    [:section
     [:div.clock-label "Countdown Timer:"]
     [:div.clock
      (if @enable
        (countdown-mm-ss
         (t/instant (-> @timer (t/on (t/date))  (t/in (t/zone))))
         @end-time
         enable)
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
