(ns iplant_groups.service.attributes
  (:require [iplant_groups.clients.grouper :as grouper]
            [iplant_groups.service.format :as fmt]))

(defn add-attribute-name
  [{:keys [name description display_extension attribute_definition]} {:keys [user]}]
  (let [attribute-name (grouper/add-attribute-name user (:id attribute_definition) name display_extension description)]
    (fmt/format-attribute-name attribute-name)))

(defn assign-role-permission
  [{:keys [user allowed]} attribute-id role-id action-name]
  (let [attribute-assign (grouper/assign-role-permission user attribute-id role-id allowed [action-name])]
    (fmt/format-attribute-assign attribute-assign)))

(defn remove-role-permission
  [{:keys [user allowed]} attribute-id role-id action-name]
  (let [attribute-assign (grouper/remove-role-permission user attribute-id role-id allowed [action-name])]
    (fmt/format-attribute-assign attribute-assign)))

(defn assign-membership-permission
  [{:keys [user allowed]} attribute-id role-id subject-id action-name]
  (let [attribute-assign (grouper/assign-membership-permission user attribute-id role-id subject-id allowed [action-name])]
    (fmt/format-attribute-assign attribute-assign)))

(defn remove-membership-permission
  [{:keys [user allowed]} attribute-id role-id subject-id action-name]
  (let [attribute-assign (grouper/remove-membership-permission user attribute-id role-id subject-id allowed [action-name])]
    (fmt/format-attribute-assign attribute-assign)))
