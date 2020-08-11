(ns wongjoel.control.control
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [clojure.string :as str]
            [cljs.pprint :as pprint]
            ))

(defonce ipcRenderer (.-ipcRenderer (js/require "electron")))

(defonce countdown-enable (r/atom false))
(defn toggle-countdown-enable-button
  [enable]
  (if @enable
    [:button.control.button
     {:on-click (fn []
                  (swap! enable not)
                  (.send ipcRenderer "control-countdown-enable" "stop"))}
     "Stop Countdown"]
    [:button.control.button
     {:on-click (fn []
                  (swap! enable not)
                  (.send ipcRenderer "control-countdown-enable" "start"))}
     "Start Countdown"]))

(defonce countdown-minutes (r/atom 15))
(defn countdown-minutes-form
  [minutes]
  [:form.control {:on-submit (fn [^js/Event e]
                       (.preventDefault e)
                       (println "submitted"))}
   [:label {:for "minutes"} "Countdown Minutes: "]
   [:input#minutes.number-input
    {:type :number
     :on-change (fn [^js/Event e]
                  (println "changed")
                  (reset! minutes (.-value (.-target e))))
     :size 3
     :value @minutes}]])

(defonce alarm-sound (r/atom "alarms/Timer.ogg"))
(defonce alarm-enable (r/atom false))
(defonce alarm-repeats (r/atom 5))
(defonce alarm-count (r/atom 0))
(defn alarm-sound-element
  [sound enable repeats count]
  (if @enable
    [:audio {:controls false
             :onPlay (fn [] (reset! enable true))
             :onPause (fn []
                        (reset! enable false)
                        (swap! count inc)
                        (if (<= @count @repeats)
                          (js/setTimeout #(reset! enable true) 10)
                          (reset! count 0)))
             :autoPlay true} ;can't work out how else to send the play event
     [:source {:src @sound :type "audio/ogg"}]]
    [:div ""]
    ))

(defn alarm-test-button
  [enable]
  (if @enable
    [:button.control.button
     {:on-click (fn [] (swap! enable not))}
     "Stop Alarm"]
    [:button.control.button
     {:on-click (fn [] (swap! enable not))}
     "Test Alarm"]))

(defn alarm-minutes-form
  [count repeats]
  [:form.control {:on-submit (fn [^js/Event e]
                       (.preventDefault e)
                       (println "submitted"))}
   [:label {:for "repeats"} (str "Repetition " @count " out of ")]
   [:input#repeats.number-input
    {:type :number
     :on-change (fn [^js/Event e]
                  (println "changed")
                  (reset! repeats (.-value (.-target e))))
     :size 3
     :value @repeats}]])

(defn root-component []
  [:div
   [toggle-countdown-enable-button countdown-enable]
   [countdown-minutes-form countdown-minutes]
   [alarm-sound-element alarm-sound alarm-enable alarm-repeats alarm-count]
   [alarm-test-button alarm-enable]
   [alarm-minutes-form alarm-count alarm-repeats]
   ])

(defn start []
  (rdom/render [root-component]
            (js/document.getElementById "app")))

(start)
