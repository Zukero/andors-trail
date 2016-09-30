package com.gpl.rpg.AndorsTrail.model.map;

import com.gpl.rpg.AndorsTrail.model.item.DropList;
import com.gpl.rpg.AndorsTrail.model.script.Requirement;
import com.gpl.rpg.AndorsTrail.util.CoordRect;

public final class MapObject {
	public static enum MapObjectType {
		sign
		,newmap
		,rest
		,keyarea
		,container
		,script
	}

	public static enum MapObjectEvaluationType {
		whenEntering
		,onEveryStep
		,afterEveryRound
		,continuously
	}

	public final CoordRect position;
	public final MapObjectType type;
	public final String id; //placeName on this map or phraseID
	public final String map;
	public final String place;
	public final String group;
	public final Requirement enteringRequirement;
	public final DropList dropList;
	public final MapObjectEvaluationType evaluateWhen;
	public boolean isActive;

	private MapObject(
			final CoordRect position
			, final MapObjectType type
			, final String id
			, final String map
			, final String place
			, final Requirement enteringRequirement
			, final DropList dropList
			, final MapObjectEvaluationType evaluateWhen
			, final String group
	) {
		this.position = new CoordRect(position);
		this.type = type;
		this.id = id;
		this.map = map;
		this.place = place;
		this.enteringRequirement = enteringRequirement;
		this.dropList = dropList;
		this.evaluateWhen = evaluateWhen;
		this.group = group;
		this.isActive = true;
	}


	public static MapObject createMapSignEvent(
			final CoordRect position
			, final String phraseID
			, String group
	) {
		return new MapObject(position, MapObjectType.sign, phraseID, null, null, null, null, MapObjectEvaluationType.whenEntering, group);
	}

	public static MapObject createMapChangeArea(
			final CoordRect position
			, final String thisMapTitle
			, final String destinationMap
			, final String destinationPlace
			, String group
	) {
		return new MapObject(position, MapObjectType.newmap, thisMapTitle, destinationMap, destinationPlace, null, null, MapObjectEvaluationType.whenEntering, group);
	}

	public static MapObject createRestArea(
			final CoordRect position
			, final String placeId
			, String group
	) {
		return new MapObject(position, MapObjectType.rest, placeId, null, null, null, null, MapObjectEvaluationType.whenEntering, group);
	}

	public static MapObject createKeyArea(
			final CoordRect position
			, final String phraseID
			, final Requirement enteringRequirement
			, String group
	) {
		return new MapObject(position, MapObjectType.keyarea, phraseID, null, null, enteringRequirement, null, MapObjectEvaluationType.whenEntering, group);
	}

	public static MapObject createContainerArea(
			final CoordRect position
			, final DropList dropList
			, String group
	) {
		return new MapObject(position, MapObjectType.container, null, null, null, null, dropList, MapObjectEvaluationType.whenEntering, group);
	}

	public static MapObject createScriptArea(
			final CoordRect position
			, final String phraseID
			, final MapObjectEvaluationType evaluateWhen
			, String group
	) {
		return new MapObject(position, MapObjectType.script, phraseID, null, null, null, null, evaluateWhen, group);
	}
}
