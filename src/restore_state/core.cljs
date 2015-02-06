(ns restore-state.core
  (:require [cljsjs.react]
            [figwheel.client :as figwheel]
            [reagent.core    :as reagent]))

(defonce counter   (atom 0))
(defonce undo-redo (atom [[[]] '()]))
(defonce state     (reagent/atom []))

(defn add
  [colour]
  (swap! state conj [colour @counter])
  (swap! counter inc))

(defn kill
  [n]
  (swap! state #(vec (remove (fn [[_ nn]] (= nn n)) %))))

(defn record
  [f]
  (fn []
    (swap! undo-redo (fn [[u _]] [(conj u @state) '()]))
    (f)))

(defn undo
  []
  (let [[u r] @undo-redo
        s     @state]
    (when (seq u)
      (reset! state (last u))
      (reset! undo-redo [(pop u) (cons s r)]))))

(defn redo
  []
  (let [[u r] @undo-redo
        s     @state]
    (when (seq r)
      (reset! state (first r))
      (reset! undo-redo [(conj u s) (rest r)]))))

(defn boxes
  []
  (for [[c n] @state]
    [:div {:class (str "box " c) :key n :on-click (record #(kill n))} (str n)]))

(defn app
  []
  (apply
    vector
    :span
    [:button {:on-click (record #(add "red"))} "Add Red"]
    [:button {:on-click (record #(add "green"))} "Add Green"]
    [:button {:on-click undo} "Undo"]
    [:button {:on-click redo} "Redo"]
    (boxes)))

(defn start
  []
  (figwheel/start)
  (reagent/render-component [app] (.getElementById js/document "app")))
