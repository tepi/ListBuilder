package org.tepi.listbuilder.client;

import java.util.List;

import com.vaadin.shared.communication.ServerRpc;

public interface ListBuilderServerRpc extends ServerRpc {
	void updateValue(List<String> newValue);
}