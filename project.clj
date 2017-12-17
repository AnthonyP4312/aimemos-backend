(defproject aimemos "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.postgresql/postgresql "42.1.4"]
                 [com.layerware/hugsql "0.4.8"]
                 [buddy/buddy-auth "2.1.0"]
                 [buddy/buddy-hashers "1.3.0"]
                 [compojure "1.6.0"]
                 [cheshire "5.8.0"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-jetty-adapter "1.5.1"]]
  :plugins [[lein-ring "0.9.7"]
            [cider/cider-nrepl "0.16.0-SNAPSHOT"]]
  :ring {:handler aimemos.core/app}
  :main aimemos.core
  :aot [:all]
  :profiles
    {:dev {:dependencies [[ring/ring-jetty-adapter "1.5.1"]
                          [ring/ring-mock "0.3.0"]]}})

               
