package org.vaadin.tepi.listbuilder.widgetset.client.ui;

/* 
 * Copyright 2010 IT Mill Ltd. [original TwinColSelect]
 * 
 * Modified by Teppo Kurki
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.HasDoubleClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.Focusable;
import com.vaadin.client.UIDL;
import com.vaadin.client.Util;
import com.vaadin.client.ui.Field;
import com.vaadin.client.ui.SubPartAware;
import com.vaadin.client.ui.VButton;

public class VListBuilder extends Composite implements Field, ClickHandler,
        ChangeHandler, Focusable, KeyDownHandler, DoubleClickHandler,
        SubPartAware {

    private static final String CLASSNAME = "v-listbuilder";

    ApplicationConnection client;
    String id;

    ArrayList<String> selectedKeys = new ArrayList<String>();
    private ArrayList<String> originalOrder = new ArrayList<String>();

    boolean immediate;
    boolean disabled;
    boolean readonly;

    int cols = 0;
    int rows = 0;
    private static final int VISIBLE_COUNT = 10;
    private static final int DEFAULT_COLUMN_COUNT = 10;

    final Panel container;

    private boolean widthSet = false;
    private boolean moving = false;

    public static final String ATTRIBUTE_LEFT_CAPTION = "lc";
    public static final String ATTRIBUTE_RIGHT_CAPTION = "rc";
    private String leftColumnCaptionStyle;
    private String rightColumnCaptionStyle;

    private float buttonWidthEm = 2;

    private final DoubleClickListBox options;
    private final DoubleClickListBox selections;
    FlowPanel captionWrapper;
    private HTML optionsCaption = null;
    private HTML selectionsCaption = null;
    private final VCustomButton add;
    private final VCustomButton remove;
    private final FlowPanel buttons;
    private final VCustomButton up;
    private final VCustomButton down;
    private final FlowPanel moveButtons;

    public VListBuilder() {
        container = new FlowPanel();
        initWidget(container);
        container.setStyleName(CLASSNAME);
        immediate = false;

        /* Create ListBoxes */
        options = new DoubleClickListBox(true);
        options.addClickHandler(this);
        options.addDoubleClickHandler(this);
        options.setVisibleItemCount(VISIBLE_COUNT);
        options.setStyleName(CLASSNAME + "-options");
        selections = new DoubleClickListBox(true);
        selections.addClickHandler(this);
        selections.addDoubleClickHandler(this);
        selections.setVisibleItemCount(VISIBLE_COUNT);
        selections.setStyleName(CLASSNAME + "-selections");

        /* Create add/remove buttons and their container */
        buttons = new FlowPanel();
        buttons.setStyleName(CLASSNAME + "-buttons");
        add = new VCustomButton();
        add.addStyleName(CLASSNAME + "-button-add");
        add.setText(" ");
        add.addClickHandler(this);
        remove = new VCustomButton();
        remove.addStyleName(CLASSNAME + "-button-remove");
        remove.setText(" ");
        remove.addClickHandler(this);
        final HTML br = new HTML("<span/>");
        br.setStyleName(CLASSNAME + "-deco");
        buttons.add(add);
        buttons.add(br);
        buttons.add(remove);

        /* Create up/down buttons and their container */
        moveButtons = new FlowPanel();
        moveButtons.setStyleName(CLASSNAME + "-buttons");
        up = new VCustomButton();
        up.addStyleName(CLASSNAME + "-button-up");
        up.setText(" ");
        up.addClickHandler(this);
        down = new VCustomButton();
        down.addStyleName(CLASSNAME + "-button-down");
        down.setText(" ");
        down.addClickHandler(this);
        final HTML br2 = new HTML("<span/>");
        br2.setStyleName(CLASSNAME + "-deco");
        moveButtons.add(up);
        moveButtons.add(br2);
        moveButtons.add(down);

        /* Create ListBox captions */
        captionWrapper = new FlowPanel();
        container.add(captionWrapper);
        captionWrapper.getElement().getStyle().setOverflow(Overflow.HIDDEN);
        // Hide until there actually is a caption to prevent IE from rendering
        // extra empty space
        captionWrapper.setVisible(false);

        /* Add items to container */
        container.add(options);
        container.add(buttons);
        container.add(selections);
        container.add(moveButtons);

        /* Add Handlers */
        options.addKeyDownHandler(this);
        options.addChangeHandler(this);
        selections.addKeyDownHandler(this);
        selections.addChangeHandler(this);
    }

    public HTML getOptionsCaption() {
        if (optionsCaption == null) {
            optionsCaption = new HTML();
            optionsCaption.getElement().getStyle()
                    .setFloat(com.google.gwt.dom.client.Style.Float.LEFT);
            captionWrapper.add(optionsCaption);
        }
        return optionsCaption;
    }

    public HTML getSelectionsCaption() {
        if (selectionsCaption == null) {
            selectionsCaption = new HTML();
            selectionsCaption.getElement().getStyle()
                    .setFloat(com.google.gwt.dom.client.Style.Float.RIGHT);
            captionWrapper.add(selectionsCaption);
        }
        return selectionsCaption;
    }

    void updateCaptions(UIDL uidl) {
        String leftCaption = (uidl.hasAttribute(ATTRIBUTE_LEFT_CAPTION) ? uidl
                .getStringAttribute(ATTRIBUTE_LEFT_CAPTION) : null);
        String rightCaption = (uidl.hasAttribute(ATTRIBUTE_RIGHT_CAPTION) ? uidl
                .getStringAttribute(ATTRIBUTE_RIGHT_CAPTION) : null);

        /* Column caption styles */
        if (uidl.hasAttribute("leftColumnCaptionStyle")) {
            leftColumnCaptionStyle = uidl
                    .getStringAttribute("leftColumnCaptionStyle");
        } else {
            leftColumnCaptionStyle = null;
        }
        if (uidl.hasAttribute("rightColumnCaptionStyle")) {
            rightColumnCaptionStyle = uidl
                    .getStringAttribute("rightColumnCaptionStyle");
        } else {
            rightColumnCaptionStyle = null;
        }

        boolean hasCaptions = (leftCaption != null || rightCaption != null);

        if (leftCaption == null) {
            removeOptionsCaption();
        } else {
            getOptionsCaption().setText(leftCaption);
            if (leftColumnCaptionStyle != null) {
                getOptionsCaption().setStyleName(leftColumnCaptionStyle);
                getOptionsCaption().addStyleName(CLASSNAME + "-caption-left");
            } else {
                getOptionsCaption().setStyleName(CLASSNAME + "-caption-left");
            }
        }

        if (rightCaption == null) {
            removeSelectionsCaption();
        } else {
            getSelectionsCaption().setText(rightCaption);
            if (rightColumnCaptionStyle != null) {
                getSelectionsCaption().setStyleName(rightColumnCaptionStyle);
                getSelectionsCaption().addStyleName(
                        CLASSNAME + "-caption-right");
            } else {
                getSelectionsCaption().setStyleName(
                        CLASSNAME + "-caption-right");
            }
        }

        captionWrapper.setVisible(hasCaptions);
    }

    void buildOptions(UIDL uidl) {
        final boolean enabled = !isDisabled() && !isReadonly();
        options.setEnabled(enabled);
        selections.setEnabled(enabled);
        add.setEnabled(enabled);
        remove.setEnabled(enabled);
        options.clear();
        selections.clear();
        originalOrder.clear();

        HashMap<String, String> capts = new HashMap<String, String>();
        for (final Iterator<?> i = uidl.getChildIterator(); i.hasNext();) {
            final UIDL optionUidl = (UIDL) i.next();
            if (optionUidl.hasAttribute("selected")) {
                capts.put(optionUidl.getStringAttribute("key"),
                        optionUidl.getStringAttribute("caption"));
            } else {
                options.addItem(optionUidl.getStringAttribute("caption"),
                        optionUidl.getStringAttribute("key"));
            }
            originalOrder.add(optionUidl.getStringAttribute("key"));
        }
        for (String key : selectedKeys) {
            selections.addItem(capts.get(key), key);
        }

        int cols = -1;
        if (getColumns() > 0) {
            cols = getColumns();
        } else if (!widthSet) {
            cols = DEFAULT_COLUMN_COUNT;
        }

        if (cols >= 0) {
            String colWidth = cols + "em";
            String containerWidth = (2 * cols + 2 * buttonWidthEm + 0.5) + "em";
            // Caption wrapper width == optionsSelect + buttons +
            // selectionsSelect
            String captionWrapperWidth = (2 * cols + buttonWidthEm) + "em";

            options.setWidth(colWidth);
            if (optionsCaption != null) {
                optionsCaption.setWidth(Util.getRequiredWidth(options) + "px");
            }
            selections.setWidth(colWidth);
            if (selectionsCaption != null) {
                selectionsCaption.setWidth(Util.getRequiredWidth(selections)
                        + "px");
            }
            buttons.setWidth(String.valueOf(buttonWidthEm) + "em");
            moveButtons.setWidth(String.valueOf(buttonWidthEm) + "em");
            container.setWidth(containerWidth);
            captionWrapper.setWidth(captionWrapperWidth);
        }
        if (getRows() > 0) {
            options.setVisibleItemCount(getRows());
            selections.setVisibleItemCount(getRows());
        }
    }

    private void removeOptionsCaption() {
        if (optionsCaption == null) {
            return;
        }
        if (optionsCaption.getParent() != null) {
            captionWrapper.remove(optionsCaption);
        }
        optionsCaption = null;
    }

    private void removeSelectionsCaption() {
        if (selectionsCaption == null) {
            return;
        }
        if (selectionsCaption.getParent() != null) {
            captionWrapper.remove(selectionsCaption);
        }
        selectionsCaption = null;
    }

    private String[] getSelectedItems() {
        final ArrayList<String> selectedItemKeys = new ArrayList<String>();
        for (int i = 0; i < selections.getItemCount(); i++) {
            selectedItemKeys.add(selections.getValue(i));
        }
        return selectedItemKeys.toArray(new String[selectedItemKeys.size()]);
    }

    private boolean[] getItemsToAdd() {
        final boolean[] selectedIndexes = new boolean[options.getItemCount()];
        for (int i = 0; i < options.getItemCount(); i++) {
            if (options.isItemSelected(i)) {
                selectedIndexes[i] = true;
            } else {
                selectedIndexes[i] = false;
            }
        }
        return selectedIndexes;
    }

    private boolean[] getItemsToRemove() {
        final boolean[] selectedIndexes = new boolean[selections.getItemCount()];
        for (int i = 0; i < selections.getItemCount(); i++) {
            if (selections.isItemSelected(i)) {
                selectedIndexes[i] = true;
            } else {
                selectedIndexes[i] = false;
            }
        }
        return selectedIndexes;
    }

    private void addItem() {
        final boolean[] sel = getItemsToAdd();
        clearSelections(selections, false);
        for (int i = 0; i < sel.length; i++) {
            if (sel[i]) {
                final int optionIndex = i
                        - (sel.length - options.getItemCount());
                selectedKeys.add(options.getValue(optionIndex));
                // Move selection to another column
                final String text = options.getItemText(optionIndex);
                final String value = options.getValue(optionIndex);
                selections.addItem(text, value);
                selections.setItemSelected(selections.getItemCount() - 1, true);
                options.removeItem(optionIndex);
            }
        }
        selections.setFocus(true);
        fixButtonStates();
        updateChanges();
    }

    private void removeItem() {
        final boolean[] sel = getItemsToRemove();
        clearSelections(options, false);
        for (int i = 0; i < sel.length; i++) {
            if (sel[i]) {
                final int selectionIndex = i
                        - (sel.length - selections.getItemCount());
                selectedKeys.remove(selections.getValue(selectionIndex));
                // Move selection to another column
                final String text = selections.getItemText(selectionIndex);
                final String value = selections.getValue(selectionIndex);

                /* Preserve original order on left side */
                boolean inserted = false;
                int origIndex = originalOrder.indexOf(value);
                for (int j = 0; j < options.getItemCount(); j++) {
                    String curVal = options.getValue(j);
                    int curIndex = originalOrder.indexOf(curVal);
                    if (curIndex > origIndex) {
                        options.insertItem(text, value, j);
                        options.setItemSelected(j, true);
                        inserted = true;
                        break;
                    }
                }
                /* If order preservation failed, just insert at the end */
                if (!inserted) {
                    options.addItem(text, value);
                    options.setItemSelected(options.getItemCount() - 1, true);
                }
                selections.removeItem(selectionIndex);
            }
        }
        options.setFocus(true);
        fixButtonStates();
        updateChanges();
    }

    void clearInternalHeights() {
        selections.setHeight("");
        options.setHeight("");
    }

    void setInternalHeights() {
        int captionHeight = 0;
        int totalHeight = getOffsetHeight();

        if (optionsCaption != null) {
            captionHeight = Util.getRequiredHeight(optionsCaption);
        } else if (selectionsCaption != null) {
            captionHeight = Util.getRequiredHeight(selectionsCaption);
        }
        String selectHeight = (totalHeight - captionHeight) + "px";

        selections.setHeight(selectHeight);
        options.setHeight(selectHeight);
    }

    void clearInternalWidths() {
        int cols = -1;
        if (getColumns() > 0) {
            cols = getColumns();
        } else if (!widthSet) {
            cols = DEFAULT_COLUMN_COUNT;
        }

        if (cols >= 0) {
            String colWidth = cols + "em";
            String containerWidth = (2 * cols + 2 * buttonWidthEm + 0.5) + "em";
            // Caption wrapper width == optionsSelect + buttons +
            // selectionsSelect
            String captionWrapperWidth = (2 * cols + 2 * buttonWidthEm) + "em";

            options.setWidth(colWidth);
            if (optionsCaption != null) {
                optionsCaption.setWidth(Util.getRequiredWidth(options) + "px");
            }
            selections.setWidth(colWidth);
            if (selectionsCaption != null) {
                selectionsCaption.setWidth(Util.getRequiredWidth(selections)
                        + "px");
            }
            buttons.setWidth(String.valueOf(buttonWidthEm) + "em");
            moveButtons.setWidth(String.valueOf(buttonWidthEm) + "em");
            container.setWidth(containerWidth);
            captionWrapper.setWidth(captionWrapperWidth);
        }
    }

    void setInternalWidths() {
        DOM.setStyleAttribute(getElement(), "position", "relative");
        int bordersAndPaddings = Util.measureHorizontalPaddingAndBorder(
                buttons.getElement(), 0)
                + Util.measureHorizontalPaddingAndBorder(
                        moveButtons.getElement(), 0);
        int buttonWidth = Util.getRequiredWidth(buttons);
        int moveButtonWidth = Util.getRequiredWidth(moveButtons);
        int totalWidth = getOffsetWidth();

        int spaceForSelect = (totalWidth - buttonWidth - moveButtonWidth - bordersAndPaddings) / 2;

        options.setWidth(spaceForSelect + "px");
        if (optionsCaption != null) {
            optionsCaption.setWidth(spaceForSelect + "px");
        }

        selections.setWidth(spaceForSelect + "px");
        if (selectionsCaption != null) {
            selectionsCaption.setWidth(spaceForSelect + "px");
        }
        int captionWidth = totalWidth - moveButtonWidth;
        captionWrapper.setWidth(captionWidth + "px");
    }

    void setTabIndex(int tabIndex) {
        options.setTabIndex(tabIndex);
        selections.setTabIndex(tabIndex);
        add.setTabIndex(tabIndex);
        remove.setTabIndex(tabIndex);
        up.setTabIndex(tabIndex);
        down.setTabIndex(tabIndex);
    }

    public void onKeyDown(KeyDownEvent event) {
        int keycode = event.getNativeKeyCode();
        // Catch tab and move between select:s
        if (keycode == KeyCodes.KEY_TAB && event.getSource() == options
                && !event.isShiftKeyDown()) {
            // Prevent default behavior
            event.preventDefault();
            // Remove current selections
            clearSelections(options, true);
            // Focus selections
            selections.setFocus(true);
        }

        if (keycode == KeyCodes.KEY_TAB && event.isShiftKeyDown()
                && event.getSource() == selections) {
            // Prevent default behavior
            event.preventDefault();
            // Remove current selections
            clearSelections(selections, true);
            // Focus options
            options.setFocus(true);
        }

        if (keycode == getNavigationSelectKey()) {
            // Prevent default behavior
            event.preventDefault();
            // Decide which select the selection was made in
            if (event.getSource() == options) {
                // Prevents the selection to become a single selection when
                // using Enter key as the selection key (default)
                options.setFocus(false);
                addItem();
            } else if (event.getSource() == selections) {
                // Prevents the selection to become a single selection when
                // using Enter key as the selection key (default)
                selections.setFocus(false);
                removeItem();
            }
        }
    }

    public void onClick(ClickEvent event) {
        if (event.getSource() == add) {
            addItem();
        } else if (event.getSource() == remove) {
            removeItem();
        } else if (event.getSource() == up) {
            moveSelectedItems(true);
        } else if (event.getSource() == down) {
            moveSelectedItems(false);
        }
    }

    public void onDoubleClick(DoubleClickEvent event) {
        if (event.getSource() == options) {
            addItem();
        } else if (event.getSource() == selections) {
            removeItem();
        }
    }

    /**
     * Clear selections and update button states if selection is changed in
     * either ListBox
     */
    public void onChange(ChangeEvent event) {
        if (event.getSource() == options) {
            clearSelections(selections, true);
        } else if (event.getSource() == selections) {
            clearSelections(options, true);
        }
    }

    /**
     * Clears selections from a ListBox.
     * 
     * @param box
     *            ListBox to clear
     * @param fixButtons
     *            true to update button states also
     */
    private void clearSelections(ListBox box, boolean fixButtons) {
        for (int i = 0; i < box.getItemCount(); i++) {
            box.setItemSelected(i, false);
        }
        if (fixButtons) {
            fixButtonStates();
        }
    }

    /**
     * Updates button enable/disable states to reflect current selection.
     */
    void fixButtonStates() {
        boolean continuousSelection = isSelectionContinuous();
        boolean enableRemoveButton = isSelectionSelected();
        add.setReallyEnabled(isOptionSelected());
        remove.setReallyEnabled(enableRemoveButton);
        down.setReallyEnabled(!isLastSelected() && enableRemoveButton
                && continuousSelection);
        up.setReallyEnabled(!isFirstSelected() && enableRemoveButton
                && continuousSelection);
    }

    private boolean isSelectionSelected() {
        for (int i = 0; i < selections.getItemCount(); i++) {
            if (selections.isItemSelected(i)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOptionSelected() {
        for (int i = 0; i < options.getItemCount(); i++) {
            if (options.isItemSelected(i)) {
                return true;
            }
        }
        return false;
    }

    private boolean isFirstSelected() {
        if (selections.getItemCount() == 0) {
            return false;
        }
        return selections.isItemSelected(0);
    }

    private boolean isLastSelected() {
        if (selections.getItemCount() == 0) {
            return false;
        }
        return selections.isItemSelected(selections.getItemCount() - 1);
    }

    /**
     * Checks if the selected items in selections ListBox are form a continuous
     * selection.
     * 
     * @return true if selection is continuous
     */
    private boolean isSelectionContinuous() {
        boolean firstSelectedFound = false;
        boolean gapExists = false;
        boolean continuousSelection = true;
        for (int i = 0; i < selections.getItemCount(); i++) {
            if (selections.isItemSelected(i)) {
                if (firstSelectedFound && gapExists) {
                    continuousSelection = false;
                    break;
                }
                firstSelectedFound = true;
            } else {
                if (firstSelectedFound) {
                    gapExists = true;
                }
            }
        }
        return continuousSelection;
    }

    /**
     * Moves currently selected items in the selections ListBox. If the
     * selection is non-existent or invalid, nothing will be done.
     * 
     * @param up
     *            true to move items up, false to move items down
     */
    private void moveSelectedItems(boolean up) {
        /* Ensures that the previous move is completed */
        if (moving) {
            return;
        }
        /* Check that the selection exists and is continuous */
        if (!isSelectionSelected() || !isSelectionContinuous()) {
            return;
        }
        moving = true;
        final ArrayList<String> selectedItemKeys = new ArrayList<String>();
        int firstIndex = -1;
        int lastIndex = -1;
        /* Fetch items to be moved */
        for (int i = 0; i < selections.getItemCount(); i++) {
            if (selections.isItemSelected(i)) {
                if (firstIndex == -1) {
                    firstIndex = i;
                }
                lastIndex = i;
                selectedItemKeys.add(selections.getValue(i));
            }
        }
        /* If the items are already at the top/bottom, do nothing */
        if ((firstIndex < 1 && up)
                || ((lastIndex == -1 || lastIndex == selections.getItemCount() - 1) && !up)) {
            return;
        }
        final int movementLenght = lastIndex - firstIndex + 1;
        for (int i = firstIndex; i <= lastIndex; i++) {
            final int propertyIndex = up ? i : firstIndex;
            final int newIndex = up ? i - 1 : firstIndex + movementLenght + 1;
            final int indexToRemove = up ? i + 1 : firstIndex;
            final String text = selections.getItemText(propertyIndex);
            final String value = selections.getValue(propertyIndex);
            selections.insertItem(text, value, newIndex);
            selections.removeItem(indexToRemove);
        }
        updateChanges();

        /* Fix selected items */
        firstIndex = up ? firstIndex - 1 : firstIndex + 1;
        lastIndex = up ? lastIndex - 1 : lastIndex + 1;
        for (int i = firstIndex; i <= lastIndex; i++) {
            selections.setItemSelected(i, true);
        }

        fixButtonStates();
        moving = false;
    }

    /**
     * Updates selections to internal ordered data structure. Sends or queues
     * changes to server.
     */
    private void updateChanges() {
        selectedKeys.clear();
        String[] selected = getSelectedItems();
        for (int i = 0; i < selected.length; i++) {
            selectedKeys.add(selected[i]);
        }
        client.updateVariable(id, "selected",
                selectedKeys.toArray(new String[selectedKeys.size()]),
                isImmediate());
    }

    public void focus() {
        options.setFocus(true);
    }

    private int getNavigationSelectKey() {
        return KeyCodes.KEY_ENTER;
    }

    private boolean isImmediate() {
        return immediate;
    }

    private boolean isDisabled() {
        return disabled;
    }

    private boolean isReadonly() {
        return readonly;
    }

    int getColumns() {
        return cols;
    }

    private int getRows() {
        return rows;
    }

    private static final String SUBPART_OPTION_SELECT = "leftSelect";
    private static final String SUBPART_SELECTION_SELECT = "rightSelect";
    private static final String SUBPART_LEFT_CAPTION = "leftCaption";
    private static final String SUBPART_RIGHT_CAPTION = "rightCaption";
    private static final String SUBPART_ADD_BUTTON = "add";
    private static final String SUBPART_REMOVE_BUTTON = "remove";
    private static final String SUBPART_UP_BUTTON = "up";
    private static final String SUBPART_DOWN_BUTTON = "down";

    public Element getSubPartElement(String subPart) {
        if (SUBPART_OPTION_SELECT.equals(subPart)) {
            return options.getElement();
        } else if (SUBPART_SELECTION_SELECT.equals(subPart)) {
            return selections.getElement();
        } else if (optionsCaption != null
                && SUBPART_LEFT_CAPTION.equals(subPart)) {
            return optionsCaption.getElement();
        } else if (selectionsCaption != null
                && SUBPART_RIGHT_CAPTION.equals(subPart)) {
            return selectionsCaption.getElement();
        } else if (SUBPART_ADD_BUTTON.equals(subPart)) {
            return add.getElement();
        } else if (SUBPART_REMOVE_BUTTON.equals(subPart)) {
            return remove.getElement();
        } else if (SUBPART_UP_BUTTON.equals(subPart)) {
            return up.getElement();
        } else if (SUBPART_DOWN_BUTTON.equals(subPart)) {
            return down.getElement();
        }
        return null;
    }

    public String getSubPartName(Element subElement) {
        if (optionsCaption != null
                && optionsCaption.getElement().isOrHasChild(subElement)) {
            return SUBPART_LEFT_CAPTION;
        } else if (selectionsCaption != null
                && selectionsCaption.getElement().isOrHasChild(subElement)) {
            return SUBPART_RIGHT_CAPTION;
        } else if (options.getElement().isOrHasChild(subElement)) {
            return SUBPART_OPTION_SELECT;
        } else if (selections.getElement().isOrHasChild(subElement)) {
            return SUBPART_SELECTION_SELECT;
        } else if (add.getElement().isOrHasChild(subElement)) {
            return SUBPART_ADD_BUTTON;
        } else if (remove.getElement().isOrHasChild(subElement)) {
            return SUBPART_REMOVE_BUTTON;
        } else if (up.getElement().isOrHasChild(subElement)) {
            return SUBPART_UP_BUTTON;
        } else if (down.getElement().isOrHasChild(subElement)) {
            return SUBPART_DOWN_BUTTON;
        }
        return null;
    }

    /**
     * A ListBox which catches double clicks
     */
    private class DoubleClickListBox extends ListBox implements
            HasDoubleClickHandlers {
        public DoubleClickListBox(boolean isMultipleSelect) {
            super(isMultipleSelect);
        }

        @Override
        public HandlerRegistration addDoubleClickHandler(
                DoubleClickHandler handler) {
            return addDomHandler(handler, DoubleClickEvent.getType());
        }
    }

    /**
     * A custom extension of VButton, replacing the caption span with a DIV to
     * allow creating VButtons containing an image in client side code.
     */
    private class VCustomButton extends VButton {
        public VCustomButton() {
            super();
            /* Replace default caption element with a DIV */
            Element e = DOM.createDiv();
            e.setClassName("v-button-caption");
            wrapper.replaceChild(e, captionElement);
        }

        public void setReallyEnabled(boolean reallyEnabled) {
            setEnabled(reallyEnabled);
            setStyleName(ApplicationConnection.DISABLED_CLASSNAME,
                    !reallyEnabled);
        }
    }
}