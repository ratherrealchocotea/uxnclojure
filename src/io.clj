;; Handles file I/O
(ns uxnclojure
  (:require [clojure.java.io :as io]))

(defn rom2mem [path]
  (try
    (let [bytes (-> path io/file .ioPath java.nio.file.Files/readAllBytes)]
      ;; 0x100 is the offset where ROMs are loaded in UXN
      (System/arraycopy bytes 0 vm-memory 0x100
                        (min (alength bytes) (- memory-size 0x100)))
      true)
    (catch Exception _ false)))
