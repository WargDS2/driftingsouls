package net.driftingsouls.ds2.server.modules.viewmodels;

import net.driftingsouls.ds2.server.config.Medal;
import net.driftingsouls.ds2.server.framework.ViewModel;

/**
 * Standard-ViewModel einer Ordens ({@link net.driftingsouls.ds2.server.config.Medal}).
 */
@ViewModel
public class MedalViewModel
{
	public String name;
	public int id;
	public String image;
	public String imageSmall;

	/**
	 * Mappt eine Medal-Entity zu einer Instanz dieses ViewModels.
	 * @param medal Die zu mappende Entity
	 * @return Das ViewModel
	 */
	public static MedalViewModel map(Medal medal)
	{
		MedalViewModel viewModel = new MedalViewModel();
		viewModel.name = medal.getName();
		viewModel.id = medal.getID();
		viewModel.image = medal.getImage(Medal.IMAGE_NORMAL);
		viewModel.imageSmall = medal.getImage(Medal.IMAGE_SMALL);
		return viewModel;
	}
}