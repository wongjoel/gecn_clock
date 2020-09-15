(ns wongjoel.control.control
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [clojure.string :as str]
            [cljs.pprint :as pprint]
            ))

(defonce ipcRenderer (.-ipcRenderer (js/require "electron")))

(defonce countdown-enable (r/atom true))
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
                  (reset! minutes (.-value (.-target e)))
                  (.send ipcRenderer "control-countdown-minutes" @minutes))
     :min -1
     :max 1000
     :value @minutes}]])

(defonce countdown-repeat (r/atom true))
(defn countdown-repeat-form
  [repeat]
  [:form.control {:on-submit (fn [^js/Event e]
                       (.preventDefault e)
                       (println "submitted"))}
   [:label {:for "repeat"} "Countdown repeats: "]
   [:input#repeat.checkbox
    {:type :checkbox
     :on-change (fn [^js/Event e]
                  (println "repeat changed")
                  (swap! repeat not))
     :checked @repeat}]])

(defonce alarm-sound (r/atom "alarms/Timer.ogg"))
(defonce alarm-enable (r/atom false))
(defonce alarm-repeats (r/atom 4))
(defonce alarm-count (r/atom 0))
(.on ipcRenderer "control-countdown-finished"
     (fn [event arg]
       (println (str "Control received " arg))
       (case arg
         "finished" (do
                      (reset! alarm-count 0)
                      (reset! alarm-enable true)
                      (if @countdown-repeat
                        (.send ipcRenderer "control-countdown-enable" "start")
                        (reset! countdown-enable false)))
         :default (js/alert "Error handling control-countdown-finished")
         )))

(.on ipcRenderer "control-open-file-result"
     (fn [event arg]
       (println (str "Control received " arg))
       (reset! alarm-sound arg)))

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
             :src @sound
             :autoPlay true}] ;can't work out how else to send the play event
    [:div ""]))

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
  [repeats]
  [:form.control {:on-submit (fn [^js/Event e]
                       (.preventDefault e)
                       (println "submitted"))}
   [:label {:for "repeats"} "Alarm duration: "]
   [:input#repeats.number-input
    {:type :number
     :on-change (fn [^js/Event e]
                  (println "changed")
                  (reset! repeats (.-value (.-target e))))
     :min 0
     :max 1000
     :value @repeats}]])

(defn alarm-display-sound
  [sound]
  [:details.control
   [:summary "Selected sound"]
   [:pre @sound]])

(defn alarm-select-sound
  []
  [:button.control.button
   {:on-click (fn [] (.send ipcRenderer "control-open-file-request" ""))}
   "Select Sound"])

(defn root-component []
  [:div
   [toggle-countdown-enable-button countdown-enable]
   [countdown-minutes-form countdown-minutes]
   [countdown-repeat-form countdown-repeat]
   [alarm-sound-element alarm-sound alarm-enable alarm-repeats alarm-count]
   [alarm-test-button alarm-enable]
   [alarm-minutes-form alarm-repeats]
   [alarm-select-sound]
   [alarm-display-sound alarm-sound]
   ])

(defn start []
  (rdom/render [root-component]
            (js/document.getElementById "app")))

(start)
