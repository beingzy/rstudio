package org.rstudio.studio.client.workbench.prefs.views;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.ListBox;
import com.google.inject.Inject;
import org.rstudio.studio.client.workbench.prefs.model.UIPrefs;

public class AppearancePreferencesPane extends PreferencesPane
{
   @Inject
   public AppearancePreferencesPane(PreferencesDialogResources res,
                                    UIPrefs uiPrefs)
   {
      res_ = res;
      uiPrefs_ = uiPrefs;

      String[] labels = {"10", "12", "14", "16", "18"};
      String[] values = {"Pt10", "Pt12", "Pt14", "Pt16", "Pt18"};

      fontSize_ = new SelectWidget("Console/Source Font Size",
                                             labels,
                                             values);
      String value = uiPrefs.fontSize().getValue();
      boolean matched = false;
      for (int i = 0; i < values.length; i++)
         if (values[i].equals(value))
         {
            fontSize_.getListBox().setSelectedIndex(i);
            matched = true;
            break;
         }
      if (!matched)
         fontSize_.getListBox().setSelectedIndex(1);

      add(fontSize_);
   }

   @Override
   public ImageResource getIcon()
   {
      return res_.iconAppearance();
   }

   @Override
   public void onApply()
   {
      ListBox list = fontSize_.getListBox();
      uiPrefs_.fontSize().setValue(list.getValue(list.getSelectedIndex()));
   }

   @Override
   public String getName()
   {
      return "Appearance";
   }

   private final PreferencesDialogResources res_;
   private final UIPrefs uiPrefs_;
   private SelectWidget fontSize_;
}