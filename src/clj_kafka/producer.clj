(ns ^{:doc "Clojure interface to the Kafka Producer API. For
           complete JavaDocs, see:
           https://kafka.apache.org/10/javadoc/index.html?org/apache/kafka/clients/producer/KafkaProducer.html "}
  clj-kafka.producer
  (:refer-clojure :exclude [send])
  (:require [clj-kafka.core :refer [to-clojure]])
  (:import [java.util.concurrent Future TimeUnit TimeoutException]
           [org.apache.kafka.clients.producer Callback KafkaProducer ProducerRecord RecordMetadata]
           [org.apache.kafka.common.serialization Serializer]))

(defn producer
  "Return a `KafkaProducer` for publishing records to Kafka.
  `KafkaProducer` instances are thread-safe and should generally be
  shared for best performance.

  Implements `Closeable`, so suitable for use with `with-open`.

  For available config options, see:
  https://kafka.apache.org/documentation/#producerconfigs "
  ([^java.util.Map config]
   (KafkaProducer. config))
  ([^java.util.Map config ^Serializer key-serializer ^Serializer value-serializer]
   (KafkaProducer. config key-serializer value-serializer)))

(defn record
  "Return a record that can be published to Kafka using [[send]]."
  ([topic value]
   (ProducerRecord. topic value))
  ([topic key value]
   (ProducerRecord. topic key value))
  ([topic partition key value]
   (ProducerRecord. topic partition key value)))

(defn- map-future-val
  [^Future fut]
  (reify
    java.util.concurrent.Future
    (cancel [_ interrupt?] (.cancel fut interrupt?))
    (get [_] (to-clojure (.get fut)))
    (get [_ timeout unit] (to-clojure (.get fut timeout unit)))
    (isCancelled [_] (.isCancelled fut))
    (isDone [_] (.isDone fut))))

(defn send
  "Asynchronously send a record to Kafka. Returns a `Future` of a map
  with `:topic`, `:partition` and `:offset` keys. Optionally provide
  a callback fn that will be called when the operation completes.
  Callback should be a fn of two arguments, a map as above, and an
  exception. Exception will be nil if operation succeeded.

  For details on behaviour, see http://kafka.apache.org/082/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html#send(org.apache.kafka.clients.producer.ProducerRecord,org.apache.kafka.clients.producer.Callback)"
  ([^KafkaProducer producer record]
   (let [fut (.send producer record)]
     (map-future-val fut)))
  ([^KafkaProducer producer record callback]
   (let [fut (.send producer record (reify Callback
                                      (onCompletion [_ metadata exception]
                                        (callback (and metadata (to-clojure metadata)) exception))))]
     (map-future-val fut))))
