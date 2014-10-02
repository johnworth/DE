(ns metadactyl.service.app-metadata
  "DE app metadata services."
  (:use [clojure.java.io :only [reader]]
        [clojure-commons.validators]
        [kameleon.queries :only [get-existing-user-id]]
        [metadactyl.user :only [current-user]]
        [metadactyl.util.service :only [build-url success-response parse-json]]
        [metadactyl.validation :only [verify-app-ownership]]
        [korma.db :only [transaction]]
        [slingshot.slingshot :only [try+ throw+]])
  (:require [cheshire.core :as cheshire]
            [clj-http.client :as client]
            [clojure-commons.error-codes :as ce]
            [clojure.tools.logging :as log]
            [metadactyl.persistence.app-metadata :as amp]
            [metadactyl.translations.app-metadata :as atx]
            [metadactyl.util.config :as config]))

(defn- get-valid-user-id
  "Gets the user ID for the given username, or throws an error if that username is not found."
  [username]
  (let [user-id (get-existing-user-id username)]
    (when (nil? user-id)
      (throw+ {:error_code ce/ERR_BAD_REQUEST
               :reason     (str "No user found for username " username)}))
    user-id))

(defn- validate-app-existence
  "Verifies that apps exist."
  [app-id]
  (amp/get-app app-id))

(defn relabel-app
  "This service allows labels to be updated in any app, whether or not the app has been submitted
   for public use."
  [body]
  (verify-app-ownership (validate-app-existence (:id body)))
  (transaction (amp/update-app-labels body))
  (success-response))

(defn- validate-app-ownership
  "Verifies that a user owns an app."
  [username app-id]
  (when-not (every? (partial = username) (amp/app-accessible-by app-id))
    (throw+ {:error_code ce/ERR_BAD_REQUEST
             :reason     (str username " does not own app " app-id)})))

(defn- validate-deletion-request
  "Validates an app deletion request."
  [req]
  (when (empty? (:app_ids req))
    (throw+ {:error_code ce/ERR_BAD_REQUEST
             :reason     "no app identifiers provided"}))
  (when (and (nil? (:username current-user)) (not (:root_deletion_request req)))
    (throw+ {:error_code ce/ERR_BAD_REQUEST
             :reason     "no username provided for non-root deletion request"}))
  (dorun (map validate-app-existence (:app_ids req)))
  (when-not (:root_deletion_request req)
    (dorun (map (partial validate-app-ownership (:username current-user)) (:app_ids req)))))

(defn delete-apps
  "This service marks existing apps as deleted in the database."
  [req]
  (validate-deletion-request req)
  (transaction (dorun (map amp/delete-app (:app_ids req))))
  {})

(defn delete-app
  "This service marks an existing app as deleted in the database."
  [app-id]
  (validate-app-existence app-id)
  (validate-app-ownership (:username current-user) app-id)
  (amp/delete-app app-id)
  {})

(defn preview-command-line
  "This service sends a command-line preview request to the JEX."
  [body]
  (let [jex-req (atx/template-cli-preview-req body)]
    (cheshire/decode-stream
     ((comp reader :body)
      (client/post
       (build-url (config/jex-base-url) "arg-preview")
       {:body             (cheshire/encode jex-req)
        :content-type     :json
        :as               :stream}))
     true)))

(defn rate-app
  "Adds or updates a user's rating and comment ID for the given app. The request must contain either
   the rating or the comment ID, and the rating must be between 1 and 5, inclusive."
  [app-id {:keys [rating comment_id] :as request}]
  (validate-app-existence app-id)
  (let [user-id (get-valid-user-id (:username current-user))]
    (when (and (nil? rating) (nil? comment_id))
      (throw+ {:error_code ce/ERR_BAD_REQUEST
               :reason     (str "No rating or comment ID given")}))
    (when (or (> 1 rating) (> rating 5))
      (throw+ {:error_code ce/ERR_BAD_REQUEST
               :reason     (str "Rating must be an integer between 1 and 5 inclusive."
                                " Invalid rating (" rating ") for App ID " app-id)}))
    (amp/rate-app app-id user-id request)
    (amp/get-app-avg-rating app-id)))

(defn delete-app-rating
  "Removes a user's rating and comment ID for the given app."
  [app-id]
  (validate-app-existence app-id)
  (let [user-id (get-valid-user-id (:username current-user))]
    (amp/delete-app-rating app-id user-id)
    (amp/get-app-avg-rating app-id)))
