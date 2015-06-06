(ns donkey.clients.metadata
  (:require [donkey.clients.metadata.raw :as raw]
            [donkey.util.service :as service]))

(defn- parse-body
  [response]
  (service/decode-json (:body response)))

(defn list-templates
  []
  (parse-body (raw/list-templates)))

(defn get-template
  [template-id]
  (parse-body (raw/get-template template-id)))

(defn get-attribute
  [attr-id]
  (parse-body (raw/get-attribute attr-id)))

(defn admin-list-templates
  []
  (parse-body (raw/admin-list-templates)))