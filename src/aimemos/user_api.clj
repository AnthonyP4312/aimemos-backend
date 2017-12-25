(ns aimemos.user-api
  (:require [aimemos.db :as db :refer [db current-users]]
            [cheshire.core :as json]
            [hugsql.core :as sql]
            [org.httpkit.server :refer [send!]]))


;; This file should define all the functions for users interaction

(defmacro safe-db
  [func]
  `(try 
     ~func
     (catch Exception ex#
       (println "Ate an exception: " ex#))))

(defn send-message 
  "{:to :from :message}\nSends a Message from one user to another"
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
  "{:username :buddyname :groupname}\nAdds adds one user to another's
  buddylist under the groupname provided"
  [{:keys [username buddyname groupname]} params]
  (when (safe-db (db/add-buddy db params)))
  )


(defn delete-buddy
  "{:username :buddyname}\nDeletes a Buddy from a user's Buddy List"
  [{:keys [username buddyname]} params]
  
  )

(defn update-status
  "{:username :status}\nToggle the user between the statuses of ONLINE,
  OFFLINE, and AWAY"
  [{:keys [user status]} params])

(defn update-groupname
  "{:username :buddyname :groupname}\nChange the group specified for a buddy"
  [{:keys [user buddy group]} params])

(defn buddies-by-user
  "{:username}\nReturn the current list of buddies for a user"
  [{:keys [username]} params]
  )
