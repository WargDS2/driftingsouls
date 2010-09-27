/*
 *	Drifting Souls 2
 *	Copyright (c) 2008 Christopher Jung
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
package net.driftingsouls.ds2.server.tools;

import net.driftingsouls.ds2.server.framework.ContextMap;
import net.driftingsouls.ds2.server.framework.DSApplication;
import net.driftingsouls.ds2.server.ships.Ship;

import org.hibernate.CacheMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Transaction;

/**
 * Kommandozeilentool zur Neuberechnung von redundanten Schiffsdaten.
 * @author Christopher Jung
 *
 */
public class RecalcShips extends DSApplication {
	private Integer shipid;
	
	/**
	 * Konstruktor.
	 * @param args Die Kommandozeilenargumente
	 * @throws Exception
	 */
	public RecalcShips(String[] args) throws Exception {
		super(args);
		
		if( getContext().getRequest().getParameter("ship") != null ) {
			shipid = getContext().getRequest().getParameterInt("ship");
		}
	}
	
	private void printHelp() {
		log("Recalculate Ships");
		log("Berechnet redundante Daten eines oder aller Schiffe neu");
		log("");
		log("java "+getClass().getName()+" --config $configpfad [--ship $shipid] [--help]");
		log(" * --config Der Pfad zum DS2-Konfigurationsverzeichnis");
		log(" * [optional] --ship Die ID des zu berechnenden Schiffes. Falls nicht angegeben werden alle Schiffe neuberechnet");
		log(" * [optional] --help Zeigt diese Hilfe an");
	}
	
	/**
	 * Startet die Ausfuehrung.
	 *
	 */
	public void execute() {
		if( getContext().getRequest().getParameter("help") != null ) {
			printHelp();
			return;
		}
		
		log("\nBeginne:");

		org.hibernate.Session db = ContextMap.getContext().getDB();
		Transaction transaction = db.beginTransaction();
		if( shipid == null ) 
		{
			ScrollableResults ships = db.createQuery("from Ship as s left join fetch s.modules " +
			 										 "where s.id>0 order by s.owner,s.docked,s.shiptype asc")
			 										 .setCacheMode(CacheMode.IGNORE)
			 										 .scroll(ScrollMode.FORWARD_ONLY);
			
			int count = 0;
			while(ships.next())
			{
				Ship ship = (Ship) ships.get(0);
				try
				{
					ship.recalculateModules();
					ship.recalculateShipStatus();
					if(count%100 == 0)
					{
						log("sid: "+ship.getId()+"");
					}
				}
				catch (Exception e) 
				{
					log("WARNUNG: Konnte Schiff "+ship.getId()+" nicht neu berechnen\n"+e);
					e.printStackTrace();
				}
				count++;
			
				if(count % 50 == 0)
				{
					db.flush();
					db.clear();
				}
			}
		}
		else {
			log("sid: "+shipid+"\n");
			Ship ship = (Ship)db.get(Ship.class, shipid);
			if( ship != null ) {
				ship.recalculateShipStatus();
				ship.recalculateModules();
			}
			else {
				log("-> Schiff existiert nicht");
			}
		}
		transaction.commit();
	}

	/**
	 * Main.
	 * @param args Die Argumente
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		RecalcShips rs = new RecalcShips(args);
		rs.execute();
		rs.dispose();
	}

}
