/*
 *	Drifting Souls 2
 *	Copyright (c) 2006 Christopher Jung
 *
 *	This library is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU Lesser General Public
 *	License as published by the Free Software Foundation; either
 *	version 2.1 of the License, or (at your option) any later version.
 *
 *	This library is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *	Lesser General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public
 *	License along with this library; if not, write to the Free Software
 *	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.driftingsouls.ds2.server.modules.ks;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import net.driftingsouls.ds2.server.ContextCommon;
import net.driftingsouls.ds2.server.battles.Battle;
import net.driftingsouls.ds2.server.battles.BattleShip;
import net.driftingsouls.ds2.server.framework.Common;
import net.driftingsouls.ds2.server.framework.Configuration;
import net.driftingsouls.ds2.server.framework.Context;
import net.driftingsouls.ds2.server.framework.ContextMap;
import net.driftingsouls.ds2.server.ships.ShipTypeData;

/**
 * Cheat eigenes Schiff regenerieren.
 * @author Christopher Jung
 *
 */
@Configurable
public class KSCheatRegenerateOwnAction extends BasicKSAction {
	
	private Configuration config;
	
    /**
     * Injiziert die DS-Konfiguration.
     * @param config Die DS-Konfiguration
     */
    @Autowired
    public void setConfiguration(Configuration config) 
    {
    	this.config = config;
    }
    
	@Override
	public Result execute(Battle battle) throws IOException {
		Result result = super.execute(battle);
		if( result != Result.OK ) {
			return result;
		}
		
		Context context = ContextMap.getContext();
		
		if( config.getInt("ENABLE_CHEATS") == 0 ) {
			context.addError("Cheats sind deaktiviert!");
			return Result.HALT;
		}
		
		BattleShip ownShip = battle.getOwnShip();

		battle.logenemy("<action side=\""+battle.getOwnSide()+"\" time=\""+Common.time()+"\" tick=\""+context.get(ContextCommon.class).getTick()+"\"><![CDATA[\n");
		
		ShipTypeData ownShipType = ownShip.getTypeData();
		ownShip.getShip().setCrew(ownShipType.getCrew());
		ownShip.getShip().setHull(ownShipType.getHull());
		ownShip.getShip().setEnergy(ownShipType.getEps());
		ownShip.getShip().setShields(ownShipType.getShields());
		ownShip.getShip().setEngine(100);
		ownShip.getShip().setWeapons(100);
		ownShip.getShip().setSensors(100);
		ownShip.getShip().setComm(100);
		ownShip.getShip().setHeat(0);
		ownShip.getShip().setWeaponHeat("");
		
		ownShip.setHull(ownShip.getShip().getHull());
		ownShip.setShields(ownShip.getShip().getShields());
		ownShip.setEngine(100);
		ownShip.setWeapons(100);
		ownShip.setSensors(100);
		ownShip.setComm(100);
		ownShip.setAction(0);
		
		battle.logme( "CHEAT: Gegnerisches Schiff regeneriert\n" );
		battle.logenemy( "CHEAT: [color=green]"+ownShip.getName()+"[/color] regeneriert\n" );

		battle.logenemy("]]></action>\n");
		
		ownShip.getShip().recalculateShipStatus();
		
		return Result.OK;
	}
}
