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

;; Zeros out an array or an atom
(defmacro zero! [x]
  `(let [v# ~x]
     (if (instance? clojure.lang.Atom v#)
       (reset! v# 0)
       (java.util.Arrays/fill v# (byte 0)))))

(defn u8 [x] (bit-and x 0xff))

(defn helper-imm16 []
  (let [pc @vm-program-counter
        x (bit-or
           (bit-shift-left
            (u8 (aget vm-memory pc)) 8)
           (u8 (aget vm-memory (inc pc))))]
    (swap! vm-program-counter + 2)
    x))

;; Pushes an 8-bit value onto the stack
(defn helper-push8! [stack ptr x]
  (aset-byte stack @ptr (u8 x))
  (swap! ptr inc))

;; Pushes a 16-bit value onto the stack
(defn helper-push16! [stack ptr x]
  (helper-push8! stack ptr (u8 (bit-shift-right x 8)))
  (helper-push8! stack ptr (u8 x)))

;; Pops an 8-bit value from the stack
(defn helper-pop8! [stack ptr]
  (swap! ptr dec)
  (u8 (aget stack @ptr)))

;; Pops a 16-bit value from the stack
(defn helper-pop16! [stack ptr]
  (bit-or (bit-shift-left (helper-pop8! stack ptr) 8)
          (helper-pop! stack ptr)))

;; BRK: Break
;; Halts the processor
(defn vm-brk []
  (reset! vm-should-run false))

;; JCI: Jump Conditional Immediate
;; Checks if the next value in the stack is 0
;; If yes: nothing; If no: Jumps to that address
(defn vm-jci []
  (if-not (= 0 (helper-imm16))
    (reset! vm-program-counter (helper-imm16))))

;; JMI: Jump Immediate
;; Jumps to the next address in memory with no
;; checks whatsoever
(defn vm-jmi []
  (reset! vm-program-counter (helper-imm16)))

;; JSI: Jump Stash Return Immediate
;; Jumps to an address and notes the address 
;; on the return stack
(defn vm-jsi []
  (let [target-addr (helper-imm16)
        return-addr @vm-program-counter]
    (helper-push16! vm-stack-return vm-stack-return-ptr return-addr)
    (reset! vm-program-counter target-addr)))

;; INC: Increment
;; Increments the items on top of the stack by 1
(defn vm-inc []
  (let [ptr @vm-stack-working-ptr
        curr-val (u8 (aget vm-stack-working ptr))
        new-val (u8 (inc curr-val))]
    (aset-byte vm-stack-working ptr new-val)))

;; Reads an opcode and executes the according function
(defn vm-opcode-eval [opcode]
  (cond
    (= opcode 0x00)(vm-brk) ; BRK
    (= opcode 0x20)(vm-jci) ; JCI
    (= opcode 0x40)(vm-jmi) ; JMI
    (= opcode 0x60)(vm-jsi) ; JSI
    :else
    (println "Unknown opcode:" opcode)))

;; Reboots/Resets the VM
(defn vm-reset! []
  (zero! vm-memory)
  (zero! vm-devices)
  (zero! vm-stack-working)
  (zero! vm-stack-return)
  (zero! vm-stack-working-ptr)
  (zero! vm-stack-return-ptr)
  (zero! vm-should-run)
  (zero! vm-program-counter))

;; Steps through the memory by incrementing
;; the PC and gets the next opcode
(defn vm-step []
  (let [pc @vm-program-counter
        opcode (u8 (aget vm-memory pc))]
    (swap! vm-program-counter inc)
    (vm-opcode-eval opcode)))
