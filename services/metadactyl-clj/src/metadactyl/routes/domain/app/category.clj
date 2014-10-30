(ns metadactyl.routes.domain.app.category
  (:use [metadactyl.routes.domain.app]
        [metadactyl.routes.params]
        [ring.swagger.schema :only [describe]]
        [schema.core :only [defschema optional-key Any]])
  (:import [java.util UUID]))

(defschema AppCategory
  {:id
   AppCategoryIdPathParam

   :name
   (describe String "The App Category's name")

   :app_count
   (describe Long "The number of Apps under this Category and all of its children")

   :is_public
   (describe Boolean
     "Whether this App Category is viewable to all users or private to only the user that owns its
      Workspace")

   ;; KLUDGE
   :categories
   (describe [Any]
     "A listing of child App Categories under this App Category.
      <b>Note</b>: This will be a list of more categories like this one, but the documentation
      library does not currently support recursive model schema definitions")})

(defschema AppCategoryListing
  {:categories (describe [AppCategory] "A listing of App Categories visisble to the requesting user")})

(defschema AppCategoryIdList
  {:category_ids (describe [UUID] "A List of UUIDs used to identify App Categories")})

(defschema AppCategoryAppListing
  (merge (dissoc AppCategory :categories)
         {:apps (describe [AppListingDetail] "A listing of Apps under this Category")}))

(defschema AppCategorization
  (merge AppCategoryIdList
    {:app_id (describe UUID "The UUID of the App to be Categorized")}))

(defschema AppCategorizationRequest
  {:categories (describe [AppCategorization] "Apps and the Categories they should be listed under")})
