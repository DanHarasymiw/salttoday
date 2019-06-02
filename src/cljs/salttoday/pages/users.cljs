(ns salttoday.pages.users
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [clojure.core.async :as a]
            [cljs-http.client :as http]
            [salttoday.common :refer [display-comment]]
            [salttoday.pages.common :refer [get-selected-value make-navbar make-content make-right-offset]]))

(defn get-users [state]
  (go (let [options {:query-params {:offset    (:offset @state)
                                    :amount    (:amount @state)
                                    :sort-type (:sort-type @state)
                                    :days (:days @state)}
                     :with-credentials? false
                     :headers {}}
            {:keys [status headers body error] :as resp} (a/<! (http/get "/api/v1/users" options))]
        (swap! state assoc :users body))))

(defn filter-by-sort
  [event state]
  (let [sort (get-selected-value event state)]
    (swap! state assoc :sort-type sort)
    (get-users state)))

(defn filter-by-days
  [event state]
  (let [sort (get-selected-value event state)]
    (swap! state assoc :days sort)
    (get-users state)))

(defn display-user
  [user]
  [:div.row
   [:div.row.user-name-row
    [:span
     (:name user)]]
   [:div.row.user-stats-row
    [:span.positive
     (:upvotes user)
     " "
     [:i.fas.fa-thumbs-up]]
    [:span.negative
     (:downvotes user)
     " "
     [:i.fas.fa-thumbs-down]]]])

(defn leaderboard-content [state]
  (list [:div.row.justify-center.header-wrapper.sort-bar
         [:div.column.sort-item.sort-dropdown
          [:select {:on-change (fn [e]
                                 (filter-by-sort e state))}
           [:option {:value "score"} "Top"]
           [:option {:value "downvotes"} "Dislikes"]
           [:option {:value "upvotes"} "Likes"]]]
         [:div.column.sort-dropdown
          [:select {:on-change (fn [e]
                                 (filter-by-days e state))}
           [:option {:value 1} "Past Day"]
           [:option {:value 7} "Past Week"]
           [:option {:value 30} "Past Month"]
           [:option {:value 365} "Past Year"]
           [:option {:value 0} "Of All Time"]]]]
        [:div.column.justify-center.comments-wrapper
         (for [user (:users @state)]
           (display-user user))]))

(defn users-page []
  (let [state (r/atom {:users []
                       :offset 0
                       :amount 10
                       :days "0"})]
    (get-users state)
    (fn []
      [:div.page-wrapper
       (make-navbar :users)
       (make-content :users (leaderboard-content state))
       (make-right-offset)])))
