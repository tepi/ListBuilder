package org.tepi.listbuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.tepi.listbuilder.client.VListBuilder;

import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.Property;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.server.Resource;
import com.vaadin.v7.ui.AbstractSelect;
import com.vaadin.ui.UI;

/**
 * Multiselect component with two lists: left side for available items and right
 * side for selected items.
 */
@SuppressWarnings("serial")
public class ListBuilder extends AbstractSelect {

    private int columns = 0;
    private int rows = 0;

    private String leftColumnCaption;
    private String rightColumnCaption;

    private ArrayList<String> orderedValue = new ArrayList<String>();
    private final ArrayList<String> leftColumnStyles = new ArrayList<String>();
    private final ArrayList<String> rightColumnStyles = new ArrayList<String>();

    public ListBuilder() {
        super();
        super.setMultiSelect(true);
    }

    public ListBuilder(String caption) {
        super(caption);
        super.setMultiSelect(true);
    }

    public ListBuilder(String caption, Container dataSource) {
        super(caption, dataSource);
        super.setMultiSelect(true);
    }

    /**
     * Paints the content of this component.
     * 
     * @param target
     *            the Paint Event.
     * @throws PaintException
     *             if the paint operation failed.
     */
    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        target.addAttribute("type", "twincol");
        // Adds the number of columns
        if (columns != 0) {
            target.addAttribute("cols", columns);
        }
        // Adds the number of rows
        if (rows != 0) {
            target.addAttribute("rows", rows);
        }

        // Right and left column captions and/or icons (if set)
        String lc = getLeftColumnCaption();
        String rc = getRightColumnCaption();
        if (lc != null) {
            target.addAttribute(VListBuilder.ATTRIBUTE_LEFT_CAPTION, lc);
        }
        if (rc != null) {
            target.addAttribute(VListBuilder.ATTRIBUTE_RIGHT_CAPTION, rc);
        }

        // Column caption styles
        if (leftColumnStyles.size() > 0) {
            target.addAttribute("leftColumnCaptionStyle",
                    getLeftColumnCaptionStyleName());
        }
        if (rightColumnStyles.size() > 0) {
            target.addAttribute("rightColumnCaptionStyle",
                    getRightColumnCaptionStyleName());
        }

        // Send ordered selections
        target.addVariable(this, "orderedselection",
                orderedValue.toArray(new String[] {}));

        // Paints field properties
        // The tab ordering number
        if (getTabIndex() != 0) {
            target.addAttribute("tabindex", getTabIndex());
        }

        // If the field is modified, but not committed, set modified attribute
        if (isModified()) {
            target.addAttribute("modified", true);
        }

        // Adds the required attribute
        if (!isReadOnly() && isRequired()) {
            target.addAttribute("required", true);
        }

        // Hide the error indicator if needed
        if (isRequired() && isEmpty() && getComponentError() == null
                && getErrorMessage() != null) {
            target.addAttribute("hideErrors", true);
        }

        // Paints select attributes
        if (isMultiSelect()) {
            target.addAttribute("selectmode", "multi");
        }
        if (isNewItemsAllowed()) {
            target.addAttribute("allownewitem", true);
        }
        if (isNullSelectionAllowed()) {
            target.addAttribute("nullselect", true);
            if (getNullSelectionItemId() != null) {
                target.addAttribute("nullselectitem", true);
            }
        }

        // Constructs selected keys array
        String[] selectedKeys;
        if (isMultiSelect()) {
            selectedKeys = new String[((List<?>) getValue()).size()];
        } else {
            selectedKeys = new String[(getValue() == null
                    && getNullSelectionItemId() == null ? 0 : 1)];
        }

        // ==
        // first remove all previous item/property listeners
        getCaptionChangeListener().clear();
        // Paints the options and create array of selected id keys

        target.startTag("options");
        int keyIndex = 0;
        // Support for external null selection item id
        final Collection<?> ids = getItemIds();
        if (isNullSelectionAllowed() && getNullSelectionItemId() != null
                && !ids.contains(getNullSelectionItemId())) {
            final Object id = getNullSelectionItemId();
            // Paints option
            target.startTag("so");
            paintItem(target, id);
            if (isSelected(id)) {
                selectedKeys[keyIndex++] = itemIdMapper.key(id);
            }
            target.endTag("so");
        }

        final Iterator<?> i = getItemIds().iterator();
        // Paints the available selection options from data source
        while (i.hasNext()) {
            // Gets the option attribute values
            final Object id = i.next();
            if (!isNullSelectionAllowed() && id != null
                    && id.equals(getNullSelectionItemId())) {
                // Remove item if it's the null selection item but null
                // selection is not allowed
                continue;
            }
            final String key = itemIdMapper.key(id);
            // add listener for each item, to cause repaint if an item changes
            getCaptionChangeListener().addNotifierForItem(id);
            target.startTag("so");
            paintItem(target, id);
            if (isSelected(id) && keyIndex < selectedKeys.length) {
                selectedKeys[keyIndex++] = key;
            }
            target.endTag("so");
        }
        target.endTag("options");
        // ==

        // Paint variables
        target.addVariable(this, "selected", selectedKeys);
        if (isNewItemsAllowed()) {
            target.addVariable(this, "newitem", "");
        }

    }

    @Override
    protected void paintItem(PaintTarget target, Object itemId)
            throws PaintException {
        final String key = itemIdMapper.key(itemId);
        final String caption = getItemCaption(itemId);
        final Resource icon = getItemIcon(itemId);
        if (icon != null) {
            target.addAttribute("icon", icon);
        }
        target.addAttribute("caption", caption);
        if (itemId != null && itemId.equals(getNullSelectionItemId())) {
            target.addAttribute("nullselection", true);
        }
        target.addAttribute("key", key);
        if (isSelected(itemId)) {
            target.addAttribute("selected", true);
        }
    }

    /**
     * Invoked when the value of a variable has changed.
     * 
     * @see com.vaadin.ui.AbstractComponent#changeVariables(java.lang.Object,
     *      java.util.Map)
     */
    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        // Selection change
        if (variables.containsKey("selected")) {
            final String[] ka = (String[]) variables.get("selected");

            // Converts the key-array to id-set
            final LinkedList<Object> s = new LinkedList<Object>();
            for (int i = 0; i < ka.length; i++) {
                final Object id = itemIdMapper.get(ka[i]);
                if (!isNullSelectionAllowed()
                        && (id == null || id == getNullSelectionItemId())) {
                    // skip empty selection if nullselection is not allowed
                    markAsDirty();
                } else if (id != null && containsId(id)) {
                    s.add(id);
                }
            }

            if (!isNullSelectionAllowed() && s.size() < 1) {
                // empty selection not allowed, keep old value
                markAsDirty();
                return;
            }
            /* Store ordered selections */
            orderedValue.clear();
            for (int i = 0; i < ka.length; i++) {
                orderedValue.add(ka[i]);
            }
            /* Must set internal value to null first to enforce ordering */
            setInternalValue(null);
            /* Sets the actual value */
            setValue(s, true);
            /* Mark connector as clean again */
            UI uI = getUI();
            if (uI != null) {
                uI.getConnectorTracker().markClean(this);
            }
        }
    }

    /**
     * Sets the number of columns in the editor. If the number of columns is set
     * 0, the actual number of displayed columns is determined implicitly by the
     * adapter.
     * <p>
     * The number of columns overrides the value set by setWidth. Only if
     * columns are set to 0 (default) the width set using
     * {@link #setWidth(float, int)} or {@link #setWidth(String)} is used.
     * 
     * @param columns
     *            the number of columns to set.
     */
    public void setColumns(int columns) {
        if (columns < 0) {
            columns = 0;
        }
        if (this.columns != columns) {
            this.columns = columns;
            markAsDirty();
        }
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    /**
     * Sets the number of rows in the editor. If the number of rows is set to 0,
     * the actual number of displayed rows is determined implicitly by the
     * adapter.
     * <p>
     * If a height if set (using {@link #setHeight(String)} or
     * {@link #setHeight(float, int)}) it overrides the number of rows. Leave
     * the height undefined to use this method. This is the opposite of how
     * {@link #setColumns(int)} work.
     * 
     * 
     * @param rows
     *            the number of rows to set.
     */
    public void setRows(int rows) {
        if (rows < 0) {
            rows = 0;
        }
        if (this.rows != rows) {
            this.rows = rows;
            markAsDirty();
        }
    }

    /**
     * @param caption
     * @param options
     */
    public ListBuilder(String caption, Collection<?> options) {
        super(caption, options);
        setMultiSelect(true);
    }

    /**
     * Sets the text shown above the right column.
     * 
     * @param caption
     *            The text to show
     */
    public void setRightColumnCaption(String rightColumnCaption) {
        this.rightColumnCaption = rightColumnCaption;
        markAsDirty();
    }

    /**
     * Returns the text shown above the right column.
     * 
     * @return The text shown or null if not set.
     */
    public String getRightColumnCaption() {
        return rightColumnCaption;
    }

    /**
     * Sets the text shown above the left column.
     * 
     * @param caption
     *            The text to show
     */
    public void setLeftColumnCaption(String leftColumnCaption) {
        this.leftColumnCaption = leftColumnCaption;
        markAsDirty();
    }

    /**
     * Returns the text shown above the left column.
     * 
     * @return The text shown or null if not set.
     */
    public String getLeftColumnCaption() {
        return leftColumnCaption;
    }

    /**
     * Selects an item.
     * 
     * @param itemId
     *            the identifier of Item to be selected.
     */
    @Override
    public void select(Object itemId) {
        if (itemId != null && items.containsId(itemId)
                && !((List<?>) getValue()).contains(itemId)) {
            final List<Object> s = new ArrayList<Object>((List<?>) getValue());
            s.add(itemId);
            setValue(s);
        }
    }

    /**
     * Unselects an item.
     * 
     * @param itemId
     *            the identifier of the Item to be unselected.
     */
    @Override
    public void unselect(Object itemId) {
        if (isSelected(itemId)) {
            final List<Object> s = new ArrayList<Object>((List<?>) getValue());
            s.remove(itemId);
            setValue(s);
        }
    }

    private ArrayList<Object> getOrderedValue() {
        ArrayList<Object> retVal = new ArrayList<Object>();
        for (String key : orderedValue) {
            retVal.add(itemIdMapper.get(key));
        }
        return retVal;
    }

    /**
     * Sets the visible value of the property.
     * 
     * <p>
     * The value of the select is a Collection of selected item keys.
     * </p>
     * 
     * ListBuilder note: Pass an ordered collection to this method to select the
     * items in the correct order.
     * 
     * @param newValue
     *            the New collection of selected items.
     * @see com.vaadin.ui.AbstractField#setValue(java.lang.Object)
     */
    @Override
    @SuppressWarnings(value = "rawtypes")
    public void setValue(Object newValue) throws Property.ReadOnlyException {
        if (orderedValue == null) {
            orderedValue = new ArrayList<String>();
        }
        if (newValue == null) {
            orderedValue.clear();
            markAsDirty();
        } else {
            if (newValue instanceof Collection) {
                orderedValue.clear();
                Iterator i = ((Collection) newValue).iterator();
                while (i.hasNext()) {
                    Object o = i.next();
                    if (containsId(o)) {
                        orderedValue.add(itemIdMapper.key(o));
                    }
                }
                markAsDirty();
            }
        }
    }

    /**
     * Gets the current list of selected ids.
     * 
     * @see com.vaadin.ui.AbstractField#getValue()
     */
    @Override
    public Object getValue() {
        return getInternalValue();
    }

    @Override
    protected Object getInternalValue() {
        return orderedValue == null ? new ArrayList<Object>() : Collections
                .unmodifiableList(getOrderedValue());
    }

    public String getLeftColumnCaptionStyleName() {
        String s = "";
        if (leftColumnStyles != null) {
            for (final Iterator<String> it = leftColumnStyles.iterator(); it
                    .hasNext();) {
                s += it.next();
                if (it.hasNext()) {
                    s += " ";
                }
            }
        }
        return s;
    }

    public void setLeftColumnCaptionStyleName(String style) {
        leftColumnStyles.clear();
        if (style != null && !"".equals(style)) {
            leftColumnStyles.add(style);
            leftColumnStyles.add("v-listbuilder-caption-left-" + style);
        }
        markAsDirty();
    }

    public void addLeftColumnCaptionStyleName(String style) {
        if (style == null || "".equals(style)) {
            return;
        }
        if (!leftColumnStyles.contains(style)) {
            leftColumnStyles.add(style);
            leftColumnStyles.add("v-listbuilder-caption-left-" + style);
            markAsDirty();
        }
    }

    public void removeLeftColumnCaptionStyleName(String style) {
        leftColumnStyles.remove(style);
        leftColumnStyles.remove("v-listbuilder-caption-left-" + style);
        markAsDirty();
    }

    public String getRightColumnCaptionStyleName() {
        String s = "";
        if (rightColumnStyles != null) {
            for (final Iterator<String> it = rightColumnStyles.iterator(); it
                    .hasNext();) {
                s += it.next();
                if (it.hasNext()) {
                    s += " ";
                }
            }
        }
        return s;
    }

    public void setRightColumnCaptionStyleName(String style) {
        rightColumnStyles.clear();
        if (style != null && !"".equals(style)) {
            rightColumnStyles.add(style);
            rightColumnStyles.add("v-listbuilder-caption-right-" + style);
        }
        markAsDirty();
    }

    public void addRightColumnCaptionStyleName(String style) {
        if (style == null || "".equals(style)) {
            return;
        }
        if (!rightColumnStyles.contains(style)) {
            rightColumnStyles.add(style);
            rightColumnStyles.add("v-listbuilder-caption-right-" + style);
            markAsDirty();
        }
    }

    public void removeRightColumnCaptionStyleName(String style) {
        rightColumnStyles.remove(style);
        rightColumnStyles.remove("v-listbuilder-caption-right-" + style);
        markAsDirty();
    }

    @Override
    public boolean isMultiSelect() {
        return true;
    }

    @Override
    public void setMultiSelect(boolean multiSelect) {
        if (!multiSelect)
            throw new UnsupportedOperationException(
                    "ListBuilder is always in multiselect mode. "
                            + "Use TwinColSelect if single-selection is required.");
    }

    @Override
    public Class<?> getType() {
        return List.class;
    }

    @Override
    public boolean isSelected(Object itemId) {
        if (itemId == null) {
            return false;
        }
        return ((List<?>) getValue()).contains(itemId);
    }

}