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
                  (reset! minutes (.-value (.-target e)))
                  (.send ipcRenderer "control-countdown-minutes" @minutes))
     :min -1
     :max 1000
     :value @minutes}]])

(defonce alarm-sound (r/atom "alarms/Timer.ogg"))
(defonce alarm-enable (r/atom false))
(defonce alarm-repeats (r/atom 5))
(defonce alarm-count (r/atom 0))
(.on ipcRenderer "control-countdown-finished"
     (fn [event arg]
       (println (str "Control received " arg))
       (case arg
         "finished" (do (reset! alarm-count 0) (reset! alarm-enable true) (reset! countdown-enable false))
         :default (js/alert "Error handling control-countdown-finished")
         )))

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

(defn root-component []
  [:div
   [toggle-countdown-enable-button countdown-enable]
   [countdown-minutes-form countdown-minutes]
   [alarm-sound-element alarm-sound alarm-enable alarm-repeats alarm-count]
   [alarm-test-button alarm-enable]
   [alarm-minutes-form alarm-repeats]
   ])

(defn start []
  (rdom/render [root-component]
            (js/document.getElementById "app")))

(start)
