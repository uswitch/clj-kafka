(defproject uswitch/clj-kafka "0.4.0-SNAPSHOT"
  :description "Clojure wrapper for Kafka's Java API"
  :min-lein-version "2.5.0"
  :url "https://github.com/pingles/clj-kafka"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 ; [org.clojure/data.json "0.2.2"]
                 ; [org.clojure/tools.logging "0.3.1"]
                 ; [org.apache.kafka/kafka_2.10 "0.8.2.1"]
                 [org.apache.kafka/kafka-clients "1.0.1"]
                 [org.apache.kafka/kafka-streams "1.0.1"]
                 ; [zookeeper-clj "0.9.3"]
                 
                 ]
  :exclusions [javax.mail/mail
               javax.jms/jms
               com.sun.jdmk/jmxtools
               com.sun.jmx/jmxri
               jline/jline]
  ; :plugins [[lein-expectations "0.0.8"]
  ;           [codox "0.8.12"]]
  ; :codox {:src-dir-uri "http://github.com/pingles/clj-kafka/blob/master/"
  ;         :src-linenum-anchor-prefix "L"
  ;         :defaults {:doc/format :markdown}}
  :profiles {:dev {:resource-paths ["dev-resources"]
                   :dependencies [[commons-io/commons-io "2.4"]
                                  [expectations "1.4.45"]]}}
  )
