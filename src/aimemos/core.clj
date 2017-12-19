(ns aimemos.core
  (:require [hugsql.core :refer [def-db-fns]]
            [aimemos.db :refer [db]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.util.response :as r]
            [cheshire.core :as json]
            [org.httpkit.server :refer :all]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [buddy.auth :refer [authenticated?]]
            [buddy.auth.middleware :refer [wrap-authorization
                                           wrap-authentication]]
            [buddy.auth.backends :refer [basic token]]
            [buddy.hashers :refer [derive check] :rename {derive bcrypt}])
  (:gen-class))


(def-db-fns "sql/queries.sql")

(defonce current-users (atom {}))

(defn send-message [params]
  (println "hey were sending a message!")
  (println params)
  (println @current-users))

(defn socket-handler
  "Receives a JSON string and performs various operations based on the contents"
  ;; :method - message or api call
  ;; :params - map of relevant parameters to method
  [user, m]
  (println "oh shit socket action going on")
  (println m)
  (println (json/decode m true))
  (let [{:keys [method params]} (json/decode m true)]
    (case method
      "send-message" (send-message (assoc params :from user))
      "add-buddy" (add-buddy db (assoc params :username user))
      "delete-buddy" (delete-buddy db (assoc params :username user))
      "update-status" (update-status db (assoc params :username user))
      "update-groupname" (update-groupname db (assoc params :username user))
      "buddies-by-user" (buddies-by-user db {:username user}))))


(defn disconnect!
  [user status]
  (println (str "shits dead my dude: " user status)))

(defn my-authfn [req auth]
  (when (check (:password auth) 
               (:password (password-by-user db {:username (:username auth)})))
    (:username auth)))

;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Route Handlers

(defn chat-handler [req]

  (println req)
  (println "WEBSOCKET REQUEST =====")
  (if (authenticated? req)
    (swap! current-users
           assoc
           (keyword (:identity req))
           (with-channel req channel
             (on-close channel (partial disconnect! (:identity req)))
             (on-receive channel (partial socket-handler (:identity req)))))
    {:status 401}))

(defn home-page [] )
(defn sign-up-page [])
(defn create-account [user pass]
  (println user)
  (println pass)
  (add-user db {:username user
                :password (bcrypt pass)
                :status "OFFLINE"}))


(defn buddy-list [req]
  "Returns a map of buddies by user"
  (println (str req))
  (let [buddies (buddies-by-user db {:username req})]
    (println buddies "\n\n")

    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/encode buddies)}))
  


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Route Definitions




(defroutes webpage
  (GET  "/aim" req (home-page))
  (GET  "/aim/signup" req (sign-up-page))
  (POST "/aim/signup" [user pass] (create-account user pass)))

(defroutes user-login
  (GET  "/ws" req (chat-handler req)))

(defroutes user-api
  (POST "/aim/add-buddy" req nil)
  (POST "/aim/delete-buddy" req nil)
  (GET "/aim/buddies-by-user/:user" [user] (buddy-list user))
  (POST "/aim/update-status" req nil)
  (POST "/aim/update-groupname" req nil))

(def app 
  (routes
   user-api
   webpage
   (-> user-login
       (wrap-authentication (basic {:authfn my-authfn}))
       (wrap-authorization (basic {:authfn my-authfn})))
;   (wrap-authentication user-api token)
   ))


(defn -main
  [& args]
  (run-server (wrap-reload app) {:port 3000}))
