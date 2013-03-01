package org.vaadin.tepi.listbuilder.widgetset.client.ui;

import org.vaadin.tepi.listbuilder.ListBuilder;

import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.DirectionalManagedLayout;
import com.vaadin.client.Paintable;
import com.vaadin.client.UIDL;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

@Connect(ListBuilder.class)
public class ListBuilderConnector extends AbstractComponentConnector implements
        DirectionalManagedLayout, Paintable {

    @SuppressWarnings("deprecation")
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        // Captions are updated before super call to ensure the widths are set
        // correctly
        if (isRealUpdate(uidl)) {
            getWidget().updateCaptions(uidl);
            getLayoutManager().setNeedsHorizontalLayout(this);
        }

        // Save details
        getWidget().client = client;
        getWidget().id = uidl.getId();

        if (!isRealUpdate(uidl)) {
            return;
        }

        getWidget().selectedKeys.clear();
        String[] selected = uidl.getStringArrayVariable("orderedselection");
        for (int i = 0; i < selected.length; i++) {
            getWidget().selectedKeys.add(selected[i]);
        }

        getWidget().readonly = uidl.getBooleanAttribute("readonly");
        getWidget().disabled = uidl.getBooleanAttribute("disabled");
        // getWidget().immediate = uidl.getBooleanAttribute("immediate");

        if (uidl.hasAttribute("cols")) {
            getWidget().cols = uidl.getIntAttribute("cols");
        }
        if (uidl.hasAttribute("rows")) {
            getWidget().rows = uidl.getIntAttribute("rows");
        }

        final UIDL ops = uidl.getChildUIDL(0);

        if (getWidget().getColumns() > 0) {
            getWidget().container.setWidth(getWidget().getColumns() + "em");
        }

        getWidget().buildOptions(ops);

        getWidget().setTabIndex(
                uidl.hasAttribute("tabindex") ? uidl
                        .getIntAttribute("tabindex") : 0);
        getWidget().fixButtonStates();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);
        getWidget().immediate = getState().immediate;
    }

    @Override
    protected void init() {
        getLayoutManager().registerDependency(this,
                getWidget().captionWrapper.getElement());
    }

    @Override
    public void onUnregister() {
        getLayoutManager().unregisterDependency(this,
                getWidget().captionWrapper.getElement());
    }

    @Override
    public VListBuilder getWidget() {
        return (VListBuilder) super.getWidget();
    }

    public void layoutVertically() {
        if (isUndefinedHeight()) {
            getWidget().clearInternalHeights();
        } else {
            getWidget().setInternalHeights();
        }
    }

    public void layoutHorizontally() {
        if (isUndefinedWidth()) {
            getWidget().clearInternalWidths();
        } else {
            getWidget().setInternalWidths();
        }
    }
}