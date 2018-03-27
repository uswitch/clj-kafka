(ns clj-kafka.consumer
  (:require [clj-kafka.core :refer [to-clojure map-to-clojure]])
  (:import [org.apache.kafka.clients.consumer Consumer KafkaConsumer ConsumerRecord OffsetAndMetadata]
           [org.apache.kafka.common.serialization Deserializer]
           [org.apache.kafka.common TopicPartition]))

(defn consumer
  ([^java.util.Map config topics]
   (doto (KafkaConsumer. config)
     (.subscribe topics)))
  ([^java.util.Map config topics ^Deserializer key-deserializer ^Deserializer value-deserializer]
   (doto (KafkaConsumer. config key-deserializer value-deserializer)
     (.subscribe topics))))

(defn partition-records [^Consumer consumer]
  (let [records (.poll consumer Long/MAX_VALUE)]
    (for [partition (.partitions records)
          record (.records records partition)]
      (to-clojure record))))

(defn records
  [^Consumer consumer timeout-ms]
  (as-> consumer x
        (.poll x timeout-ms)
        (map to-clojure x)))

(defn records-seq
  [^Consumer consumer]
  (lazy-cat
    (records consumer Long/MAX_VALUE)
    (records-seq consumer)))

(defn commit-sync
  ([^Consumer c]
  (.commitSync c))
  ([^Consumer c {:keys [topic partition offset]}]
   (.commitSync c (TopicPartition. topic partition) (OffsetAndMetadata. offset))))

(defn list-topics [^Consumer c]
  (map-to-clojure (.listTopics c)))
