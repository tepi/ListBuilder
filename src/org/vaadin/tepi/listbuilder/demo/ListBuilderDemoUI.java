package org.vaadin.tepi.listbuilder.demo;

import java.util.Collection;

import org.vaadin.tepi.listbuilder.ListBuilder;

import com.vaadin.annotations.Title;
import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

/**
 * Main UI class
 */
@SuppressWarnings("serial")
@Title("ListBuilder Demo Application")
public class ListBuilderDemoUI extends UI {

    private ListBuilder listBuilder;

    @Override
    protected void init(VaadinRequest request) {

        VerticalLayout vl = new VerticalLayout();
        vl.addStyleName(Reindeer.LAYOUT_BLACK);
        vl.setMargin(true);
        vl.setSizeFull();

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setSizeUndefined();

        setContent(vl);

        listBuilder = new ListBuilder(
                "ListBuilder component - preserves the item order");

        listBuilder.setImmediate(true);

        listBuilder.setLeftColumnCaption("Select from here");
        listBuilder.setRightColumnCaption("Selected items");

        listBuilder.setContainerDataSource(getBeanCont());
        listBuilder.setItemCaptionPropertyId("name");

        listBuilder.setColumns(15);

        content.addComponent(listBuilder);

        Button b = new Button("Show selected");
        b.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
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
                        String note = selection.substring(0,
                                selection.length() - 2);
                        new Notification("Selected items", note,
                                Notification.Type.TRAY_NOTIFICATION).show(Page
                                .getCurrent());
                    } else {
                        new Notification("Nothing selected",
                                Notification.Type.TRAY_NOTIFICATION).show(Page
                                .getCurrent());
                    }
                }
            }
        });
        content.addComponent(b);

        vl.addComponent(content);
        vl.setComponentAlignment(content, Alignment.MIDDLE_CENTER);
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