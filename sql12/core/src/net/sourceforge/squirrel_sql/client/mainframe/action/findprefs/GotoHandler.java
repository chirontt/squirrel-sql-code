package net.sourceforge.squirrel_sql.client.mainframe.action.findprefs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.List;
import java.util.TreeMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import net.sourceforge.squirrel_sql.fw.gui.GUIUtils;

public class GotoHandler
{
   private GlobalPreferencesDialogFindInfo _openDialogsFindInfo;
   private TreeMap<List<String>, List<PrefComponentInfo>> _globalPrefsComponentInfoByPath;
   private Timer _timer;
   private int _blinkCount;

   public GotoHandler(GlobalPreferencesDialogFindInfo openDialogsFindInfo)
   {
      _openDialogsFindInfo = openDialogsFindInfo;
      _globalPrefsComponentInfoByPath = ComponentInfoByPathUtil.globalPrefsFindInfoToComponentInfoByPath(openDialogsFindInfo);
   }

   public void gotoPath(List<String> path)
   {
      Component tabComponent = getTabComponent(path);
      _openDialogsFindInfo.selectTabOfPathComponent(tabComponent);

      Component componentToGoTo = getComponentByPath(path);

      if(tabComponent instanceof JScrollPane && tabComponent != componentToGoTo)
      {
         JComponent componentInScrollPane = (JComponent) ((JScrollPane)tabComponent).getViewport().getView();

         int x = componentToGoTo.getX();
         int y = componentToGoTo.getY();

         Container parent = componentToGoTo.getParent();
         while (parent != componentInScrollPane)
         {
            x += parent.getX();
            y += parent.getY();
            parent = parent.getParent();
         }

         final Rectangle rect = new Rectangle(x, y, componentToGoTo.getWidth(), componentToGoTo.getHeight());

         GUIUtils.forceProperty(() -> {
            componentInScrollPane.scrollRectToVisible(rect);
            final boolean contains = ((JScrollPane) tabComponent).getVisibleRect().contains(rect);
            return contains;
         });
      }

      _timer = new Timer(500, e -> onBlinkComponent(componentToGoTo));

      _timer.setRepeats(true);

      SwingUtilities.invokeLater(() -> onBlinkComponent(componentToGoTo));

      _timer.start();
   }

   private void onBlinkComponent(Component component)
   {
      Graphics graphics = component.getGraphics();

      if(_blinkCount++ % 2 == 0)
      {
         Color formerColor = graphics.getColor();
         graphics.setColor(Color.red);

         Stroke formerStroke = null;
         int strokeWidth = 4;
         if (graphics instanceof Graphics2D )
         {
            formerStroke = ((Graphics2D)graphics).getStroke();
            ((Graphics2D)graphics).setStroke(new BasicStroke(strokeWidth));
         }

         graphics.drawRect(strokeWidth, strokeWidth, component.getBounds().width - 2 * strokeWidth, component.getBounds().height - 2 * strokeWidth);

         graphics.setColor(formerColor);

         if (graphics instanceof Graphics2D)
         {
            ((Graphics2D)graphics).setStroke(formerStroke);
         }
      }
      else
      {
         component.repaint();
      }


      if(_blinkCount > 10)
      {
         _timer.stop();

         component.repaint();
      }
   }

   private Component getComponentByPath(List<String> path)
   {
      return _globalPrefsComponentInfoByPath.get(path).get(0).getComponent();
   }

   private Component getTabComponent(List<String> path)
   {
      final List<PrefComponentInfo> prefComponentInfoList = _globalPrefsComponentInfoByPath.get(path.subList(0,1));
      return prefComponentInfoList.get(0).getComponent();
   }
}
