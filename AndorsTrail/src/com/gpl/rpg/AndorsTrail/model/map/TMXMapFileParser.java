package com.gpl.rpg.AndorsTrail.model.map;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.util.Base64;
import com.gpl.rpg.AndorsTrail.util.L;
import com.gpl.rpg.AndorsTrail.util.XmlResourceParserUtils;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public final class TMXMapFileParser {
	private static final int TILESIZE = 32;

	public static TMXObjectMap readObjectMap(Resources r, int xmlResourceId, String name) {
		return readObjectMap(r.getXml(xmlResourceId), xmlResourceId, name);
	}

	public static TMXLayerMap readLayerMap(Resources r, int xmlResourceId, String name) {
		return readLayerMap(r.getXml(xmlResourceId), name);
	}

	private static TMXObjectMap readObjectMap(XmlResourceParser xrp, int xmlResourceId, String name) {
		final TMXObjectMap map = new TMXObjectMap();
		map.xmlResourceId = xmlResourceId;
		try {
			// Map format: http://sourceforge.net/apps/mediawiki/tiled/index.php?title=Examining_the_map_format
			int eventType;
			while ((eventType = xrp.next()) != XmlResourceParser.END_DOCUMENT) {
				if (eventType == XmlResourceParser.START_TAG) {
					String s = xrp.getName();
					if (s.equals("map")) {
						readMapValues(xrp, name, map);
						XmlResourceParserUtils.readCurrentTagUntilEnd(xrp, new XmlResourceParserUtils.TagHandler() {
							@Override
							public void handleTag(XmlResourceParser xrp, String tagName) throws XmlPullParserException, IOException {
								if (tagName.equals("objectgroup")) {
									map.objectGroups.add(readTMXObjectGroup(xrp));
								} else if (tagName.equals("property")) {
									map.properties.add(readTMXProperty(xrp));
								}
							}
						});
					}
				}
			}
			xrp.close();
		} catch (XmlPullParserException e) {
			L.log("Error reading map \"" + name + "\": XmlPullParserException : " + e.toString());
		} catch (IOException e) {
			L.log("Error reading map \"" + name + "\": IOException : " + e.toString());
		}
		return map;
	}

	private static void readMapValues(XmlResourceParser xrp, String mapName, TMXMap map) {
		map.name = mapName;
		map.width = xrp.getAttributeIntValue(null, "width", -1);
		map.height = xrp.getAttributeIntValue(null, "height", -1);
		map.tilewidth = TILESIZE;
		map.tileheight = TILESIZE;
		if (AndorsTrailApplication.DEVELOPMENT_VALIDATEDATA) {
			int tilewidth = xrp.getAttributeIntValue(null, "tilewidth", TILESIZE);
			if (tilewidth != TILESIZE) {
				L.log("Map \"" + mapName + "\" has tilewidth=" + tilewidth + " . Expected " + TILESIZE);
			}
			int tileheight = xrp.getAttributeIntValue(null, "tileheight", TILESIZE);
			if (tileheight != TILESIZE) {
				L.log("Map \"" + mapName + "\" has tileheight=" + tileheight + " . Expected " + TILESIZE);
			}
		}
	}


	private static TMXLayerMap readLayerMap(XmlResourceParser xrp, final String name) {
		final TMXLayerMap map = new TMXLayerMap();
		try {
			int eventType;
			final ArrayList<TMXLayer> layers = new ArrayList<TMXLayer>();
			final ArrayList<TMXTileSet> tileSets = new ArrayList<TMXTileSet>();
			while ((eventType = xrp.next()) != XmlResourceParser.END_DOCUMENT) {
				if (eventType == XmlResourceParser.START_TAG) {
					String s = xrp.getName();
					if (s.equals("map")) {
						readMapValues(xrp, name, map);
						XmlResourceParserUtils.readCurrentTagUntilEnd(xrp, new XmlResourceParserUtils.TagHandler() {
							@Override
							public void handleTag(XmlResourceParser xrp, String tagName) throws XmlPullParserException, IOException {
								if (tagName.equals("tileset")) {
									tileSets.add(readTMXTileSet(xrp));
								} else if (tagName.equals("layer")) {
									layers.add(readTMXMapLayer(xrp, map.width, map.height));
								} else if (tagName.equals("property")) {
									map.properties.add(readTMXProperty(xrp));
								} else if (tagName.equals("objectgroup")) {
									map.objectGroups.add(readTMXObjectGroup(xrp));
								}
							}
						});
					}
				}
			}
			xrp.close();
			map.layers = layers.toArray(new TMXLayer[layers.size()]);
			map.tileSets = tileSets.toArray(new TMXTileSet[tileSets.size()]);
		} catch (XmlPullParserException e) {
			L.log("Error reading layered map \"" + name + "\": XmlPullParserException : " + e.toString());
		} catch (IOException e) {
			L.log("Error reading layered map \"" + name + "\": IOException : " + e.toString());
		}
		return map;
	}


	private static TMXTileSet readTMXTileSet(XmlResourceParser xrp) {
		final TMXTileSet ts = new TMXTileSet();
		ts.firstgid = xrp.getAttributeIntValue(null, "firstgid", 1);
		ts.name = xrp.getAttributeValue(null, "name");
		if (AndorsTrailApplication.DEVELOPMENT_VALIDATEDATA) {
			int tilewidth = xrp.getAttributeIntValue(null, "tilewidth", TILESIZE);
			if (tilewidth != TILESIZE) {
				L.log("Tileset \"" + ts.name + "\" has tilewidth=" + tilewidth + " . Expected " + TILESIZE);
			}
			int tileheight = xrp.getAttributeIntValue(null, "tileheight", TILESIZE);
			if (tileheight != TILESIZE) {
				L.log("Tileset \"" + ts.name + "\" has tileheight=" + tileheight + " . Expected " + TILESIZE);
			}
		}
		return ts;
	}

	private static TMXObjectGroup readTMXObjectGroup(XmlResourceParser xrp) throws XmlPullParserException, IOException {
		final TMXObjectGroup group = new TMXObjectGroup();
		group.name = xrp.getAttributeValue(null, "name");
		XmlResourceParserUtils.readCurrentTagUntilEnd(xrp, new XmlResourceParserUtils.TagHandler() {
			@Override
			public void handleTag(XmlResourceParser xrp, String tagName) throws XmlPullParserException, IOException {
				if (tagName.equals("object")) {
					group.objects.add(readTMXObject(xrp));
				} else if (tagName.equals("property")) {
					group.properties.add(readTMXProperty(xrp));
				}
			}
		});
		return group;
	}

	private static TMXObject readTMXObject(XmlResourceParser xrp) throws XmlPullParserException, IOException {
		final TMXObject object = new TMXObject();
		object.name = xrp.getAttributeValue(null, "name");
		object.type = xrp.getAttributeValue(null, "type");
		object.x = xrp.getAttributeIntValue(null, "x", -1);
		object.y = xrp.getAttributeIntValue(null, "y", -1);
		object.width = xrp.getAttributeIntValue(null, "width", -1);
		object.height = xrp.getAttributeIntValue(null, "height", -1);
		XmlResourceParserUtils.readCurrentTagUntilEnd(xrp, new XmlResourceParserUtils.TagHandler() {
			@Override
			public void handleTag(XmlResourceParser xrp, String tagName) throws XmlPullParserException, IOException {
				if (tagName.equals("property")) {
					object.properties.add(readTMXProperty(xrp));
				}
			}
		});
		return object;
	}

	private static TMXLayer readTMXMapLayer(XmlResourceParser xrp, final int width, final int height) throws XmlPullParserException, IOException {
		final TMXLayer layer = new TMXLayer();
		layer.name = xrp.getAttributeValue(null, "name");
		layer.gids = new int[width][height];
		XmlResourceParserUtils.readCurrentTagUntilEnd(xrp, new XmlResourceParserUtils.TagHandler() {
			@Override
			public void handleTag(XmlResourceParser xrp, String tagName) throws XmlPullParserException, IOException {
				if (tagName.equals("data")) {
					readTMXMapLayerData(xrp, layer, width, height);
				}
			}
		});
		return layer;
	}

	private static void readTMXMapLayerData(XmlResourceParser xrp, final TMXLayer layer, final int width, final int height) throws XmlPullParserException, IOException {
		String compressionMethod = xrp.getAttributeValue(null, "compression");
		xrp.next();
		String data = xrp.getText().replaceAll("\\s", "");
		final int len = width * height * 4;

		ByteArrayInputStream bi = new ByteArrayInputStream(Base64.decode(data));
		if (compressionMethod == null) compressionMethod = "none";

		InflaterInputStream zi;
		if (compressionMethod.equalsIgnoreCase("zlib")) {
			zi = new InflaterInputStream(bi);
		} else if (compressionMethod.equalsIgnoreCase("gzip")) {
			zi = new GZIPInputStream(bi, len);
		} else {
			throw new IOException("Unhandled compression method \"" + compressionMethod + "\" for map layer " + layer.name);
		}

		byte[] buffer = new byte[len];
		copyStreamToBuffer(zi, buffer, len);

		zi.close();
		bi.close();
		int i = 0;
		for(int y = 0; y < height; ++y) {
			for(int x = 0; x < width; ++x, i += 4) {
				int gid = readIntLittleEndian(buffer, i);
				layer.gids[x][y] = gid;
			}
		}

		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(buffer);
			layer.layoutHash = digest.digest();
		} catch (NoSuchAlgorithmException e) {
			throw new IOException("Failed to create layout hash for map layer " + layer.name);
		}
	}

	private static TMXProperty readTMXProperty(XmlResourceParser xrp) {
		final TMXProperty property = new TMXProperty();
		property.name = xrp.getAttributeValue(null, "name");
		property.value = xrp.getAttributeValue(null, "value");
		return property;
	}

	private static void copyStreamToBuffer(InflaterInputStream zi, byte[] buffer, int len) throws IOException {
		int offset = 0;
		int bytesToRead = len;
		while (bytesToRead > 0) {
			int b = zi.read(buffer, offset, bytesToRead);
			if (b <= 0) throw new IOException("Failed to read stream!");
			bytesToRead -= b;
			offset += b;
		}
	}

	/*
	private static String getHexString(byte v) {
		String result = Integer.toHexString(v & 0xff);
		if (result.length() < 2) result = '0' + result;
		return result;
	}
	private static String getHexString(byte[] buffer, int offset) {
		return getHexString(buffer[offset])
			+ getHexString(buffer[offset+1])
			+ getHexString(buffer[offset+2])
			+ getHexString(buffer[offset+3]);
	}
	*/
	private static int readIntLittleEndian(byte[] buffer, int offset) {
		return  (buffer[offset + 0] << 0 ) & 0x000000ff |
				(buffer[offset + 1] << 8 ) & 0x0000ff00 |
				(buffer[offset + 2] << 16) & 0x00ff0000 |
				(buffer[offset + 3] << 24) & 0xff000000;
	}

	public static final class TMXObjectMap extends TMXMap {
		public int xmlResourceId;
		public final ArrayList<TMXObjectGroup> objectGroups = new ArrayList<TMXObjectGroup>();
	}
	public static final class TMXLayerMap extends TMXMap {
		public TMXTileSet[] tileSets;
		public TMXLayer[] layers;
		public final ArrayList<TMXObjectGroup> objectGroups = new ArrayList<TMXObjectGroup>();
	}
	public static class TMXMap {
		public String name;
		public int width;
		public int height;
		public int tilewidth;
		public int tileheight;
		public final ArrayList<TMXProperty> properties = new ArrayList<TMXProperty>();
	}
	public static final class TMXTileSet {
		public int firstgid;
		public String name;
	}
	public static final class TMXLayer {
		public String name;
		public int[][] gids;
		public byte[] layoutHash;
	}
	public static final class TMXObjectGroup {
		public String name;
		public final ArrayList<TMXObject> objects = new ArrayList<TMXObject>();
		public final ArrayList<TMXProperty> properties = new ArrayList<TMXProperty>();
	}
	public static final class TMXObject {
		public String name;
		public String type;
		public int x;
		public int y;
		public int width;
		public int height;
		public final ArrayList<TMXProperty> properties = new ArrayList<TMXProperty>();
	}
	public static final class TMXProperty {
		public String name;
		public String value;
	}
	/*

	 <map version="1.0" orientation="orthogonal" width="10" height="10" tilewidth="32" tileheight="32">
		 <tileset firstgid="1" name="tiles" tilewidth="32" tileheight="32">
	  		<image source="tilesets/tiles.png"/>
		 </tileset>
		 <layer name="Tile Layer 1" width="10" height="10">
			<data encoding="base64" compression="gzip">
			   H4sIAAAAAAAAA/NgYGDwIBK7AbEnHkyOOmwYXR02MwZSHQyTah4xGADnAt2SkAEAAA==
			</data>
		 </layer>
		 <layer name="Tile Layer 2" width="10" height="10">
			<data encoding="base64" compression="gzip">
			   H4sIAAAAAAAAA2NgoA1gYUHlP2HGro6NBbt4MysqXw2oLhEqlgSlU4H0YjR12EAbUE0KFnXPgG5iRLJ/GQ6zHuNwOy7gxE6aemQAAJRT7VKQAQAA
			</data>
		 </layer>
	 </map>

	 */
}
