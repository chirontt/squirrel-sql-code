package net.sourceforge.squirrel_sql.plugins.editextras;
/*
 * Copyright (C) 2003 Gerd Wagner
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
import net.sourceforge.squirrel_sql.client.IApplication;
import net.sourceforge.squirrel_sql.client.action.ActionCollection;
import net.sourceforge.squirrel_sql.client.plugin.DefaultSessionPlugin;
import net.sourceforge.squirrel_sql.client.plugin.PluginException;
import net.sourceforge.squirrel_sql.client.plugin.PluginResources;
import net.sourceforge.squirrel_sql.client.session.ISQLPanelAPI;
import net.sourceforge.squirrel_sql.client.session.ISession;
import net.sourceforge.squirrel_sql.client.session.event.ISQLPanelListener;
import net.sourceforge.squirrel_sql.client.session.event.SQLPanelAdapter;
import net.sourceforge.squirrel_sql.client.session.event.SQLPanelEvent;
import net.sourceforge.squirrel_sql.fw.util.log.ILogger;
import net.sourceforge.squirrel_sql.fw.util.log.LoggerController;
import net.sourceforge.squirrel_sql.plugins.editextras.searchandreplace.*;

import javax.swing.*;
import java.util.Hashtable;
/**
 * The plugin class.
 *
 * @author  Gerd Wagner
 */
public class EditExtrasPlugin extends DefaultSessionPlugin
{
	/** Logger for this class. */
	private final static ILogger
			s_log = LoggerController.createLogger(EditExtrasPlugin.class);

	private interface IMenuResourceKeys 
	{
		String MENU = "editextras";
	}

	/** Name of file to store user prefs in. */
	static final String USER_PREFS_FILE_NAME = "prefs.xml";

	/** Resources for this plugin. */
	private Resources _resources;

	/** Listener to the SQL panel. */
	private ISQLPanelListener _lis = new SQLPanelListener();

	private Hashtable _searchAndReplaceKernelsBySessionID = new Hashtable();

	/**
	 * Return the internal name of this plugin.
	 *
	 * @return  the internal name of this plugin.
	 */
	public String getInternalName()
	{
		return "editextras";
	}

	/**
	 * Return the descriptive name of this plugin.
	 *
	 * @return  the descriptive name of this plugin.
	 */
	public String getDescriptiveName()
	{
		return "SQL Entry Area Enhancements";
	}

	/**
	 * Returns the current version of this plugin.
	 *
	 * @return  the current version of this plugin.
	 */
	public String getVersion()
	{
		return "0.13";
	}

	/**
	 * Returns the authors name.
	 *
	 * @return  the authors name.
	 */
	public String getAuthor()
	{
		return "Gerd Wagner";
	}

	/**
	 * Returns the name of the change log for the plugin. This should
	 * be a text or HTML file residing in the <TT>getPluginAppSettingsFolder</TT>
	 * directory.
	 *
	 * @return	the changelog file name or <TT>null</TT> if plugin doesn't have
	 * 			a change log.
	 */
	public String getChangeLogFileName()
	{
		return "changes.txt";
	}

	/**
	 * Returns the name of the Help file for the plugin. This should
	 * be a text or HTML file residing in the <TT>getPluginAppSettingsFolder</TT>
	 * directory.
	 *
	 * @return	the Help file name or <TT>null</TT> if plugin doesn't have
	 * 			a help file.
	 */
	public String getHelpFileName()
	{
		return "readme.txt";
	}

	/**
	 * Returns the name of the Licence file for the plugin. This should
	 * be a text or HTML file residing in the <TT>getPluginAppSettingsFolder</TT>
	 * directory.
	 *
	 * @return	the Licence file name or <TT>null</TT> if plugin doesn't have
	 * 			a licence file.
	 */
	public String getLicenceFileName()
	{
		return "licence.txt";
	}

	/**
	 * Called on application startup after application started.
	 */
	public void initialize() throws PluginException
	{
		super.initialize();

		final IApplication app = getApplication();

		// Load resources.
		_resources = new Resources(this);

		createMenu();
	}

	/**
	 * Session has been started.
	 * 
	 * @param	session		Session that has started.
	 */
	public boolean sessionStarted(ISession session)
	{
		if (super.sessionStarted(session))
		{

         ActionCollection coll = getApplication().getActionCollection();

         session.addToToolbar(coll.get(FindAction.class));
         session.addToToolbar(coll.get(ReplaceAction.class));

			//session.getSQLPanelAPI(this).addSQLPanelListener(_lis);
			FrameWorkAcessor.getSQLPanelAPI(session, this).addSQLPanelListener(_lis);

         setupSQLEntryArea(session);


			//ISQLPanelAPI api = session.getSQLPanelAPI(this);
         ISQLPanelAPI api = FrameWorkAcessor.getSQLPanelAPI(session, this);

			_searchAndReplaceKernelsBySessionID.put(session.getIdentifier(),
					new SearchAndReplaceKernel(api));

			return true;
		}
		return false;
	}

	/**
	 * Called when a session shutdown.
	 *
	 * @param	session	The session that is ending.
	 */
	public void sessionEnding(ISession session)
	{
		//session.getSQLPanelAPI(this).removeSQLPanelListener(_lis);
      FrameWorkAcessor.getSQLPanelAPI(session, this).removeSQLPanelListener(_lis);


		_searchAndReplaceKernelsBySessionID.remove(session.getIdentifier());
		super.sessionEnding(session);
	}

	/**
	 * Retrieve plugins resources.
	 * 
	 * @return	Plugins resources.
	 */
	public PluginResources getResources()
	{
		return _resources;
	}

	public SearchAndReplaceKernel getSearchAndReplaceKernel(ISession sess)
	{
		return (SearchAndReplaceKernel)_searchAndReplaceKernelsBySessionID.get(sess.getIdentifier());
	}

	private void createMenu() 
	{
		IApplication app = getApplication();
		ActionCollection coll = app.getActionCollection();

		JMenu menu = _resources.createMenu(IMenuResourceKeys.MENU);
		app.addToMenu(IApplication.IMenuIDs.SESSION_MENU, menu);

		Action act = new InQuotesAction(app, this);
		coll.add(act);
		_resources.addToMenu(act, menu);

		act = new RemoveQuotesAction(app, this);
		coll.add(act);
		_resources.addToMenu(act, menu);

		act = new ConvertToStringBufferAction(app, this);
		coll.add(act);
		_resources.addToMenu(act, menu);

		act = new FormatSQLAction(app, this);
		coll.add(act);
		_resources.addToMenu(act, menu);

		act = new FindAction(getApplication(), _resources, this);
		coll.add(act);
		_resources.addToMenu(act, menu);

		act = new RepeatLastFindAction(getApplication(), _resources, this);
		coll.add(act);
		_resources.addToMenu(act, menu);

		act = new FindSelectedAction(getApplication(), _resources, this);
		coll.add(act);
		_resources.addToMenu(act, menu);

		act = new ReplaceAction(getApplication(), _resources, this);
		coll.add(act);
		_resources.addToMenu(act, menu);

      act = new EscapeDateAction(getApplication(), _resources);
      coll.add(act);
      _resources.addToMenu(act, menu);
	}

	private void setupSQLEntryArea(ISession session)
	{
		//ISQLPanelAPI api = session.getSQLPanelAPI(this);
		ISQLPanelAPI api = FrameWorkAcessor.getSQLPanelAPI(session, this);
      
		ActionCollection actions = getApplication().getActionCollection();
		api.addToSQLEntryAreaMenu(actions.get(InQuotesAction.class));
		api.addToSQLEntryAreaMenu(actions.get(RemoveQuotesAction.class));
		api.addToSQLEntryAreaMenu(actions.get(ConvertToStringBufferAction.class));
		api.addToSQLEntryAreaMenu(actions.get(FormatSQLAction.class));
      api.addToSQLEntryAreaMenu(actions.get(EscapeDateAction.class));
	}

	private class SQLPanelListener extends SQLPanelAdapter
	{
		public void sqlEntryAreaReplaced(SQLPanelEvent evt)
		{
			setupSQLEntryArea(evt.getSession());
		}
	}
}
