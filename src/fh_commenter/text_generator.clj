(ns fh-commenter.text-generator
  (:require [clojure.string :as string]))

(def adjectives ["nice" "sweet" "fantastic" "incredible"])
(def nouns ["shot" "photo" "capture" "frame" "work" "moment" "scene"])
(def trash ["wow"])
(def signs ["." "!"])

(def gen-data
  { :adjective adjectives :noun nouns :sign signs :trash trash })

(def seps #{:name :trash})

(defn sep-with-comma? [item]
  (contains? seps item))

(def probabilities
  { :adjective 1.0 :name 0.66 :noun 0.75 :sign 0.9 :name-end 0.5 :trash 0.25 })

(defn probable? [thing] (< (rand) (probabilities thing)))

(defn map-probabilities [coll]
  (let [probs (map probable? coll)]
    (zipmap coll probs)))

(def common-components [:trash :name :adjective :noun :sign])
(defn name? [item] (= item :name))

(defn generate-scheme-probabilities [incl-name]
  (let [comps (if incl-name common-components (remove name? common-components))]
    (map-probabilities comps)))

(defn value-true [[key value]] (if value key))

(defn list-only-probable [probs]
  (->> probs
      (map value-true)
      (remove nil?)
       reverse))

(defn generate-scheme [incl-name]
  (-> incl-name
      generate-scheme-probabilities
      list-only-probable))

(defn random-item [item-type]
  (rand-nth (gen-data item-type)))

(defn item-value [item-type data]
  (if (= item-type :name)
    (data :name)
    (random-item item-type)))

(defn fill-item [item-type data]
  (let [item-value (item-value item-type data)]
    [item-type item-value]))

(defn prefill-scheme [scheme data]
  (map #(fill-item % data) scheme))

(def last-index (comp dec count))

(defn meta-mapper [idx [item value] last-idx]
  (let [first? (= idx 0)
        last? (= idx last-idx)]
    { :idx idx :first? first? :last? last? :item item :value value }))

(defn prefill-meta [prefill]
  (let [last-idx (last-index prefill)]
    (map-indexed #(meta-mapper %1 %2 last-idx) prefill)))

(def comma [:comma ","])

(defn punctuator [memo {:keys [first? last? item value] :as item-data}]
  (let [last-token (last memo)
        last-comma? (= (first last-token) :comma)
        last-not-comma? (not last-comma?)
        sep? (sep-with-comma? item)
        not-first? (not first?)
        not-last? (not last?)
        comma-pre? (and sep? not-first? last-not-comma?)
        comma-post? (and sep? not-last?)
        comma-pre (if comma-pre? comma)
        comma-post (if comma-post? comma)
        i [item value]
        v [comma-pre i comma-post]
        final-v (remove nil? v)]
    (concat memo final-v)))

(defn punctuate-prefill [prefill]
  (reduce punctuator [] prefill))

(defn joiner-meta [{:keys [first? last? item value]}]
  (let [sign? (= item :sign)
        comma? (= item :comma)
        any-sign? (or sign? comma?)
        not-sign? (not any-sign?)
        not-first? (not first?)
        not-last? (not last?)
        tr-value (if first? (string/capitalize value) value)
        pre (if (and not-first? not-sign?) " " "")
        post ""]
    { :value tr-value :pre pre :post post }))

(defn joiner [memo item-data]
  (let [{:keys [pre post value]} (joiner-meta item-data)]
    (str memo pre value post)))

(defn join-prefill [prefill]
  (reduce joiner "" prefill))

(defn generate-text [data]
  (-> (generate-scheme (:name data))
      (prefill-scheme data)
      prefill-meta
      punctuate-prefill
      prefill-meta
      join-prefill))
