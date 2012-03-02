(ns bot1.pbot
  (:gen-class
   :name bot1.pbot
   :extends org.jibble.pircbot.PircBot
   :exposes-methods {setName expsetName}))

(defn -boo [this param]
  (. this expsetName param))

