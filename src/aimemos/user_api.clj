(ns aimemos.user-api
  (:require [aimemos.db :as db :refer [db current-users]]
            [cheshire.core :as json]
            [hugsql.core :as sql]
            [org.httpkit.server :refer [send!]]))

(defmacro safe-db
  [func]
  `(try 
     ~func
     (catch Exception ex#
       (println "Ate an exception: " ex#))))

(defn send-message 
  "{:to :from :message}\n  Sends a Message from one user to another"
  [params]
  (println "hey were sending a message!")
  (println params)
  (println @current-users)
  (try
    (send! ((keyword (:to params)) @current-users) (json/encode params))
    (catch Exception ex
      (send! 
       ((keyword (:from params)) @current-users) 
       (json/encode {:to (:from params)
                     :from "SYSTEM_MSG"
                     :message (str "Theyre probably not online. " 
                                   "Sorry. Have an error instead.\n\n"
                                   (.getMessage ex))})))))



(defn add-buddy 
  "{:username :buddyname :groupname}\n  Adds adds one user to another's
  buddylist under the groupname provided"
  [params]
  (when (safe-db (db/add-buddy db params))
    (send! (@current-users (keyword (:username params))) 
           (json/encode (db/buddies-by-user db {:username (:username params)})))))


(defn delete-buddy
  "{:username :buddyname}\n  Deletes a Buddy from a user's Buddy List"
  [params]
  (when (safe-db (db/delete-buddy db params))
    (send! (@current-users (keyword (params :username)))
           (json/encode (db/buddies-by-user db {:username (params :username)})))))

(defn update-status
  "{:username :status}\n  Toggle the user between the statuses of ONLINE,
  OFFLINE, and AWAY"
  [params]
  (when (safe-db (db/update-status db params))
    ;; Query users with this person in their list, and notify them of
    ;; the change
    (let [users (db/users-by-buddy {:buddyname (params :username)})]
      (doseq [user users]
        (when-let [user-chan (@current-users (keyword user))]
          (json/encode (send! user-chan (db/buddies-by-user db {:username user}))))))))

(defn update-groupname
  "{:username :buddyname :groupname}\n  Change the group specified for a
  buddy"
  [params]
  (when (safe-db (db/update-groupname db params))
    (send! (@current-users (keyword (params :username)))
           (json/encode (db/buddies-by-user db {:username (params :username)})))))

(defn buddies-by-user
  "{:username}\n  Return the current list of buddies for a user"
  [params]
  (send! (@current-users (keyword (params :username))) 
         (json/encode (db/buddies-by-user db params))))
