(ns uxnclojure)

(def memory-size 65536)
(def device-size 256)
(def stack-size 256)

(def vm-memory (byte-array memory-size))
(def vm-devices (byte-array device-size))
(def vm-stack-working (byte-array stack-size))
(def vm-stack-return (byte-array stack-size))
(def vm-stack-working-ptr (atom 0))
(def vm-stack-return-ptr (atom 0))
(def vm-should-run (atom true))
(def vm-program-counter (atom 0))

(defn u8 [x] (bit-and x 0xff))

(defn helper-imm16 []
  (let [pc @vm-program-counter
        x (bit-or
           (bit-shift-left
            (u8 (aget vm-memory pc)) 8)
           (u8 (aget vm-memory (inc pc))))]
    (swap! vm-program-counter + 2)
    x))

(defn helper-push! [stack ptr x]
  (aset-byte stack @ptr (byte x))
  (swap! ptr inc))

(defn helper-pop! [stack ptr]
  (swap! ptr dec)
  (u8 (aget stack @ptr)))

(defn vm-brk []
  (reset! vm-should-run false))

(defn vm-jci []
  (reset! vm-program-counter (helper-imm16)))

(defn vm-opcode-eval [opcode]
  (cond
    (= opcode 0x00)(vm-brk) ; BRK
    (= opcode 0x20)(vm-jci) ; JCI
    :else
    (println "Unknown opcode:" opcode)))

(defn vm-step []
  (let [pc @vm-program-counter
        opcode (u8 (aget vm-memory pc))]
    (swap! vm-program-counter inc)
    (vm-opcode-eval opcode)))
