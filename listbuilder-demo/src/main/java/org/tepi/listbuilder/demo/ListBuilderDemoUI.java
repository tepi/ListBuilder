package org.tepi.listbuilder.demo;

import java.util.Collection;

import javax.servlet.annotation.WebServlet;

import org.tepi.listbuilder.ListBuilder;

import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.data.util.IndexedContainer;

@Title("ListBuilder Add-on Demo")
@SuppressWarnings("serial")
public class ListBuilderDemoUI extends UI {

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = ListBuilderDemoUI.class, widgetset = "org.tepi.listbuilder.demo.ListBuilderDemoWidgetset")
	public static class Servlet extends VaadinServlet {
	}

	@Override
	protected void init(VaadinRequest request) {


		IndexedContainer ic = new IndexedContainer();
		ic.addContainerProperty("title", String.class, "");
		for (int i = 0; i < 100; i++) {
			String itemId="Item #" + (i + 1);
			ic.addItem(itemId);
			ic.getContainerProperty(itemId, "title").setValue(itemId);
		}

		ListBuilder listBuilder = new ListBuilder("ListBuilder Demo", ic);
		listBuilder.setLeftColumnCaption("Available options");
		listBuilder.setRightColumnCaption("Current selection");
		listBuilder.setRows(15);
		listBuilder.setWidth(50, Unit.PERCENTAGE);

		listBuilder.addValueChangeListener(e-> {
			showValueNotification(e.getProperty().getValue());
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
	
	private void showValueNotification(Object ordered) {
		StringBuilder selection = new StringBuilder();
		if (ordered instanceof Collection) {
			@SuppressWarnings("rawtypes")
			Collection c = (Collection) ordered;
			for (Object itemId : c) {
				selection.append(itemId.toString());
				selection.append(", ");
			}
			if (selection.length() > 5) {
				String note = selection.substring(0, selection.length() - 2);
				new Notification("Selected items", note, Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
			} else {
				new Notification("Nothing selected", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
			}
		}
	}
}
