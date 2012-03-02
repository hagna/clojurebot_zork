(ns bot1.core
  ;; In an (ns ..) form, you typically use keywords. These are flags
  ;; telling ns what to do, which is why it is different from the REPL
  ;; and outside of an ns form. These are flags that call the
  ;; functions/macros for you.
  (:require hobbit.bitly)
  (:use [clojure.java.io :only (as-file copy)])
  ;; You can have multiple imports (and requires too).
  (:import (hobbit.bitly Bitly)
           (java.io ByteArrayOutputStream ByteArrayInputStream)
           (org.jibble.pircbot PircBot)
           (org.apache.commons.exec DefaultExecutor CommandLine DefaultExecuteResultHandler PumpStreamHandler)
           
           ))

(def ^:dynamic *bot*)

(defn e [cmdline]
  (let [cmd (CommandLine/parse cmdline) executor (DefaultExecutor. )
        resulthandler (DefaultExecuteResultHandler. )
        os (ByteArrayOutputStream. )
        es (ByteArrayOutputStream. )
        is (ByteArrayInputStream. (.getBytes (String. "WHYISTHISNEEDED?")))
        pStreamHandler (PumpStreamHandler. os es is)
        ]
    (do
      (.setStreamHandler executor pStreamHandler)
      (.execute executor cmd resulthandler)
      {:res resulthandler :os os :es es :cmd cmd :is is})))

(def url-regex #"[A-Za-z]+://[^  ^/]+\.[^  ^/]+[^ ]+")

(def random (java.util.Random.))
;define characters list to use to generate string
(def nchars 
   (map char (concat (range 48 58) (range 66 92) (range 97 123))))
;generates 1 random character
(defn random-char [] 
  (nth nchars (.nextInt random (count nchars))))
; generates random string of length characters
(defn random-string [length]
  (apply str (take length (repeatedly random-char))))

;; Clojure doesn't use camelCase for names. Use hypens to separate words
;; in names.
(defn send-msg "send a message to a recv, a recv is a channel name or a nick"
  [this recv msg]
  (.sendMessage this recv (.replace (str msg) \newline \ )))

(def *bitly* (hobbit.bitly/Bitly. "R_3e31226c1421a34e074cc8f4367ec33c" "hagna" "j.mp"))

(defn rick "generates a nice rick roll that seems different every time"
  []
  (let [roll "http://www.youtube.com/watch?v=XZ5TajZYW6Y"]
    (hobbit.core/shorten *bitly* (str roll "/#" (random-string 5)))))

(defn is-priv-url [url]
  (or (.startsWith url "http://10")
      (.startsWith url "https://10")
      (.startsWith url "https://web0.sec")
      (.startsWith url "http://web0.sec")))

(defn handle-text [channel sender message]
  (println channel sender message))


(defn handle-url [channel sender url]
  (println channel sender url)
  (if (is-priv-url url)
    (println "not shortening private url" url)
    (let [s-url (hobbit.core/shorten *bitly* url) saved (- (count url) (count s-url))]
      (println "shorter url is" s-url)
      (if (> saved 3)
        (let [msg (str s-url)]
          (send-msg *bot* channel msg))
        (println "didn't save enough by shortening url" url)))))

(defn fuse [n msg]
  (future ((Thread/sleep n) (msg))))

(def desalpha { "i:" 10400 "eI" 10429 "aI" 10434})

(defn handle-priv-message [sender login hostname message]
  (if (= sender "matt")
    (send-msg *bot* "#dev" message)))

(defn handle-message [channel sender login hostname message]
  (println channel sender login hostname message)
  (let [url (re-find url-regex message)]
   (if (= url nil)
     (handle-text channel sender message)
     (if (or (= channel "#dev") (= channel "#test"))
       (handle-url channel sender url))
     )))

(defn pircbot []
  (proxy [PircBot] []
    (onMessage [channel sender login hostname message]
      (handle-message channel sender login hostname message)
      )
    (onPrivateMessage [sender login hostname message]
      (handle-priv-message sender login hostname message)
      )))
    
(defn -main [& args]
  (println "args are" args)
  (def *bot* (pircbot))
  (.setName *bot* "FooBot")
  (.changeNick *bot* "skeletor")
  (.connect *bot* "10.1.2.209")
  (.joinChannel *bot* "#test"))
