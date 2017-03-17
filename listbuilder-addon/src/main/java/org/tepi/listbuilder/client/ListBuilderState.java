package org.tepi.listbuilder.client;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.shared.ui.twincolselect.TwinColSelectState;

@SuppressWarnings("serial")
public class ListBuilderState extends TwinColSelectState {
	public List<String> orderedSelection = new ArrayList<>();
}
