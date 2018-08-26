(ns aimemos.routes
  (:require [buddy.auth.middleware :refer [wrap-authorization
                                           wrap-authentication]]
            [buddy.auth.backends :refer [basic token]]
            [buddy.hashers :refer [derive check] :rename {derive bcrypt}]
            [compojure.core :refer :all]
            [aimemos.db :refer :all]
            [aimemos.handlers :refer :all]))

(defn my-authfn [req auth]
  (println auth)
  (when (check (:password auth)
               (:password (password-by-user db {:username (:username auth)})))
    (:username auth)))

(defroutes webpage
  (GET  "/aim" req nil)
  (GET  "/aim/signup" req nil)
  (POST "/aim/signup" [user pass] (create-account user pass)))

(defroutes user-login
  (GET  "/ws" req (chat-handler req)))

(defroutes user-api
  (GET "/aim/buddies-by-user/:user" [user] (buddy-list user)))

(def app
  (routes
   user-api
   webpage
   (-> user-login
       (wrap-authentication (basic {:authfn my-authfn}))
       (wrap-authorization (basic {:authfn my-authfn})))))
