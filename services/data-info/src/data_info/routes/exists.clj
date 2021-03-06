(ns data-info.routes.exists
  (:use [common-swagger-api.schema]
        [data-info.routes.domain.common]
        [data-info.routes.domain.exists])
  (:require [data-info.services.exists :as exists]
            [data-info.util.service :as svc]
            [data-info.util.schema :as s]))


(defroutes* existence-marker

  (context* "/existence-marker" []
    :tags ["bulk"]

    (POST* "/" [:as {uri :uri}]
      :query [params StandardUserQueryParams]
      :body [body (describe Paths "The paths to check for existence.")]
      :return (s/doc-only ExistenceInfo ExistenceResponse)
      :summary "File and Folder Existence"
      :description (str
"This endpoint allows the caller to check for the existence of a set of files and folders."
(get-error-code-block
  "ERR_NOT_A_USER, ERR_TOO_MANY_RESULTS"))
      (svc/trap uri exists/do-exists params body))))
