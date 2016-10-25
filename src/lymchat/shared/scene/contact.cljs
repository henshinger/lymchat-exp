(ns lymchat.shared.scene.contact
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [lymchat.styles :refer [styles pl-style]]
            [lymchat.shared.ui :refer [text view touchable-highlight list-view react-native button icon image input touchable-opacity material-icon-button dimensions] :as ui]
            [lymchat.photo :refer [offline-avatar-cp]]
            [lymchat.util :as util]))

(defn row-cp
  [row]
  (let [{:keys [id name avatar]} (js->clj row :keywordize-keys true)]
    [touchable-opacity {:style {:flex 1
                                :padding 10}
                        :on-press #(do
                                     (dispatch [:nav/pop])
                                     (dispatch [:nav/root-push {:key :conversation
                                                                :title name}])
                                     (dispatch [:load-conversation-messages id]))
                        :underlay-color "#eee"}

     [view {:key id
            :style {:flex-direction "row"}}
      [offline-avatar-cp
       id
       avatar
       {:height 40
        :width 40
        :border-radius 4}]

      [text {:style {:margin-left 10
                     :font-size 14}}
       name]]]))

(defn filter-contacts
  [pattern contacts]
  (if (and pattern (not (empty? contacts)))
    (filter #(re-find (js/RegExp. pattern "i") (:name %)) contacts)
    contacts))

(defn original-contacts-cp
  []
  (let [contacts (subscribe [:contacts])
        visible-height (r/atom (.-height (.get dimensions "window")))
        current-input (subscribe [:contact-search-input])
        ds (.-DataSource (.-ListView react-native))
        list-view-ds (new ds #js {:rowHasChanged #(not= %1 %2)})]
    [view (pl-style :contacts)
     [view {:style {:flex 1
                    :background-color "rgba(255,255,255,0.8)"
                    :height (- @visible-height 10)
                    :top 10}}
      [view {:style {:flex-direction "row"
                     :margin-top -12}}
       [material-icon-button {:name "arrow-back"
                              :on-press (fn []
                                          (util/show-statusbar)
                                          (dispatch [:nav/pop]))
                              :size 40
                              :background-color "transparent"
                              :color "rgba(0,0,0,0.7)"}]

       [input {:style (:no-border-input styles)
               :auto-focus true
               :auto-correct true
               :clear-button-mode "always"
               :on-change-text (fn [value] (dispatch [:reset-contact-search-input value]))
               :placeholder "Direct Message"}]]
      (if-not (seq @contacts)
        [view {:style {:flex 1
                       :top 80
                       :align-items "center"}}
         [text {:style {:font-size 20
                        :font-family "indie-flower"}}
          "No contacts yet."]]
        [list-view {:keyboardShouldPersistTaps true
                    :enableEmptySections true
                    :style {:padding-left 5}
                    :automaticallyAdjustContentInsets false
                    :dataSource (.cloneWithRows list-view-ds (clj->js (filter-contacts @current-input @contacts)))
                    :renderRow (fn [row] (r/as-element (row-cp row)))
                    :renderSeparator (fn [section-id row-id]
                                       (r/as-element
                                        [view {:key (str section-id "-" row-id)
                                               :style {:height 1
                                                       :background-color "#ddd"}}]))}])]]))

(defn contacts-cp
  [props]
  (util/wrap-route original-contacts-cp
                   {:navigationBar {:visible false
                                    :title "Contacts"}}))
