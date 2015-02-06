(defproject restore-state "0.1.0-SNAPSHOT"
  :license {:name "MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2760"]
                 [cljsjs/react        "0.12.2-5"]
                 [figwheel            "0.2.2-SNAPSHOT"]
                 [reagent             "0.5.0-alpha"]]
  :plugins [[lein-cljsbuild "1.0.4"]
            [lein-figwheel  "0.2.2-SNAPSHOT"]]
  :profiles {:dev {:resource-paths ["target/dev"]}}
  :cljsbuild {:builds {:dev {:source-paths ["src"]
                             :compiler {:optimizations :none
                                        :output-dir    "target/dev/public/js"
                                        :output-to     "target/dev/public/js/main.js"
                                        :pretty-print  true}}}})

