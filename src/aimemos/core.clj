(ns aimemos.core
  (:require [aimemos.routes :refer [app]]
            [org.httpkit.server :refer [run-server]])
  (:gen-class))

(defn -main
  [& args]
  (run-server app {:port 3000}))
