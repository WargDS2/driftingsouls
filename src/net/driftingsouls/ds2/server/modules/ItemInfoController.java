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
package net.driftingsouls.ds2.server.modules;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.driftingsouls.ds2.server.bases.Base;
import net.driftingsouls.ds2.server.cargo.Cargo;
import net.driftingsouls.ds2.server.cargo.ItemCargoEntry;
import net.driftingsouls.ds2.server.cargo.ItemID;
import net.driftingsouls.ds2.server.cargo.ResourceEntry;
import net.driftingsouls.ds2.server.cargo.ResourceList;
import net.driftingsouls.ds2.server.cargo.Resources;
import net.driftingsouls.ds2.server.config.ModuleSlots;
import net.driftingsouls.ds2.server.config.NoSuchSlotException;
import net.driftingsouls.ds2.server.config.Rassen;
import net.driftingsouls.ds2.server.config.Weapon;
import net.driftingsouls.ds2.server.config.Weapons;
import net.driftingsouls.ds2.server.config.items.Item;
import net.driftingsouls.ds2.server.config.items.Items;
import net.driftingsouls.ds2.server.config.items.effects.IEAmmo;
import net.driftingsouls.ds2.server.config.items.effects.IEDisableShip;
import net.driftingsouls.ds2.server.config.items.effects.IEDraftShip;
import net.driftingsouls.ds2.server.config.items.effects.IEModule;
import net.driftingsouls.ds2.server.config.items.effects.IEModuleSetMeta;
import net.driftingsouls.ds2.server.config.items.effects.ItemEffect;
import net.driftingsouls.ds2.server.entities.Ammo;
import net.driftingsouls.ds2.server.entities.Forschung;
import net.driftingsouls.ds2.server.entities.StatItemLocations;
import net.driftingsouls.ds2.server.entities.StatUserCargo;
import net.driftingsouls.ds2.server.entities.User;
import net.driftingsouls.ds2.server.framework.Common;
import net.driftingsouls.ds2.server.framework.Configuration;
import net.driftingsouls.ds2.server.framework.Context;
import net.driftingsouls.ds2.server.framework.pipeline.generators.Action;
import net.driftingsouls.ds2.server.framework.pipeline.generators.ActionType;
import net.driftingsouls.ds2.server.framework.pipeline.generators.TemplateGenerator;
import net.driftingsouls.ds2.server.framework.templates.TemplateEngine;
import net.driftingsouls.ds2.server.ships.Ship;
import net.driftingsouls.ds2.server.ships.ShipTypeChangeset;
import net.driftingsouls.ds2.server.ships.ShipTypeData;
import net.driftingsouls.ds2.server.ships.ShipTypes;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Zeigt Informationen zu Items an
 * @author Christopher Jung
 *
 */
public class ItemInfoController extends TemplateGenerator {

	/**
	 * Konstruktor
	 * @param context Der zu verwendende Kontext
	 */
	public ItemInfoController(Context context) {
		super(context);
		
		setTemplate("iteminfo.html");
		
		setPageTitle("Item");
	}
	
	@Override
	protected boolean validateAndPrepare(String action) {
		return true;
	}
	
	private void colorize(StringBuilder effecttext, Object mod) {
		long value = ((Number)mod).longValue();
		if( value < 0 ) {
			effecttext.append("<span style=\"color:red\">");	
		}
		else {
			effecttext.append("<span style=\"color:green\">");
		}
	}

	private String parseModuleModifiers( ShipTypeChangeset mods ) {
		StringBuilder effecttext = new StringBuilder(300);
		
		if( mods.getRu() != 0 ) {
			colorize(effecttext, mods.getRu());
			effecttext.append("Reaktor <img src=\""+Cargo.getResourceImage(Resources.URAN)+"\" alt=\"\" /> "+mods.getRu());
			effecttext.append("</span><br />\n");
		}
		
		if( mods.getRd() != 0 ) {
			colorize(effecttext, mods.getRd());
			effecttext.append("Reaktor <img src=\""+Cargo.getResourceImage(Resources.DEUTERIUM)+"\" alt=\"\" /> "+mods.getRd());
			effecttext.append("</span><br />\n");
		}
		
		if( mods.getRa() != 0 ) {
			colorize(effecttext, mods.getRa());
			effecttext.append("Reaktor <img src=\""+Cargo.getResourceImage(Resources.ANTIMATERIE)+"\" alt=\"\" /> "+mods.getRa());
			effecttext.append("</span><br />\n");
		}
		
		if( mods.getRm() != 0 ) {
			colorize(effecttext, mods.getRm());
			effecttext.append("Reaktor <img src=\""+Configuration.getSetting("URL")+"data/interface/energie.gif\" alt=\"\" /> "+mods.getRm());
			effecttext.append("</span><br />\n");
		}
		
		if( mods.getEps() != 0 ) {
			colorize(effecttext, mods.getEps());
			effecttext.append("Energiespeicher "+mods.getEps());
			effecttext.append("</span><br />\n");
		}
		
		if( mods.getCost() != 0 ) {
			colorize(effecttext, mods.getCost());
			effecttext.append("Flugkosten "+mods.getCost());
			effecttext.append("</span><br />\n");
		}
		
		if( mods.getHeat() != 0 ) {
			colorize(effecttext, mods.getHeat());
			effecttext.append("&Uuml;berhitzung "+mods.getHeat());
			effecttext.append("</span><br />\n");
		}
		
		if( mods.getHull() != 0 ) {
			colorize(effecttext, mods.getHull());
			effecttext.append("H&uuml;lle "+mods.getHull());
			effecttext.append("</span><br />\n");
		}
		
		if( mods.getPanzerung() != 0 ) {
			colorize(effecttext, mods.getPanzerung());
			effecttext.append("Panzerung "+mods.getPanzerung());
			effecttext.append("</span><br />\n");
		}
		
		if( mods.getAblativeArmor() != 0 ) {
			colorize(effecttext, mods.getAblativeArmor());
			effecttext.append("Ablative Panzerung "+mods.getAblativeArmor());
			effecttext.append("</span><br />\n");
		}
		
		if( mods.getCargo() != 0 ) {
			colorize(effecttext, mods.getCargo());
			effecttext.append("Cargo "+mods.getCargo());
			effecttext.append("</span><br />\n");
		}
		
		if( mods.getCrew() != 0 ) {
			colorize(effecttext, mods.getCrew());
			effecttext.append("Crew "+mods.getCrew());
			effecttext.append("</span><br />\n");
		}
		
		if( mods.getShields() != 0 ) {
			colorize(effecttext, mods.getShields());
			effecttext.append("Schilde "+mods.getShields());
			effecttext.append("</span><br />\n");
		}
		
		if( mods.getSize() != 0 ) {
			colorize(effecttext, mods.getSize());
			effecttext.append("Gr&ouml;&szlig;e "+mods.getSize());
			effecttext.append("</span><br />\n");
		}
		
		if( mods.getJDocks() != 0 ) {
			colorize(effecttext, mods.getJDocks());
			effecttext.append("J&auml;gerdocks "+mods.getJDocks());
			effecttext.append("</span><br />\n");
		}
		
		if( mods.getADocks() != 0 ) {
			colorize(effecttext, mods.getADocks());
			effecttext.append("Externe Docks "+mods.getADocks());
			effecttext.append("</span><br />\n");
		}
		
		if( mods.getSensorRange() != 0 ) {
			colorize(effecttext, mods.getSensorRange());
			effecttext.append("Sensorreichweite "+mods.getSensorRange());
			effecttext.append("</span><br />\n");
		}
		
		if( mods.getHydro() != 0 ) {
			colorize(effecttext, mods.getHydro());
			effecttext.append("Produziert <img src=\""+Cargo.getResourceImage(Resources.NAHRUNG)+"\" alt=\"\" />"+mods.getHydro());
			effecttext.append("</span><br />\n");
		}
		
		if( mods.getDeutFactor() != 0 ) {
			colorize(effecttext, mods.getDeutFactor());
			effecttext.append("Sammelt <img src=\""+Cargo.getResourceImage(Resources.DEUTERIUM)+"\" alt=\"\" />"+mods.getDeutFactor());
			effecttext.append("</span><br />\n");
		}
		
		if( mods.getReCost() != 0 ) {
			colorize(effecttext, mods.getReCost());
			effecttext.append("Wartungskosten "+mods.getReCost());
			effecttext.append("</span><br />\n");
		}
		
		if( mods.getWerft() != 0 ) {
			colorize(effecttext, mods.getWerft());
			effecttext.append("Werftslots "+mods.getWerft());
			effecttext.append("</span><br />\n");
		}
		
		if( mods.getFlags() != null ) {
			String[] flags = StringUtils.split(mods.getFlags(), ' ');
			for( int i=0; i < flags.length; i++ ) {
				if( i > 0 ) {
					effecttext.append("<br />\n");	
				}
				effecttext.append(ShipTypes.getShipTypeFlagName(flags[i]));
			}
			effecttext.append("<br />\n");
		}
		
		if( mods.getWeapons() != null ) {
			Map<String,Integer[]> weaponlist = mods.getWeapons();
					
			StringBuilder wpntext = new StringBuilder(50);
			for( Object entry : weaponlist.entrySet() ) {
				Entry weaponclass = (Entry)entry;
				String weaponclassname = (String)weaponclass.getKey();
				Integer[] weaponmods = (Integer[])weaponclass.getValue();
					
				int weaponcount = weaponmods[0];
				int weaponheat = weaponmods[1];	
						
				if( wpntext.length() > 0 ) {
					wpntext.append("<br />");	
				}
				wpntext.append("<span class=\"nobr\">");
				if( Math.abs(weaponcount) > 1 ) {
					wpntext.append(weaponcount+"x ");	
				}
				wpntext.append(Weapons.get().weapon(weaponclassname).getName());
				wpntext.append("</span><br />[Max. Hitze: "+weaponheat+"]");
			}
			effecttext.append(wpntext);
			effecttext.append("<br />\n");
		}
		
		if( mods.getMaxHeat() != null ) {
			Map<String,Integer> weaponlist = mods.getMaxHeat();
					
			StringBuilder wpntext = new StringBuilder(50);
			for( Object entry : weaponlist.entrySet() ) {
				Entry weaponclass = (Entry)entry;
				String weaponclassname = (String)weaponclass.getKey();
				int weaponheat = (Integer)weaponclass.getValue();
				
				if( wpntext.length() > 0 ) {
					wpntext.append("<br />");	
				}
				wpntext.append(Weapons.get().weapon(weaponclassname).getName()+":<br />+"+weaponheat+" Max-Hitze");
			}
			effecttext.append(wpntext);
			effecttext.append("<br />\n");
		}
				
		return effecttext.toString();	
	}
	
	/**
	 * Zeigt Details zu einem Item an
	 * @urlparam Integer itemid Die ID des anzuzeigenden Items
	 */
	@Action(ActionType.DEFAULT)
	public void detailsAction() {
		TemplateEngine t = getTemplateEngine();
		User user = (User)getUser();
		
		parameterNumber("item");
		int itemid = getInteger("item");
				
		if( Items.get().item(itemid) == null ) {
			t.setVar("iteminfo.message", "Es ist kein Item mit dieser Identifikationsnummer bekannt");
		
			return;
		}
		
		if( Items.get().item(itemid).getAccessLevel() > user.getAccessLevel() ) {
			t.setVar("iteminfo.message", "Es ist kein Item mit dieser Identifikationsnummer bekannt");
		
			return;
		}
		
		if( Items.get().item(itemid).isUnknownItem() && !user.isKnownItem(itemid) && (user.getAccessLevel() < 15) ) {
			t.setVar("iteminfo.message", "Es ist kein Item mit dieser Identifikationsnummer bekannt");
		
			return;
		}
		
		String name = Common._plaintitle(Items.get().item(itemid).getName());
		if( Items.get().item(itemid).getQuality().color().length() > 0 ) {
			name = "<span style=\"color:"+Items.get().item(itemid).getQuality().color()+"\">"+name+"</span>";	
		}
		
		t.setVar(	"iteminfo.details",	1,
					"item.picture",		Items.get().item(itemid).getPicture(),
					"item.name",		name,
					"item.cargo",		Items.get().item(itemid).getCargo(),
					"item.accesslevel",	Items.get().item(itemid).getAccessLevel(),
					"item.allyitem",	Items.get().item(itemid).getEffect().hasAllyEffect(),
					"item.class",		Items.get().item(itemid).getEffect().getType().getName(),
					"item.description",	Common._text(Items.get().item(itemid).getDescription()) );
		
		t.setBlock("_ITEMINFO", "itemdetails.entry", "itemdetails.entrylist");
		
		switch( Items.get().item(itemid).getEffect().getType() ) {
		/*
		
			EFFECT_DRAFT_SHIP
			
		*/
		case DRAFT_SHIP: {
			IEDraftShip effect = (IEDraftShip)Items.get().item(itemid).getEffect();
			
			ShipTypeData shiptype = Ship.getShipType( effect.getShipType() );
	
			StringBuilder data = new StringBuilder(100);
			if( shiptype == null ) {
				data.append("<span style=\"color:red\">Unbekannter Schiffstyp</span>");
			}
			else if( shiptype.isHide() ) {
        		if( user.getAccessLevel() > 20 ) {
        			data.append("<a class=\"forschinfo\" href=\""+Common.buildUrl("default", "module", "schiffinfo", "ship", effect.getShipType())+"\">"+shiptype.getNickname()+"</a><br /><span style=\"font-style:italic;color:red\" class=\"verysmallfont\">[unsichtbar]</span>\n");
	        	} 
	        	else {
    	    		data.append("Unbekannt");
        		}
			}	
			else {
				data.append("<a class=\"forschinfo\" href=\""+Common.buildUrl("default", "module", "schiffinfo", "ship", effect.getShipType())+"\">"+shiptype.getNickname()+"</a>\n");
			}
			
			t.setVar(	"entry.name",	"Schiffstyp",
						"entry.data",	data );
			
			t.parse("itemdetails.entrylist", "itemdetails.entry", true);
						
			t.setVar(	"entry.name",	"Rasse",
						"entry.data",	Rassen.get().rasse(effect.getRace()).getName() );
			
			t.parse("itemdetails.entrylist", "itemdetails.entry", true);
	
			data.setLength(0);
			boolean entry = false;
			for( int i = 0; i < 3; i++ ) {
				if( effect.getTechReq(i) != 0 ) {
					Forschung dat = Forschung.getInstance(effect.getTechReq(i));
					if( !dat.isVisibile(user) && (!user.hasResearched(dat.getRequiredResearch(1)) || !user.hasResearched(dat.getRequiredResearch(2)) || !user.hasResearched(dat.getRequiredResearch(3))) ) {
						data.append("Unbekannt");
						if( user.getAccessLevel() > 20 ) {
							data.append(" [ID:"+effect.getTechReq(i)+"]");
						}	
						data.append("<br />\n");
						entry = true;
						
						continue;
					}
					
					data.append("<a class=\"nonbold\" href=\""+Common.buildUrl("default","module", "forschinfo", "res", effect.getTechReq(i))+"\">");
			 		
			 		if( user.hasResearched(effect.getTechReq(i)) ) {
			 			data.append("<span style=\"color:green; font-size:14px\">");
		 			}
		 			else {
			 			data.append("<span style=\"color:red; font-size:14px\">");
			 		}
			 		data.append(dat.getName());
		 			data.append("</span></a><br />\n");
		 			entry = true;
				}
			}
			if( !entry ) {
				data.append("-");
			}
			
			t.setVar(	"entry.name",	"Ben&ouml;tigt",
						"entry.data",	data );
			
			t.parse("itemdetails.entrylist", "itemdetails.entry", true);

			data.setLength(0);
			Cargo costs = effect.getBuildCosts();
			ResourceList reslist = costs.getResourceList();
			for( ResourceEntry res : reslist ) {
				data.append("<img src=\""+res.getImage()+"\" alt=\"\" />"+res.getCargo1()+"<br />\n");
			}
			data.append("<img src=\""+Configuration.getSetting("URL")+"data/interface/energie.gif\" alt=\"\" />"+effect.getE()+"<br />\n");
			data.append("<img src=\""+Configuration.getSetting("URL")+"data/interface/besatzung.gif\" alt=\"\" />"+effect.getCrew()+"<br />\n");
			
			t.setVar(	"entry.name",	"Kosten",
						"entry.data",	data );
			
			t.parse("itemdetails.entrylist", "itemdetails.entry", true);
			
			t.setVar(	"entry.name",	"Dauer",
						"entry.data",	"<img valign=\"middle\" src=\""+Configuration.getSetting("URL")+"data/interface/time.gif\" alt=\"\" />"+effect.getDauer() );
			
			t.parse("itemdetails.entrylist", "itemdetails.entry", true);
	
			t.setVar(	"entry.name",	"Werftslots",
						"entry.data",	"<img valign=\"middle\" src=\""+Configuration.getSetting("URL")+"data/interface/schiffinfo/werftslots.png\" alt=\"\" />"+effect.getWerftSlots() );
			
			t.parse("itemdetails.entrylist", "itemdetails.entry", true);
	
			t.setVar(	"entry.name",	"Flagschiff",
						"entry.data",	(effect.isFlagschiff() ? "ja" : "nein") );
			
			t.parse("itemdetails.entrylist", "itemdetails.entry", true);
			
			break;
		}
		/*
		
			EFFECT_DISABLE_SHIP
			
		*/
		case DISABLE_SHIP: {
			IEDisableShip effect = (IEDisableShip)Items.get().item(itemid).getEffect();
			t.setVar( "entry.name", "Schiffstyp" );
			
			ShipTypeData shiptype = Ship.getShipType( effect.getShipType() );
			if( shiptype != null ) {
				t.setVar("entry.data", "<a class=\"forschinfo\" href=\""+Common.buildUrl("default","module", "schiffinfo", "ship", effect.getShipType())+"\">"+shiptype.getNickname()+"</a>" );
			}
			else {
				t.setVar("entry.data", "<span style=\"color:red\">Unbekannter Schiffstyp</span>");
			}
			
			t.parse("itemdetails.entrylist", "itemdetails.entry", true);
			
			break;
		}
		/*
		
			EFFECT_MODULE
			
		*/
		case MODULE: {
			IEModule effect = (IEModule)Items.get().item(itemid).getEffect();
			
			StringBuilder targetslots = new StringBuilder(50);
			List<String> slots = effect.getSlots();
			for( int i=0; i < slots.size(); i++ ) {
				String aslot = slots.get(i);
				
				if( targetslots.length() > 0 ) {
					targetslots.append(", ");	
				}
				try {
					targetslots.append(ModuleSlots.get().slot(aslot).getName());
				}
				catch( NoSuchSlotException e ) {
					targetslots.append("Ungueltiger Slot '"+aslot+"'");
				}
			}
			
			int setknowncount = 0;
			if( effect.getSetID() != 0 ) {
				int setcount = 0;
				
				for( Item aitem : Items.get() ) {
					if( aitem.getEffect().getType() != ItemEffect.Type.MODULE ) {
						continue;	
					}	
					if( ((IEModule)aitem.getEffect()).getSetID() != effect.getSetID() ) {
						continue;	
					}
					setcount++;
					if( (user.getAccessLevel() >= 15) || user.isKnownItem(aitem.getID()) ) {
						setknowncount++;	
					}
				}
				
				t.setVar(	"entry.name",	"Set",
							"entry.data",	((IEModuleSetMeta)Items.get().item(effect.getSetID()).getEffect()).getName()+" ("+setcount+")" );
								
				t.parse("itemdetails.entrylist", "itemdetails.entry", true);	
			}
			
			t.setVar(	"entry.name",	"Passt in",
						"entry.data",	targetslots.toString() );
								
			t.parse("itemdetails.entrylist", "itemdetails.entry", true);
			
			String effecttext = this.parseModuleModifiers( effect.getMods() );
			
			t.setVar(	"entry.name",	"Effekt",
						"entry.data",	effecttext );
								
			t.parse("itemdetails.entrylist", "itemdetails.entry", true);
								
			if( effect.getSetID() != 0 ) {
				IEModuleSetMeta meta = ((IEModuleSetMeta)Items.get().item(effect.getSetID()).getEffect());
				Map<Integer,ShipTypeChangeset> modlist = meta.getCombos();
				
				for( Map.Entry<Integer, ShipTypeChangeset> entry: modlist.entrySet() ) {
					Integer modulecount = entry.getKey();
					ShipTypeChangeset mods = entry.getValue();
			
					if( modulecount <= setknowncount ) {
						effecttext = this.parseModuleModifiers( mods );
						
						t.setVar(	"entry.name",	"Set-Combo ("+modulecount+" Items)",
									"entry.data",	effecttext );	
						t.parse("itemdetails.entrylist", "itemdetails.entry", true);
					}
				}	
			}
			
			break;
		}	
		/*
		
			EFFECT_AMMO
			
		*/
		case AMMO: {
			IEAmmo effect = (IEAmmo)Items.get().item(itemid).getEffect();
			
			Ammo ammo = effect.getAmmo();
		
			if( ammo == null ) {
				t.setVar(	"entry.name",	"Munition",
							"entry.data",	"Es liegen keine genaueren Daten zur Munition vor" );
			
				t.parse("itemdetails.entrylist", "itemdetails.entry", true);
			}
			else {
				StringBuilder data = new StringBuilder(100);
				boolean entry = false;
				for( int i = 1; i < 4; i++ ) {
					if( ammo.getRes(i) != 0 ) {
						if( entry ) {
							data.append(",<br />\n");
						}
						
						Forschung dat = Forschung.getInstance(ammo.getRes(i));
						if( (ammo.getRes(i) == -1) || 
							(!dat.isVisibile(user) && (!user.hasResearched(dat.getRequiredResearch(1)) || !user.hasResearched(dat.getRequiredResearch(2)) || !user.hasResearched(dat.getRequiredResearch(3)))) ) {
							
							data.append("Unbekannte Technologie");
							if( user.getAccessLevel() > 20 ) {
								data.append(" [ID:"+ammo.getRes(i)+"]");
							}
							entry = true;
							continue;
						}
						
						data.append("<a class=\"nonbold\" href=\""+Common.buildUrl("default", "module", "forschinfo", "res", ammo.getRes(i))+"\">");
		 				if( user.hasResearched(ammo.getRes(i)) ) {
		 					data.append("<span style=\"color:green; font-size:14px\">");
		 				}
			 			else {
			 				data.append("<span style=\"color:red; font-size:14px\">");
			 			}
		 				data.append(dat.getName());
		 				data.append("</span></a>\n");
						entry = true;
					}
				}
				if( !entry ) {
					data.append("-");
				}
				
				t.setVar(	"entry.name",	"Ben&ouml;tigt",
							"entry.data",	data );
			
				t.parse("itemdetails.entrylist", "itemdetails.entry", true);
				
				Cargo buildcosts = ammo.getBuildCosts();
			
				data.setLength(0);

				ResourceList reslist = buildcosts.getResourceList();
				for( ResourceEntry res : reslist ) {
					data.append("<img src=\""+res.getImage()+"\" alt=\"\" />"+res.getCargo1()+" ");
				}	
				
				t.setVar(	"entry.name",	"Kosten",
							"entry.data",	data );
			
				t.parse("itemdetails.entrylist", "itemdetails.entry", true);
				
				data.setLength(0);
				if( ammo.getShotsPerShot() > 1 ) {
					data.append(ammo.getShotsPerShot()+" Salven<br />\n");
				}
				if( ammo.getDamage() != 0 ) {
					data.append(ammo.getDamage()+" Schaden<br />\n");
				}
				if( ammo.getDamage() != ammo.getShieldDamage() ) {
					data.append(ammo.getShieldDamage()+" Schildschaden<br />\n");
				}
				if( ammo.getSubDamage() != 0 ) {
					data.append(ammo.getSubDamage()+" Subsystemschaden<br />\n");
					data.append(ammo.getSubWS()+"% Subsystem-Trefferws<br />\n");
				}
				data.append(ammo.getSmallTrefferWS()+"% Trefferws (J&auml;ger)<br />\n");
				data.append(ammo.getTrefferWS()+"% Trefferws (Capitals)\n");
				if( ammo.getTorpTrefferWS() != 0 ) {
					data.append("<br />"+ammo.getTorpTrefferWS()+"% Trefferws (Torpedos)\n");
				}
				if( ammo.getAreaDamage() != 0 ) {
					data.append("<br />Umgebungsschaden ("+ammo.getAreaDamage()+")\n");
				}
				if( ammo.getDestroyable() > 0 ) {
					data.append("<br />Durch Abwehrfeuer zerst&ouml;rbar\n");
				}
				if( ammo.getReplaces() != null ) {
					Ammo replammo = ammo.getReplaces();
					data.append("<br />Ersetzt <a style=\"font-size:14px\" class=\"forschinfo\" href=\""+
							Common.buildUrl("details", "module", "iteminfo", "item", replammo.getItemId())+"\">"+
							replammo.getName()+"</a>\n");
				}
					
				t.setVar(	"entry.name",	"Daten",
							"entry.data",	data );
			
				t.parse("itemdetails.entrylist", "itemdetails.entry", true);
				
				StringBuilder weapons = new StringBuilder(50);
				for( Weapon weapon : Weapons.get() ) {
					if( !Common.inArray(ammo.getType(), weapon.getAmmoType()) ) continue;
			
					if( weapons.length() == 0 ) {
						weapons.append(weapon.getName());
					}
					else {
						weapons.append(",<br />\n"+weapon.getName());
					}
				}
				if( weapons.length() > 0 ) {
					t.setVar(	"entry.name",	"Waffe",
								"entry.data",	weapons );
					t.parse("itemdetails.entrylist", "itemdetails.entry", true);
				}
			}
			
			break;
		}
		/*
		 * 
		 *  EFFECT_MODUE_SET_META
		 * 
		 */
		case MODULE_SET_META: {
			if( user.getAccessLevel() < 15 ) {
				return;
			}
			Cargo setitemlist = new Cargo();
			
			for( Item item : Items.get() ) {
				if( (item.getEffect().getType() == ItemEffect.Type.MODULE) && (((IEModule)item.getEffect()).getSetID() == itemid) ) {
					setitemlist.addResource(new ItemID(item.getID()), 1);
				}
			}
			
			StringBuilder tmp = new StringBuilder(50);
			ResourceList reslist = setitemlist.getResourceList();
			for( ResourceEntry res : reslist ) {
				tmp.append("<span class=\"nobr\"><img style=\"vertical-align:middle\" src=\""+res.getImage()+"\" alt=\"\">"+res.getName()+"</span><br />\n");	
			}
			
			if( tmp.length() > 0 ) {
				t.setVar(	"entry.name",	"Set-Items",
							"entry.data",	tmp );
			
				t.parse("itemdetails.entrylist", "itemdetails.entry", true);
			}
			
			break;
		}
		} // Ende switch
	}
	
	/**
	 * Zeigt die Liste aller bekannten Items sowie ihren Aufenthaltsort, sofern man sie besitzt, an
	 *
	 */
	@Action(ActionType.DEFAULT)
	public void knownAction() {
		TemplateEngine t = getTemplateEngine();
		User user = (User)getUser();
		org.hibernate.Session db = getDB();
		
		StatUserCargo ownCargoRow = (StatUserCargo)db.get(StatUserCargo.class, user.getId());
		Cargo owncargo = null;
		if( ownCargoRow != null ) {
			owncargo = new Cargo(ownCargoRow.getCargo());
		}
		else {
			owncargo = new Cargo();
		}

		t.setVar("iteminfo.knownlist", 1);

		t.setBlock("_ITEMINFO", "knownlist.listitem", "knownlist.list");
		
		Map<Integer,String[]> reslocations = new HashMap<Integer,String[]>();
		List modules = db.createQuery("from StatItemLocations where user=?")
			.setEntity(0, user)
			.list();
		for( Iterator iter=modules.iterator(); iter.hasNext(); ) {
			StatItemLocations amodule = (StatItemLocations)iter.next();
			reslocations.put(amodule.getItemId(), StringUtils.split(amodule.getLocations(),';'));
		}
		
		final String shipimage = "<td class='noBorderX' style='text-align:right'><img style='vertical-align:middle' src='"+Configuration.getSetting("URL")+"data/interface/schiffe/"+user.getRace()+"/icon_schiff.gif' alt='' title='Schiff' /></td>";
		final String baseimage = "<td class='noBorderX' style='text-align:right'><img style='vertical-align:middle;width:15px;height:15px' src='"+Configuration.getSetting("URL")+"data/starmap/asti/asti.png' alt='' title='Asteroid' /></td>";

		for( Item aitem : Items.get() ) {
			int itemid = aitem.getID();
			
			ItemEffect itemeffect = aitem.getEffect();
			
			if( aitem.getAccessLevel() > user.getAccessLevel() ) {
				continue;	
			}
			
			if( aitem.isUnknownItem() && !user.isKnownItem(itemid) && (user.getAccessLevel() < 15) ) {
				continue;
			}
			
			String name = Common._plaintitle(aitem.getName());
			if( aitem.getQuality().color().length() > 0 ) {
				name = "<span style=\"color:"+aitem.getQuality().color()+"\">"+name+"</span>";	
			}
			String tooltip = "";
			if( reslocations.containsKey(itemid) ) {				
				StringBuilder tooltiptext = new StringBuilder(200);
				tooltiptext.append(StringUtils.replaceChars(Common.tableBegin(350, "left"), '"', '\'') );
				tooltiptext.append("<table class='noBorderX'>");
				String[] locations = reslocations.get(itemid);
				for( int i=0; i < locations.length; i++ ) {
					String alocation = locations[i];
					int objectid = Integer.parseInt(alocation.substring(1));
					
					tooltiptext.append("<tr>");
					switch( alocation.charAt(0) ) {
					case 's': {
						Ship ship = (Ship)db.get(Ship.class, objectid);
						if( ship == null ) {
							continue;
						}
						tooltiptext.append(shipimage+"<td class='noBorderX'><a style='font-size:14px' class='forschinfo' href='"+Common.buildUrl("default", "module", "schiff", "ship", objectid)+"'>"+ship.getName()+" ("+ship.getId()+")</a></td>");
						break;
					}
					case 'b': {
						Base base = (Base)db.get(Base.class, objectid);
						if( base == null ) {
							continue;
						}
						tooltiptext.append(baseimage+"<td class='noBorderX'><a style='font-size:14px' class='forschinfo' href='"+Common.buildUrl("default", "module", "base", "col", objectid)+"'>"+base.getName()+" - "+base.getLocation()+"</a></td>");
						break;
					}
					case 'g': {
						Ship ship = (Ship)db.get(Ship.class, objectid);
						if( ship == null ) {
							continue;
						}
						tooltiptext.append("<td colspan='2' class='noBorderX' style='font-size:14px'>"+ship.getName()+"</td>");
						break;
					}
					default:
						tooltiptext.append("<td colspan='2' class='noBorderX' style='font-size:14px'>Unbekanntes Objekt "+alocation+"</td>");
					}	
					
					tooltiptext.append("</tr>");
				}
				tooltiptext.append("</table>");
				tooltiptext.append(StringUtils.replaceChars(Common.tableEnd(), '"', '\'') );
				tooltip = StringEscapeUtils.escapeJavaScript(StringUtils.replace(StringUtils.replace(tooltiptext.toString(), ">", "&gt;"), "<", "&lt;"));
			}
		
			t.setVar(	"item.picture",	aitem.getPicture(),
						"item.id",		itemid,
						"item.name",	name,
						"item.class",	itemeffect.getType().getName(),
						"item.cargo",	Common.ln(aitem.getCargo()),
						"item.locationtext",	tooltip,
						"item.count",		Common.ln(owncargo.getResourceCount(new ItemID(itemid)) ) );
			
			t.parse("knownlist.list", "knownlist.listitem", true);
		}
	}
	
	/**
	 * Zeigt eine Itemliste an
	 * @urlparam String itemlist Die Itemliste
	 */
	@Override
	@Action(ActionType.DEFAULT)
	public void defaultAction() {
		TemplateEngine t = this.getTemplateEngine();
		User user = (User)getUser();
		
		parameterString("itemlist");	
		Cargo itemlist = null;

		try {
			itemlist = new Cargo();
			itemlist.addResource(Resources.fromString(getString("itemlist")),1);
		}
		catch( Exception e ) {
			try {
				// Offenbar keine Item-ID
				// Jetzt versuchen, die Liste als Itemliste zu parsen
				itemlist = new Cargo(Cargo.Type.ITEMSTRING,getString("itemlist"));
			}
			catch( Exception f ) {
				addError("Kein gueltiges Item angegeben");
				return;
			}
		}

		t.setVar("iteminfo.itemlist", 1);

		t.setBlock("_ITEMINFO", "itemlist.listitem", "itemlist.list");

		List<ItemCargoEntry> myitemlist = itemlist.getItems();
		for( int i=0; i < myitemlist.size(); i++ ) {
			ItemCargoEntry item = myitemlist.get(i);
			
			Item itemobject = item.getItemObject();
		
			if( itemobject == null ) {
				continue;
			}
			
			if( itemobject.getAccessLevel() > user.getAccessLevel() ) {
				continue;	
			}
			
			if( itemobject.isUnknownItem() && !user.isKnownItem(item.getItemID()) && (user.getAccessLevel() < 15) ) {
				continue;
			}
			
			ItemEffect itemeffect = item.getItemEffect();
			
			String name = Common._plaintitle(itemobject.getName());
			if( itemobject.getQuality().color().length() > 0 ) {
				name = "<span style=\"color:"+itemobject.getQuality().color()+"\">"+name+"</span>";	
			}
		
			t.setVar(	"item.picture",	itemobject.getPicture(),
						"item.id",		item.getItemID(),
						"item.name",	name,
						"item.class",	itemeffect.getType().getName(),
						"item.cargo",	itemobject.getCargo(),
						"item.count",	item.getCount() );
			
			t.parse("itemlist.list", "itemlist.listitem", true);
		}
	}
}
