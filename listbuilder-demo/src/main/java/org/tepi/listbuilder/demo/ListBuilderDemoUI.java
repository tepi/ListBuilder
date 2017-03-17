package org.tepi.listbuilder.demo;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;

import org.tepi.listbuilder.ListBuilder;

import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Title("ListBuilder Add-on Demo")
@SuppressWarnings("serial")
public class ListBuilderDemoUI extends UI {

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = ListBuilderDemoUI.class, widgetset = "org.tepi.listbuilder.demo.ListBuilderDemoWidgetset")
	public static class Servlet extends VaadinServlet {
	}

	@Override
	protected void init(VaadinRequest request) {

		List<String> items = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			items.add("Item #" + (i + 1));
		}
		ListDataProvider<String> dataprovider = new ListDataProvider<>(items);

		ListBuilder<String> listBuilder = new ListBuilder<>("ListBuilder Demo", dataprovider);
		listBuilder.setLeftColumnCaption("Available options");
		listBuilder.setRightColumnCaption("Current selection");
		listBuilder.setRows(15);
		listBuilder.setWidth(50, Unit.PERCENTAGE);

		listBuilder.addSelectionListener(e -> {
			System.err.println("getAddedSelection: " + e.getAddedSelection());
			System.err.println("getRemovedSelection: " + e.getRemovedSelection());
			System.err.println("getAllSelectedItems: " + e.getAllSelectedItems());
			System.err.println("getFirstSelectedItem: " + e.getFirstSelectedItem());
			System.err.println("getNewSelection: " + e.getNewSelection());
			System.err.println("getOldSelection: " + e.getOldSelection());
			Notification.show("Selected items: " + e.getValue(), Type.HUMANIZED_MESSAGE);
		});

		final VerticalLayout layout = new VerticalLayout();
		layout.setStyleName("demoContentLayout");
		layout.setSizeFull();
		layout.setMargin(false);
		layout.setSpacing(false);
		layout.addComponent(listBuilder);
		layout.setComponentAlignment(listBuilder, Alignment.MIDDLE_CENTER);
		setContent(layout);
	}
}
