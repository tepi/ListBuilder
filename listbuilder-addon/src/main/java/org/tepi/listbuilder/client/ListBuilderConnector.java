package org.tepi.listbuilder.client;

import java.util.List;

import org.tepi.listbuilder.ListBuilder;

import com.vaadin.client.DirectionalManagedLayout;
import com.vaadin.client.annotations.OnStateChange;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.connectors.AbstractMultiSelectConnector;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("serial")
@Connect(ListBuilder.class)
public class ListBuilderConnector extends AbstractMultiSelectConnector
		implements DirectionalManagedLayout, OrderedValueChangeListener {

	private ListBuilderServerRpc rpc;

	public ListBuilderConnector() {
		rpc = RpcProxy.create(ListBuilderServerRpc.class, this);
		getWidget().setOrderedValueChangeListener(this);
	}

	@Override
	protected void init() {
		super.init();
		getLayoutManager().registerDependency(this, getWidget().getCaptionWrapper().getElement());
	}

	@Override
	public void onUnregister() {
		getLayoutManager().unregisterDependency(this, getWidget().getCaptionWrapper().getElement());
	}

	@Override
	public VListBuilder getWidget() {
		return (VListBuilder) super.getWidget();
	}

	@Override
	public ListBuilderState getState() {
		return (ListBuilderState) super.getState();
	}

	@OnStateChange(value = { "leftColumnCaption", "rightColumnCaption", "caption" })
	void updateCaptions() {
		getWidget().updateCaptions(getState().leftColumnCaption, getState().rightColumnCaption);

		getLayoutManager().setNeedsHorizontalLayout(this);
	}

	@OnStateChange("orderedSelection")
	void updateSelectionOrder() {
		getWidget().updateSelectionOrder(getState().orderedSelection);
	}

	@OnStateChange("readOnly")
	void updateReadOnly() {
		getWidget().setReadOnly(isReadOnly());
	}

	@OnStateChange("tabIndex")
	void updateTabIndex() {
		getWidget().setTabIndex(getState().tabIndex);
	}

	@Override
	public void layoutVertically() {
		if (isUndefinedHeight()) {
			getWidget().clearInternalHeights();
		} else {
			getWidget().setInternalHeights();
		}
	}

	@Override
	public void layoutHorizontally() {
		if (isUndefinedWidth()) {
			getWidget().clearInternalWidths();
		} else {
			getWidget().setInternalWidths();
		}
	}

	@Override
	public MultiSelectWidget getMultiSelectWidget() {
		return getWidget();
	}

	@Override
	public void valueChanged(List<String> value) {
		rpc.updateValue(value);
	}
}