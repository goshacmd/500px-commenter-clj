(ns fh-commenter.five-hundred
  (:refer-clojure :exclude [get])
  (:require [fh-commenter.oauth-ext :as oa]
            [clj-http.client :as http]
            [clojure.data.json :as json]))

(def base-url "https://api.500px.com/v1")

(defn build-url [part] (str base-url part))

(def req-token-url (build-url "/oauth/request_token"))
(def acc-token-url (build-url "/oauth/access_token"))
(def authorize-url (build-url "/oauth/authorize"))

(defn make-base
  [consumer-key consumer-secret username password]
  (let [consumer (oa/make-consumer consumer-key
                                   consumer-secret
                                   req-token-url
                                   acc-token-url
                                   authorize-url
                                   :hmac-sha1)
        request-token (oa/request-token consumer)
        {token :oauth_token token-secret :oauth_token_secret} (oa/xauth-access-token consumer request-token username password)]
    {:consumer consumer :token token :token-secret token-secret}))

(defn credentials
  [{:keys [consumer token token-secret]} url method & [data]]
  (oa/credentials consumer token token-secret method url data))

(defn auth-header [base url method params]
  (-> (credentials base url method params)
      (oa/authorization-header)))

(defn request [base method path params-key params]
  (let [full-url (build-url path)
        auth-header (auth-header base full-url method params)]
    (http/request {:method method :url full-url :headers {"Authorization" auth-header} params-key params})))

(def get #(request %1 :get %2 :query-params %3))
(def post #(request %1 :post %2 :form-params %3))

(defn parse-json [json-str]
  (json/read-str json-str :key-fn keyword))

(defn get-and-parse [base path params resp-key]
  (-> (get base path params)
      (:body)
      (parse-json)
      (resp-key)))

(defn list-photos
  ([base] (list-photos base {}))
  ([base {:keys [feature rpp] :or {feature "fresh_today" rpp 20}}]
   (get-and-parse base "/photos.json" {:include_states 1 :feature feature :rpp rpp} :photos)))

(defn like [base {id :id}]
  (post base (str "/photos/" id "/vote") {:vote 1}))

(defn comment-on [base {id :id} text]
  (post base (str "/photos/" id "/comments") {:body text}))
