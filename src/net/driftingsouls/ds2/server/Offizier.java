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
package net.driftingsouls.ds2.server;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.driftingsouls.ds2.server.cargo.HibernateCargoType;
import net.driftingsouls.ds2.server.config.Offiziere;
import net.driftingsouls.ds2.server.entities.User;
import net.driftingsouls.ds2.server.framework.Common;
import net.driftingsouls.ds2.server.framework.Configuration;
import net.driftingsouls.ds2.server.framework.ContextMap;
import net.driftingsouls.ds2.server.framework.DSObject;
import net.driftingsouls.ds2.server.units.HibernateUnitCargoType;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Repraesentiert einen Offizier in DS.
 * @author Christopher Jung
 *
 */
@TypeDefs(
	{
		@TypeDef(
				name="cargo",
				typeClass = HibernateCargoType.class
		),
		@TypeDef(
				name="unitcargo",
				typeClass = HibernateUnitCargoType.class
		)
	}
)
@Entity
@Table(name="offiziere")
@Configurable
public class Offizier extends DSObject {
	/**
	 * Die Attribute eines Offiziers.
	 * @author Christopher Jung
	 *
	 */
	public enum Ability {
		/**
		 * Der Navigationsskill.
		 */
		NAV,
		/**
		 * Der Ingenieursskill/Technikskill.
		 */
		ING,
		/**
		 * Der Waffenskill.
		 */
		WAF,
		/**
		 * Der Sicherheitsskill.
		 */
		SEC,
		/**
		 * Der Kommandoskill.
		 */
		COM
	}
	
	/**
	 * Die Spezialfaehigkeiten der Offiziere.
	 * Jeder Offizier besitzt eine Spezialfaehigkeit
	 * @author Christopher Jung
	 *
	 */
	public enum Special {
		/**
		 * Keine Spezialfaehigkeit.
		 */
		NONE("Nichts"),
		/**
		 * Motivationskuenstler.
		 */
		MOTIVATIONSKUENSTLER("Motivationsk&uuml;nstler"),
		/**
		 * Schnellmerker.
		 */
		SCHNELLMERKER("Schnellmerker"),
		/**
		 * Technikfreak.
		 */
		TECHNIKFREAK("Technikfreak"),
		/**
		 * Waffennarr.
		 */
		WAFFENNARR("Waffennarr"),
		/**
		 * Bleifuss.
		 */
		BLEIFUSS("Bleifuss"),
		/**
		 * Verrueckter Diktator.
		 */
		VERRUECKTER_DIKTATOR("Verr&uuml;ckter Diktator");
		
		private String name;
		private Special(String name) {
			this.name = name;
		}
		
		/**
		 * Gibt den Namen der Spezialfaehigkeit zurueck (entspricht nicht 
		 * zwangslaeufig der Konstante!).
		 * @return Der Name
		 */
		public String getName() {
			return name;
		}
	}
	
	@Id @GeneratedValue
	private int id;
	private String name;
	private int rang;
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="userid")
	private User owner;
	private String dest;
	private int ing;
	private int waf;
	private int nav;
	private int sec;
	private int com;
	private int spec;
	private int ingu;
	private int navu;
	@SuppressWarnings("unused")
	private int wafu;
	@SuppressWarnings("unused")
	private int secu;
	@SuppressWarnings("unused")
	private int comu;
	
	@Transient
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
	
	/**
	 * Konstruktor.
	 *
	 */
	public Offizier() {
		// EMPTY
	}
	
	/**
	 * Konstruktor.
	 * @param owner Der Besitzer des Offiziers
	 * @param name Der Name des Offiziers
	 */
	public Offizier(User owner, String name) {
		setOwner(owner);
		setName(name);
	}

	/**
	 * Gibt den Namen des Offiziers zurueck.
	 * @return Der Name
	 */
	public String getName() {
		return name;	
	}
	
	/**
	 * Setzt den Namen des Offiziers.
	 * @param name der neue Name
	 */
	public void setName( String name ) {
		this.name = name;
	}
	
	/**
	 * Gibt die ID des Offiziers zurueck.
	 * @return die ID
	 */
	public int getID() {
		return id;	
	}
	
	/**
	 * Gibt den Rang des Offiziers zurueck.
	 * @return der Rang
	 */
	public int getRang() {
		return rang;	
	}
	
	/**
	 * Setzt den Rang des Offiziers.
	 * @param rang Der Rang
	 */
	public void setRang(int rang) {
		this.rang = rang;
	}
	
	/**
	 * Gibt den Pfad des zum Offizier gehoerenden Bilds zurueck.
	 * Der Pfad ist bereits eine vollstaendige URL.
	 * @return Der Pfad des Bildes
	 */
	public String getPicture() {
		return config.get("URL")+"data/interface/offiziere/off"+getRang()+".png";
	}
	
	/**
	 * Gibt den Aufenthaltsort des Offiziers als Array der Laenge 2 zurueck.
	 * Das erste Element kennzeichnet den Typ des Aufenthaltsortes (<code>s</code> bei Schiffen,
	 * <code>b</code> bei Basen und <code>t</code> bei aktuell laufender Ausbildung auf einer Basis).
	 * Das zweite Feld enthaelt die ID des Aufenthaltsortes
	 * @return Der Aufenthaltsort
	 */
	public String[] getDest() {
		return StringUtils.split(dest, ' ');
	}
	
	/**
	 * Setzt den Aufenthaltsort eines Offiziers.
	 * @param dest Der Typ des Aufenthaltsortes (s, b, t)
	 * @param objectid Die ID des Aufenthaltsortes
	 * @see #getDest()
	 */
	public void setDest( String dest, int objectid ) {
		this.dest = dest+' '+objectid;
	}
	
	/**
	 * Gibt den Besitzers des Offiziers zurueck.
	 * @return der Besitzer
	 */
	public User getOwner() {
		return owner;	
	}
	
	/**
	 * Setzt den Besitzer des Offiziers.
	 * @param owner der neue Besitzer
	 */
	public void setOwner( User owner ) {
		this.owner = owner; 
	}
	
	/**
	 * Gibt den aktuellen Skillwert der angegebenen Faehigkeit des Offiziers zurueck.
	 * @param ability Die Faehigkeit
	 * @return Der aktuelle Skill in dieser Faehigkeit
	 */
	public int getAbility( Ability ability ) {
		switch( ability ) {
			case ING:
				return ing;
			case WAF:
				return waf;
			case NAV:
				return nav;
			case SEC:
				return sec;
			case COM:
				return com;
		}
		return 0;
	}
	
	/**
	 * Setzt den Skillwert einer Faehigkeit des offiziers.
	 * @param ability Die Faehigkeit
	 * @param value Der Skill
	 */
	public void setAbility(Ability ability, int value) {
		switch( ability ) {
		case ING:
			ing = value;
			break;
		case WAF:
			waf = value;
			break;
		case NAV:
			nav = value;
			break;
		case SEC:
			sec = value;
			break;
		case COM:
			com = value;
			break;
	}
	}
	
	/**
	 * Benutzt einen Skill des Offiziers unter Beruecksichtigung 
	 * der Schwierigkeit der Aufgabe. Der Offizier kann dabei seinen
	 * Skill verbessern. Entsprechende Hinweistexte koennen via {@link DSObject#MESSAGE}
	 * erfragt werden. Zurueckgegeben wird, wie oft der Skill erfolgreich angewandt wurde.
	 * 
	 * @param ability Die Faehigkeit
	 * @param difficulty Die Schwierigkeit der Aufgabe
	 * @return Die Anzahl der erfolgreichen Anwendungen des Skills
	 */
	public int useAbility( Ability ability, int difficulty ) {
		int count = 0;

		switch( ability ) {
			case ING: {
				int fak = difficulty;
				if( this.spec == 3 ) {
					fak *= 0.6;
				}
				if( this.ing > fak*(RandomUtils.nextInt(101)/100d) ) {
					count++;
					
					if( RandomUtils.nextInt(31) > 10 ) {
						this.ingu++;
						fak = 2;
						if( this.spec == 2) {
							fak = 1;
						}
						if( this.ingu > this.ing * fak ) {
							MESSAGE.get().append(Common._plaintitle(this.name)+" hat seine Ingeneursf&auml;higkeit verbessert\n");
							this.ing++;
							this.ingu = 0;
						}
					}
				}
				break;
			}
			case WAF:
				break;
				
			case NAV: {
				int fak = difficulty;
				if( this.spec == 5 ) {
					fak *= 0.6;
				}
				if( this.nav > fak*(RandomUtils.nextInt(101)/100d) ) {
					count++;
					
					if( RandomUtils.nextInt(31) > 10 ) {
						this.navu++;
						fak = 2;
						if( this.spec == 2) {
							fak = 1;
						}
						if( this.navu > this.nav * fak ) {
							MESSAGE.get().append(Common._plaintitle(this.name)+" hat seine Navigationsf&auml;higkeit verbessert\n");
							this.nav++;
							this.navu = 0;
						}
					}
				}
				break;
			}	
			case SEC:
				break;
				
			case COM:
				break;
		}
		
		if( count != 0 ) {
			double rangf = (this.ing+this.waf+this.nav+this.sec+this.com)/5.0;
			int rang = (int)(rangf/125);
			if( rang > Offiziere.MAX_RANG ) {
				rang = Offiziere.MAX_RANG;
			}
						
			if( rang > this.rang ) {
				MESSAGE.get().append(this.name+" wurde bef&ouml;rdert\n");
				this.rang = rang;
			}
		}

		return count;	
	}
	
	/**
	 * Gibt die Spezialfaehigkeit des Offiziers zurueck.
	 * @return Die Spezialfaehigkeit
	 */
	public Special getSpecial() {
		return Special.values()[spec];
	}
	
	/**
	 * Setzt die Spezialeigenschaft des Offiziers.
	 * @param special Die Spezialeigenschaft
	 */
	public void setSpecial(Special special) {
		this.spec = special.ordinal();
	}
	
	/**
	 * Prueft, ob der Offizier die angegebene Spezialfaehigkeit hat.
	 * @param special Die Spezialfaehigkeit
	 * @return <code>true</code>, falls der Offizier die Faehigkeit hat
	 */
	public boolean hasSpecial( Special special ) {
		return spec == special.ordinal();	
	}
	
	/**
	 * @return Offensivskill des Offiziers.
	 */
	public int getOffensiveSkill()
	{
		int weaponSkill = getAbility(Offizier.Ability.WAF);
		int commSkill = getAbility(Offizier.Ability.COM);
		
		return (int)Math.round((weaponSkill + commSkill) / 2d);
	}
	
	/**
	 * @return Defensivskill des Offiziers.
	 */
	public int getDefensiveSkill()
	{
		return (getAbility(Offizier.Ability.NAV) + getAbility(Offizier.Ability.COM)) / 2;
	}
		
	/**
	 * Gibt einen Offizier am angegebenen Aufenthaltsort zurueck. Sollten mehrere
	 * Offiziere sich an diesem Ort aufhalten, so wird der beste von ihnen zurueckgegeben.
	 * Sollte sich an dem Ort kein Offizier aufhalten, so wird <code>null</code> zurueckgegeben.
	 * 
	 * @param dest Der Typ des Aufenthaltsortes (s, t, b)
	 * @param objid Die ID des Aufenthaltsortes
	 * @return Ein Offizier oder <code>null</code> 
	 * @see #getDest()
	 */
	public static Offizier getOffizierByDest(char dest, int objid) {
		org.hibernate.Session db = ContextMap.getContext().getDB();
		
		return (Offizier)db.createQuery("from Offizier where dest=? order by rang desc")
			.setString(0, dest+" "+objid)
			.setMaxResults(1)
			.uniqueResult();
	}
	
	/**
	 * Gibt den Offizier mit der angegebenen ID zurueck. Sollte kein solcher Offizier
	 * existieren, so wird <code>null</code> zurueckgegeben.
	 * @param id Die ID des Offiziers
	 * @return Der Offizier oder <code>null</code>
	 */
	public static Offizier getOffizierByID(int id) {
		org.hibernate.Session db = ContextMap.getContext().getDB();
		
		return (Offizier)db.get(Offizier.class, id);
	}
	
	/**
	 * Gibt alle Offiziere am angegebenen Aufenthaltsort zurueck.
	 * @param dest Der Typ des Aufenthaltsortes (s, t, b)
	 * @param objid Die ID des Aufenthaltsortes
	 * @return Die Liste aller Offiziere
	 * @see #getDest()
	 */
	@SuppressWarnings("unchecked")
	public static List<Offizier> getOffiziereByDest(char dest, int objid) {
		org.hibernate.Session db = ContextMap.getContext().getDB();
		
		return db.createQuery("from Offizier where dest= :dest")
			.setString("dest", dest+" "+objid)
			.list();
	}
}
