(ns ^{:doc "Offset operations."}
  clj-kafka.offset
  (:require [clj-kafka.core :refer [to-clojure map-to-clojure]])
  (:import [org.apache.kafka.clients.consumer OffsetAndTimestamp]
    [org.apache.kafka.common TopicPartition]))

(defn topic-partition [topic partition]
  (TopicPartition. topic partition))

(defn seek [^Consumer c topic partition offset]
  (doto c (.seek (TopicPartition. topic partition) offset)))

(defn beginning-offsets [^Consumer c topic topic-partitions]
  (.beginningOffsets c topic-partitions))

(defn end-offsets [^Consumer c topic topic-partitions]
  (.endOffsets c topic-partitions))

(defn seek-beginning [^Consumer c topic topic-partitions]
  (doto c (.seekToBeginning topic-partitions)))

(defn seek-end [^Consumer c topic topic-partitions]
  (doto c (.seekToEnd topic-partitions)) )

(defn position [^Consumer c topic position]
  (.position c (topic-partition topic partition)))

(defn offsets-for-time [^Consumer c timestamps-to-search]
  (map-to-clojure (.offsetsForTimes c timestamps-to-search)))