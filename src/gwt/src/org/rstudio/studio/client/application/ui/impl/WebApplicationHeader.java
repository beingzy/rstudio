/*
 * WebApplicationHeader.java
 *
 * Copyright (C) 2009-11 by RStudio, Inc.
 *
 * This program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */

package org.rstudio.studio.client.application.ui.impl;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import org.rstudio.core.client.BrowseCap;
import org.rstudio.core.client.command.*;
import org.rstudio.core.client.command.impl.WebMenuCallback;
import org.rstudio.core.client.theme.res.ThemeResources;
import org.rstudio.core.client.widget.HyperlinkLabel;
import org.rstudio.core.client.widget.MessageDialogLabel;
import org.rstudio.core.client.widget.events.GlassVisibilityEvent;
import org.rstudio.studio.client.RStudioGinjector;
import org.rstudio.studio.client.application.events.EventBus;
import org.rstudio.studio.client.application.events.LogoutRequestedEvent;
import org.rstudio.studio.client.application.ui.ApplicationHeader;
import org.rstudio.studio.client.application.ui.impl.header.HeaderPanel;
import org.rstudio.studio.client.common.GlobalDisplay;
import org.rstudio.studio.client.common.dialog.WebDialogBuilderFactory;
import org.rstudio.studio.client.workbench.commands.Commands;
import org.rstudio.studio.client.workbench.events.SessionInitEvent;
import org.rstudio.studio.client.workbench.events.SessionInitHandler;
import org.rstudio.studio.client.workbench.model.Session;
import org.rstudio.studio.client.workbench.model.SessionInfo;

public class WebApplicationHeader extends Composite implements ApplicationHeader
{
   public WebApplicationHeader()
   {
      RStudioGinjector.INSTANCE.injectMembers(this);
   }

   @Inject
   public void initialize(
                  final Commands commands,
                  EventBus eventBus,
                  GlobalDisplay globalDisplay,
                  ThemeResources themeResources,
                  final Session session)
   {
      eventBus_ = eventBus;
      globalDisplay_ = globalDisplay;
      preferredHeight_ = 40;

      // Use the outer panel to just aggregate the menu bar/account area,
      // with the logo. The logo can't be inside the HorizontalPanel because
      // it needs to overflow out of the top of the panel, and it was much
      // easier to do this with absolute positioning.
      FlowPanel outerPanel = new FlowPanel();
      outerPanel.getElement().getStyle().setPosition(Position.RELATIVE);

      // header container
      HorizontalPanel headerBarPanel = new HorizontalPanel() ;
      headerBarPanel.setStylePrimaryName(themeResources.themeStyles().header());
      headerBarPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
      headerBarPanel.setWidth("100%");

      if (BrowseCap.INSTANCE.suppressBrowserForwardBack())
         suppressBrowserForwardBack();

      // override Cmd+W keybaord shortcut for Chrome
      if (BrowseCap.isChrome())
      {
         int modifiers = (BrowseCap.hasMetaKey() ? KeyboardShortcut.META : 
                                                   KeyboardShortcut.CTRL) |
                         KeyboardShortcut.SHIFT;
             
         AppCommand closeSourceDoc = commands.closeSourceDoc();
         closeSourceDoc.setShortcut(new KeyboardShortcut(modifiers, 'L'));
         ShortcutManager.INSTANCE.register(modifiers, 'L', closeSourceDoc);
      }
      
      // main menu
      advertiseEditingShortcuts(globalDisplay, commands);
      WebMenuCallback menuCallback = new WebMenuCallback();
      commands.mainMenu(menuCallback);
      mainMenu_ = menuCallback.getMenu();
      mainMenu_.setAutoHideRedundantSeparators(false);
      fixup(mainMenu_);
      mainMenu_.addStyleName(themeResources.themeStyles().mainMenu());
      AppMenuBar.addSubMenuVisibleChangedHandler(new SubMenuVisibleChangedHandler()
      {
         public void onSubMenuVisibleChanged(SubMenuVisibleChangedEvent event)
         {
            // When submenus of the main menu appear, glass over any iframes
            // so that mouse clicks can make the menus disappear
            if (event.isVisible())
               eventBus_.fireEvent(new GlassVisibilityEvent(true));
            else
               eventBus_.fireEvent(new GlassVisibilityEvent(false));
         }
      });
      headerBarPanel.add(mainMenu_);

      HTML spacer = new HTML();
      headerBarPanel.add(spacer);
      headerBarPanel.setCellWidth(spacer, "100%");

      // commands panel (no widgets added until after session init)
      headerBarCommandsPanel_ = new HorizontalPanel();
      headerBarPanel.add(headerBarCommandsPanel_);
      headerBarPanel.setCellHorizontalAlignment(headerBarCommandsPanel_,
                                                HorizontalPanel.ALIGN_RIGHT);

      eventBus.addHandler(SessionInitEvent.TYPE, new SessionInitHandler()
      {
         public void onSessionInit(SessionInitEvent sie)
         {
            SessionInfo sessionInfo = session.getSessionInfo();
            
            // only show the user identity if we are in server mode
           if (sessionInfo.getMode().equals(SessionInfo.SERVER_MODE))
               initCommandsPanel(sessionInfo);

            if (!sessionInfo.isGoogleDocsIntegrationEnabled())
            {
               commands.publishPDF().remove();
               commands.importDatasetFromGoogleSpreadsheet().remove();
            }
         }
      });

      outerPanel.add(new HeaderPanel(headerBarPanel));

      // logo
      Image logo = new Image(ThemeResources.INSTANCE.rstudio());
      ((ImageElement)logo.getElement().cast()).setAlt("RStudio");
      Style style = logo.getElement().getStyle();
      style.setPosition(Position.ABSOLUTE);
      style.setTop(5, Unit.PX);
      style.setLeft(18, Unit.PX);
      outerPanel.add(logo);

      // initialize widget
      initWidget(outerPanel);
   }

   private native final void suppressBrowserForwardBack() /*-{
      var outerWindow = $wnd.parent;
      if (outerWindow.addEventListener) {
         var handler = function(evt) {
            if ((evt.keyCode == 37 || evt.keyCode == 39) && (evt.metaKey && !evt.ctrlKey && !evt.shiftKey && !evt.altKey)) {
               evt.preventDefault();
               evt.stopPropagation();
            }
         };
         outerWindow.addEventListener('keydown', handler, false);
         $wnd.addEventListener('keydown', handler, false);
      }
   }-*/;

   private void advertiseEditingShortcuts(final GlobalDisplay display,
                                          final Commands commands)
   {
      int mod = BrowseCap.hasMetaKey() ? KeyboardShortcut.META : KeyboardShortcut.CTRL;

      commands.undoDummy().setShortcut(new KeyboardShortcut(mod, 'Z'));
      commands.redoDummy().setShortcut(new KeyboardShortcut(mod| KeyboardShortcut.SHIFT, 'Z'));

      commands.cutDummy().setShortcut(new KeyboardShortcut(mod, 'X'));
      commands.copyDummy().setShortcut(new KeyboardShortcut(mod, 'C'));
      commands.pasteDummy().setShortcut(new KeyboardShortcut(mod, 'V'));

      CommandHandler useKeyboardNotification = new CommandHandler()
      {
         public void onCommand(AppCommand command)
         {
            MessageDialogLabel label = new MessageDialogLabel();
            label.setHtml("Your browser does not allow access to your<br/>" +
            		        "computer's clipboard. As a result you must<br/>" +
            		        "use keyboard shortcuts for:" +
                          "<br/><br/><table cellpadding=0 cellspacing=0 border=0>" +
                          makeRow(commands.undoDummy()) +
                          makeRow(commands.redoDummy()) +
                          makeRow(commands.cutDummy()) +
                          makeRow(commands.copyDummy()) +
                          makeRow(commands.pasteDummy()) +
                          "</table>"
                          );
            new WebDialogBuilderFactory().create(
                  GlobalDisplay.MSG_WARNING,
                  "Use Keyboard Shortcut",
                  label).showModal();
         }

         private String makeRow(AppCommand cmd)
         {
            String textAlign = BrowseCap.hasMetaKey()
                               ? "text-align: right"
                               : "";
            return "<tr><td>" + cmd.getMenuLabel(true) + "</td>" +
                   "<td style='padding-left: 12px; " + textAlign + "'>"
                   + cmd.getShortcutPrettyHtml() + "</td></tr>";
         }
      };

      commands.undoDummy().addHandler(useKeyboardNotification);
      commands.redoDummy().addHandler(useKeyboardNotification);
      commands.cutDummy().addHandler(useKeyboardNotification);
      commands.copyDummy().addHandler(useKeyboardNotification);
      commands.pasteDummy().addHandler(useKeyboardNotification);
   }

   public int getPreferredHeight()
   {
      return preferredHeight_ ;
   }

   /**
    * Without this fixup, the main menu doesn't properly deselect its items
    * when the mouse takes focus away.
    */
   private void fixup(final AppMenuBar mainMenu)
   {
      mainMenu.addCloseHandler(new CloseHandler<PopupPanel>()
      {
         public void onClose(CloseEvent<PopupPanel> popupPanelCloseEvent)
         {
            // Only dismiss the selection if the panel that just closed belongs
            // to the currently selected item. Otherwise, the selected item
            // has already changed and we don't want to mess with it. (This is
            // NOT an edge case, it is very common.)
            MenuItem menuItem = mainMenu.getSelectedItem();
            if (menuItem != null)
            {
               MenuBar subMenu = menuItem.getSubMenu();
               if (subMenu != null &&
                   popupPanelCloseEvent.getTarget() != null &&
                   subMenu.equals(popupPanelCloseEvent.getTarget().getWidget()))
               {
                  Scheduler.get().scheduleDeferred(new ScheduledCommand()
                  {
                     public void execute()
                     {
                        mainMenu.selectItem(null);
                     }
                  });
               }
            }
         }
      });
   }

   private void initCommandsPanel(final SessionInfo sessionInfo)
   {  
      // add username 
      Label usernameLabel = new Label();
      usernameLabel.setText(sessionInfo.getUserIdentity());
      usernameLabel.setStylePrimaryName(
            ThemeResources.INSTANCE.themeStyles().applicationHeaderStrong());
      headerBarCommandsPanel_.add(usernameLabel);
      headerBarCommandsPanel_.add(createCommandSeparator());
      
      // help link
      Widget docsLink = createCommandLink("Docs", new ClickHandler() {
         public void onClick(ClickEvent event)
         {
            String customDocsURL = sessionInfo.docsURL();
            if (customDocsURL.length() > 0)
               globalDisplay_.openWindow(customDocsURL);
            else
               globalDisplay_.openRStudioLink("docs");
         }
      });
      headerBarCommandsPanel_.add(docsLink);
      headerBarCommandsPanel_.add(createCommandSeparator());
      
      // support link
      Widget supportLink = createCommandLink("Support", new ClickHandler() {
         public void onClick(ClickEvent event)
         {
            globalDisplay_.openRStudioLink("support");
         }
      });
      headerBarCommandsPanel_.add(supportLink);
      headerBarCommandsPanel_.add(createCommandSeparator());
       
      // signout link 
      Widget signoutLink = createCommandLink("Sign Out", new ClickHandler() {
         public void onClick(ClickEvent event)
         {
            eventBus_.fireEvent(new LogoutRequestedEvent());
         }
      });
      headerBarCommandsPanel_.add(signoutLink);
   }

   private Widget createCommandSeparator()
   {
      return new HTML("&nbsp;|&nbsp;");
   }
   
   private Widget createCommandLink(String caption, ClickHandler clickHandler)
   {
      HyperlinkLabel link = new HyperlinkLabel(caption, clickHandler);
      link.setAlwaysUnderline(true);
      return link;
   }

   public Widget toWidget()
   {
      return this;
   }


   private int preferredHeight_ ;

   private HorizontalPanel headerBarCommandsPanel_;
   private AppMenuBar mainMenu_;
   private EventBus eventBus_;
   private GlobalDisplay globalDisplay_;
}
