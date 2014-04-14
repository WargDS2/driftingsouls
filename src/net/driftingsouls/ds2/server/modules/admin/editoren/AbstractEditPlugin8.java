package net.driftingsouls.ds2.server.modules.admin.editoren;

import net.driftingsouls.ds2.server.bases.Building;
import net.driftingsouls.ds2.server.entities.User;
import net.driftingsouls.ds2.server.framework.Common;
import net.driftingsouls.ds2.server.framework.Context;
import net.driftingsouls.ds2.server.framework.ContextMap;
import net.driftingsouls.ds2.server.framework.pipeline.Request;
import net.driftingsouls.ds2.server.framework.utils.StringToTypeConverter;
import net.driftingsouls.ds2.server.modules.AdminController;
import net.driftingsouls.ds2.server.modules.admin.AdminPlugin;
import net.driftingsouls.ds2.server.framework.db.batch.EvictableUnitOfWork;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

/**
 * Bsisklasse fuer einfache Editor-Plugins (fuer einzelne Entities). Stellt
 * regelmaessig benoetigte Funktionen bereit.
 *
 * @author christopherjung
 */
public abstract class AbstractEditPlugin8<T> implements AdminPlugin
{
	private Class<? extends T> clazz;
	private Class<T> baseClass;

	public AbstractEditPlugin8(Class<T> clazz)
	{
		this.clazz = clazz;
		this.baseClass = clazz;
	}

	@Override
	public final void output(AdminController controller) throws IOException
	{
		Context context = ContextMap.getContext();
		Session db = context.getDB();

		Writer echo = context.getResponse().getWriter();

		Request request = context.getRequest();
		String entityId = request.getParameter("entityId");

		EditorForm8<T> form = new EditorForm8<>(EditorMode.UPDATE, this.getClass(), echo);
		configureFor(form);

		if (this.isUpdateExecuted())
		{
			try
			{
				@SuppressWarnings("unchecked") T entity = (T) db.get(this.clazz, toEntityId(this.clazz, entityId));
				if (form.isUpdateAllowed(entity) && isUpdatePossible(entity))
				{
					db.evict(entity);
					@SuppressWarnings("unchecked") T updatedEntity = (T) db.get(this.clazz, toEntityId(this.clazz, entityId));
					form.applyRequestValues(request, updatedEntity);
					processJobs(echo, entity, updatedEntity, form.getUpdateTasks());
					echo.append("<p>Update abgeschlossen.</p>");
				}
			}
			catch (IOException | RuntimeException e)
			{
				echo.append("<p>Fehler bei Update: ").append(e.getMessage());
			}
		}
		else if (this.isAddExecuted())
		{
			try
			{
				form = new EditorForm8<>(EditorMode.CREATE, this.getClass(), echo);
				configureFor(form);

				T entity = createEntity();
				form.applyRequestValues(request, entity);
				if( entity.getClass() != this.clazz )
				{
					// Klasse wurde geaendert - erneut anwenden
					entity = createEntity();
					form.applyRequestValues(request, entity);
				}
				db.persist(entity);
				echo.append("<p>Hinzufügen abgeschlossen.</p>");
			}
			catch (IOException | RuntimeException e)
			{
				echo.append("<p>Fehler bei Update: ").append(e.getMessage()).append("</p>");
			}
		}
		else if (this.isResetExecuted())
		{
			try
			{
				@SuppressWarnings("unchecked") T entity = (T) db.get(this.clazz, toEntityId(this.clazz, entityId));
				reset(new DefaultStatusWriter(echo), entity);
				echo.append("<p>Update abgeschlossen.</p>");
			}
			catch (IOException | RuntimeException e)
			{
				echo.append("<p>Fehler bei Reset: ").append(e.getMessage()).append("</p>");
			}
		}

		outputEntitySelection(db, echo, form);

		if (entityId != null && !entityId.isEmpty())
		{
			@SuppressWarnings("unchecked") T entity = (T) db.get(clazz, toEntityId(this.clazz, entityId));
			if (entity == null)
			{
				return;
			}

			beginEditorTable(echo, this.getClass(), entityId);
			form.generateForm(entity);
			endEditorTable(echo);
		}
		else if( isAddDisplayed() )
		{
			T entity = createEntity();
			form = new EditorForm8<>(EditorMode.CREATE, this.getClass(), echo);
			configureFor(form);
			beginEditorTable(echo, this.getClass(), -1);
			form.generateForm(entity);
		}
	}

	private void outputEntitySelection(Session db, Writer echo, EditorForm8<T> form) throws IOException
	{
		echo.append("<div class='gfxbox adminSelection' style='width:390px'>");
		Long count = (Long)db.createCriteria(baseClass).setProjection(Projections.rowCount()).uniqueResult();
		if( count != null )
		{
			beginSelectionBox(echo, this.getClass());

			if (count < 500)
			{
				List<Building> entities = Common.cast(db.createCriteria(baseClass).list());
				echo.append("<select size=\"1\" name=\"entityId\">");
				for (Object entity : entities)
				{
					addSelectionOption(echo, db.getIdentifier(entity), new ObjectLabelGenerator().generateFor(null, entity));
				}
				if (isAddDisplayed())
				{
					addSelectionOption(echo, null, "[Neu]");
				}
				echo.append("</select>");
			}
			else
			{
				String currentIdStr = ContextMap.getContext().getRequest().getParameter("entityId");
				if( currentIdStr == null )
				{
					currentIdStr = "";
				}
				echo.append("<input type=\"text\" name=\"entityId\" value=\""+currentIdStr+"\" ").append(isAddDisplayed() ? "readonly=\"readonly\"" : "").append(" />");
			}

			endSelectionBox(echo);
		}
		if (form.isAddAllowed())
		{
			addForm(echo, this.getClass());
		}
		echo.append("</div>");
	}

	private Serializable toEntityId(Class<?> entity, String idString)
	{
		org.hibernate.Session db = ContextMap.getContext().getDB();
		Class<?> targetType = db.getSessionFactory().getClassMetadata(entity).getIdentifierType().getReturnedClass();
		return (Serializable)StringToTypeConverter.convert(targetType, idString);
	}

	private void processJobs(Writer echo, T entity, T updatedEntity, List<EditorForm8.Job<T, ?>> updateTasks) throws IOException
	{
		org.hibernate.Session db = ContextMap.getContext().getDB();
		db.getTransaction().commit();

		for (final EditorForm8.Job<T, ?> updateTask : updateTasks)
		{
			@SuppressWarnings("unchecked") final EditorForm8.Job<T, Object> updateTask1 = (EditorForm8.Job<T, Object>) updateTask;
			Collection<Object> jobData = updateTask1.supplier.apply(updatedEntity);
			new EvictableUnitOfWork<Object>(updateTask.name) {
				@Override
				public void doWork(Object object) throws Exception
				{
					updateTask1.job.accept(entity,updatedEntity,object);
				}
			}.setFlushSize(10).executeFor(jobData);

			echo.append(updateTask.name);
			if( jobData.size() > 1 )
			{
				echo.append(": ").append(Integer.toString(jobData.size())).append(" Objekte aktualisiert");
			}
			echo.append("<br />");
		}

		db.getTransaction().begin();
	}

	private void endEditorTable(Writer echo) throws IOException
	{
		echo.append("</table>");
		echo.append("</form>\n");
		echo.append("</div>");
	}

	protected void setEntityClass(String name)
	{
		try
		{
			this.clazz = Class.forName(name).asSubclass(this.baseClass);
		}
		catch (ClassNotFoundException e)
		{
			throw new IllegalArgumentException("Unbekannte Klasse "+name);
		}
	}

	protected void setEntityClass(Class<? extends T> clazz)
	{
		this.clazz = clazz;
	}

	protected @Nonnull String getEntityClass()
	{
		return this.clazz.getName();
	}

	private T createEntity()
	{
		try
		{
			Constructor<? extends T> constructor = this.clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			return constructor.newInstance();
		}
		catch (NoSuchMethodException e)
		{
			throw new AssertionError("Kein default-Konstruktor fuer Entity '"+this.clazz.getName()+"' vorhanden");
		}
		catch (InvocationTargetException | InstantiationException | IllegalAccessException e)
		{
			throw new IllegalStateException("Konnte Entity '"+this.clazz.getName()+"' nicht instantiieren");
		}
	}

	protected final @Nonnull Session getDB()
	{
		return ContextMap.getContext().getDB();
	}

	protected void reset(StatusWriter writer, T entity) throws IOException
	{
		// TODO
	}

	protected abstract void configureFor(@Nonnull EditorForm8<T> form);

	protected boolean isUpdatePossible(@Nonnull T entity)
	{
		return true;
	}

	private void beginSelectionBox(Writer echo, Class<? extends AdminPlugin> plugin) throws IOException
	{
		echo.append("<form action=\"./ds\" method=\"post\">");
		echo.append("<input type=\"hidden\" name=\"namedplugin\" value=\"").append(plugin.getName()).append("\" />\n");
		echo.append("<input type=\"hidden\" name=\"module\" value=\"admin\" />\n");
	}

	private void addSelectionOption(Writer echo, Object id, String label) throws IOException
	{
		Context context = ContextMap.getContext();
		String currentIdStr = context.getRequest().getParameter("entityId");
		String idStr = id != null ? id.toString() : null;
		boolean selektiert = idStr != null ? idStr.equals(currentIdStr) : null == currentIdStr;

		echo.append("<option value=\"").append(idStr).append("\" ").append(selektiert ? "selected=\"selected\"" : "").append(">").append(label).append("</option>");
	}

	private void endSelectionBox(Writer echo) throws IOException
	{
		echo.append("<input type=\"submit\" name=\"choose\" value=\"Ok\" />");
		echo.append("</form>");
	}

	private void addForm(Writer echo, Class<? extends AdminPlugin> plugin) throws IOException
	{
		echo.append("<form action=\"./ds\" method=\"post\">");
		echo.append("<input type=\"hidden\" name=\"namedplugin\" value=\"").append(plugin.getName()).append("\" />\n");
		echo.append("<input type=\"hidden\" name=\"module\" value=\"admin\" />\n");
		echo.append("<input type=\"submit\" name=\"add\" value=\"+\" />");
		echo.append("</form>");
	}

	private void beginEditorTable(final Writer echo, Class<? extends AdminPlugin> plugin, Object entityId) throws IOException
	{
		echo.append("<div class='gfxbox adminEditor' style='width:700px'>");
		echo.append("<form action=\"./ds\" method=\"post\" enctype='multipart/form-data'>");
		echo.append("<input type=\"hidden\" name=\"namedplugin\" value=\"").append(plugin.getName()).append("\" />\n");
		echo.append("<input type=\"hidden\" name=\"module\" value=\"admin\" />\n");
		echo.append("<input type=\"hidden\" name=\"entityId\" value=\"").append(entityId != null ? entityId.toString() : "").append("\" />\n");

		echo.append("<table>");
	}

	protected final boolean isResetted(String name)
	{
		Context context = ContextMap.getContext();
		String reset = context.getRequest().getParameterString("reset");
		return name.equals(reset);
	}

	private boolean isResetExecuted()
	{
		Context context = ContextMap.getContext();
		String reset = context.getRequest().getParameterString("reset");
		return reset != null && !reset.trim().isEmpty();
	}

	private boolean isUpdateExecuted()
	{
		Context context = ContextMap.getContext();
		String change = context.getRequest().getParameterString("change");
		return "Aktualisieren".equals(change);
	}

	private boolean isAddExecuted()
	{
		Context context = ContextMap.getContext();
		String change = context.getRequest().getParameterString("change");
		return "Hinzufügen".equals(change);
	}

	private boolean isAddDisplayed()
	{
		Context context = ContextMap.getContext();
		String change = context.getRequest().getParameterString("add");
		return !change.trim().isEmpty();
	}

	protected final User getActiveUser()
	{
		return (User)ContextMap.getContext().getActiveUser();
	}
}
