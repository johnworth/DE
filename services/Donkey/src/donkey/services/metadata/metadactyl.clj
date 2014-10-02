(ns donkey.services.metadata.metadactyl
  (:use [clojure.java.io :only [reader]]
        [donkey.util.config]
        [donkey.util.transformers]
        [donkey.auth.user-attributes]
        [donkey.clients.user-info :only [get-user-details]]
        [donkey.persistence.workspaces :only [get-or-create-workspace]]
        [donkey.services.fileio.actions :only [upload]]
        [donkey.services.user-prefs :only [user-prefs]]
        [donkey.util.email]
        [donkey.util.service]
        [kameleon.queries :only [record-login record-logout]]
        [korma.db :only [with-db]]
        [medley.core :only [dissoc-in]]
        [ring.util.codec :only [url-encode]])
  (:require [cheshire.core :as cheshire]
            [clj-http.client :as client]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [donkey.clients.data-info :as di]
            [donkey.clients.metadactyl :as dm]
            [donkey.clients.notifications :as dn]
            [donkey.util.db :as db]))

(defn- secured-notification-url
  [req & components]
  (apply build-url-with-query (notificationagent-base-url)
         (add-current-user-to-map (:params req)) components))

(defn- secured-params
  ([]
   (secured-params {}))
  ([existing-params]
   (add-current-user-to-map existing-params)))

(defn- metadactyl-request
  "Prepares a metadactyl request by extracting only the body of the client request and sets the
   forwarded request's content-type to json."
  [req]
  (assoc (select-keys req [:body]) :content-type :json))

(defn- metadactyl-url
  "Adds the name and email of the currently authenticated user to the metadactyl URL with the given
   relative URL path."
  [query & components]
  (apply build-url-with-query (metadactyl-unprotected-base-url)
                              (secured-params query)
                              components))

(defn- build-metadactyl-secured-url
  "Adds the name and email of the currently authenticated user to the secured
   metadactyl URL with the given relative URL path."
  [{query :params} & components]
  (apply build-url-with-query (metadactyl-base-url) (secured-params query) components))

(defn- build-metadactyl-unprotected-url
  "Builds an unsecured metadactyl URL from the given relative URL path.  Any
   query-string parameters that are present in the request will be forwarded
   to metadactyl."
  [{query :params} & components]
  (apply build-url-with-query (metadactyl-unprotected-base-url) query components))

(defn get-workflow-elements
  "A service to get information about workflow elements."
  [element-type]
  (client/get (metadactyl-url {} "apps" "elements" element-type)
              {:as :stream}))

(defn search-deployed-components
  "A service to search information about deployed components."
  [req search-term]
  (let [url (build-metadactyl-unprotected-url req "search-deployed-components" search-term)]
    (forward-get url req)))

(defn get-all-app-ids
  "A service to get the list of app identifiers."
  []
  (client/get (metadactyl-url {} "apps" "ids")
              {:as :stream}))

(defn delete-categories
  "A service used to delete app categories."
  [req]
  (let [url (build-metadactyl-unprotected-url req "delete-categories")]
    (forward-post url req)))

(defn validate-app-for-pipelines
  "A service used to determine whether or not an app can be included in a
   pipeline."
  [req app-id]
  (let [url (build-metadactyl-unprotected-url req "validate-analysis-for-pipelines" app-id)]
    (forward-get url req)))

(defn categorize-apps
  "A service used to recategorize apps."
  [req]
  (let [url (metadactyl-url {} "admin" "apps")
        req (metadactyl-request req)]
    (forward-post url req)))

(defn get-app-categories
  "A service used to get a list of app categories."
  [req category-set]
  (let [url (build-metadactyl-unprotected-url req "get-analysis-categories" category-set)]
    (forward-get url req)))

(defn can-export-app
  "A service used to determine whether or not an app can be exported to Tito."
  [req]
  (let [url (build-metadactyl-unprotected-url req "can-export-analysis")]
    (forward-post url req)))

(defn add-app-to-group
  "A service used to add an existing app to an app group."
  [req]
  (let [url (build-metadactyl-unprotected-url req "add-analysis-to-group")]
    (forward-post url req)))

(defn get-app
  "A service used to get an app in the format required by the DE."
  [req app-id]
  (let [url (build-metadactyl-unprotected-url req "get-analysis" app-id)]
    (forward-get url req)))

(defn list-app
  "This service lists a single application.  The response body contains a JSON
   string representing an object containing a list of apps.  If an app with the
   provided identifier exists then the list will contain that app.  Otherwise,
   the list will be empty."
  [req app-id]
  (let [url (build-metadactyl-unprotected-url req "list-analysis" app-id)]
    (forward-get url req)))

(defn export-template
  "This service will export the template with the given identifier."
  [req template-id]
  (let [url (build-metadactyl-unprotected-url req "export-template" template-id)]
    (forward-get url req)))

(defn export-workflow
  "This service will export a workflow with the given identifier."
  [req app-id]
  (let [url (build-metadactyl-unprotected-url req "export-workflow" app-id)]
    (forward-get url req)))

(defn export-deployed-components
  "This service will export all or selected deployed components."
  [req]
  (let [url (build-metadactyl-unprotected-url req "export-deployed-components")]
    (forward-post url req)))

(defn preview-template
  "This service will convert a JSON document in the format consumed by
   the import service into the format required by the DE."
  [req]
  (let [url (build-metadactyl-unprotected-url req "preview-template")]
    (forward-post url req)))

(defn preview-workflow
  "This service will convert a JSON document in the format consumed by
   the import service into the format required by the DE."
  [req]
  (let [url (build-metadactyl-unprotected-url req "preview-workflow")]
    (forward-post url req)))

(defn import-template
  "This service will import a template into the DE."
  [req]
  (let [url (build-metadactyl-unprotected-url req "import-template")]
    (forward-post url req)))

(defn import-workflow
  "This service will import a workflow into the DE."
  [req]
  (let [url (build-metadactyl-unprotected-url req "import-workflow")]
    (forward-post url req)))

(defn import-tools
  "This service will import deployed components into the DE and send
   notifications if notification information is included and the deployed
   components are successfully imported."
  [req]
  (let [json-string (slurp (:body req))
        json-obj    (cheshire/decode json-string true)
        url (build-metadactyl-unprotected-url req "import-tools")]
    (forward-post url req json-string)
    (dorun (map #(dn/send-tool-notification %) (:components json-obj))))
  (success-response))

(defn update-app
  "This service will update the information at the top level of an analysis.
   It will not update any of the components of the analysis."
  [req]
  (let [url (build-metadactyl-unprotected-url req "update-analysis")]
    (forward-post url req)))

(defn update-template
  "This service will either update an existing template or import a new template."
  [req]
  (let [url (build-metadactyl-unprotected-url req "update-template")]
    (forward-post url req)))

(defn create-pipeline
  "This service will create a pipeline."
  [req]
  (let [url (metadactyl-url {} "apps" "pipelines")
        req (metadactyl-request req)]
    (forward-post url req)))

(defn update-pipeline
  "This service will update an existing pipeline."
  [req app-id]
  (let [url (metadactyl-url {} "apps" "pipelines" app-id)
        req (metadactyl-request req)]
    (forward-put url req)))

(defn update-app-labels
  "This service updates the labels in a single-step app. Both vetted and unvetted apps can be
   modified using this service."
  [req app-id]
  (let [url (metadactyl-url {} "apps" app-id)
        req (metadactyl-request req)]
    (forward-patch url req)))

(defn delete-app
  "This service will logically remove an app from the DE."
  [req app-id]
  (client/delete (metadactyl-url {} "apps" app-id)
                 {:as :stream}))

(defn delete-apps
  "This service will logically remove a list of apps from the DE."
  [req]
  (let [url (metadactyl-url {} "apps" "shredder")
        req (metadactyl-request req)]
    (forward-post url req)))

(defn bootstrap
  "This service obtains information about and initializes the workspace for the authenticated user.
   It also records the fact that the user logged in."
  [{{:keys [ip-address]} :params {user-agent "user-agent"} :headers}]
  (assert-valid ip-address "Missing or empty query string parameter: ip-address")
  (assert-valid user-agent "Missing or empty request parameter: user-agent")
  (let [username    (:username current-user)
        user        (:shortUsername current-user)
        workspace   (get-or-create-workspace username)
        preferences (user-prefs (:username current-user))
        login-time  (with-db db/de
                      (record-login username ip-address user-agent))]
    (cheshire/encode
      {:workspaceId   (:id workspace)
       :newWorkspace  (:newWorkspace workspace)
       :loginTime     (str login-time)
       :username      user
       :full_username username
       :email         (:email current-user)
       :firstName     (:firstName current-user)
       :lastName      (:lastName current-user)
       :userHomePath  (di/user-home-folder user)
       :userTrashPath (di/user-trash-folder user)
       :baseTrashPath (di/base-trash-folder)
       :preferences   preferences})))

(defn logout
  "This service records the fact that the user logged out."
  [{:keys [ip-address login-time]}]
  (assert-valid ip-address "Missing or empty query string parameter: ip-address")
  (assert-valid login-time "Missing or empty query string parameter: login-time")
  (with-db db/de
    (record-logout (:username current-user)
                   ip-address
                   (string->long login-time "Long integer expected: login-time")))
  {})

(defn get-messages
  "This service forwards requests to the notification agent in order to
   retrieve notifications that the user may or may not have seen yet."
  [req]
  (let [url (dn/notificationagent-url "messages" (:params req))]
    (dn/add-app-details (forward-get url req))))

(defn get-unseen-messages
  "This service forwards requests to the notification agent in order to
   retrieve notifications that the user hasn't seen yet."
  [req]
  (let [url (dn/notificationagent-url "unseen-messages")]
    (dn/add-app-details (forward-get url req))))

(defn last-ten-messages
  "This service forwards requests for the ten most recent notifications to the
   notification agent."
  [req]
  (let [url (dn/notificationagent-url "last-ten-messages" (:params req))]
    (dn/add-app-details (forward-get url req))))

(defn count-messages
  "This service forwards requests to the notification agent in order to
   retrieve the number of notifications satisfying the conditions in the
   query string."
  [req]
  (let [url (dn/notificationagent-url "count-messages" (:params req))]
    (forward-get url req)))

(defn delete-notifications
  "This service forwards requests to the notification agent in order to delete
   existing notifications."
  [req]
  (let [url (dn/notificationagent-url "delete")]
    (forward-post url req)))

(defn delete-all-notifications
  "This service forwards requests to the notification agent in order to delete
   all notifications for the user."
  [params]
  (let [url (dn/notificationagent-url "delete-all" params)]
    (forward-delete url params)))

(defn mark-notifications-as-seen
  "This service forwards requests to the notification agent in order to mark
   notifications as seen by the user."
  [req]
  (let [url (dn/notificationagent-url "seen")]
    (forward-post url req)))

(defn mark-all-notifications-seen
  "This service forwards requests to the notification agent in order to mark all
   notifications as seen for the user."
  [req]
  (let [url (dn/notificationagent-url "mark-all-seen")]
    (forward-post url req (cheshire/encode (add-current-user-to-map {})))))

(defn send-notification
  "This service forwards a notifiction to the notification agent's general
   notification endpoint."
  [req]
  (let [url (dn/notificationagent-url "notification")]
    (forward-post url req)))

(defn get-system-messages
  "This service forwards a notification to the notification agent's endpoint
   for retrieving system messages."
  [req]
  (forward-get (secured-notification-url req "system" "messages") req))

(defn get-new-system-messages
  "Forwards a request to the notification agent's endpoint for getting new system messages."
  [req]
  (forward-get (secured-notification-url req "system" "new-messages") req))

(defn get-unseen-system-messages
  "Forwards a request to the notification agent's endpoint for getting
   unseen system messages."
  [req]
  (forward-get (secured-notification-url req "system" "unseen-messages") req))

(defn mark-system-messages-received
  "Forwards a request to the notification to mark a set of system notifications as received."
  [req]
  (forward-post (secured-notification-url req "system" "received") req))

(defn mark-all-system-messages-received
  "Forwards a request to the notification-agent to mark all system notifications as received."
  [req]
  (forward-post (secured-notification-url req "system" "mark-all-received") req))

(defn mark-system-messages-seen
  "Forwards a request to the notification to mark a set of system notifications
   as seen."
  [req]
  (forward-post (secured-notification-url req "system" "seen") req))

(defn mark-all-system-messages-seen
  "Forwards a request to the notification-agent to mark all system notifications as seen."
  [req]
  (forward-post (secured-notification-url req "system" "mark-all-seen") req))

(defn delete-system-messages
  "Forwards a request to the notification-agent to soft-delete a set of system messages."
  [req]
  (forward-post (secured-notification-url req "system" "delete") req))

(defn delete-all-system-messages
  "Forwards a request to to the notification-agent to soft-delete all system messages for a
   set of users."
  [req]
  (forward-delete (secured-notification-url req "system" "delete-all") req))

(defn admin-add-system-message
  "Forwards a request to the notification-agent to allow an admin to add a new system
   message."
  [req]
  (forward-put (secured-notification-url req "admin" "system") req))

(defn admin-list-system-types
  "Forwards a request to the notification-agent to allow an admin to list the current
   list of system notification types."
  [req]
  (forward-get (secured-notification-url req "admin" "system-types") req))

(defn admin-list-system-messages
  "Forwards a request to the notification agent to allow an admin to list existing system
   notifications."
  [req]
  (forward-get (secured-notification-url req "admin" "system") req))

(defn admin-get-system-message
  "Forwards a request to the notification-agent to get a system notification for an admin."
  [req uuid]
  (forward-get (secured-notification-url req "admin" "system" uuid) req))

(defn admin-update-system-message
  "Forwards a request to the notification-agent to update a system notification for an admin."
  [req uuid]
  (forward-post (secured-notification-url req "admin" "system" uuid) req))

(defn admin-delete-system-message
  "Forwards a request to the notification-agent to delete a system notification for an admin."
  [req uuid]
  (forward-delete (secured-notification-url req "admin" "system" uuid) req))

(defn edit-app
  "This service makes an app available in Tito for editing and returns a
   representation of the app in the JSON format required by the DE as of
   version 1.8."
  [app-id]
  (client/get (metadactyl-url {} "apps" app-id "ui")
              {:as :stream}))

(defn copy-app
  "This service makes a copy of an app available in Tito for editing."
  [req app-id]
  (let [url (build-metadactyl-secured-url req "copy-template" app-id)]
    (forward-get url req)))

(defn update-template-secured
  "This service will import an app into or update an app in the DE."
  [req]
  (let [url (build-metadactyl-secured-url req "update-template")]
    (forward-put url req)))

(defn update-app-secured
  "This service will import a single-step app into or update an existing app in the DE."
  [req]
  (let [url (build-metadactyl-secured-url req "update-app")]
    (forward-put url req)))

(defn make-app-public
  "This service copies an app from a user's private workspace to the public
   workspace."
  [req]
  (let [url (build-metadactyl-secured-url req "make-analysis-public")]
    (forward-post url req)))

(defn app-publishable?
  "This service determines whether or not an app can safely be made public."
  [app-id]
  (cheshire/encode (dm/app-publishable? app-id)))

(defn list-reference-genomes
  "Lists the reference genomes in the database."
  [req]
  (let [url (build-metadactyl-secured-url req "reference-genomes")]
    (forward-get url req)))

(defn replace-reference-genomes
  "Replaces the reference genomes in the database with a new set of reference
   genomes."
  [req]
  (let [url (build-metadactyl-secured-url req "reference-genomes")]
    (forward-put url req)))

(defn- extract-uploaded-path
  "Gets the file ID as a path from the given upload results."
  [upload]
  (get-in upload [:file :id]))

(defn- upload-tool-request-file
  "Uploads a file with a tmp path, found in params by the given file-key, to the
   given user's final-path dir, then updates file-key in params with the file's
   new path."
  [params file-key user final-path]
  (let [tmp-path (params file-key)]
    (if (nil? tmp-path)
      params
      (assoc params
             file-key
             (extract-uploaded-path (upload user tmp-path final-path))))))

(defn- postprocess-tool-request
  "Postprocesses a tool request update or submission. The postprocessing function
   should take the tool request and user details as arguments."
  [res f]
  (if (<= 200 (:status res) 299)
    (let [tool-req     (cheshire/decode-stream (reader (:body res)) true)
          username     (string/replace (:submitted_by tool-req) #"@.*" "")
          user-details (get-user-details username)]
      (f tool-req user-details))
    res))

(defn submit-tool-request
  "Submits a tool request on behalf of the user found in the request params."
  [req]
  (let [tool-request-url (metadactyl-url {} "tool-requests")
        req (metadactyl-request req)]
    (postprocess-tool-request
      (forward-post tool-request-url req)
      (fn [tool-req user-details]
        (send-tool-request-email tool-req user-details)
        (dn/send-tool-request-notification tool-req user-details)
        (success-response tool-req)))))

(defn list-tool-requests
  "Lists the tool requests that were submitted by the authenticated user."
  []
  (client/get (metadactyl-url {} "tool-requests")
              {:as :stream}))

(defn admin-list-tool-requests
  "Lists the tool requests that were submitted by any user."
  [params]
  (success-response (dm/admin-list-tool-requests params)))

(defn list-tool-request-status-codes
  "Lists the known tool request status codes."
  [params]
  (success-response (dm/list-tool-request-status-codes params)))

(defn update-tool-request
  "Updates a tool request with comments and possibly a new status."
  [req request-id]
  (let [url (metadactyl-url {} "admin" "tool-requests" request-id "status")
        req (metadactyl-request req)]
    (postprocess-tool-request
      (forward-post url req)
      (fn [tool-req user-details]
        (dn/send-tool-request-update-notification tool-req user-details)
        (success-response tool-req)))))

(defn get-tool-request
  "Lists details about a specific tool request."
  [request-id]
  (client/get (metadactyl-url {} "admin" "tool-requests" request-id)
              {:as :stream}))

(defn preview-args
  "Previews the command-line arguments for a job request."
  [req]
  (let [url (metadactyl-url {} "apps" "arg-preview")
        req (metadactyl-request req)]
    (forward-post url req)))

(defn provide-user-feedback
  "Forwards feedback from the user to iPlant."
  [body]
  (send-feedback-email (cheshire/decode-stream (reader body)))
  (success-response))
