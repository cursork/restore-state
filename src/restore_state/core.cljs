(ns restore-state.core
  (:require [cljs.reader     :refer [read-string]]
            [cljsjs.react]
            [figwheel.client :as figwheel]
            [reagent.core    :as reagent]))

(enable-console-print!)

(defonce counter   (atom 0))
(defonce undo-redo (atom [[[]] '()]))
(defonce state     (reagent/atom []))

;; State Updating ;;

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

;; Dumping and Restoring ;;
(defn dump-to-file
  []
  (let [to-dump (pr-str {:counter   @counter
                         :undo-redo @undo-redo
                         :state     @state})
        url     (->> (js/Blob. #js [to-dump] #js {"type" "application/edn"})
                     (.createObjectURL js/URL))
        a       (.createElement js/document "a")]
    (set! (.-href a) url)
    (set! (.-download a) "state.edn")
    (.appendChild (.-body js/document) a)
    (.click a)
    (.remove a)))

(defn restore-state
  [text]
  (when-let [to-restore (read-string text)]
    (reset! counter   (:counter to-restore))
    (reset! undo-redo (:undo-redo to-restore))
    (reset! state     (:state to-restore))))

(defn restore-from-file
  [input file]
  (let [fr (js/FileReader.)]
    (set! (.-onload fr)
          #(restore-state (.-result fr)))
    (.readAsText fr file)
    (set! (.-value input) nil)))

;; Reagent ;;

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
