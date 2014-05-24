(ns fh-commenter.oauth-ext
  (:require [oauth.signature :as sig]
            [oauth.client :as oa]))

(def make-consumer oa/make-consumer)
(def request-token oa/request-token)
(def credentials oa/credentials)
(def authorization-header oa/authorization-header)

(defn build-xauth-access-token-request
  [consumer {token :oauth_token token-secret :oauth_token_secret} username password nonce timestamp]
  (let [oauth-params (sig/oauth-params consumer nonce timestamp token)
        post-params {:x_auth_username username
                     :x_auth_password password
                     :x_auth_mode "client_auth"}
        signature (sig/sign consumer
                            (sig/base-string "POST"
                                             (:access-uri consumer)
                                             (merge oauth-params
                                                    post-params))
                            token-secret)
        params (assoc oauth-params
                 :oauth_signature signature)]
    (oa/build-request params post-params)))

(defn- nonce [] (sig/rand-str 30))
(defn- current-ts [] (sig/msecs->secs (System/currentTimeMillis)))

(defn xauth-access-token
  "Request an access token with a username and password with xAuth."
  [consumer request-token username password]
  (oa/post-request-body-decoded (:access-uri consumer)
                             (build-xauth-access-token-request consumer
                                                               request-token
                                                               username
                                                               password
                                                               (nonce)
                                                               (current-ts))))
