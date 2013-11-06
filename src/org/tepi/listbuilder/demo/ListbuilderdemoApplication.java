package org.tepi.listbuilder.demo;

import java.util.Collection;
import java.util.Collections;

import org.tepi.listbuilder.ListBuilder;

import com.vaadin.Application;
import com.vaadin.data.Container;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
public class ListbuilderdemoApplication extends Application {
    private ListBuilder listBuilder;

    @Override
    public void init() {
        Window mainWindow = new Window("ListBuilder Demo Application");
        VerticalLayout vl = (VerticalLayout) mainWindow.getContent();
        vl.addStyleName(Reindeer.LAYOUT_BLACK);
        vl.setMargin(true);
        vl.setSizeFull();

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setSizeUndefined();

        setMainWindow(mainWindow);

        listBuilder = new ListBuilder(
                "ListBuilder component - preserves the item order");
        listBuilder.setImmediate(true);

        listBuilder.setLeftColumnCaption("Select from here");
        listBuilder.setRightColumnCaption("Selected items");

        listBuilder.setContainerDataSource(getBeanCont());
        listBuilder.setItemCaptionPropertyId("name");

        listBuilder.setColumns(15);

        listBuilder.setLeftColumnCaptionStyleName("lcap");
        listBuilder.setRightColumnCaptionStyleName("rcap");

        listBuilder.addListener(new ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {
                showSelectedAsNotification();
            }
        });

        content.addComponent(listBuilder);

        Button b = new Button("Show selected");
        b.addListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                showSelectedAsNotification();
            }
        });
        content.addComponent(b);

        vl.addComponent(content);
        vl.setComponentAlignment(content, Alignment.MIDDLE_CENTER);

        listBuilder.setRequired(true);
        listBuilder.setValue(Collections.singletonList(listBuilder.getItemIds()
                .iterator().next()));

        System.err.println("Valid: " + listBuilder.isValid());
    }

    private Container getBeanCont() {
        BeanItemContainer<TestBean> id = new BeanItemContainer<TestBean>(
                TestBean.class);
        for (int i = 0; i < 100; i++) {
            TestBean fb = new TestBean(String.valueOf(i), "Item " + i);
            id.addItem(fb);
        }
        return id;
    }

    private void showSelectedAsNotification() {
        Object ordered = listBuilder.getValue();
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
                getMainWindow().showNotification("Selected items", note,
                        Notification.TYPE_TRAY_NOTIFICATION);
            } else {
                getMainWindow().showNotification("Nothing selected",
                        Notification.TYPE_TRAY_NOTIFICATION);
            }

        }
    }

    public class TestBean {
        private String name;
        private String id;

        public TestBean(String id, String name) {
            setId(id);
            setName(name);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}
