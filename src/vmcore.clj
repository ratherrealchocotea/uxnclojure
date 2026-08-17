(ns uxnclojure)

(def memory-size 65536)
(def device-size 256)
(def stack-size 256)

;; Stacks and registers
(def vm-memory (byte-array memory-size))
(def vm-devices (byte-array device-size))
(def vm-stack-working (byte-array stack-size))
(def vm-stack-return (byte-array stack-size))
(def vm-stack-working-ptr 0)
(def vm-stack-return-ptr 0)
(def vm-is-running true)
