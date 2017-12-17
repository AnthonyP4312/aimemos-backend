(ns aimemos.core
  (:require [hugsql.core :refer [def-db-fns]]
            [aimemos.db :refer [db]]
            [cheshire.core :as json]
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

(defn my-authfn [req auth]
  (when (check (:password auth) 
               (:password (password-by-user db {:username (:username auth)})))
    (swap! current-users 
           assoc
           (keyword (:username auth))
           (str (java.util.UUID/randomUUID)))
    (keyword (:username auth))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Route Handlers

(defn home-page [] )
(defn sign-up-page [])
(defn create-account [user pass]
  (println user)
  (println pass)
  (add-user db {:username user
                :password (bcrypt pass)
                :status "OFFLINE"}))


(defn login [req]
  "Login Handler, returns a token for future auth or 401"
  (let [user (str (:identity req))]
    (println  (authenticated? req))
    (println (str req))
    (println @current-users)
    (if (authenticated? req)
      (json/encode {:token ((:identity req) @current-users)
                    :buddies (buddies-by-user db {:username user})})
      {:status 401})))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Route Definitions


(defroutes webpage
  (GET  "/aim" req (home-page))
  (GET  "/aim/signup" req (sign-up-page))
  (POST "/aim/signup" [user pass] (create-account user pass)))

(defroutes user-login
  (POST "/aim/login" req (login req)))

(defroutes user-api
  (POST "/aim/add-buddy" [])
  (POST "/aim/delete-buddy" [])
  (POST "/aim/buddies-by-user" [])
  (POST "/aim/update-status" [])
  (POST "/aim/update-groupname" []))

(def app 
  (routes 
   webpage
   (-> user-login
       (wrap-authentication (basic {:authfn my-authfn}))
       (wrap-authorization (basic {:authfn my-authfn})))
;   (wrap-authentication user-api token)
   ))
