(ns wongjoel.control.control
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [clojure.string :as str]
            [cljs.pprint :as pprint]
            ))

(def join-lines (partial str/join "\n"))

(defonce shell-result (r/atom ""))
(defonce command      (r/atom ""))

(defonce enable-countdown (r/atom true))

(defonce proc (js/require "child_process"))
(defonce ipcRenderer (.-ipcRenderer (js/require "electron")))

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

(defn root-component []
  [:div
   [:button
    {:on-click #(println "reset")}
    "Reset countdown timer"]
   [:br]
   [:button
    {:on-click #(reset! enable-countdown false)}
    "Stop countdown timer"]
   [:br]
   [:button
    {:on-click #(reset! enable-countdown true)}
    "Start countdown timer"]
   [:br]
   [:button
    {:on-click #(do
                  (.send ipcRenderer "async-message" "ping")
                  (println "sent ping")
                  )}
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

(defn start []
  (rdom/render [root-component]
            (js/document.getElementById "app")))

(start)
