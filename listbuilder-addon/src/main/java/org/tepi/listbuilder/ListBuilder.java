package org.tepi.listbuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.tepi.listbuilder.client.ListBuilderServerRpc;
import org.tepi.listbuilder.client.ListBuilderState;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.event.selection.MultiSelectionEvent;
import com.vaadin.shared.communication.SharedState;
import com.vaadin.ui.TwinColSelect;

@SuppressWarnings("serial")
public class ListBuilder<T> extends TwinColSelect<T> {
	private LinkedHashSet<T> orderedValue = new LinkedHashSet<>();

	public ListBuilder() {
		super();
		registerRpc(new ListBuilderServerRpc() {

			@Override
			public void updateValue(List<String> newValue) {
				List<String> clone = new ArrayList<>(newValue);
				for (T item : orderedValue) {
					clone.remove(getDataCommunicator().getKeyMapper().key(item));
				}
				LinkedHashSet<T> oldSelection = new LinkedHashSet<>(orderedValue);
				orderedValue = newValue.stream().map(key -> getDataCommunicator().getKeyMapper().get(key))
						.collect(Collectors.toCollection(LinkedHashSet::new));
				updateState();
				if (clone.isEmpty()) {
					fireEvent(new MultiSelectionEvent<>(ListBuilder.this, oldSelection, true));
				}
			}
		});
	}

	public ListBuilder(String caption) {
		this();
		setCaption(caption);
	}

	public ListBuilder(String caption, DataProvider<T, ?> dataProvider) {
		this(caption);
		setDataProvider(dataProvider);
	}

	public ListBuilder(String caption, Collection<T> options) {
		this(caption, DataProvider.ofCollection(options));
	}

	@Override
	public Class<? extends SharedState> getStateType() {
		return ListBuilderState.class;
	}

	@Override
	protected ListBuilderState getState() {
		return (ListBuilderState) super.getState();
	}

	@Override
	protected ListBuilderState getState(boolean markAsDirty) {
		return (ListBuilderState) super.getState(markAsDirty);
	}

	@Override
	public Set<T> getValue() {
		return orderedValue;
	}

	@Override
	public void setValue(Set<T> value) {
		Objects.requireNonNull(value);
		Stream.of(value).forEach(Objects::requireNonNull);
		orderedValue = value.stream().map(Objects::requireNonNull).collect(Collectors.toCollection(LinkedHashSet::new));
		updateState();
		super.setValue(value);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void select(T... items) {
		Objects.requireNonNull(items);
		Stream.of(items).forEach(Objects::requireNonNull);
		for (T item : items) {
			orderedValue.add(item);
		}
		updateState();
		super.select(items);
	}

	@Override
	protected void deselect(Set<T> items, boolean userOriginated) {
		Objects.requireNonNull(items);
		Stream.of(items).forEach(Objects::requireNonNull);
		orderedValue.removeAll(items);
		updateState();
		super.deselect(items, userOriginated);
	}

	@Override
	public void deselectAll() {
		orderedValue.clear();
		updateState();
		super.deselectAll();
	}

	private void updateState() {
		getState().orderedSelection = orderedValue.stream().map(item -> getDataCommunicator().getKeyMapper().key(item))
				.collect(Collectors.toCollection(ArrayList::new));
	}
}