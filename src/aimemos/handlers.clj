(ns aimemos.handlers
  (:require [aimemos.db :refer :all]
            [aimemos.user-api :as api]
            [cheshire.core :as json]
            [buddy.auth :refer [authenticated?]]
            [org.httpkit.server :refer :all]
            [buddy.hashers :refer [derive check] :rename {derive bcrypt}]))

(defn socket-handler
  "Receives a JSON string and performs various operations based on the
  contents"
  ;; :method - message or api call
  ;; :params - map of relevant parameters to method
  [user, m]
  (println "oh shit socket action going on")
  (println m)
  (println (json/decode m true))
  (let [{:keys [method params]} (json/decode m true)]
    (case method
      "send-message" (api/send-message (assoc params :from user))
      "add-buddy" (api/add-buddy (assoc params :username user))
      "delete-buddy" (api/delete-buddy (assoc params :username user))
      "update-status" (api/update-status (assoc params :username user))
      "update-groupname" (api/update-groupname (assoc params :username user))
      "buddies-by-user" (api/buddies-by-user {:username user}))))


(defn disconnect!
  [user status]
  (println (str "shits dead my dude: " user status))
  (swap! current-users dissoc (keyword user))
  (api/update-status {:username user :status "OFFLINE"})
  (println "New CUrrent Users: " @current-users))

(defn chat-handler
  [req]
  (println req)
  (println "WEBSOCKET REQUEST =====")
  (if (authenticated? req)
    (let [res (with-channel req channel
                (on-close channel (partial disconnect! (:identity req))) 
                (on-receive channel (partial socket-handler (:identity req))))]
      (println "oh")
      (api/update-status {:username (:identity req) :status "ONLINE"})
      (swap! current-users assoc (keyword (:identity req)) (:body res))
      res)
    {:status 401}))

(defn create-account 
  [user pass]
  (println user)
  (println pass)
  (add-user db {:username user
                :password (bcrypt pass)
                :status "OFFLINE"}))

(defn buddy-list 
  "Returns a map of buddies by user"
  [req]
  (println (str req))
  (let [buddies (buddies-by-user db {:username req})]
    (println buddies "\n\n")
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/encode buddies)}))
  

